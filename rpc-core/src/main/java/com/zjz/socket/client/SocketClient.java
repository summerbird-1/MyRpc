package com.zjz.socket.client;

import com.zjz.RpcClient;
import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import com.zjz.enums.ResponseCode;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import com.zjz.registry.NacosServiceRegistry;
import com.zjz.registry.ServiceRegistry;
import com.zjz.serializer.CommonSerializer;
import com.zjz.util.RpcMessageChecker;
import com.zjz.utils.ObjectReader;
import com.zjz.utils.ObjectWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * SocketClient 类实现了 RpcClient 接口，通过RPC（Remote Procedure Call）发送请求。
 * 使用@Slf4j注解以便记录日志。
 */
@Slf4j
public class SocketClient implements RpcClient {

    private final ServiceRegistry serviceRegistry;

    private  CommonSerializer serializer; // 序列化器

    /**
     * SocketClient 构造函数。初始化服务注册表。
     */
    public SocketClient(){
     this.serviceRegistry = new NacosServiceRegistry();
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
        InetSocketAddress inetSocketAddress = serviceRegistry.lookupService(rpcRequest.getInterfaceName());
        try (Socket socket = new Socket()) { // 创建socket连接
            socket.connect(inetSocketAddress);
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
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            // 返回响应数据
            return rpcResponse.getData();
        } catch (IOException e) {
             log.error("调用时有错误发生", e);
             throw new RpcException("服务调用失败",e);
        }

    }

    /**
     * 设置序列化器。
     * @param serializer 序列化器对象。用于设置客户端使用的序列化方式。
     */
    @Override
    public void setSerializer(CommonSerializer serializer) {
       this.serializer = serializer;
    }
}
