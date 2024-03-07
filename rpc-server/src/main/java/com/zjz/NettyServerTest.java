package com.zjz;

import com.zjz.netty.server.NettyServer;
import com.zjz.serializer.CommonSerializer;
import com.zjz.serializer.HessianSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServerTest {
    public static void main(String[] args) {
        HelloServiceImpl1 helloService = new HelloServiceImpl1();
        NettyServer nettyServer = new NettyServer("127.0.0.1", 8080, CommonSerializer.HESSIAN_SERIALIZER);
        nettyServer.publishService(helloService, HelloService.class);
    }
}
