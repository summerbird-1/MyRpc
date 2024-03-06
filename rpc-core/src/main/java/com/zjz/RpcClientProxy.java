package com.zjz;

import com.zjz.entity.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
/**
 * RPC客户端代理类，用于动态生成RPC客户端代理对象。
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final RpcClient rpcClient;
    public RpcClientProxy(RpcClient rpcClient){

        this.rpcClient = rpcClient;
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
        log.info("调用方法：{}#{}" ,method.getDeclaringClass().getName() , method.getName());
        // 构造RPC请求
        RpcRequest rpcRequest = new RpcRequest(method.getDeclaringClass().getName(),
                method.getName(),args,method.getParameterTypes());

        return rpcClient.sendRequest(rpcRequest);
    }
}
