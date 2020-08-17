package cn.sp.rpc.client.discovery;

import org.I0Itec.zkclient.IZkChildListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 子节点事件监听处理类
 */
public class ZkChildListenerImpl implements IZkChildListener {

    private static Logger logger = LoggerFactory.getLogger(ZkChildListenerImpl.class);

    @Override
    public void handleChildChange(String parentPath, List<String> childList) throws Exception {
        logger.info("Child change parentPath:[{}] -- childList:[{}]", parentPath, childList);
        // todo 同步ServerDiscoveryCache中的缓存
    }
}
