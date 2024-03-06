package com.zjz.util;

import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import com.zjz.enums.ResponseCode;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC消息检查器，用于验证RPC请求和响应的正确性和一致性。
 */
@Slf4j
public class RpcMessageChecker {

    /**
     * RPC接口名称常量
     */
    public static final String INTERFACE_NAME = "interfaceName";


    /**
     * 私有构造函数，防止实例化。
     */
    private RpcMessageChecker() {
    }

    /**
     * 检查RPC请求和响应的一致性和正确性。
     *
     * @param rpcRequest RPC请求对象
     * @param rpcResponse RPC响应对象
     * @throws RpcException 如果响应为null、请求ID不匹配或响应状态码表示失败，则抛出RPC异常。
     */
    public static void check(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        // 检查响应对象是否为null
        if (rpcResponse == null) {
            log.error("调用服务失败,serviceName:{}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        // 检查请求ID是否匹配
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcError.RESPONSE_NOT_MATCH, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        // 检查响应状态码是否表示成功
        if (rpcResponse.getStatusCode() == null || !rpcResponse.getStatusCode().equals(ResponseCode.SUCCESS.getCode())) {
            log.error("调用服务失败,serviceName:{},RpcResponse:{}", rpcRequest.getInterfaceName(), rpcResponse);
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }

}
