package com.zjz.client;

import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import com.zjz.enums.ResponseCode;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
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
        try (Socket socket = new Socket(host, port)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();
            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();
            if(rpcResponse == null) {
                log.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            if(rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                log.error("调用服务失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            return rpcResponse.getData();
        } catch (IOException | ClassNotFoundException e) {
                 log.error("调用时有错误发生", e);
                 throw new RpcException("服务调用失败",e);
            }

    }
}
