package com.github.ship.client.generic;

import com.github.ship.client.core.MethodInvoker;
import com.github.ship.util.SpringContextHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public final class GenericServiceFactory {

    /**
     * 实例缓存，key:接口类名
     */
    private static final Map<String, GenericService> INSTANCE_MAP = new ConcurrentHashMap<>();

    private GenericServiceFactory() {}

    /**
     * @param interfaceClassName
     * @return
     */
    public static GenericService getInstance(String interfaceClassName) {
        return INSTANCE_MAP.computeIfAbsent(interfaceClassName, clz -> {
            MethodInvoker methodInvoker = SpringContextHolder.getBean(MethodInvoker.class);
            DefaultGenericService genericService = new DefaultGenericService(methodInvoker, interfaceClassName);
            return genericService;
        });
    }
}
