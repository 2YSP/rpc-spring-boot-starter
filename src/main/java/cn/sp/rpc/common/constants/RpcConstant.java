package cn.sp.rpc.common.constants;

/**
 * @author 2YSP
 * @date 2020/7/25 20:00
 */
public class RpcConstant {

    private RpcConstant(){}

    /**
     * Zookeeper服务注册地址
     */
    public static final String ZK_SERVICE_PATH = "/rpc";
    /***
     * 编码
     */
    public static final String UTF_8 = "UTF-8";
    /**
     * 路径分隔符
     */
    public static final String PATH_DELIMITER = "/";
    /**
     * java序列化协议
     */
    public static final String PROTOCOL_JAVA = "java";
    /**
     * protobuf序列化协议
     */
    public static final String PROTOCOL_PROTOBUF = "protobuf";

}
