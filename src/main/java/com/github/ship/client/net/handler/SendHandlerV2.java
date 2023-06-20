package com.github.ship.client.net.handler;

import com.github.ship.client.net.NettyNetClient;
import com.github.ship.client.net.RpcFuture;
import com.github.ship.common.model.RpcRequest;
import com.github.ship.common.model.RpcResponse;
import com.github.ship.spi.protocol.MessageProtocol;
import com.github.ship.common.exception.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author 2YSP
 * @date 2020/8/19 20:06
 */
public class SendHandlerV2 extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(SendHandlerV2.class);

    /**
     * 等待通道建立最大时间
     */
    static final int CHANNEL_WAIT_TIME = 4;
    /**
     * 等待响应最大时间
     */
    static final int RESPONSE_WAIT_TIME = 8;

    private volatile Channel channel;

    private String remoteAddress;

    private static Map<String, RpcFuture<RpcResponse>> requestMap = new ConcurrentHashMap<>();

    private MessageProtocol messageProtocol;

    private CountDownLatch latch = new CountDownLatch(1);

    public SendHandlerV2(MessageProtocol messageProtocol,String remoteAddress) {
        this.messageProtocol = messageProtocol;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        latch.countDown();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Connect to server successfully:{}", ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("Client reads message:{}", msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] resp = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(resp);
        // 手动回收
        ReferenceCountUtil.release(byteBuf);
        RpcResponse response = messageProtocol.unmarshallingResponse(resp);
        RpcFuture<RpcResponse> future = requestMap.get(response.getRequestId());
        future.setResponse(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.error("Exception occurred:{}", cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.error("channel inactive with remoteAddress:[{}]",remoteAddress);
        NettyNetClient.connectedServerNodes.remove(remoteAddress);

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    public RpcResponse sendRequest(RpcRequest request) {
        RpcResponse response;
        RpcFuture<RpcResponse> future = new RpcFuture<>();
        requestMap.put(request.getRequestId(), future);
        try {
            byte[] data = messageProtocol.marshallingRequest(request);
            ByteBuf reqBuf = Unpooled.buffer(data.length);
            reqBuf.writeBytes(data);
            if (latch.await(CHANNEL_WAIT_TIME,TimeUnit.SECONDS)){
                channel.writeAndFlush(reqBuf);
                // 等待响应
                response = future.get(RESPONSE_WAIT_TIME, TimeUnit.SECONDS);
            }else {
                throw new RpcException("establish channel time out");
            }
        } catch (Exception e) {
            throw new RpcException(e.getMessage());
        } finally {
            requestMap.remove(request.getRequestId());
        }
        return response;
    }


}
