package cn.sp.rpc.discovery.nacos;

import cn.sp.rpc.common.model.Service;
import cn.sp.rpc.discovery.ServerDiscovery;

import java.util.List;

/**
 * @author Ship
 * @version 1.0.0
 * @description:
 * @date 2023/06/16 15:21
 */
public class NacosServerDiscovery implements ServerDiscovery {

    @Override
    public void exportService(Service serviceResource) {

    }

    @Override
    public List<Service> findServiceList(String name) {
        return null;
    }

    @Override
    public void registerChangeListener() {

    }
}
