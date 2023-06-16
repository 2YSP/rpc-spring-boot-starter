package cn.sp.rpc.client.core;

import cn.sp.rpc.client.manager.MessageProtocolsManager;
import cn.sp.rpc.client.manager.ServerDiscoveryManager;
import cn.sp.rpc.client.net.NetClient;
import cn.sp.rpc.common.model.RpcRequest;
import cn.sp.rpc.common.model.RpcResponse;
import cn.sp.rpc.common.model.Service;
import cn.sp.rpc.common.exception.RpcException;
import cn.sp.rpc.spi.balance.LoadBalance;
import cn.sp.rpc.spi.protocol.MessageProtocol;

import java.util.List;
import java.util.UUID;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class DefaultMethodInvoker implements MethodInvoker {

    private ServerDiscoveryManager serverDiscoveryManager;

    private NetClient netClient;

    private LoadBalance loadBalance;

    public DefaultMethodInvoker(ServerDiscoveryManager serverDiscoveryManager, NetClient netClient, LoadBalance loadBalance) {
        this.serverDiscoveryManager = serverDiscoveryManager;
        this.netClient = netClient;
        this.loadBalance = loadBalance;
    }

    @Override
    public Object $invoke(String interfaceClassName, String methodName, String[] parameterTypeNames, Object[] args) {
        // 1.获得服务信息
        String serviceName = interfaceClassName;
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
