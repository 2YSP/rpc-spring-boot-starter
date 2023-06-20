package com.github.ship.spi.protocol.impl;

import com.github.ship.annotation.MessageProtocolAno;
import com.github.ship.common.constants.RpcConstant;
import com.github.ship.common.constants.RpcStatusEnum;
import com.github.ship.common.model.RpcRequest;
import com.github.ship.common.model.RpcResponse;
import com.github.ship.spi.protocol.MessageProtocol;
import com.github.ship.util.GenericObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.hessian.generic.io.GenericSerializerFactory;
import com.alipay.hessian.generic.model.GenericObject;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * @author Ship
 * @version 1.0.0
 * @description:
 * @date 2023/06/15 14:00
 */
@MessageProtocolAno(RpcConstant.PROTOCOL_HESSIAN)
public class HessianMessageProtocol implements MessageProtocol {

    private final static SerializerFactory serializerFactory = new SerializerFactory();

    private final static GenericSerializerFactory genericSerializerFactory = new GenericSerializerFactory();

    @Override
    public byte[] marshallingRequest(RpcRequest request) throws Exception {
        Map<String, Object> dataMap = JSON.parseObject(JSON.toJSONString(request), Map.class);
        GenericObject genericObject = GenericObjectUtil.buildGenericObject(RpcRequest.class.getName(), dataMap);
        // Do serializer
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Hessian2Output hout = new Hessian2Output(bout);
        hout.setSerializerFactory(genericSerializerFactory); // set to genericSerializerFactory
        hout.writeObject(genericObject);
        hout.close();
        return bout.toByteArray();
    }

    @Override
    public RpcRequest unmarshallingRequest(byte[] data) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(data, 0, data.length);
        Hessian2Input hin = new Hessian2Input(bin);
        hin.setSerializerFactory(serializerFactory);
        Object dst = hin.readObject();
        hin.close();
        RpcRequest rpcRequest = (RpcRequest) dst;
        for (int i = 0; i < rpcRequest.getParameterTypeNames().length; i++) {
            if ("java.lang.Long".equals(rpcRequest.getParameterTypeNames()[i]) && rpcRequest.getParameters()[i] != null) {
                rpcRequest.getParameters()[i] = Long.valueOf(rpcRequest.getParameters()[i].toString());
            }
        }
        return rpcRequest;
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) throws Exception {
        Map<String, Object> dataMap = JSON.parseObject(JSON.toJSONString(response), Map.class);
        GenericObject genericObject = GenericObjectUtil.buildGenericObject(RpcResponse.class.getName(), dataMap);
        // Do serializer
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Hessian2Output hout = new Hessian2Output(bout);
        hout.setSerializerFactory(genericSerializerFactory); // set to genericSerializerFactory
        hout.writeObject(genericObject);
        hout.close();
        return bout.toByteArray();
    }

    @Override
    public RpcResponse unmarshallingResponse(byte[] data) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(data, 0, data.length);
        Hessian2Input hin = new Hessian2Input(bin);
        hin.setSerializerFactory(serializerFactory);
        Object dst = hin.readObject();
        hin.close();
        return (RpcResponse) dst;
    }

    public static void main(String[] args) throws Exception {
        RpcRequest request = new RpcRequest();
        request.setRequestId("001");
        request.setServiceName("user-service");
        request.setMethod("sayHello");
        request.setParameterTypeNames(new String[]{"java.lang.Long"});
        request.setParameters(new Object[]{1L});

        HessianMessageProtocol hessianMessageProtocol = new HessianMessageProtocol();
        byte[] bytes = hessianMessageProtocol.marshallingRequest(request);
        System.out.println(bytes.length);

        RpcRequest request1 = hessianMessageProtocol.unmarshallingRequest(bytes);
        System.out.println(request1);

        RpcResponse response = new RpcResponse();
        response.setRequestId("001");
        response.setReturnValue("aaa");
        response.setRpcStatus(RpcStatusEnum.SUCCESS.getCode());

        byte[] bytes1 = hessianMessageProtocol.marshallingResponse(response);

        RpcResponse rpcResponse = hessianMessageProtocol.unmarshallingResponse(bytes1);
        System.out.println(rpcResponse.toString());

    }


}
