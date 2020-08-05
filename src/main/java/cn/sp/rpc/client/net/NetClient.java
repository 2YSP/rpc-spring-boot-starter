package cn.sp.rpc.client.net;

import cn.sp.rpc.common.model.Service;

/**
 *
 * 网络请求客户端，定义请求规范
 * @author 2YSP
 * @date 2020/7/25 20:11
 *
 */
public interface NetClient {

    byte[] sendRequest(byte[] data, Service service) throws InterruptedException;
}
