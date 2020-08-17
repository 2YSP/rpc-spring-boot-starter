package cn.sp.rpc.client.discovery;

import cn.sp.rpc.client.cache.ServerDiscoveryCache;
import org.I0Itec.zkclient.IZkChildListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 子节点事件监听处理类
 */
public class ZkChildListenerImpl implements IZkChildListener {

    private static Logger logger = LoggerFactory.getLogger(ZkChildListenerImpl.class);

    /**
     * 监听子节点的删除和新增事件
     * @param parentPath /rpc/serviceName/service
     * @param childList
     * @throws Exception
     */
    @Override
    public void handleChildChange(String parentPath, List<String> childList) throws Exception {
        logger.debug("Child change parentPath:[{}] -- childList:[{}]", parentPath, childList);
        // 只要子节点有改动就清空缓存
        String[] arr = parentPath.split("/");
        ServerDiscoveryCache.removeAll(arr[2]);
    }
}
