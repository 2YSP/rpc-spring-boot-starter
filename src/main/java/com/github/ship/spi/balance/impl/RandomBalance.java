package com.github.ship.spi.balance.impl;

import com.github.ship.common.model.Service;
import com.github.ship.spi.balance.LoadBalance;

import java.util.List;
import java.util.Random;

/**
 * 随机算法
 */
public class RandomBalance implements LoadBalance {

    private static Random random = new Random();

    @Override
    public Service chooseOne(List<Service> services) {
        return services.get(random.nextInt(services.size()));
    }
}
