package cn.sp.rpc.client.balance;

import cn.sp.rpc.common.model.Service;

import java.util.List;

/**
 * 轮询算法
 */
public class FullRoundBalance implements LoadBalance {

    private int index;

    @Override
    public Service chooseOne(List<Service> services) {
        if (index == services.size()) {
            index = 0;
        }
        return services.get(index++);
    }
}
