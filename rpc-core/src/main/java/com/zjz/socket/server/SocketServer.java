package com.zjz.socket.server;

import com.zjz.RequestHandler;
import com.zjz.RpcServer;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import com.zjz.registry.ServiceRegistry;
import com.zjz.serializer.CommonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

@Slf4j
public class SocketServer implements RpcServer {
    private static  final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 50;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final int BOCKING_QUEUE_CAPACITY = 100;
    private final ExecutorService threadPool;
    private RequestHandler requestHandler = new RequestHandler();
    private final ServiceRegistry serviceRegistry;

    private CommonSerializer  serializer;
    public SocketServer(ServiceRegistry serviceRegistry){
        this.serviceRegistry = serviceRegistry;
        BlockingQueue<Runnable> workerQueue = new ArrayBlockingQueue<>(BOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workerQueue, threadFactory);
    }
    public void start(int port){
        if(serializer == null) {
            log.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        try(ServerSocket serverSocket = new ServerSocket(port)){
            log.info("服务端正在启动...");
            Socket socket;
            while((socket = serverSocket.accept()) != null){
                log.info("消费者连接：{}：{}" , socket.getInetAddress() , socket.getPort());
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serviceRegistry,serializer));
            }
        }catch (IOException e){
            log.error("服务端启动失败！",e);
        }
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
