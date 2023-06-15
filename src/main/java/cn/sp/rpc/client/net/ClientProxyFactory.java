package cn.sp.rpc.client.net;

import cn.sp.rpc.client.balance.LoadBalance;
import cn.sp.rpc.client.cache.ServerDiscoveryCache;
import cn.sp.rpc.client.discovery.ServerDiscovery;
import cn.sp.rpc.client.manager.MessageProtocolsManager;
import cn.sp.rpc.client.manager.ServerDiscoveryManager;
import cn.sp.rpc.common.model.Service;
import cn.sp.rpc.common.protocol.MessageProtocol;
import cn.sp.rpc.common.model.RpcRequest;
import cn.sp.rpc.common.model.RpcResponse;
import cn.sp.rpc.exception.RpcException;
import cn.sp.rpc.util.ReflectUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 客户端代理工厂：用于创建远程服务代理类
 * 封装编组请求、请求发送、编组响应等操作
 *
 * @author 2YSP
 * @date 2020/7/25 20:55
 */
public class ClientProxyFactory {

    private ServerDiscoveryManager serverDiscoveryManager;

    private NetClient netClient;

    private Map<Class<?>, Object> objectCache = new HashMap<>();

    private LoadBalance loadBalance;

    /**
     * 通过Java动态代理获取服务代理类
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz) {
        return (T) objectCache.computeIfAbsent(clazz, clz ->
                Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, new ClientInvocationHandler(clz))
        );
    }


    private class ClientInvocationHandler implements InvocationHandler {

        private Class<?> clazz;

        public ClientInvocationHandler(Class<?> clazz) {
            this.clazz = clazz;
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("toString")) {
                return proxy.toString();
            }

            if (method.getName().equals("hashCode")) {
                return 0;
            }
            // 1.获得服务信息
            String serviceName = clazz.getName();
            List<Service> services = serverDiscoveryManager.getServiceList(serviceName);
            Service service = loadBalance.chooseOne(services);
            // 2.构造request对象
            RpcRequest request = new RpcRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setServiceName(service.getName());
            request.setMethod(method.getName());
            request.setParameters(args);
//            request.setParameterTypes(method.getParameterTypes());
            request.setParameterTypeNames(ReflectUtils.getParameterTypeNames(method));
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


    public LoadBalance getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    public NetClient getNetClient() {
        return netClient;
    }

    public void setNetClient(NetClient netClient) {
        this.netClient = netClient;
    }

    public Map<Class<?>, Object> getObjectCache() {
        return objectCache;
    }

    public void setObjectCache(Map<Class<?>, Object> objectCache) {
        this.objectCache = objectCache;
    }

    public ServerDiscoveryManager getServerDiscoveryManager() {
        return serverDiscoveryManager;
    }

    public void setServerDiscoveryManager(ServerDiscoveryManager serverDiscoveryManager) {
        this.serverDiscoveryManager = serverDiscoveryManager;
    }
}
