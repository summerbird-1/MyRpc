package com.zjz.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
public class NacosUtil {


    private static final String SERVER_ADDR = "127.0.0.1:8848";
    private static final NamingService namingService;
    private static final Set<String> serviceNames = new HashSet<>();
    private static InetSocketAddress address;
    static {
        namingService = getNacosNamingService();
    }
    public static NamingService getNacosNamingService() {
        try {
            return NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            log.error("连接到Nacos时有错误发生: ", e);
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    /**
     * 注册服务到NacosNamingService。
     *
     * @param serviceName 要注册的服务名称。
     * @param address 服务的网络地址，包括主机名和端口号。
     * @throws NacosException 如果注册过程中发生错误，则抛出NacosException。
     */
    public static void registerService(String serviceName, InetSocketAddress address) throws NacosException {
        // 向Nacos注册服务实例
        namingService.registerInstance(serviceName, address.getHostName(), address.getPort());
        // 更新NacosUtil中的地址为最新注册的服务地址
        NacosUtil.address = address;
        // 将服务名称添加到服务名称列表中，用于后续管理
        serviceNames.add(serviceName);
    }

    /**
     * 获取指定服务的所有实例列表。
     *
     * @param serviceName 需要查询实例的服务名称。
     * @return 返回指定服务下的所有实例列表。
     * @throws NacosException 如果查询过程中发生任何异常，则抛出。
     */
    public static List<Instance> getAllInstance(String serviceName) throws NacosException {
        // 通过命名服务实例获取指定服务的所有实例列表
        return namingService.getAllInstances(serviceName);
    }

    /**
     * 清除注册中心的注册信息。
     * 该方法会遍历服务名称集合，并尝试为每个服务从注册中心注销。如果注销失败，将会记录错误日志。
     * 注意：此方法不接受任何参数，也不返回任何值。
     */
    public static void clearRegistry() {
        // 检查是否有服务需要注销且注册中心地址不为空
        if(!serviceNames.isEmpty() && address != null) {
            // 获取注册中心的主机名和端口号
            String host = address.getHostName();
            int port = address.getPort();
            Iterator<String> iterator = serviceNames.iterator();
            // 遍历所有服务名称进行注销操作
            while(iterator.hasNext()) {
                String serviceName = iterator.next();
                try {
                    // 尝试注销服务实例
                    namingService.deregisterInstance(serviceName, host, port);
                } catch (NacosException e) {
                    // 如果注销失败，记录错误日志
                    log.error("注销服务 {} 失败", serviceName, e);
                }
            }
        }
    }

}