package com.github.ship.client.manager;

import com.github.ship.common.exception.RpcException;
import com.github.ship.spi.ExtensionLoader;
import com.github.ship.spi.balance.LoadBalance;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class LoadBalanceManager {

    private static final ExtensionLoader<LoadBalance> EXTENSION_LOADER = ExtensionLoader.getExtensionLoader(LoadBalance.class);

    /**
     * 使用spi匹配符合配置的负载均衡算法
     * @param name
     * @return
     */
    public static LoadBalance getLoadBalance(String name) {
        LoadBalance loadBalance = EXTENSION_LOADER.getExtension(name);
        if (loadBalance == null) {
            throw new RpcException("invalid load balance config: " + name);
        }
        return loadBalance;
    }
}
