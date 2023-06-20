package com.github.ship.client.generic;

import com.github.ship.client.core.MethodInvoker;


/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class DefaultGenericService implements GenericService {

    private MethodInvoker methodInvoker;

    private String interfaceClassName;

    public DefaultGenericService(MethodInvoker methodInvoker, String interfaceClassName) {
        this.methodInvoker = methodInvoker;
        this.interfaceClassName = interfaceClassName;
    }


    @Override
    public Object $invoke(String methodName, String[] parameterTypeNames, Object[] args) {
        return methodInvoker.$invoke(interfaceClassName, methodName, parameterTypeNames, args, true);
    }


}
