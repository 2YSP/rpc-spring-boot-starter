package com.github.ship.client.proxy.impl;

import com.github.ship.client.core.MethodInvoker;
import com.github.ship.client.proxy.AbstractClientProxyFactory;
import com.github.ship.common.exception.RpcException;
import com.github.ship.util.ReflectUtils;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @Author: Ship
 * @Description: Javassist字节码创建代理对象
 * @Date: Created in 2023/7/10
 */
public class JavassistClientProxyFactory extends AbstractClientProxyFactory {

    private static Logger logger = LoggerFactory.getLogger(JavassistClientProxyFactory.class);

    private static final String METHOD_INVOKER_CLASS_PATH = "com.github.ship.client.core.MethodInvoker";

    public JavassistClientProxyFactory(MethodInvoker methodInvoker) {
        super(methodInvoker);
    }

    @Override
    protected Object newProxyInstance(Class clazz) {
        return this.generateClassInstance(clazz);
    }

    private Object generateClassInstance(Class<?> clazz) {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new LoaderClassPath(clazz.getClassLoader()));
        // 创建实现类
        String implClassName = clazz.getName() + "Impl";
        CtClass cc = pool.makeClass(implClassName);
        try {
            // 添加接口
            cc.addInterface(toCtClass(clazz, pool));

            // 2. 新增一个字段 private MethodInvoker methodInvoker;
            // 字段名为 methodInvoker
            CtField param = new CtField(pool.get(METHOD_INVOKER_CLASS_PATH), "methodInvoker", cc);
            // 访问级别是 private
            param.setModifiers(Modifier.PRIVATE);
            cc.addField(param);

            // 添加构造方法
            CtConstructor constructor = new CtConstructor(new CtClass[]{pool.get(METHOD_INVOKER_CLASS_PATH)}, cc);
            constructor.setBody("{$0.methodInvoker = $1;}");
            cc.addConstructor(constructor);

            Method[] declaredMethods = clazz.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                // 创建方法
                CtClass[] parameters = new CtClass[method.getParameterTypes().length];
                for (int i = 0; i < method.getParameterTypes().length; i++) {
                    parameters[i] = toCtClass(method.getParameterTypes()[i], pool);
                }
                CtMethod ctMethod = new CtMethod(toCtClass(method.getReturnType(), pool)
                        , method.getName(), parameters, cc);
                ctMethod.setModifiers(Modifier.PUBLIC);
                /***
                 *
                 *
                 * return methodInvoker.$invoke(clazz.getName(), method.getName(), ReflectUtils.getParameterTypeNames(method), args, false)
                 */
                String body = String.format("{return methodInvoker.$invoke(\"%s\", \"%s\", new String[]{%s},new Object[]{%s}, Boolean.FALSE);}",
                        clazz.getName(),
                        method.getName(),
                        buildParameters(method), buildRealParameters(method));
                ctMethod.setBody(body);
                cc.addMethod(ctMethod);
            }
            // 创建实例
            Constructor<?> con = cc.toClass().getConstructor(MethodInvoker.class);
            Object instance = con.newInstance(methodInvoker);
            return instance;
        } catch (Exception e) {
            logger.error("generateClassInstance error", e);
        }
        return null;
    }

    /**
     *
     * @param method
     * @return
     */
    private static String buildRealParameters(Method method) {
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
     *
     * @param method
     * @return
     */
    private static String buildParameters(Method method) {
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

    private static CtClass toCtClass(Class clazz, ClassPool pool) throws NotFoundException {
        return pool.get(clazz.getName());
    }
}
