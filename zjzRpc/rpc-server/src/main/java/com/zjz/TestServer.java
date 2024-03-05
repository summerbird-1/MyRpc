package com.zjz;

import com.zjz.server.RpcServer;

public class TestServer {
    public static void main(String[] args) {
        HelloServiceImpl helloService = new HelloServiceImpl();
        RpcServer rpcServer = new RpcServer();
        rpcServer.register(helloService, 8080);
    }
}
