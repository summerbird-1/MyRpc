package com.zjz.registry;

import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认服务注册表，实现服务注册功能。
 */
@Slf4j
public class DefaultServiceRegistry implements ServiceRegistry{
    // 用于存储注册的服务，线程安全的ConcurrentHashMap
    private static final Map<String,Object> serviceMap = new ConcurrentHashMap<>();
    // 存储已注册服务的名称，线程安全的ConcurrentHashMap的新键集
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * 注册服务到注册表。
     * @param service 要注册的服务实例。
     * @param <T> 服务的类型。
     */
    @Override
    public synchronized <T> void register(T service) {
        String serviceName = service.getClass().getCanonicalName(); // 获取服务的完整类名
        // 若服务已注册，则直接返回
        if(registeredService.contains(serviceName)) return;
        registeredService.add(serviceName); // 将服务名称添加到已注册服务集合中
        Class<?>[] interfaces = service.getClass().getInterfaces(); // 获取服务实现的接口数组
        // 若服务没有实现任何接口，抛出异常
        if(interfaces.length == 0)
            throw new RpcException(RpcError.SERVICE_CAN_NOT_BE_NULL);
        // 为服务的每个接口在serviceMap中添加映射
        for(Class<?> clazz : interfaces){
            serviceMap.put(clazz.getCanonicalName(),service);
        }
        log.info("向接口：{},注册服务:{}",interfaces,serviceName);
    }

    /**
     * 通过服务名称获取服务实例。
     * @param serviceName 服务的名称。
     * @return 服务的实例。
     * @throws RpcException 如果服务未找到，则抛出异常。
     */
    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName); // 通过名称获取服务实例
        if(service == null){
            // 若服务实例为null，抛出服务未找到异常
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return service; // 返回服务实例
    }
}
