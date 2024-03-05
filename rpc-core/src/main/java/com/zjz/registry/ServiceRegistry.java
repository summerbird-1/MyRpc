package com.zjz.registry;

/**
 * 服务注册接口，提供服务的注册与获取功能。
 */
public interface ServiceRegistry {
    /**
     * 注册服务。
     *
     * @param service 要注册的服务实例，泛型T代表任意类型的服务。
     *                该方法允许注册任何类型的对象作为服务。
     */
    <T> void register(T service);

    /**
     * 获取服务。
     *
     * @param serviceName 要获取服务的名称，为String类型。
     *                    通过服务名称来获取注册的服务实例。
     * @return 返回对应服务名称的服务实例对象。
     *         如果找不到对应服务，则返回null。
     */
    Object getService(String serviceName);
}
