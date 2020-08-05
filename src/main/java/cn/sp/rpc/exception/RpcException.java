package cn.sp.rpc.exception;

/**
 * @author 2YSP
 * @date 2020/7/25 21:34
 */
public class RpcException extends RuntimeException {

    public RpcException(String message) {
        super(message);
    }
}
