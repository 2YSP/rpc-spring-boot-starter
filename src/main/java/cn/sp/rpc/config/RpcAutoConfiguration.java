package cn.sp.rpc.config;

import cn.sp.rpc.annotation.LoadBalanceAno;
import cn.sp.rpc.annotation.MessageProtocolAno;
import cn.sp.rpc.client.balance.LoadBalance;
import cn.sp.rpc.client.discovery.ServerDiscovery;
import cn.sp.rpc.client.discovery.zk.ZookeeperServerDiscovery;
import cn.sp.rpc.client.manager.ServerDiscoveryManager;
import cn.sp.rpc.client.net.ClientProxyFactory;
import cn.sp.rpc.client.net.NettyNetClient;
import cn.sp.rpc.common.protocol.MessageProtocol;
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
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.ServiceLoader;

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
        return new RequestHandler(getMessageProtocol(rpcConfig.getProtocol()), serverRegister);
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
        LoadBalance loadBalance = getLoadBalance(rpcConfig.getLoadBalance());
        return loadBalance;
    }


    @Bean
    public ClientProxyFactory proxyFactory(@Autowired ServerDiscoveryManager manager,
                                           @Autowired LoadBalance loadBalance) {
        ClientProxyFactory clientProxyFactory = new ClientProxyFactory();
        // 设置服务发现着
        clientProxyFactory.setServerDiscoveryManager(manager);
        // 设置负载均衡算法
        clientProxyFactory.setLoadBalance(loadBalance);
        // 设置网络层实现
        clientProxyFactory.setNetClient(new NettyNetClient());
        return clientProxyFactory;
    }

    private MessageProtocol getMessageProtocol(String name) {
        ServiceLoader<MessageProtocol> loader = ServiceLoader.load(MessageProtocol.class);
        Iterator<MessageProtocol> iterator = loader.iterator();
        while (iterator.hasNext()) {
            MessageProtocol messageProtocol = iterator.next();
            MessageProtocolAno ano = messageProtocol.getClass().getAnnotation(MessageProtocolAno.class);
            Assert.notNull(ano, "message protocol name can not be empty!");
            if (name.equals(ano.value())) {
                return messageProtocol;
            }
        }
        throw new RpcException("invalid message protocol config!");
    }


    /**
     * 使用spi匹配符合配置的负载均衡算法
     *
     * @param name
     * @return
     */
    private LoadBalance getLoadBalance(String name) {
        ServiceLoader<LoadBalance> loader = ServiceLoader.load(LoadBalance.class);
        Iterator<LoadBalance> iterator = loader.iterator();
        while (iterator.hasNext()) {
            LoadBalance loadBalance = iterator.next();
            LoadBalanceAno ano = loadBalance.getClass().getAnnotation(LoadBalanceAno.class);
            Assert.notNull(ano, "load balance name can not be empty!");
            if (name.equals(ano.value())) {
                return loadBalance;
            }
        }
        throw new RpcException("invalid load balance config");
    }

    @Bean
    public DefaultRpcProcessor rpcProcessor(@Autowired ClientProxyFactory clientProxyFactory,
                                            @Autowired ServerRegister serverRegister,
                                            @Autowired RpcServer rpcServer) {
        return new DefaultRpcProcessor(clientProxyFactory, serverRegister, rpcServer);
    }


}
