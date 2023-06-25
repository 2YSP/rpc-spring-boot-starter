package com.github.ship.client.core;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public interface MethodInvoker {

    /**
     * @param interfaceClassName
     * @param methodName
     * @param parameterTypeNames
     * @param args
     * @param generic
     * @return
     */
    Object $invoke(String interfaceClassName, String methodName, String[] parameterTypeNames, Object[] args, Boolean generic);
}
