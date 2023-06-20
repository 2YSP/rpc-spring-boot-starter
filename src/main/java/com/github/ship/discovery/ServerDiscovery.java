package com.github.ship.discovery;

import com.github.ship.common.model.Service;

import java.util.List;

/**
 *
 * 服务发现抽象类
 * @author 2YSP
 * @date 2020/7/25 19:45
 */
public interface ServerDiscovery {

    /**
     * 服务暴露
     * @param serviceResource
     */
    void exportService(Service serviceResource);

    /**
     * 根据服务名查找服务列表
     * @param serviceName
     * @return
     */
    List<Service> findServiceList(String serviceName);

    /**
     * 注册服务监听
     */
    void registerChangeListener();
}
