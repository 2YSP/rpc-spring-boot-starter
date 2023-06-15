package cn.sp.rpc.config;

import cn.sp.rpc.client.core.DefaultMethodInvoker;
import cn.sp.rpc.client.core.MethodInvoker;
import cn.sp.rpc.client.manager.LoadBalanceManager;
import cn.sp.rpc.client.manager.MessageProtocolsManager;
import cn.sp.rpc.client.net.NetClientFactory;
import cn.sp.rpc.spi.balance.LoadBalance;
import cn.sp.rpc.client.discovery.ServerDiscovery;
import cn.sp.rpc.client.discovery.zk.ZookeeperServerDiscovery;
import cn.sp.rpc.client.manager.ServerDiscoveryManager;
import cn.sp.rpc.client.net.ClientProxyFactory;
import cn.sp.rpc.spi.protocol.MessageProtocol;
import cn.sp.rpc.exception.RpcException;
import cn.sp.rpc.properties.RpcConfig;
import cn.sp.rpc.server.NettyRpcServer;
import cn.sp.rpc.server.RequestHandler;
import cn.sp.rpc.server.RpcServer;
import cn.sp.rpc.server.register.DefaultRpcProcessor;
import cn.sp.rpc.server.register.ServerRegister;
import cn.sp.rpc.server.register.ZookeeperServerRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

    @Bean
    public ServerRegister serverRegister() {
        return new ZookeeperServerRegister(
                rpcConfig.getRegisterAddress(),
                rpcConfig.getServerPort(),
                rpcConfig.getProtocol(),
                rpcConfig.getWeight());
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
    public ServerDiscovery serverDiscovery() {
        ServerDiscovery serverDiscovery = new ZookeeperServerDiscovery(rpcConfig.getRegisterAddress());
        return serverDiscovery;
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
    public DefaultRpcProcessor rpcProcessor(@Autowired ClientProxyFactory clientProxyFactory,
                                            @Autowired ServerRegister serverRegister,
                                            @Autowired RpcServer rpcServer,
                                            @Autowired ServerDiscoveryManager manager) {
        return new DefaultRpcProcessor(clientProxyFactory, serverRegister, rpcServer, manager);
    }


}
