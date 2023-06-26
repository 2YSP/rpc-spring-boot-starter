package com.github.ship.client.manager;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.github.ship.client.cache.ServerDiscoveryCache;
import com.github.ship.common.exception.RpcException;
import com.github.ship.common.model.Service;
import com.github.ship.discovery.ServerDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class ServerDiscoveryManager {

    private static Logger logger = LoggerFactory.getLogger(ServerDiscoveryManager.class);

    private ServerDiscovery serverDiscovery;
    /**
     * 已经注册的远程服务service监听器集合
     */
    private static volatile Set<String> SERVICE_REGISTERED_LISTENERS = new ConcurrentHashSet<>();

    public ServerDiscoveryManager(ServerDiscovery serverDiscovery) {
        this.serverDiscovery = serverDiscovery;
    }

    /**
     * 注册监听
     */
    public void registerChangeListener(String serviceName) {
        synchronized (serviceName.intern()) {
            if (SERVICE_REGISTERED_LISTENERS.contains(serviceName)) {
                return;
            }
            serverDiscovery.registerChangeListener(serviceName);
            SERVICE_REGISTERED_LISTENERS.add(serviceName);
        }
    }

    /**
     * 根据服务名获取可用的服务地址列表
     *
     * @param serviceName
     * @return
     */
    public List<Service> getServiceList(String serviceName) {
        List<Service> serviceList = null;
        try {
            serviceList = ServerDiscoveryCache.get(serviceName, () -> {
                List<Service> services = serverDiscovery.findServiceList(serviceName);
                if (services == null || services.size() == 0) {
                    throw new RpcException("No provider available!");
                }
                this.registerChangeListener(serviceName);
                return services;
            });
        } catch (ExecutionException e) {
            logger.error("加载服务列表缓存异常", e);
            throw new RpcException(e.getMessage());
        } catch (Exception e) {
            logger.error("加载服务列表缓存异常", e);
            throw new RpcException(e.getMessage());
        }
        return serviceList;
    }
}
