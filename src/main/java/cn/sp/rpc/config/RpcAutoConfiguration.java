package cn.sp.rpc.config;

import cn.sp.rpc.client.core.DefaultMethodInvoker;
import cn.sp.rpc.client.core.MethodInvoker;
import cn.sp.rpc.client.manager.LoadBalanceManager;
import cn.sp.rpc.client.manager.MessageProtocolsManager;
import cn.sp.rpc.client.manager.ServerDiscoveryManager;
import cn.sp.rpc.client.net.ClientProxyFactory;
import cn.sp.rpc.client.net.NetClientFactory;
import cn.sp.rpc.common.exception.RpcException;
import cn.sp.rpc.config.properties.RpcConfig;
import cn.sp.rpc.discovery.ServerDiscovery;
import cn.sp.rpc.discovery.ServerRegister;
import cn.sp.rpc.discovery.nacos.NacosServerDiscovery;
import cn.sp.rpc.discovery.register.DefaultServerRegister;
import cn.sp.rpc.discovery.zk.ZookeeperServerDiscovery;
import cn.sp.rpc.server.NettyRpcServer;
import cn.sp.rpc.server.RequestHandler;
import cn.sp.rpc.server.RpcServer;
import cn.sp.rpc.server.register.DefaultRpcProcessor;
import cn.sp.rpc.spi.balance.LoadBalance;
import cn.sp.rpc.spi.protocol.MessageProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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

    @Bean
    public ServerDiscovery nacosServerDiscovery() {
        if ("nacos".equals(rpcConfig.getRegisterCenterType())) {
            return new NacosServerDiscovery(rpcConfig.getRegisterAddress());
        }
        if ("zk".equals(rpcConfig.getRegisterCenterType())) {
            return new ZookeeperServerDiscovery(rpcConfig.getRegisterAddress());
        }
        throw new RpcException("注册中心类型配置错误");
    }

    @Bean
    public ServerRegister serverRegister(@Autowired ServerDiscovery serverDiscovery) {
        return new DefaultServerRegister(serverDiscovery, rpcConfig);
    }


    @Bean
    public ServerDiscoveryManager serverDiscoveryManager(@Autowired ServerDiscovery serverDiscovery) {
        return new ServerDiscoveryManager(serverDiscovery);
    }


    @Bean
    public LoadBalance loadBalance() {
        // 设置负载均衡算法
        LoadBalance loadBalance = LoadBalanceManager.getLoadBalance(rpcConfig.getLoadBalance());
        return loadBalance;
    }

    @Bean
    public DefaultMethodInvoker methodInvoker(@Autowired ServerDiscoveryManager manager,
                                              @Autowired LoadBalance loadBalance) {
        return new DefaultMethodInvoker(manager, NetClientFactory.getInstance(), loadBalance);
    }


    @Bean
    public ClientProxyFactory proxyFactory(@Autowired MethodInvoker methodInvoker) {
        ClientProxyFactory clientProxyFactory = new ClientProxyFactory();
        clientProxyFactory.setMethodInvoker(methodInvoker);
        return clientProxyFactory;
    }

    @Bean
    public RequestHandler requestHandler(@Autowired ServerRegister serverRegister) {
        MessageProtocol messageProtocol = MessageProtocolsManager.get(rpcConfig.getProtocol());
        if (messageProtocol == null) {
            throw new RpcException("invalid message protocol config!");
        }
        return new RequestHandler(messageProtocol, serverRegister);
    }

    @Bean
    public RpcServer rpcServer(@Autowired RequestHandler requestHandler) {
        return new NettyRpcServer(rpcConfig.getServerPort(), rpcConfig.getProtocol(), requestHandler);
    }

    @Bean
    public DefaultRpcProcessor rpcProcessor(@Autowired ClientProxyFactory clientProxyFactory,
                                            @Autowired ServerRegister serverRegister,
                                            @Autowired RpcServer rpcServer,
                                            @Autowired ServerDiscoveryManager manager) {
        return new DefaultRpcProcessor(clientProxyFactory, serverRegister, rpcServer, manager);
    }


}
