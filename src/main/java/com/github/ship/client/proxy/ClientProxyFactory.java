package com.github.ship.client.proxy;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/7/10
 */
public interface ClientProxyFactory {

    /**
     * 获取客户端服务代理对象
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T getProxy(Class<T> clazz);
}
