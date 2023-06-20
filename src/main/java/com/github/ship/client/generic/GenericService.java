package com.github.ship.client.generic;


/**
 * @author Ship
 * @version 1.0.0
 * @description:
 * @date 2023/06/15 14:38
 */
public interface GenericService {

    /**
     * 泛化调用
     * @param methodName
     * @param parameterTypeNames
     * @param args
     * @return
     */
    Object $invoke(String methodName, String[] parameterTypeNames, Object[] args);
}
