package com.zjz;

import com.zjz.netty.server.NettyServer;
import com.zjz.registry.DefaultServiceRegistry;
import com.zjz.registry.ServiceRegistry;
import com.zjz.serializer.HessianSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServerTest {
    public static void main(String[] args) {
        HelloServiceImpl helloService = new HelloServiceImpl();
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        serviceRegistry.register(helloService);
        NettyServer nettyServer = new NettyServer();
        nettyServer.setSerializer(new HessianSerializer());
        nettyServer.start(8080);
    }
}
