package cn.sp.rpc.client.net;


import cn.sp.rpc.client.net.handler.SendHandler;
import cn.sp.rpc.common.model.Service;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author 2YSP
 * @date 2020/7/25 20:12
 */
public class NettyNetClient implements NetClient {

    private static Logger logger = LoggerFactory.getLogger(NettyNetClient.class);

    @Override
    public byte[] sendRequest(byte[] data, Service service) throws InterruptedException {
        String[] addrInfo = service.getAddress().split(":");
        String serverAddress = addrInfo[0];
        String serverPort = addrInfo[1];
        SendHandler sendHandler = new SendHandler(data);
        byte[] respData;
        // 配置客户端
        EventLoopGroup loopGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(loopGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(sendHandler);
                        }
                    });
            // 启用客户端连接
            b.connect(serverAddress,Integer.parseInt(serverPort)).sync();
            respData = (byte[]) sendHandler.respData();
            logger.debug("SendRequest get reply: {}", respData);
        }finally {
            loopGroup.shutdownGracefully();
        }

        return respData;
    }
}
