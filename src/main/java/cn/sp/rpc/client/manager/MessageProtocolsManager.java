package cn.sp.rpc.client.manager;

import cn.sp.rpc.annotation.MessageProtocolAno;
import cn.sp.rpc.common.protocol.MessageProtocol;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class MessageProtocolsManager {

    private static Map<String, MessageProtocol> SUPPORT_MESSAGE_PROTOCOL_MAP = new HashMap<>();

    static {
        ServiceLoader<MessageProtocol> loader = ServiceLoader.load(MessageProtocol.class);
        Iterator<MessageProtocol> iterator = loader.iterator();
        while (iterator.hasNext()) {
            MessageProtocol messageProtocol = iterator.next();
            MessageProtocolAno ano = messageProtocol.getClass().getAnnotation(MessageProtocolAno.class);
            Assert.notNull(ano, "message protocol name can not be empty!");
            SUPPORT_MESSAGE_PROTOCOL_MAP.put(ano.value(), messageProtocol);
        }
    }

    /**
     * 获取消息协议实现
     * @param protocolName
     * @return
     */
    public static MessageProtocol get(String protocolName) {
        return SUPPORT_MESSAGE_PROTOCOL_MAP.get(protocolName);
    }
}
