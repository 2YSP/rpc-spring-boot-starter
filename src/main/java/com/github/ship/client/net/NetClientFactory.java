package com.github.ship.client.net;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class NetClientFactory {

    private NetClientFactory(){

    }

    public static NetClient getInstance() {
        return new NettyNetClient();
    }
}
