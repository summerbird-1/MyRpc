package com.zjz;

import com.zjz.netty.server.NettyServer;
import com.zjz.provider.ServiceProviderImpl;
import com.zjz.provider.ServiceProvider;
import com.zjz.serializer.HessianSerializer;
import com.zjz.serializer.ProtostuffSerializer;
import com.zjz.socket.server.SocketServer;

public class SocketServerTest {
    /**
     * 程序的主入口函数。
     * @param args 命令行参数，本程序未使用该参数。
     */
    public static void main(String[] args) {
        HelloServiceImpl helloService = new HelloServiceImpl();
        SocketServer socketServer = new SocketServer("127.0.0.1", 9090);
        socketServer.setSerializer(new ProtostuffSerializer());
        socketServer.publishService(helloService, HelloService.class);
    }

}
