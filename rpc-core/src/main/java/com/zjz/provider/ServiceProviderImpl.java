package com.zjz.provider;

import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认服务注册表，实现服务注册功能。
 */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider {
    // 用于存储注册的服务，线程安全的ConcurrentHashMap
    private static final Map<String,Object> serviceMap = new ConcurrentHashMap<>();
    // 存储已注册服务的名称，线程安全的ConcurrentHashMap的新键集
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * 注册服务到注册表。
     * 该方法用于将一个服务实例注册到服务注册表中。如果该服务已经注册，则不会重复注册。
     * @param service 要注册的服务实例。该参数是服务的实例对象，它必须是泛型T的实例。
     * @param <T> 服务的类型。指定服务的类型，使得方法可以支持不同类型的服務注册。
     */
    @Override
    public  <T> void addServiceProvider(T service,Class<T> serviceClass) {
        String serviceName = serviceClass.getCanonicalName(); // 获取服务的完整类名

        // 检查服务是否已经注册，若已注册，则直接返回
        if(registeredService.contains(serviceName)) return;

        // 将服务名称添加到已注册服务集合中，并将服务实例添加到服务映射表中
        registeredService.add(serviceName);
        serviceMap.put(serviceName, service);

        // 记录服务注册日志
        log.info("向接口：{},注册服务:{}",service.getClass().getInterfaces(),serviceName);
    }


    /**
     * 通过服务名称获取服务实例。
     * @param serviceName 服务的名称。
     * @return 服务的实例。
     * @throws RpcException 如果服务未找到，则抛出异常。
     */
    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName); // 通过名称获取服务实例
        if(service == null){
            // 若服务实例为null，抛出服务未找到异常
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return service; // 返回服务实例
    }
}
