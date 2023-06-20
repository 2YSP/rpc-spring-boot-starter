package com.github.ship.spi.balance.impl;

import com.github.ship.annotation.LoadBalanceAno;
import com.github.ship.common.constants.RpcConstant;
import com.github.ship.common.model.Service;
import com.github.ship.spi.balance.LoadBalance;

import java.util.List;

/**
 * 加权轮询
 */
@LoadBalanceAno(RpcConstant.BALANCE_WEIGHT_ROUND)
public class WeightRoundBalance implements LoadBalance {

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
