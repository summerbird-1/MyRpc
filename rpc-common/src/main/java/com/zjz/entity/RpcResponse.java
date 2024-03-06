package com.zjz.entity;

import com.zjz.enums.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
/**
 * RpcResponse 类用于封装 RPC 调用的响应结果。
 * @param <T> 响应数据的类型。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {
    private Integer statusCode; // 响应状态码
    private String message; // 响应消息

    private T data; // 响应携带的数据

    /**
     * 创建一个表示成功的 RpcResponse 实例。
     * @param data 成功时返回的数据。
     * @param <T> 数据的类型。
     * @return 返回一个填充了成功状态码、消息和数据的 RpcResponse 实例。
     */
    public static <T> RpcResponse<T> success(T data) {
        RpcResponse<T> response = new RpcResponse<T>();
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setMessage(ResponseCode.SUCCESS.getMessage());
        response.setData(data);
        return response;
    }

    /**
     * 创建一个表示失败的 RpcResponse 实例。
     * @param code 失败时的状态码。
     * @param <T> 数据的类型。
     * @return 返回一个填充了失败状态码和消息的 RpcResponse 实例。
     */
    public static <T> RpcResponse<T> fail(ResponseCode code) {
        RpcResponse<T> response = new RpcResponse<T>();
        response.setStatusCode(code.getCode());
        response.setMessage(code.getMessage());
        return response;
    }

}

