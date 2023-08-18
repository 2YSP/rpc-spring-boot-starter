package com.github.ship.util;

import com.github.ship.common.exception.RpcException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author Ship
 * @version 1.0.0
 * @description: 代码生成工具类
 * @date 2023/08/18 14:27
 */
public class CodeGenerateUtils {


    /**
     * 构建Javassist需要的方法体
     * @param interfaceClazz
     * @param method
     * @return
     */
    public static String genJavassistMethodBody(Class interfaceClazz, Method method) {
        String methodBody = String.format("{\n  return methodInvoker.$invoke(\"%s\", \"%s\", new String[]{%s},new Object[]{%s}, Boolean.FALSE);\n}",
                interfaceClazz.getName(),
                method.getName(),
                buildParameters(method), buildRealParameters(method));
        return methodBody;
    }

    /**
     * 构建JavaCompiler需要的方法体
     * @param interfaceClazz
     * @param method
     * @return
     */
    public static String genJavaCompilerMethodBody(Class interfaceClazz, Method method) {
        String methodBody = String.format("{\n  return methodInvoker.$invoke(\"%s\", \"%s\", new String[]{%s},new Object[]{%s}, Boolean.FALSE);\n}",
                interfaceClazz.getName(),
                method.getName(),
                buildParameters(method), buildRealParameterNames(method));
        return methodBody;
    }


    public static String buildRealParameterNames(Method method) {
        int paramLength = method.getParameterCount();
        if (paramLength == 0) {
            throw new RpcException("at last one parameter required");
        }
        final StringBuilder sb = new StringBuilder();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < paramLength; i++) {
            sb.append(parameters[i].getName());
            if (i != paramLength - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * @param method
     * @return
     */
    public static String buildRealParameters(Method method) {
        int paramLength = method.getParameterCount();
        if (paramLength == 0) {
            throw new RpcException("at last one parameter required");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paramLength; i++) {
            sb.append("$" + (i + 1));
            if (i != paramLength - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * @param method
     * @return
     */
    public static String buildParameters(Method method) {
        if (method.getParameterCount() == 0) {
            throw new RpcException("at last one parameter required");
        }
        String[] parameterTypeNames = ReflectUtils.getParameterTypeNames(method);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String p : parameterTypeNames) {
            sb.append("\"");
            sb.append(p);
            sb.append("\"");
            if (i != parameterTypeNames.length - 1) {
                sb.append(",");
            }
            i++;
        }
        return sb.toString();
    }
}
