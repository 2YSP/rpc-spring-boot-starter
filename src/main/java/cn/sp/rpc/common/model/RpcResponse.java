package cn.sp.rpc.common.model;

import cn.sp.rpc.common.constants.RpcStatusEnum;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 2YSP
 * @date 2020/7/25 21:03
 */
public class RpcResponse implements Serializable {

    private String requestId;

    private Map<String, String> headers = new HashMap<>();

    private Object returnValue;

    private Exception exception;

    private RpcStatusEnum rpcStatus;

    public RpcResponse() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public RpcResponse(RpcStatusEnum rpcStatus) {
        this.rpcStatus = rpcStatus;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public RpcStatusEnum getRpcStatus() {
        return rpcStatus;
    }

    public void setRpcStatus(RpcStatusEnum rpcStatus) {
        this.rpcStatus = rpcStatus;
    }
}
