package com.zjz.client;

import com.zjz.entity.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * RpcClient 类用于通过RPC（Remote Procedure Call）发送请求。
 * 使用@Slf4j注解记录日志。
 */
@Slf4j
public class RpcClient {

    /**
     * 向指定的RPC服务发送请求，并等待应答。
     *
     * @param rpcRequest RPC请求对象，包含调用的详细信息。
     * @param host 服务的主机地址。
     * @param port 服务监听的端口号。
     * @return 返回服务端处理结果，如果通信或处理失败则返回null。
     */
    public Object sendRequest(RpcRequest rpcRequest, String host, int port){
        try(Socket socket = new Socket(host, port)){ // 建立与服务端的连接
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream()); // 创建对象输出流
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream()); // 创建对象输入流
            objectOutputStream.writeObject(rpcRequest); // 将请求对象写入输出流
            objectOutputStream.flush(); // 刷新输出流，确保请求被发送
            return objectInputStream.readObject(); // 读取服务端返回的对象
        }catch (IOException | ClassNotFoundException e){
            log.error("调用失败", e); // 记录调用失败的日志
            return null;
        }
    }
}
