package cn.sp.rpc.spi.balance.impl;

import cn.sp.rpc.annotation.LoadBalanceAno;
import cn.sp.rpc.common.constants.RpcConstant;
import cn.sp.rpc.common.model.Service;
import cn.sp.rpc.spi.balance.LoadBalance;

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

}
