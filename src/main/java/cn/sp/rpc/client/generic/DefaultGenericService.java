package cn.sp.rpc.client.generic;

import cn.sp.rpc.client.balance.LoadBalance;
import cn.sp.rpc.client.cache.ServerDiscoveryCache;
import cn.sp.rpc.client.discovery.ServerDiscovery;
import cn.sp.rpc.client.manager.MessageProtocolsManager;
import cn.sp.rpc.client.manager.ServerDiscoveryManager;
import cn.sp.rpc.client.net.NetClient;
import cn.sp.rpc.common.model.RpcRequest;
import cn.sp.rpc.common.model.RpcResponse;
import cn.sp.rpc.common.model.Service;
import cn.sp.rpc.common.protocol.MessageProtocol;
import cn.sp.rpc.exception.RpcException;
import cn.sp.rpc.util.ReflectUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class DefaultGenericService implements GenericService {

    private ServerDiscoveryManager serverDiscoveryManager;

    private NetClient netClient;

    private LoadBalance loadBalance;

    private Class interfaceClazz;

    @Override
    public Object $invoke(String methodName, String[] parameterTypeNames, Object[] args) {
        // 1.获得服务信息
        String serviceName = interfaceClazz.getName();
        List<Service> services = serverDiscoveryManager.getServiceList(serviceName);
        Service service = loadBalance.chooseOne(services);
        // 2.构造request对象
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setServiceName(service.getName());
        request.setMethod(methodName);
        request.setParameters(args);
        request.setParameterTypeNames(parameterTypeNames);
        // 3.协议层编组
        MessageProtocol messageProtocol = MessageProtocolsManager.get(service.getProtocol());
        RpcResponse response = netClient.sendRequest(request, service, messageProtocol);
        if (response == null) {
            throw new RpcException("the response is null");
        }
        // 6.结果处理
        if (response.getException() != null) {
            return response.getException();
        }
        return response.getReturnValue();
    }


}
