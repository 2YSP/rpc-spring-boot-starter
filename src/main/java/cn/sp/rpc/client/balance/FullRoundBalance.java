package cn.sp.rpc.client.balance;

import cn.sp.rpc.annotation.LoadBalanceAno;
import cn.sp.rpc.common.constants.RpcConstant;
import cn.sp.rpc.common.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 轮询算法
 */
@LoadBalanceAno(RpcConstant.BALANCE_ROUND)
public class FullRoundBalance implements LoadBalance {

    private static Logger logger = LoggerFactory.getLogger(FullRoundBalance.class);

    private int index;

    @Override
    public synchronized Service chooseOne(List<Service> services) {
        // 加锁防止多线程情况下，index超出services.size()
        if (index == services.size()) {
            index = 0;
        }
        return services.get(index++);
    }
}
