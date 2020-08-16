package cn.sp.rpc.common.model;

/**
 * @author 2YSP
 * @date 2020/7/25 19:46
 */
public class Service {
    /**
     * 服务名称
     */
    private String name;
    /**
     * 服务协议
     */
    private String protocol;
    /**
     *  服务地址，格式：ip:port
     */
    private String address;
    /**
     * 权重，越大优先级越高
     */
    private Integer weight;


    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
