package cn.sp.rpc.client.cache;

import cn.sp.rpc.common.model.Service;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 服务发现本地缓存
 */
public class ServerDiscoveryCache {

    private static final Cache<String, List<Service>> SERVICE_CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .initialCapacity(32)
            .maximumSize(2048)
            .expireAfterWrite(30, TimeUnit.SECONDS).build();

    /**
     * 客户端注入的远程服务service class
     */
    public static final List<String> SERVICE_CLASS_NAMES = new ArrayList<>();

    public static void put(String serviceName, List<Service> serviceList) {
        SERVICE_CACHE.put(serviceName, serviceList);
    }

//    /**
//     * 去除指定的值
//     * @param serviceName
//     * @param service
//     */
//    public static void remove(String serviceName, Service service) {
//        SERVER_MAP.computeIfPresent(serviceName, (key, value) ->
//                value.stream().filter(o -> !o.toString().equals(service.toString())).collect(Collectors.toList())
//        );
//    }

    /**
     * 清空缓存
     * @param serviceName
     */
    public static void removeAll(String serviceName) {
        SERVICE_CACHE.invalidate(serviceName);
    }

    /**
     * 判断缓存是否存在
     * @param serviceName
     * @return
     */
    public static boolean isEmpty(String serviceName) {
        return CollectionUtils.isEmpty(SERVICE_CACHE.getIfPresent(serviceName));
    }

    /**
     * 获取缓存服务实例
     * @param serviceName
     * @return
     */
    public static List<Service> get(String serviceName) {
        return SERVICE_CACHE.getIfPresent(serviceName);
    }
}
