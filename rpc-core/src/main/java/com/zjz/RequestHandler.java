package com.zjz;

import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import com.zjz.enums.ResponseCode;
import com.zjz.provider.ServiceProvider;
import com.zjz.provider.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * WorkerThread类实现了Runnable接口，用于处理具体的RPC请求。
 * 当创建一个WorkerThread实例时，它会通过提供的Socket和service对象来处理来自客户端的RPC请求。
 *
 */
@Slf4j
public class RequestHandler{
    private static final ServiceProvider serviceProvider;
    static {
        serviceProvider = new ServiceProviderImpl();
    }
    /**
     * 调用指定的服务方法。
     * @param rpcRequest 包含远程过程调用所需信息的请求对象，如接口名、方法名、参数类型和参数值。
     * @return 返回远程过程调用的结果，如果调用失败，可能会返回一个包含错误信息的RpcResponse对象。
     * @throws IllegalAccessException 当尝试访问或修改字段，或者调用一个方法时，如果没有相应的访问权限。
     * @throws InvocationTargetException 当调用方法时，目标方法抛出异常。
     */
    public Object invokeMethod(RpcRequest rpcRequest,Object service) throws IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        Method method;
        try{
            // 尝试获取service类中与rpcRequest指定方法名和参数类型相匹配的方法
            method = service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParamTypes());
        }catch (NoSuchMethodException e){
            // 如果未找到方法，返回方法不存在的错误响应
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestId());
        }
        // 调用找到的方法，并传入rpcRequest中的参数，返回方法的执行结果
        return method.invoke(service, rpcRequest.getParameters());
    }

    /**
     * 处理RPC请求的函数。
     *
     * @param rpcRequest 包含RPC调用信息的对象，如服务接口名称、方法名称和参数等。
     * @return 返回RPC调用的结果，其类型依据实际调用的方法而定。
     */
    public Object handle(RpcRequest rpcRequest) {
        Object result = null; // 初始化结果对象为null
        Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName()); // 获取请求的服务对象
        try{
            result = invokeMethod(rpcRequest,service); // 调用请求的方法，并保存结果
            log.info("服务:{} 成功调用方法:{}",rpcRequest.getInterfaceName(),rpcRequest.getMethodName()); // 记录调用成功的日志
        }catch (IllegalAccessException | InvocationTargetException e){
            log.error("调用或发送时有错误发生",e); // 记录调用过程中的访问或发送错误
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e); // 处理类找不到异常，抛出运行时异常
        }

        return result; // 返回调用结果
    }
}
