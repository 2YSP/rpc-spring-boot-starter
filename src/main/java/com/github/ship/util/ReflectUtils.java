package com.github.ship.util;

import java.lang.reflect.Method;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class ReflectUtils {

    /**
     * @param method
     * @return
     */
    public static String[] getParameterTypeNames(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        String[] arr = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            arr[i] = parameterTypes[i].getName();
        }
        return arr;
    }

    /**
     * @param parameterTypeNames
     * @return
     */
    public static Class[] convertToParameterTypes(String[] parameterTypeNames) {
        Class[] arr = new Class[parameterTypeNames.length];
        for (int i = 0; i < parameterTypeNames.length; i++) {
            try {
                arr[i] = Class.forName(parameterTypeNames[i]);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return arr;
    }
}
