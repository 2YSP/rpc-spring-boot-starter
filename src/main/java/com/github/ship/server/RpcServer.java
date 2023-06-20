package com.github.ship.server;

/**
 * Rpc服务端抽象类
 * @author 2YSP
 * @date 2020/7/26 14:04
 */
public abstract class RpcServer {

    /**
     * 服务端口
     */
    protected int port;
    /**
     * 服务协议
     */
    protected String protocol;
    /**
     * 请求处理者
     */
    protected RequestHandler requestHandler;

    public RpcServer(int port, String protocol, RequestHandler requestHandler) {
        this.port = port;
        this.protocol = protocol;
        this.requestHandler = requestHandler;
    }

    /**
     * 开启服务
     */
    public abstract void start();

    /**
     * 关闭服务
     */
    public abstract void stop();

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public RequestHandler getRequestHandler() {
        return requestHandler;
    }

    public void setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }
}
