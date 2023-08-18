package com.github.ship.common.constants;

import com.github.ship.common.exception.RpcException;
import com.github.ship.discovery.nacos.NacosServerDiscovery;
import com.github.ship.discovery.zk.ZookeeperServerDiscovery;

import java.util.Arrays;

/**
 * @author Ship
 * @version 1.0.0
 * @description:
 * @date 2023/06/16 16:14
 */
public enum RegisterCenterTypeEnum {

    ZOOKEEPER("zk", "zookeeper", ZookeeperServerDiscovery.class),
    NACOS("nacos", "nacos", NacosServerDiscovery.class);

    private String code;

    private String desc;

    private Class clazz;

    RegisterCenterTypeEnum(String code, String desc, Class clazz) {
        this.code = code;
        this.desc = desc;
        this.clazz = clazz;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public Class getClazz() {
        return clazz;
    }

    public static RegisterCenterTypeEnum getByCode(String code) {
        return Arrays.asList(values()).stream().filter(i -> i.getCode().equals(code)).findFirst()
                .orElseThrow(() -> new RpcException("invalid config of registerCenterType"));
    }

}
