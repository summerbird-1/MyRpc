package com.zjz.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 消费者向提供者发送的请求对象
 * @author zjz
 */
@Data
@AllArgsConstructor
@Builder
public class RpcRequest implements Serializable {

    /**
     * 请求号
     */
    private String requestId;

    /**
     * 待调用接口名称
     */
    private String interfaceName;

    /**
     * 待调用方法名称
     */
    private String methodName;

    /**
     * 调用方法的参数
     */
    private Object[] parameters;

    /**
     * 调用方法的参数类型
     */
    private Class<?>[] paramTypes;
    public RpcRequest() {}
}