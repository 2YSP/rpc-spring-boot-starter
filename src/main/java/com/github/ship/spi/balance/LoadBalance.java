package com.github.ship.spi.balance;

import com.github.ship.common.model.Service;
import com.github.ship.spi.SPI;

import java.util.List;

/**
 * 负载均衡算法接口
 */
@SPI("random")
public interface LoadBalance {
    /**
     *
     * @param services
     * @return
     */
    Service chooseOne(List<Service> services);
}
