package cn.sp.rpc.discovery;

import cn.sp.rpc.common.model.Service;

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
     * @param name
     * @return
     */
    List<Service> findServiceList(String name);

    /**
     * 注册服务监听
     */
    void registerChangeListener();
}
