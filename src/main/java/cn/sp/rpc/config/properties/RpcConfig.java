package cn.sp.rpc.config.properties;

import cn.sp.rpc.common.constants.RegisterCenterTypeEnum;
import cn.sp.rpc.common.exception.RpcException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

/**
 * @author 2YSP
 * @date 2020/7/26 15:13
 */
@ConfigurationProperties(prefix = "sp.rpc")
public class RpcConfig {

    /**
     * 服务注册中心地址
     */
    private String registerAddress = "127.0.0.1:2181";
    /**
     * 注册中心类型
     */
    private String registerCenterType;

    /**
     * 服务暴露端口
     */
    private Integer serverPort = 9999;
    /**
     * 服务协议
     */
    private String protocol = "java";
    /**
     * 负载均衡算法
     */
    private String loadBalance = "random";
    /**
     * 权重，默认为1
     */
    private Integer weight = 1;

    @PostConstruct
    public void check() {
        if (RegisterCenterTypeEnum.getByCode(this.registerCenterType) == null) {
            throw new RpcException("注册中心类型配置错误！");
        }
    }

    public String getRegisterCenterType() {
        return registerCenterType;
    }

    public void setRegisterCenterType(String registerCenterType) {
        this.registerCenterType = registerCenterType;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }

    public String getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
