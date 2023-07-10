package com.github.ship.client.proxy.impl;

import com.github.ship.client.core.MethodInvoker;
import com.github.ship.client.proxy.AbstractClientProxyFactory;
import com.github.ship.util.ReflectUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author: Ship
 * @Description: JDK动态代理实现
 * @Date: Created in 2023/7/10
 */
public class JdkClientProxyFactory extends AbstractClientProxyFactory {


    public JdkClientProxyFactory(MethodInvoker methodInvoker) {
        super(methodInvoker);
    }

    @Override
    protected Object newProxyInstance(Class clazz) {
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ClientInvocationHandler(clazz));
    }

    private class ClientInvocationHandler implements InvocationHandler {

        private Class<?> clazz;

        public ClientInvocationHandler(Class<?> clazz) {
            this.clazz = clazz;
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("toString")) {
                return proxy.toString();
            }
            if (method.getName().equals("hashCode")) {
                return 0;
            }
            return methodInvoker.$invoke(clazz.getName(), method.getName(), ReflectUtils.getParameterTypeNames(method), args, false);
        }
    }
}
