package com.github.ship.client.core;

import com.github.ship.client.manager.MessageProtocolsManager;
import com.github.ship.client.manager.ServerDiscoveryManager;
import com.github.ship.client.net.NetClient;
import com.github.ship.common.constants.RpcStatusEnum;
import com.github.ship.common.model.RpcRequest;
import com.github.ship.common.model.RpcResponse;
import com.github.ship.common.model.Service;
import com.github.ship.common.exception.RpcException;
import com.github.ship.spi.balance.LoadBalance;
import com.github.ship.spi.protocol.MessageProtocol;

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
    public Object $invoke(String interfaceClassName, String methodName, String[] parameterTypeNames, Object[] args, Boolean generic) {
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
        request.setGeneric(generic);
        // 3.协议层编组
        MessageProtocol messageProtocol = MessageProtocolsManager.get(service.getProtocol());
        RpcResponse response = netClient.sendRequest(request, service, messageProtocol);
        if (response == null) {
            throw new RpcException("the response is null");
        }
        // 6.结果处理
        if (RpcStatusEnum.ERROR.getCode().equals(response.getRpcStatus())) {
            throw response.getException();
        }
        if (RpcStatusEnum.NOT_FOUND.getCode().equals(response.getRpcStatus())) {
            throw new RpcException(" service not found!");
        }
        return response.getReturnValue();
    }
}
