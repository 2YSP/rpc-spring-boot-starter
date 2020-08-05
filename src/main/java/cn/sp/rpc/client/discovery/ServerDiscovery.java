package cn.sp.rpc.client.discovery;

import cn.sp.rpc.common.model.Service;

import java.util.List;

/**
 *
 * 服务发现抽象类
 * @author 2YSP
 * @date 2020/7/25 19:45
 */
public interface ServerDiscovery {

    List<Service> findServiceList(String name);
}
