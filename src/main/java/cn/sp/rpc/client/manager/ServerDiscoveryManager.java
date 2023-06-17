package cn.sp.rpc.client.manager;

import cn.sp.rpc.client.cache.ServerDiscoveryCache;
import cn.sp.rpc.discovery.ServerDiscovery;
import cn.sp.rpc.common.model.Service;
import cn.sp.rpc.common.exception.RpcException;

import java.util.List;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class ServerDiscoveryManager {

    private ServerDiscovery serverDiscovery;

    public ServerDiscoveryManager(ServerDiscovery serverDiscovery) {
        this.serverDiscovery = serverDiscovery;
    }

    /**
     *
     */
    public void registerChangeListener() {
        serverDiscovery.registerChangeListener();
    }

    /**
     * 根据服务名获取可用的服务地址列表
     *
     * @param serviceName
     * @return
     */
    public List<Service> getServiceList(String serviceName) {
        List<Service> services;
        synchronized (serviceName.intern()) {
            if (ServerDiscoveryCache.isEmpty(serviceName)) {
                services = serverDiscovery.findServiceList(serviceName);
                if (services == null || services.size() == 0) {
                    throw new RpcException("No provider available!");
                }
                ServerDiscoveryCache.put(serviceName, services);
            } else {
                services = ServerDiscoveryCache.get(serviceName);
            }
        }
        return services;
    }
}
