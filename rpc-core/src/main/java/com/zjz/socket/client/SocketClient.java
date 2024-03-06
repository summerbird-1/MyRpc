package com.zjz.socket.client;

import com.zjz.RpcClient;
import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import com.zjz.enums.ResponseCode;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import com.zjz.serializer.CommonSerializer;
import com.zjz.utils.ObjectReader;
import com.zjz.utils.ObjectWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

/**
 * SocketClient 类实现了 RpcClient 接口，通过RPC（Remote Procedure Call）发送请求。
 * 使用@Slf4j注解以便记录日志。
 */
@Slf4j
public class SocketClient implements RpcClient {

    private final String host; // 服务器主机地址
    private final int port; // 服务器端口号

    private  CommonSerializer serializer; // 序列化器

    /**
     * SocketClient 构造函数。
     * @param host 服务器的主机地址。
     * @param port 服务器的端口号。
     */
    public SocketClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    /**
     * 发送RPC请求。
     * @param rpcRequest RPC请求对象。
     * @return 返回RPC响应的数据部分。
     * @throws RpcException 如果序列化器未设置、服务调用失败或响应状态码非成功时抛出。
     */
    public Object sendRequest(RpcRequest rpcRequest){
        // 检查序列化器是否已设置
        if(serializer == null) {
            log.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        try (Socket socket = new Socket(host, port)) { // 创建socket连接
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            // 序列化请求并发送
            ObjectWriter.writeObject(outputStream, rpcRequest, serializer);

            // 接收并反序列化响应
            Object obj = ObjectReader.readObject(inputStream);
            RpcResponse rpcResponse = (RpcResponse) obj;

            // 检查响应是否为空或状态码是否为成功
            if(rpcResponse == null) {
                log.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            if(rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                log.error("调用服务失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            // 返回响应数据
            return rpcResponse.getData();
        } catch (IOException e) {
             log.error("调用时有错误发生", e);
             throw new RpcException("服务调用失败",e);
        }

    }

    /**
     * 设置序列化器。
     * @param serializer 序列化器对象。
     */
    @Override
    public void setSerializer(CommonSerializer serializer) {
       this.serializer = serializer;
    }
}
