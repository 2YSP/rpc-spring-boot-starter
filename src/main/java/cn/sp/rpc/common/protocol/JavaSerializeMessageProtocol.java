package cn.sp.rpc.common.protocol;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * Java序列化消息协议
 * @author 2YSP
 * @date 2020/7/25 21:07
 */
public class JavaSerializeMessageProtocol implements MessageProtocol {

    private byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(o);
        return bout.toByteArray();

    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
       final int objectNum = 1000000;
        List<RpcRequest> list = new ArrayList<>(objectNum);
        for (int i=0;i<objectNum;i++){
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setServiceName("a"+i);
            rpcRequest.setParameterTypes(new Class<?>[]{List.class,JavaSerializeMessageProtocol.class});
            rpcRequest.setParameters(new Object[]{1,"xx"});
            rpcRequest.setMethod("A method");
            rpcRequest.setHeaders(new HashMap<>());
            list.add(rpcRequest);
        }
        JavaSerializeMessageProtocol messageProtocol = new JavaSerializeMessageProtocol();
        ProtoBufMessageProtocol protoBuf = new ProtoBufMessageProtocol();

        long t1 = System.currentTimeMillis();
        int size = 0;
        for(int j=0;j<list.size();j++){
            byte[] bytes = messageProtocol.marshallingRequest(list.get(j));
            size += bytes.length;
        }
        long t2 = System.currentTimeMillis();
        // java序列化耗时：7926ms,byte数组大小为：568888890
        System.out.println("java序列化耗时："+(t2-t1)+"ms,byte数组大小为："+size);

        long t3 = System.currentTimeMillis();
        int sum = 0;
        for(int j=0;j<list.size();j++){
            RpcRequest rpcRequest = list.get(j);
            byte[] bytes = protoBuf.marshallingRequest(rpcRequest);
            sum += bytes.length;
        }
        long t4 = System.currentTimeMillis();
        // protobuf序列化耗时：1532ms,byte数组大小为：155888890
        System.out.println("protobuf序列化耗时："+(t4-t3)+"ms,byte数组大小为："+sum);

    }

    @Override
    public byte[] marshallingRequest(RpcRequest request) throws Exception {
        return this.serialize(request);
    }

    @Override
    public RpcRequest unmarshallingRequest(byte[] data) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        return (RpcRequest) in.readObject();
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) throws Exception {
        return this.serialize(response);
    }

    @Override
    public RpcResponse unmarshallingResponse(byte[] data) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        return (RpcResponse) in.readObject();
    }
}
