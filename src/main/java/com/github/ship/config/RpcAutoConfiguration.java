package com.github.ship.config;

import com.github.ship.client.core.DefaultMethodInvoker;
import com.github.ship.client.core.MethodInvoker;
import com.github.ship.client.manager.LoadBalanceManager;
import com.github.ship.client.manager.MessageProtocolsManager;
import com.github.ship.client.manager.ServerDiscoveryManager;
import com.github.ship.client.net.NetClientFactory;
import com.github.ship.client.proxy.ClientProxyFactory;
import com.github.ship.client.proxy.impl.JavassistClientProxyFactory;
import com.github.ship.client.proxy.impl.JdkClientProxyFactory;
import com.github.ship.client.proxy.impl.JdkCompilerClientProxyFactory;
import com.github.ship.common.constants.ProxyTypeEnum;
import com.github.ship.common.constants.RegisterCenterTypeEnum;
import com.github.ship.common.exception.RpcException;
import com.github.ship.config.properties.RpcConfig;
import com.github.ship.discovery.ServerDiscovery;
import com.github.ship.discovery.ServerRegister;
import com.github.ship.discovery.nacos.NacosServerDiscovery;
import com.github.ship.discovery.register.DefaultServerRegister;
import com.github.ship.discovery.zk.ZookeeperServerDiscovery;
import com.github.ship.server.NettyRpcServer;
import com.github.ship.server.RequestHandler;
import com.github.ship.server.RpcServer;
import com.github.ship.server.register.DefaultRpcProcessor;
import com.github.ship.spi.balance.LoadBalance;
import com.github.ship.spi.protocol.MessageProtocol;
import com.github.ship.util.SpringContextHolder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 注入需要的bean
 *
 * @author 2YSP
 * @date 2020/7/25 19:43
 */
@Configuration
@EnableConfigurationProperties(RpcConfig.class)
public class RpcAutoConfiguration {

    @Resource
    private RpcConfig rpcConfig;

    RpcAutoConfiguration(ApplicationContext applicationContext) {
        SpringContextHolder.setApplicationContext(applicationContext);
    }


    @Bean
    public ServerDiscovery serverDiscovery() {
        if (RegisterCenterTypeEnum.NACOS.getCode().equals(rpcConfig.getRegisterCenterType())) {
            return new NacosServerDiscovery(rpcConfig.getRegisterAddress());
        }
        if (RegisterCenterTypeEnum.ZOOKEEPER.getCode().equals(rpcConfig.getRegisterCenterType())) {
            return new ZookeeperServerDiscovery(rpcConfig.getRegisterAddress());
        }
        throw new RpcException("invalid config of registerCenterType");
    }

    @Bean
    public ServerRegister serverRegister(ServerDiscovery serverDiscovery) {
        return new DefaultServerRegister(serverDiscovery, rpcConfig);
    }


    @Bean
    public ServerDiscoveryManager serverDiscoveryManager(ServerDiscovery serverDiscovery) {
        return new ServerDiscoveryManager(serverDiscovery);
    }


    @Bean
    public LoadBalance loadBalance() {
        // 设置负载均衡算法
        LoadBalance loadBalance = LoadBalanceManager.getLoadBalance(rpcConfig.getLoadBalance());
        return loadBalance;
    }

    @Bean
    public DefaultMethodInvoker methodInvoker(ServerDiscoveryManager manager,
                                              LoadBalance loadBalance) {
        return new DefaultMethodInvoker(manager, NetClientFactory.getInstance(), loadBalance);
    }


    @Bean
    public ClientProxyFactory proxyFactory(MethodInvoker methodInvoker) {
        if (ProxyTypeEnum.JAVASSIST.getCode().equals(rpcConfig.getProxyType())) {
            return new JavassistClientProxyFactory(methodInvoker);
        }
        if (ProxyTypeEnum.JDK.getCode().equals(rpcConfig.getProxyType())) {
            return new JdkClientProxyFactory(methodInvoker);
        }
        if (ProxyTypeEnum.COMPILER.getCode().equals(rpcConfig.getProxyType())) {
            return new JdkCompilerClientProxyFactory(methodInvoker);
        }
        throw new RpcException("invalid config of proxyType!");
    }

    @Bean
    public RequestHandler requestHandler(ServerRegister serverRegister) {
        MessageProtocol messageProtocol = MessageProtocolsManager.get(rpcConfig.getProtocol());
        if (messageProtocol == null) {
            throw new RpcException("invalid message protocol config!");
        }
        return new RequestHandler(messageProtocol, serverRegister);
    }

    @Bean
    public RpcServer rpcServer(RequestHandler requestHandler) {
        return new NettyRpcServer(rpcConfig.getServerPort(), rpcConfig.getProtocol(), requestHandler);
    }

    @Bean
    public DefaultRpcProcessor rpcProcessor(ClientProxyFactory clientProxyFactory,
                                            ServerRegister serverRegister,
                                            RpcServer rpcServer,
                                            ServerDiscoveryManager manager) {
        return new DefaultRpcProcessor(clientProxyFactory, serverRegister, rpcServer, manager);
    }


}
