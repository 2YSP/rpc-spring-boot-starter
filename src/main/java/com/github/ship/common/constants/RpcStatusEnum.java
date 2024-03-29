package com.github.ship.common.constants;

/**
 * @author 2YSP
 * @date 2020/7/25 21:11
 */
public enum RpcStatusEnum {
    /**
     * SUCCESS
     */
    SUCCESS(200, "SUCCESS"),
    /**
     * ERROR
     */
    ERROR(500, "ERROR"),
    /**
     * NOT FOUND
     */
    NOT_FOUND(404, "NOT FOUND"),
    /**
     * REQUEST TIME OUT
     */
    REQUEST_TIME_OUT(504, "REQUEST TIME OUT");

    private Integer code;

    private String desc;

    RpcStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
