package com.github.ship.config;

import com.github.ship.client.core.DefaultMethodInvoker;
import com.github.ship.client.core.MethodInvoker;
import com.github.ship.client.manager.LoadBalanceManager;
import com.github.ship.client.manager.MessageProtocolsManager;
import com.github.ship.client.manager.ServerDiscoveryManager;
import com.github.ship.client.net.NetClientFactory;
import com.github.ship.client.proxy.ClientProxyFactory;
import com.github.ship.common.constants.ProxyTypeEnum;
import com.github.ship.common.constants.RegisterCenterTypeEnum;
import com.github.ship.common.exception.RpcException;
import com.github.ship.config.properties.RpcConfig;
import com.github.ship.discovery.ServerDiscovery;
import com.github.ship.discovery.ServerRegister;
import com.github.ship.discovery.register.DefaultServerRegister;
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
import java.lang.reflect.Constructor;

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
        RegisterCenterTypeEnum registerCenterTypeEnum = RegisterCenterTypeEnum.getByCode(rpcConfig.getRegisterCenterType());
        try {
            Constructor con = registerCenterTypeEnum.getClazz().getConstructor(String.class);
            return (ServerDiscovery) con.newInstance(rpcConfig.getRegisterAddress());
        } catch (Exception e) {
            throw new RpcException("init ServerDiscovery bean exception:" + e.getMessage());
        }
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
        ProxyTypeEnum proxyTypeEnum = ProxyTypeEnum.getByCode(rpcConfig.getProxyType());
        try {
            // 反射创建实例  用SPI也可以但是没法按需加载
            Constructor con = proxyTypeEnum.getClientProxyFactoryClass().getConstructor(MethodInvoker.class);
            return (ClientProxyFactory) con.newInstance(methodInvoker);
        } catch (Exception e) {
            throw new RpcException("init ClientProxyFactory bean exception:" + e.getMessage());
        }
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
