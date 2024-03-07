package com.zjz.provider;

/**
 * 服务注册接口，提供服务的注册与获取功能。
 */
public interface ServiceProvider {


    <T> void addServiceProvider(T service, Class<T> serviceClass);

    Object getServiceProvider(String serviceName);

}