package com.github.ship.spi.balance.impl;

import com.github.ship.annotation.LoadBalanceAno;
import com.github.ship.common.constants.RpcConstant;
import com.github.ship.common.model.Service;
import com.github.ship.spi.balance.LoadBalance;

import java.util.List;
import java.util.Random;

/**
 * 随机算法
 */
@LoadBalanceAno(RpcConstant.BALANCE_RANDOM)
public class RandomBalance implements LoadBalance {

    private static Random random = new Random();

    @Override
    public Service chooseOne(List<Service> services) {
        return services.get(random.nextInt(services.size()));
    }
}
