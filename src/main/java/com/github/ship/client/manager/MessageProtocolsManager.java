package com.github.ship.client.manager;

import com.github.ship.spi.ExtensionLoader;
import com.github.ship.spi.protocol.MessageProtocol;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class MessageProtocolsManager {

    private static final ExtensionLoader<MessageProtocol> EXTENSION_LOADER = ExtensionLoader.getExtensionLoader(MessageProtocol.class);

    /**
     * 获取消息协议实现
     * @param protocolName
     * @return
     */
    public static MessageProtocol get(String protocolName) {
        return EXTENSION_LOADER.getExtension(protocolName);
    }
}
