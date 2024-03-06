package com.zjz.socket.server;

import com.zjz.RequestHandler;
import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import com.zjz.registry.ServiceRegistry;
import com.zjz.serializer.CommonSerializer;
import com.zjz.utils.ObjectReader;
import com.zjz.utils.ObjectWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

@Slf4j
public class RequestHandlerThread implements Runnable{

    private Socket socket;
    private RequestHandler requestHandler;
    private ServiceRegistry serviceRegistry;
    private CommonSerializer serializer;
    public RequestHandlerThread(Socket socket, RequestHandler requestHandler, ServiceRegistry serviceRegistry, CommonSerializer serializer){
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serviceRegistry = serviceRegistry;
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
            RpcRequest rpcRequest = (RpcRequest) ObjectReader.readObject(inputStream);
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object result =  requestHandler.handle(rpcRequest,service);
            // 将调用结果封装成RPC响应，写入输出流
            RpcResponse<Object> response = RpcResponse.success(result, rpcRequest.getRequestId());
            ObjectWriter.writeObject(outputStream, response, serializer);
        } catch (IOException e) {
            // 记录异常信息
            log.error("调用或发送时有错误发生",e);
        }
    }
}
