package cn.sp.rpc.config;

import cn.sp.rpc.client.balance.FullRoundBalance;
import cn.sp.rpc.client.balance.LoadBalance;
import cn.sp.rpc.client.balance.RandomBalance;
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

import java.util.HashMap;
import java.util.Map;

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
        return new ZookeeperServerRegister(rpcConfig.getRegisterAddress(),
                rpcConfig.getServerPort(),
                rpcConfig.getProtocol());
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
        if (rpcConfig.getLoadBalance().equals(RpcConstant.BALANCE_RANDOM)){
            clientProxyFactory.setLoadBalance(new RandomBalance());
        }else if (rpcConfig.getLoadBalance().equals(RpcConstant.BALANCE_ROUND)){
            clientProxyFactory.setLoadBalance(new FullRoundBalance());
        }else {
            throw new RpcException("invalid load balance config");
        }
        // 设置网络层实现
        clientProxyFactory.setNetClient(new NettyNetClient());

        return clientProxyFactory;
    }

    @Bean
    public DefaultRpcProcessor rpcProcessor(@Autowired ClientProxyFactory clientProxyFactory,
                                            @Autowired ServerRegister serverRegister,
                                            @Autowired RpcServer rpcServer){
        return new DefaultRpcProcessor(clientProxyFactory,serverRegister,rpcServer);
    }



}
