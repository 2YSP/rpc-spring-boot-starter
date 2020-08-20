package cn.sp.rpc.client.net;


import cn.sp.rpc.client.net.handler.SendHandler;
import cn.sp.rpc.client.net.handler.SendHandlerV2;
import cn.sp.rpc.common.model.Service;
import cn.sp.rpc.common.protocol.MessageProtocol;
import cn.sp.rpc.common.model.RpcRequest;
import cn.sp.rpc.common.model.RpcResponse;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;


/**
 * @author 2YSP
 * @date 2020/7/25 20:12
 */
public class NettyNetClient implements NetClient {

    private static Logger logger = LoggerFactory.getLogger(NettyNetClient.class);

    private static ExecutorService threadPool = new ThreadPoolExecutor(4, 10, 200,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), new ThreadFactoryBuilder()
            .setNameFormat("rpcClient-%d")
            .build());

    private EventLoopGroup loopGroup = new NioEventLoopGroup(4);

    /**
     * 已连接的服务缓存
     * key: 服务地址，格式：ip:port
     */
    public static Map<String, SendHandlerV2> connectedServerNodes = new ConcurrentHashMap<>();

    @Override
    public byte[] sendRequest(byte[] data, Service service) throws InterruptedException {
        String address = service.getAddress();
        String[] addrInfo = address.split(":");
        final String serverAddress = addrInfo[0];
        final String serverPort = addrInfo[1];
        SendHandler sendHandler = new SendHandler(data);
        byte[] respData;
        // 配置客户端
        EventLoopGroup loopGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(loopGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(sendHandler);
                        }
                    });
            // 启用客户端连接
            b.connect(serverAddress, Integer.parseInt(serverPort)).sync();
            respData = (byte[]) sendHandler.respData();
            logger.debug("SendRequest get reply: {}", respData);
        } finally {
            loopGroup.shutdownGracefully();
        }

        return respData;
    }

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest, Service service, MessageProtocol messageProtocol) {

        String address = service.getAddress();
        synchronized (address) {
            if (connectedServerNodes.containsKey(address)) {
                SendHandlerV2 handler = connectedServerNodes.get(address);
                logger.info("使用现有的连接");
                return handler.sendRequest(rpcRequest);
            }

            String[] addrInfo = address.split(":");
            final String serverAddress = addrInfo[0];
            final String serverPort = addrInfo[1];
            final SendHandlerV2 handler = new SendHandlerV2(messageProtocol, address);
            threadPool.submit(() -> {
                        // 配置客户端
                        Bootstrap b = new Bootstrap();
                        b.group(loopGroup).channel(NioSocketChannel.class)
                                .option(ChannelOption.TCP_NODELAY, true)
                                .handler(new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                                        ChannelPipeline pipeline = socketChannel.pipeline();
                                        pipeline
//                                                .addLast(new FixedLengthFrameDecoder(20))
                                                .addLast(handler);
                                    }
                                });
                        // 启用客户端连接
                        ChannelFuture channelFuture = b.connect(serverAddress, Integer.parseInt(serverPort));
                        channelFuture.addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                connectedServerNodes.put(address, handler);
                            }
                        });
                    }
            );
            logger.info("使用新的连接。。。");
            return handler.sendRequest(rpcRequest);
        }
    }
}
