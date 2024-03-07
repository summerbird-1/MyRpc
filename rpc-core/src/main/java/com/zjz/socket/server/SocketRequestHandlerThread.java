package com.zjz.socket.server;

import com.zjz.RequestHandler;
import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import com.zjz.provider.ServiceProvider;
import com.zjz.registry.ServiceRegistry;
import com.zjz.serializer.CommonSerializer;
import com.zjz.utils.ObjectReader;
import com.zjz.utils.ObjectWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

@Slf4j
public class SocketRequestHandlerThread implements Runnable {

    private Socket socket; // 客户端的Socket连接
    private RequestHandler requestHandler; // 处理RPC请求的处理器

    private CommonSerializer serializer; // 序列化工具，用于序列化和反序列化数据

    /**
     * SocketRequestHandlerThread构造函数。
     *
     * @param socket 与客户端建立的Socket连接。
     * @param requestHandler 用于处理RPC请求的请求处理器。
     * @param serializer 通用序列化工具，支持将对象序列化为字节流和从字节流反序列化对象。
     */
    public SocketRequestHandlerThread(Socket socket, RequestHandler requestHandler, CommonSerializer serializer) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serializer = serializer;
    }

    /**
     * 重写的run方法是线程的执行体。
     * 它主要负责接收客户端的RPC请求，解析请求，调用相应的服务方法，并将结果返回给客户端。
     */
    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            // 从输入流读取RPC请求并反序列化
            RpcRequest rpcRequest = (RpcRequest) ObjectReader.readObject(inputStream);
            // 处理RPC请求，获取结果
            Object result = requestHandler.handle(rpcRequest);
            // 将调用结果封装成RPC响应，写入输出流
            RpcResponse<Object> response = RpcResponse.success(result, rpcRequest.getRequestId());
            ObjectWriter.writeObject(outputStream, response, serializer);
        } catch (IOException e) {
            // 记录读写异常日志
            log.error("调用或发送时有错误发生", e);
        }
    }
}
