package com.zjz.server;

import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * WorkerThread类实现了Runnable接口，用于处理具体的RPC请求。
 * 当创建一个WorkerThread实例时，它会通过提供的Socket和service对象来处理来自客户端的RPC请求。
 *
 */
@Slf4j
public class WorkerThread implements Runnable{
    private Socket socket;
    private Object service;

    /**
     * WorkerThread构造函数。
     * 初始化一个WorkerThread实例，设置其socket和service属性。
     *
     * @param socket 与客户端通信的Socket。
     * @param service 提供具体服务的对象。
     */
    public WorkerThread(Socket socket, Object service){
        this.socket = socket;
        this.service = service;
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
            // 获取服务对象中对应的方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            // 调用方法，并获取返回结果
            Object returnObject = method.invoke(service, rpcRequest.getParameters());
            // 将调用结果封装成RPC响应，写入输出流
            objectOutputStream.writeObject(RpcResponse.success(returnObject));
            objectOutputStream.flush();
        }catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            // 记录异常信息
            log.error("调用或发送时有错误发生",e);
        }
    }
}
