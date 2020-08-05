package cn.sp.rpc.server.register;

/**
 * 服务注册器，定义服务注册规范
 * @author 2YSP
 * @date 2020/7/26 13:15
 */
public interface ServerRegister {

    void register(ServiceObject so)throws Exception;

    ServiceObject getServiceObject(String name)throws Exception;
}
