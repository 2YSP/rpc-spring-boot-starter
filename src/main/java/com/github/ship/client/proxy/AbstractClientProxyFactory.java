package com.github.ship.client.proxy;

import com.github.ship.client.core.MethodInvoker;
import com.github.ship.util.ReflectUtils;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端代理工厂：用于创建远程服务代理类
 * 封装编组请求、请求发送、编组响应等操作
 *
 * @author 2YSP
 * @date 2020/7/25 20:55
 */
public abstract class AbstractClientProxyFactory implements ClientProxyFactory {

    private Map<Class<?>, Object> objectCache = new ConcurrentHashMap<>();

    protected MethodInvoker methodInvoker;


    public AbstractClientProxyFactory(MethodInvoker methodInvoker) {
        this.methodInvoker = methodInvoker;
    }

    /**
     * 获取客户端服务代理对象
     *
     * @param clazz
     * @param <T>
     * @return
     */
    @Override
    public <T> T getProxy(Class<T> clazz) {
        return (T) objectCache.computeIfAbsent(clazz, clz -> this.newProxyInstance(clz));
    }

    /**
     * 模板方法子类实现
     * @param clazz
     * @return
     */
    protected Object newProxyInstance(Class clazz) {
        throw new UnsupportedOperationException("must have impl method");
    }


}
