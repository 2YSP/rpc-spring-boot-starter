package cn.sp.rpc.client.balance;

import cn.sp.rpc.common.model.Service;

import java.util.List;

/**
 * 加权轮询
 */
public class WeightRoundBalance implements LoadBalance{

    private volatile static int index;

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
