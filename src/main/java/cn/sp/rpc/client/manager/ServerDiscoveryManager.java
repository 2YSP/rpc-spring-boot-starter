package cn.sp.rpc.client.manager;

import cn.sp.rpc.client.cache.ServerDiscoveryCache;
import cn.sp.rpc.common.exception.RpcException;
import cn.sp.rpc.common.model.Service;
import cn.sp.rpc.discovery.ServerDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class ServerDiscoveryManager {

    private static Logger logger = LoggerFactory.getLogger(ServerDiscoveryManager.class);

    private ServerDiscovery serverDiscovery;

    public ServerDiscoveryManager(ServerDiscovery serverDiscovery) {
        this.serverDiscovery = serverDiscovery;
    }

    /**
     * 注册监听
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
        List<Service> serviceList = null;
        try {
            serviceList = ServerDiscoveryCache.get(serviceName, () -> {
                List<Service> services = serverDiscovery.findServiceList(serviceName);
                if (services == null || services.size() == 0) {
                    throw new RpcException("No provider available!");
                }
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
