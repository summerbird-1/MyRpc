package com.zjz;

import com.zjz.netty.server.NettyServer;
import com.zjz.serializer.CommonSerializer;
import com.zjz.serializer.HessianSerializer;

public class NettyServerTest2 {
    public static void main(String[] args) {
        HelloServiceImpl1 helloService = new HelloServiceImpl1();
        NettyServer nettyServer = new NettyServer("127.0.0.1", 8081, CommonSerializer.KRYO_SERIALIZER);
        nettyServer.publishService(helloService, HelloService.class);
    }
}