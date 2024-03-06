package com.zjz.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Nacos服务注册类，实现了ServiceRegistry接口，用于服务的注册与查找。
 */
@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {

    // Nacos服务器地址
    private static final String SERVER_ADDR = "127.0.0.1:8848";
    // Nacos命名服务实例
    private static final NamingService namingService;

    // 静态初始化块，用于创建Nacos命名服务实例
    static {
        try {
            namingService = NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            log.error("连接到Nacos时有错误发生: ", e);
            // 如果创建命名服务实例失败，抛出RPC异常
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    /**
     * 注册服务到Nacos。
     *
     * @param serviceName 服务名称
     * @param inetSocketAddress 服务的网络地址
     * @throws RpcException 如果注册服务时发生错误，则抛出RPC异常
     */
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            // 向Nacos注册服务实例
            namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        } catch (NacosException e) {
            log.error("注册服务时有错误发生:", e);
            // 如果注册服务失败，抛出RPC异常
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILED);
        }
    }

    /**
     * 从Nacos查找服务。
     *
     * @param serviceName 服务名称
     * @return 返回服务的网络地址，如果找不到服务则返回null
     */
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            // 从Nacos获取所有服务实例
            List<Instance> instances = namingService.getAllInstances(serviceName);
            Instance instance = instances.get(0);
            // 返回第一个服务实例的网络地址
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            log.error("获取服务时有错误发生:", e);
        }
        // 如果获取服务实例失败，返回null
        return null;
    }
}
