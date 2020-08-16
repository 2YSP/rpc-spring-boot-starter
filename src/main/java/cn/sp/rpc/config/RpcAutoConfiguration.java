package cn.sp.rpc.config;

import cn.sp.rpc.annotation.LoadBalanceAno;
import cn.sp.rpc.client.balance.*;
import cn.sp.rpc.client.discovery.ZookeeperServiceDiscovery;
import cn.sp.rpc.client.net.ClientProxyFactory;
import cn.sp.rpc.client.net.NettyNetClient;
import cn.sp.rpc.common.constants.RpcConstant;
import cn.sp.rpc.common.protocol.JavaSerializeMessageProtocol;
import cn.sp.rpc.common.protocol.MessageProtocol;
import cn.sp.rpc.common.protocol.ProtoBufMessageProtocol;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 注入需要的bean
 * @author 2YSP
 * @date 2020/7/25 19:43
 */
@Configuration
@EnableConfigurationProperties(RpcConfig.class)
public class RpcAutoConfiguration {


    @Bean
    public RpcConfig rpcConfig(){
        return new RpcConfig();
    }

    @Bean
    public ServerRegister serverRegister(@Autowired RpcConfig rpcConfig){
        return new ZookeeperServerRegister(
                rpcConfig.getRegisterAddress(),
                rpcConfig.getServerPort(),
                rpcConfig.getProtocol(),
                rpcConfig.getWeight());
    }

    @Bean
    public RequestHandler requestHandler(@Autowired ServerRegister serverRegister,
                                         @Autowired RpcConfig rpcConfig){
        MessageProtocol messageProtocol = null;
        if (rpcConfig.getProtocol().equals(RpcConstant.PROTOCOL_JAVA)){
            messageProtocol = new JavaSerializeMessageProtocol();
        } else if (rpcConfig.getProtocol().equals(RpcConstant.PROTOCOL_PROTOBUF)){
            messageProtocol = new ProtoBufMessageProtocol();
        } else{
            throw new RpcException("un support message protocol!");
        }
        return new RequestHandler(messageProtocol,serverRegister);
    }

    @Bean
    public RpcServer rpcServer(@Autowired RequestHandler requestHandler,
                               @Autowired RpcConfig rpcConfig){
        return new NettyRpcServer(rpcConfig.getServerPort(),rpcConfig.getProtocol(),requestHandler);
    }


    @Bean
    public ClientProxyFactory proxyFactory(@Autowired RpcConfig rpcConfig){
        ClientProxyFactory clientProxyFactory = new ClientProxyFactory();
        // 设置服务发现着
        clientProxyFactory.setServerDiscovery(new ZookeeperServiceDiscovery(rpcConfig.getRegisterAddress()));

        // 设置支持的协议
        Map<String, MessageProtocol> supportMessageProtocols = new HashMap<>();
        supportMessageProtocols.put(RpcConstant.PROTOCOL_JAVA,new JavaSerializeMessageProtocol());
        supportMessageProtocols.put(RpcConstant.PROTOCOL_PROTOBUF,new ProtoBufMessageProtocol());
        clientProxyFactory.setSupportMessageProtocols(supportMessageProtocols);
        // 设置负载均衡算法
        LoadBalance loadBalance = getLoadBalance(rpcConfig.getLoadBalance());
        clientProxyFactory.setLoadBalance(loadBalance);
        // 设置网络层实现
        clientProxyFactory.setNetClient(new NettyNetClient());

        return clientProxyFactory;
    }

    /**
     * 使用spi匹配符合配置的负载均衡算法
     * @param name
     * @return
     */
    private LoadBalance getLoadBalance(String name){
        ServiceLoader<LoadBalance> loader = ServiceLoader.load(LoadBalance.class);
        Iterator<LoadBalance> iterator = loader.iterator();
        while (iterator.hasNext()){
            LoadBalance loadBalance = iterator.next();
            LoadBalanceAno ano = loadBalance.getClass().getAnnotation(LoadBalanceAno.class);
            Assert.notNull(ano,"load balance name can not be empty!");
            if (name.equals(ano.value())){
                return loadBalance;
            }
        }
        throw new RpcException("invalid load balance config");
    }

    @Bean
    public DefaultRpcProcessor rpcProcessor(@Autowired ClientProxyFactory clientProxyFactory,
                                            @Autowired ServerRegister serverRegister,
                                            @Autowired RpcServer rpcServer){
        return new DefaultRpcProcessor(clientProxyFactory,serverRegister,rpcServer);
    }



}
