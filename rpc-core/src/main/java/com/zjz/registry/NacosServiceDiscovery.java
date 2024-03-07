package com.zjz.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zjz.util.NacosUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 使用Nacos进行服务发现的实现类。
 */
@Slf4j
public class NacosServiceDiscovery implements ServiceDiscovery {




    /**
     * 查询指定服务的第一个实例的地址。
     *
     * @param serviceName 要查询的服务名称。
     * @return 返回该服务第一个实例的IP地址和端口，如果查询失败则返回null。
     */
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            // 从Nacos获取指定服务的所有实例
            List<Instance> instances = NacosUtil.getAllInstance(serviceName);
            // 随机选择一个实例（这里默认选择第一个）
            Instance instance = instances.get(0);
            // 返回实例的地址信息
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            // 记录查询服务时发生的异常
            log.error("获取服务时有错误发生:", e);
        }
        // 查询失败返回null
        return null;
    }

}
