package com.zjz.server;

import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import com.zjz.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

@Slf4j
public class RequestHandlerThread implements Runnable{

    private Socket socket;
    private RequestHandler requestHandler;
    private ServiceRegistry serviceRegistry;
    public RequestHandlerThread(Socket socket, RequestHandler requestHandler, ServiceRegistry serviceRegistry){
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serviceRegistry = serviceRegistry;
    }
    /**
     * 重写的run方法是线程的执行体。
     * 它主要负责接收客户端的RPC请求，解析请求，调用相应的服务方法，并将结果返回给客户端。
     */
    @Override
    public void run() {
        try(ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())){
            // 从输入流读取RPC请求
            RpcRequest rpcRequest = (RpcRequest)objectInputStream.readObject();
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object result =  requestHandler.handle(rpcRequest,service);
            // 将调用结果封装成RPC响应，写入输出流
            objectOutputStream.writeObject(RpcResponse.success(result));
            objectOutputStream.flush();
        }catch (IOException | ClassNotFoundException e){
            // 记录异常信息
            log.error("调用或发送时有错误发生",e);
        }
    }
}
