package cn.sp.rpc.client.manager;

import cn.sp.rpc.annotation.LoadBalanceAno;
import cn.sp.rpc.common.exception.RpcException;
import cn.sp.rpc.spi.balance.LoadBalance;
import org.springframework.util.Assert;

import java.util.*;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class LoadBalanceManager {

    private static final Map<String, LoadBalance> LOAD_BALANCE_MAP = new HashMap<>();

    static {
        ServiceLoader<LoadBalance> loader = ServiceLoader.load(LoadBalance.class);
        Iterator<LoadBalance> iterator = loader.iterator();
        while (iterator.hasNext()) {
            LoadBalance loadBalance = iterator.next();
            LoadBalanceAno ano = loadBalance.getClass().getAnnotation(LoadBalanceAno.class);
            Assert.notNull(ano, "load balance name can not be empty!");
            LOAD_BALANCE_MAP.put(ano.value(), loadBalance);
        }
    }

    /**
     * 使用spi匹配符合配置的负载均衡算法
     * @param name
     * @return
     */
    public static LoadBalance getLoadBalance(String name) {
        Optional<LoadBalance> optional = Optional.of(LOAD_BALANCE_MAP.get(name));
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new RpcException("invalid load balance config");
    }
}
