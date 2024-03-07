package com.zjz;

import com.zjz.serializer.ProtostuffSerializer;
import com.zjz.socket.server.SocketServer;

public class SocketServerTest {
    /**
     * 程序的主入口函数。
     * @param args 命令行参数，本程序未使用该参数。
     */
    public static void main(String[] args) {
        HelloServiceImpl2 helloService = new HelloServiceImpl2();
        SocketServer socketServer = new SocketServer("127.0.0.1", 9091);
        socketServer.setSerializer(new ProtostuffSerializer());
        socketServer.publishService(helloService, HelloService.class);
    }

}
