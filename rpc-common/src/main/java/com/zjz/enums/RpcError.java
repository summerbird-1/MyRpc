package com.zjz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RpcError {
    CLIENT_CONNECT_SERVER_FAILURE("客户端连接服务端失败"),
    SERVICE_INVOCATION_FAILURE("服务调用出现失败"),
    SERVICE_CAN_NOT_BE_NULL("注册的服务不得为空"),
    SERVICE_NOT_FOUND("未发现该服务"),
    UNKNOWN_PACKAGE_TYPE("未知的包类型"),
    UNKNOWN_PROTOCOL("未知的协议"),
    UNKNOWN_SERIALIZER("未知的序列化器"),
    SERIALIZER_NOT_FOUND("未发现该序列化器"),
    RESPONSE_NOT_MATCH("响应与请求号不匹配");
    private final String message;

}