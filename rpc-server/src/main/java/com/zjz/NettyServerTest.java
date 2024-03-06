package com.zjz;

import com.zjz.netty.server.NettyServer;
import com.zjz.provider.ServiceProviderImpl;
import com.zjz.provider.ServiceProvider;
import com.zjz.serializer.HessianSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServerTest {
    public static void main(String[] args) {
        HelloServiceImpl helloService = new HelloServiceImpl();
        NettyServer nettyServer = new NettyServer("127.0.0.1", 8080);
        nettyServer.setSerializer(new HessianSerializer());
        nettyServer.publishService(helloService, HelloService.class);
    }
}
