package cn.sp.rpc.common.constants;

import java.util.Arrays;

/**
 * @author Ship
 * @version 1.0.0
 * @description:
 * @date 2023/06/16 16:14
 */
public enum RegisterCenterTypeEnum {

    ZOOKEEPER("zk", "zookeeper"),
    NACOS("nacos", "nacos");

    private String code;

    private String desc;

    RegisterCenterTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RegisterCenterTypeEnum getByCode(String code) {
        return Arrays.asList(values()).stream().filter(i -> i.getCode().equals(code)).findFirst().orElse(null);
    }

}
