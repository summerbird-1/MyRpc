package com.zjz.socket.server;

import com.zjz.RequestHandler;
import com.zjz.RpcServer;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import com.zjz.provider.ServiceProvider;
import com.zjz.provider.ServiceProviderImpl;
import com.zjz.registry.NacosServiceRegistry;
import com.zjz.registry.ServiceRegistry;
import com.zjz.serializer.CommonSerializer;
import com.zjz.util.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * 基于Socket实现的RPC服务端类，继承自RpcServer接口。
 */
@Slf4j
public class SocketServer implements RpcServer {
    // 线程池，用于处理客户端请求
    private final ExecutorService threadPool;
    // 序列化器，用于序列化和反序列化数据
    private CommonSerializer serializer;
    // 请求处理器
    private RequestHandler requestHandler = new RequestHandler();
    // 服务端绑定的主机地址
    private final String host;
    // 服务端监听的端口号
    private final int port;
    // 服务注册中心，用于注册和发现服务
    private final ServiceRegistry serviceRegistry;
    // 服务提供者，用于管理提供的服务
    private final ServiceProvider serviceProvider;

    /**
     * 构造函数，初始化Socket服务端的基本配置。
     *
     * @param host 服务绑定的主机地址
     * @param port 服务监听的端口号
     */
    public SocketServer(String host, int port) {
        this.host = host;
        this.port = port;
        // 初始化线程池
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        // 初始化服务注册中心和服务提供者
        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
    }

    /**
     * 启动服务端，开始监听客户端请求。
     */
    public void start(){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            log.info("服务端正在启动...");
            Socket socket;
            // 循环监听客户端连接，并处理请求
            while((socket = serverSocket.accept()) != null){
                log.info("消费者连接：{}：{}" , socket.getInetAddress() , socket.getPort());
                // 使用线程池处理客户端请求
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serviceRegistry,serializer));
            }
        }catch (IOException e){
            log.error("服务端启动失败！",e);
        }
    }

    /**
     * 设置序列化器。
     *
     * @param serializer 序列化器实例
     */
    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 发布服务，将服务注册到服务注册中心，并启动服务端监听。
     *
     * @param service 待发布的服务实例
     * @param serviceClass 服务的接口类
     * @param <T> 服务接口类型
     */
    @Override
    public <T> void publishService(Object service, Class<T> serviceClass) {
        // 检查序列化器是否已设置
        if(serializer == null) {
            log.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        // 添加服务到服务提供者
        serviceProvider.addServiceProvider(service);
        // 注册服务到服务注册中心
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        // 启动服务端
        start();
    }
}
