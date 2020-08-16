package cn.sp.rpc.client.balance;

import cn.sp.rpc.common.model.Service;

import java.util.List;

/**
 * 平滑加权轮询
 */
public class SmoothWeightRoundBalance implements LoadBalance{

    @Override
    public Service chooseOne(List<Service> services) {
//        Service maxWeightServer = null;
//        int allWeight = services.stream().mapToInt(Service::getWeight).sum();
//        for(Service service : services){
//            if (maxWeightServer == null || service.getWeight() > )
//        }
        return null;
    }
}
