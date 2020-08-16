package cn.sp.rpc.client.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 *
 *  发送处理类，定义Netty入站处理细则
 * @author 2YSP
 * @date 2020/7/25 20:15
 */
public class SendHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(SendHandler.class);

    private CountDownLatch cdl;

    private Object readMsg;

    private byte[] data;

    public SendHandler(byte[] data){
        cdl = new CountDownLatch(1);
        this.data = data;
    }

    /**
     * 连接服务端成功后发送数据
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Connect to server successfully:{}",ctx);
        ByteBuf reqBuf = Unpooled.buffer(data.length);
        reqBuf.writeBytes(data);
//        ByteBuf reqBuf = Unpooled.copiedBuffer(data);
        logger.debug("Client sends message:{}",reqBuf);
        ctx.writeAndFlush(reqBuf);
    }

    /**
     * 读取数据，数据读取完毕释放cd锁
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("Client reads message:{}",msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] resp = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(resp);
        // 手动回收
        ReferenceCountUtil.release(byteBuf);
        readMsg = resp;
        cdl.countDown();
    }


    public Object respData() throws InterruptedException {
        cdl.await();
        return readMsg;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.error("Exception occurred:{}",cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
