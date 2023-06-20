package com.github.ship.common.model;

import com.github.ship.common.constants.RpcStatusEnum;
import com.github.ship.common.exception.RpcException;

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

    private RpcException exception;

    private Integer rpcStatus;

    public RpcResponse() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public RpcResponse(RpcStatusEnum rpcStatus) {
        this.rpcStatus = rpcStatus.getCode();
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

    public RpcException getException() {
        return exception;
    }

    public void setException(RpcException exception) {
        this.exception = exception;
    }

    public Integer getRpcStatus() {
        return rpcStatus;
    }

    public void setRpcStatus(Integer rpcStatus) {
        this.rpcStatus = rpcStatus;
    }
}
