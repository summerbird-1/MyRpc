package com.zjz.server;

import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import com.zjz.enums.ResponseCode;
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
public class RequestHandler implements Runnable{
    private Socket socket;
    private Object service;

    /**
     * WorkerThread构造函数。
     * 初始化一个WorkerThread实例，设置其socket和service属性。
     *
     * @param socket 与客户端通信的Socket。
     * @param service 提供具体服务的对象。
     */
    public RequestHandler(Socket socket, Object service){
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
             Object returnObject = invokeMethod(rpcRequest);
            // 将调用结果封装成RPC响应，写入输出流
            objectOutputStream.writeObject(RpcResponse.success(returnObject));
            objectOutputStream.flush();
        }catch (IOException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e){
            // 记录异常信息
            log.error("调用或发送时有错误发生",e);
        }
    }
    /**
     * 调用指定的服务方法。
     * @param rpcRequest 包含远程过程调用所需信息的请求对象，如接口名、方法名、参数类型和参数值。
     * @return 返回远程过程调用的结果，如果调用失败，可能会返回一个包含错误信息的RpcResponse对象。
     * @throws IllegalAccessException 当尝试访问或修改字段，或者调用一个方法时，如果没有相应的访问权限。
     * @throws InvocationTargetException 当调用方法时，目标方法抛出异常。
     */
    public Object invokeMethod(RpcRequest rpcRequest) throws IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        // 根据rpcRequest中的接口名加载类
        Class<?> clazz = Class.forName(rpcRequest.getInterfaceName());
        // 检查加载的类是否为服务类的实现类
        if(!clazz.isAssignableFrom(service.getClass())){
            // 如果不是，返回类未找到的错误响应
            return RpcResponse.fail(ResponseCode.CLASS_NOT_FOUND);
        }
        Method method;
        try{
            // 尝试获取service类中与rpcRequest指定方法名和参数类型相匹配的方法
            method = service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParamTypes());
        }catch (NoSuchMethodException e){
            // 如果未找到方法，返回方法不存在的错误响应
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND);
        }
        // 调用找到的方法，并传入rpcRequest中的参数，返回方法的执行结果
        return method.invoke(service, rpcRequest.getParameters());
    }
}
