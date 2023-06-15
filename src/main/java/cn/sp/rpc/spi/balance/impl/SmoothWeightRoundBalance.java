package cn.sp.rpc.spi.balance.impl;

import cn.sp.rpc.annotation.LoadBalanceAno;
import cn.sp.rpc.common.constants.RpcConstant;
import cn.sp.rpc.common.model.Service;
import cn.sp.rpc.spi.balance.LoadBalance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平滑加权轮询
 */
@LoadBalanceAno(RpcConstant.BALANCE_SMOOTH_WEIGHT_ROUND)
public class SmoothWeightRoundBalance implements LoadBalance {
    /**
     * key:服务value:当前权重
     */
    private static final Map<String, Integer> map = new HashMap<>();

    @Override
    public synchronized Service chooseOne(List<Service> services) {
        services.forEach(service ->
                map.computeIfAbsent(service.toString(), key -> service.getWeight())
        );
        Service maxWeightServer = null;
        int allWeight = services.stream().mapToInt(Service::getWeight).sum();
        for (Service service : services) {
            Integer currentWeight = map.get(service.toString());
            if (maxWeightServer == null || currentWeight > map.get(maxWeightServer.toString())) {
                maxWeightServer = service;
            }
        }

        assert maxWeightServer != null;

        map.put(maxWeightServer.toString(), map.get(maxWeightServer.toString()) - allWeight);

        for (Service service : services) {
            Integer currentWeight = map.get(service.toString());
            map.put(service.toString(), currentWeight + service.getWeight());
        }
        return maxWeightServer;
    }

    public static void main(String[] args) {
        List<Service> services = new ArrayList<>(3);
        Service service = new Service();
        service.setAddress("196.128.6.1");
        service.setWeight(1);
        services.add(service);

        Service service2 = new Service();
        service2.setAddress("196.128.6.2");
        service2.setWeight(3);
        services.add(service2);

        Service service3 = new Service();
        service3.setAddress("196.128.6.3");
        service3.setWeight(5);
        services.add(service3);

        LoadBalance loadBalance = new SmoothWeightRoundBalance();
        System.out.println("20次请求负载均衡结果为:");
        for(int i=1;i<=20;i++){
            System.out.println("第"+i+"次请求服务ip为："+loadBalance.chooseOne(services).getAddress());
        }
    }
}
