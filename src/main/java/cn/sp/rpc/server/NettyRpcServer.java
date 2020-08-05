package cn.sp.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author 2YSP
 * @date 2020/7/26 14:08
 */
public class NettyRpcServer extends RpcServer {

    private static Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

    private Channel channel;

    public NettyRpcServer(int port, String protocol, RequestHandler requestHandler) {
        super(port, protocol, requestHandler);
    }

    @Override
    public void start() {
        // 配置服务器
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,100)
                    .handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new ChannelRequestHandler());
                }
            });

            // 启动服务
            ChannelFuture future = b.bind(port).sync();
            logger.info("Server started successfully.");
            channel = future.channel();
            // 等待服务通道关闭
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
            logger.error("start netty sever failed,msg:{}",e.getMessage());
        }finally {
            // 释放线程组资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
        this.channel.close();
    }

    private class ChannelRequestHandler extends ChannelInboundHandlerAdapter{

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
           logger.info("Channel active :{}",ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            logger.info("the server receives message :{}",msg);
            ByteBuf byteBuf = (ByteBuf) msg;
            // 消息写入reqData
            byte[] reqData = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(reqData);
            byte[] respData = requestHandler.handleRequest(reqData);
            ByteBuf respBuf = Unpooled.buffer(respData.length);
            respBuf.writeBytes(respData);
            logger.info("Send response:{}",respBuf);
            ctx.write(respBuf);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            logger.error("Exception occurred:{}",cause.getMessage());
            ctx.close();
        }
    }
}
