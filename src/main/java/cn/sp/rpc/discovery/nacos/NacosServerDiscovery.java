package cn.sp.rpc.discovery.nacos;

import cn.sp.rpc.client.cache.ServerDiscoveryCache;
import cn.sp.rpc.common.constants.RpcConstant;
import cn.sp.rpc.common.exception.RpcException;
import cn.sp.rpc.common.model.Service;
import cn.sp.rpc.discovery.ServerDiscovery;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ship
 * @version 1.0.0
 * @description:
 * @date 2023/06/16 15:21
 */
public class NacosServerDiscovery implements ServerDiscovery {

    private static Logger logger = LoggerFactory.getLogger(NacosServerDiscovery.class);

    private NamingService namingService;

    public NacosServerDiscovery(String serverAddr) {
        try {
            this.namingService = NamingFactory.createNamingService(serverAddr);
        } catch (NacosException e) {
            throw new RpcException("create nacos namingService fail");
        }
    }

    @Override
    public void exportService(Service serviceResource) {
        Instance instance = new Instance();
        instance.setIp(serviceResource.getIp());
        instance.setPort(serviceResource.getPort());
        instance.setEphemeral(true);
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("protocol", serviceResource.getProtocol());
        instance.setWeight(serviceResource.getWeight());
        instance.setMetadata(metadataMap);
        try {
            namingService.registerInstance(serviceResource.getName(), RpcConstant.NACOS_APP_GROUP_NAME, instance);
        } catch (NacosException e) {
            logger.error("register to nacos fail", e);
            throw new RpcException("register to nacos fail");
        }
        logger.info("register interface info to nacos success!");
    }

    @Override
    public List<Service> findServiceList(String serviceName) {
        List<Instance> instanceList = null;
        try {
            instanceList = namingService.getAllInstances(serviceName);
        } catch (NacosException e) {
            logger.error("get all instances fail", e);
            throw new RpcException("get all instances fail");
        }
        if (CollectionUtils.isEmpty(instanceList)) {
            return Lists.newArrayList();
        }
        return instanceList.stream()
                .filter(i -> i.isHealthy())
                .map(instance -> convertToService(instance)).collect(Collectors.toList());
    }

    private Service convertToService(Instance instance) {
        Service service = new Service();
        service.setWeight((int) instance.getWeight());
        service.setName(instance.getServiceName());
        Map<String, String> metadata = instance.getMetadata();
        service.setProtocol(metadata.get("protocol"));
        service.setIp(instance.getIp());
        service.setPort(instance.getPort());
        return service;
    }

    @Override
    public void registerChangeListener() {
        ServerDiscoveryCache.SERVICE_CLASS_NAMES.forEach(serviceName -> {
            try {
                namingService.subscribe(serviceName, event -> {
                    if (event instanceof NamingEvent) {
                        List<Instance> instances = ((NamingEvent) event).getInstances();
                        if (CollectionUtils.isEmpty(instances)) {
                            ServerDiscoveryCache.removeAll(serviceName);
                        }
                        List<Service> serviceList = instances.stream().filter(i -> i.isHealthy()).map(this::convertToService).collect(Collectors.toList());
                        if (CollectionUtils.isEmpty(serviceList)) {
                            ServerDiscoveryCache.removeAll(serviceName);
                        } else {
                            ServerDiscoveryCache.put(serviceName, serviceList);
                        }
                    }
                });
            } catch (NacosException e) {
                e.printStackTrace();
            }
        });
    }
}
