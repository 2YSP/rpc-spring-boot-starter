package com.github.ship.common.constants;

import com.github.ship.client.proxy.impl.JavassistClientProxyFactory;
import com.github.ship.client.proxy.impl.JdkClientProxyFactory;
import com.github.ship.client.proxy.impl.JdkCompilerClientProxyFactory;
import com.github.ship.common.exception.RpcException;

import java.util.Arrays;

/**
 * @author Ship
 * @version 1.0.0
 * @description:
 * @date 2023/06/16 16:14
 */
public enum ProxyTypeEnum {
    /**
     * jdk动态代理
     */
    JDK("jdk", "jdk动态代理", JdkClientProxyFactory.class),
    /**
     * javassist字节码生成
     */
    JAVASSIST("javassist", "javassist字节码生成", JavassistClientProxyFactory.class),
    /**
     * java动态编译
     */
    COMPILER("compiler", "java动态编译", JdkCompilerClientProxyFactory.class);

    private String code;

    private String desc;

    private Class clientProxyFactoryClass;

    ProxyTypeEnum(String code, String desc, Class clientProxyFactoryClass) {
        this.code = code;
        this.desc = desc;
        this.clientProxyFactoryClass = clientProxyFactoryClass;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public Class getClientProxyFactoryClass() {
        return clientProxyFactoryClass;
    }

    public static ProxyTypeEnum getByCode(String code) {
        return Arrays.asList(values()).stream().filter(i -> i.getCode().equals(code)).findFirst()
                .orElseThrow(() -> new RpcException("invalid config of proxyType!"));
    }


}
