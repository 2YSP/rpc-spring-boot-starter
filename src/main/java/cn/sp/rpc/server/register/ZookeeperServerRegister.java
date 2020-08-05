package cn.sp.rpc.server.register;

import cn.sp.rpc.common.model.Service;
import cn.sp.rpc.common.serializer.ZookeeperSerializer;
import com.alibaba.fastjson.JSON;
import org.I0Itec.zkclient.ZkClient;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;

import static cn.sp.rpc.common.constants.RpcConstant.PATH_DELIMITER;
import static cn.sp.rpc.common.constants.RpcConstant.UTF_8;
import static cn.sp.rpc.common.constants.RpcConstant.ZK_SERVICE_PATH;

/**
 *
 * zk服务注册器，提供服务注册、暴露服务的能力
 * @author 2YSP
 * @date 2020/7/26 13:26
 */
public class ZookeeperServerRegister extends DefaultServerRegister {

    private ZkClient zkClient;

    public ZookeeperServerRegister(String zkAddress,Integer port,String protocol){
        zkClient = new ZkClient(zkAddress);
        zkClient.setZkSerializer(new ZookeeperSerializer());
        this.port = port;
        this.protocol = protocol;
    }

    /**
     * 服务注册
     * @param so
     * @throws Exception
     */
    @Override
    public void register(ServiceObject so) throws Exception {
        super.register(so);
        Service service = new Service();
        String host = InetAddress.getLocalHost().getHostAddress();
        String address = host +":"+ port;
        service.setAddress(address);
        service.setName(so.getClazz().getName());
        service.setProtocol(protocol);
        this.exportService(service);
    }

    /**
     * 服务暴露(其实就是把服务信息保存到Zookeeper上)
     * @param serviceResource
     */
    private void exportService(Service serviceResource){
        String serviceName = serviceResource.getName();
        String uri = JSON.toJSONString(serviceResource);
        try {
            uri = URLEncoder.encode(uri, UTF_8);
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        String servicePath = ZK_SERVICE_PATH + PATH_DELIMITER + serviceName +"/service";
        if (!zkClient.exists(servicePath)){
            // 没有该节点就创建
            zkClient.createPersistent(servicePath,true);
        }

        String uriPath = servicePath + PATH_DELIMITER + uri;
        if (zkClient.exists(uriPath)){
            // 删除之前的节点
            zkClient.delete(uriPath);
        }
        // 创建一个临时节点，会话失效即被清理
        zkClient.createEphemeral(uriPath);
    }
}
