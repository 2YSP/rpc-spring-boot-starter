package cn.sp.rpc.client.balance;

import cn.sp.rpc.annotation.LoadBalanceAno;
import cn.sp.rpc.common.constants.RpcConstant;
import cn.sp.rpc.common.model.Service;

import java.util.List;

/**
 * 加权轮询
 */
@LoadBalanceAno(RpcConstant.BALANCE_WEIGHT_ROUND)
public class WeightRoundBalance implements LoadBalance{

    private static int index;

    @Override
    public synchronized Service chooseOne(List<Service> services) {
        int allWeight = services.stream().mapToInt(Service::getWeight).sum();
        int number = (index++) % allWeight;
        for(Service service : services){
            if (service.getWeight() > number){
                return service;
            }
            number -= service.getWeight();
        }
        return null;
    }
}
