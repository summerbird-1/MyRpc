package com.zjz.server;

import com.sun.corba.se.spi.orbutil.threadpool.WorkQueue;
import com.zjz.entity.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

@Slf4j
public class RpcServer {
    private final ExecutorService threadPool;
    public RpcServer(){
        int corePoolSize = 5;
        int maximumPoolSize = 50;
        int keepAliveTime = 60;
        BlockingQueue<Runnable> workerQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workerQueue, threadFactory);
    }
    public void register(Object service,int port){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            log.info("服务端正在启动...");
            Socket socket;
            while((socket = serverSocket.accept()) != null){
                log.info("客户端连接！ip为：" + socket.getInetAddress());
                threadPool.execute(new WorkerThread(socket,service));
            }
        }catch (IOException e){
            log.error("服务端启动失败！",e);
        }
    }
}
