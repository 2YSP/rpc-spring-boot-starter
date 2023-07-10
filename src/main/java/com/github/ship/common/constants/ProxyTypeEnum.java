package com.github.ship.common.constants;

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
    JDK("jdk", "jdk动态代理"),
    /**
     * javassist字节码生成
     */
    JAVASSIST("javassist", "javassist字节码生成");

    private String code;

    private String desc;

    ProxyTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ProxyTypeEnum getByCode(String code) {
        return Arrays.asList(values()).stream().filter(i -> i.getCode().equals(code)).findFirst().orElse(null);
    }

}
