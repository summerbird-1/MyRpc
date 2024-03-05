package com.zjz.client;

import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * RPC客户端代理类，用于动态生成RPC客户端代理对象。
 */
public class RpcClientProxy implements InvocationHandler {
    private String host; // 服务端主机地址
    private int port; // 服务端端口号

    /**
     * 构造函数，初始化RPC客户端代理。
     *
     * @param host 服务端主机地址。
     * @param port 服务端端口号。
     */
    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 获取代理对象。该方法会根据传入的接口类动态创建一个实现了该接口的代理对象。
     *
     * @param clazz 需要创建代理的对象的接口类。
     * @param <T> 代理对象的类型。
     * @return 返回一个动态生成的代理对象，该对象实现了传入的接口类。
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz},this);
    }

    /**
     * 调用代理对象的方法时，实际上会执行此方法。会根据方法名和参数等信息构造一个RPC请求，
     * 然后通过RPC客户端发送该请求到服务端，并处理服务端返回的响应。
     *
     * @param proxy 代理对象。
     * @param method 被调用的方法。
     * @param args 方法调用时传入的参数。
     * @return 返回方法的执行结果。
     * @throws Throwable 如果执行过程中有异常发生，则抛出。
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构造RPC请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        // 创建RPC客户端并发送请求，处理响应
        RpcClient rpcClient = new RpcClient();
        return ((RpcResponse<?>)rpcClient.sendRequest(rpcRequest, host, port)).getData();
    }
}
