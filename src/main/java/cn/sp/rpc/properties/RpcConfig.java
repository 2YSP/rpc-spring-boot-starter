package cn.sp.rpc.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
