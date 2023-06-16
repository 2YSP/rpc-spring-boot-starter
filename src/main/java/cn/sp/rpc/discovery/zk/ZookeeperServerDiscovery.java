package cn.sp.rpc.discovery.zk;

import cn.sp.rpc.client.cache.ServerDiscoveryCache;
import cn.sp.rpc.discovery.ServerDiscovery;
import cn.sp.rpc.common.constants.RpcConstant;
import cn.sp.rpc.common.model.Service;
import cn.sp.rpc.common.serializer.ZookeeperSerializer;
import com.alibaba.fastjson.JSON;
import org.I0Itec.zkclient.ZkClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.sp.rpc.common.constants.RpcConstant.*;
import static cn.sp.rpc.common.constants.RpcConstant.PATH_DELIMITER;

/**
 * zk服务注册器，提供服务注册、暴露服务的能力
 * @author 2YSP
 * @date 2020/7/25 19:49
 */
public class ZookeeperServerDiscovery implements ServerDiscovery {

    private ZkClient zkClient;

    public ZookeeperServerDiscovery(String zkAddress) {
        zkClient = new ZkClient(zkAddress);
        zkClient.setZkSerializer(new ZookeeperSerializer());
    }

    /**
     * 服务暴露(其实就是把服务信息保存到Zookeeper上)
     * @param serviceResource
     */
    @Override
    public void exportService(Service serviceResource) {
        String serviceName = serviceResource.getName();
        String uri = JSON.toJSONString(serviceResource);
        try {
            uri = URLEncoder.encode(uri, UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String servicePath = ZK_SERVICE_PATH + PATH_DELIMITER + serviceName + "/service";
        if (!zkClient.exists(servicePath)) {
            // 没有该节点就创建
            zkClient.createPersistent(servicePath, true);
        }

        String uriPath = servicePath + PATH_DELIMITER + uri;
        if (zkClient.exists(uriPath)) {
            // 删除之前的节点
            zkClient.delete(uriPath);
        }
        // 创建一个临时节点，会话失效即被清理
        zkClient.createEphemeral(uriPath);
    }

    /**
     * 使用Zookeeper客户端，通过服务名获取服务列表
     * 服务名格式：接口全路径
     *
     * @param name
     * @return
     */
    @Override
    public List<Service> findServiceList(String name) {
        String servicePath = RpcConstant.ZK_SERVICE_PATH + RpcConstant.PATH_DELIMITER + name + "/service";
        List<String> children = zkClient.getChildren(servicePath);
        return Optional.ofNullable(children).orElse(new ArrayList<>()).stream().map(str -> {
            String deCh = null;
            try {
                deCh = URLDecoder.decode(str, RpcConstant.UTF_8);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return JSON.parseObject(deCh, Service.class);
        }).collect(Collectors.toList());
    }


    @Override
    public void registerChangeListener() {
        ServerDiscoveryCache.SERVICE_CLASS_NAMES.forEach(name -> {
            String servicePath = RpcConstant.ZK_SERVICE_PATH + RpcConstant.PATH_DELIMITER + name + "/service";
            zkClient.subscribeChildChanges(servicePath, new ZkChildListenerImpl());
        });
    }
}
