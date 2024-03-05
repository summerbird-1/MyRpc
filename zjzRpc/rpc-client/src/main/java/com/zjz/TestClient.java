package com.zjz;

import com.zjz.client.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestClient {
    public static void main(String[] args) {
        RpcClientProxy rpcClientProxy = new RpcClientProxy("127.0.0.1", 8080);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject hello = new HelloObject(1, "hello");
        String s = helloService.sayHello(hello);
        System.out.println(s);
    }
}
