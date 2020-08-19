package cn.sp.rpc.client.net.handler;

import cn.sp.rpc.client.net.RpcFuture;
import cn.sp.rpc.common.protocol.MessageProtocol;
import cn.sp.rpc.common.protocol.RpcRequest;
import cn.sp.rpc.common.protocol.RpcResponse;
import cn.sp.rpc.exception.RpcException;
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
import java.util.concurrent.TimeUnit;

/**
 * @author 2YSP
 * @date 2020/8/19 20:06
 */
public class SendHandlerV2 extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(SendHandlerV2.class);

    private volatile Channel channel;

    private static Map<String, RpcFuture<RpcResponse>> map = new ConcurrentHashMap<>();

    private MessageProtocol messageProtocol;

    private volatile boolean flag;

    public SendHandlerV2(MessageProtocol messageProtocol) {
        this.messageProtocol = messageProtocol;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        flag = true;
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
        RpcFuture<RpcResponse> future = map.get(response.getRequestId());
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

    public RpcResponse sendRequest(RpcRequest request) {
        RpcResponse response;
        RpcFuture<RpcResponse> future = new RpcFuture<>();
        map.put(request.getRequestId(), future);
        try {
            byte[] data = messageProtocol.marshallingRequest(request);
            ByteBuf reqBuf = Unpooled.buffer(data.length);
            reqBuf.writeBytes(data);
            while (!flag){
                Thread.sleep(10);
            }
            channel.writeAndFlush(reqBuf);
            // 等待响应
            response = future.get(8, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RpcException(e.getMessage());
        } finally {
            map.remove(request.getRequestId());
        }
        return response;
    }


}
