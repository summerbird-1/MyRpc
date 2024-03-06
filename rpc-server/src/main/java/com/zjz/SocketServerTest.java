package com.zjz;

import com.zjz.registry.DefaultServiceRegistry;
import com.zjz.registry.ServiceRegistry;
import com.zjz.serializer.HessianSerializer;
import com.zjz.serializer.KryoSerializer;
import com.zjz.socket.server.SocketServer;

public class SocketServerTest {
    /**
     * 程序的主入口函数。
     * @param args 命令行参数，本程序未使用该参数。
     */
    public static void main(String[] args) {
        // 创建HelloService的实现实例
        HelloServiceImpl helloService = new HelloServiceImpl();

        // 初始化服务注册表
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();

        // 将HelloService实例注册到服务注册表
        serviceRegistry.register(helloService);

        // 创建RPC服务器，并使用服务注册表
        SocketServer rpcServer = new SocketServer(serviceRegistry);
        rpcServer.setSerializer(new HessianSerializer());
        // 启动RPC服务器，监听8080端口
        rpcServer.start(8080);
    }

}
