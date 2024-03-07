package com.zjz.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import com.zjz.util.NacosUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Nacos服务注册类，实现了ServiceRegistry接口，用于服务的注册与查找。
 */
@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {


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
          NacosUtil.registerService(serviceName, inetSocketAddress);
        } catch (NacosException e) {
            log.error("注册服务时有错误发生:", e);
            // 如果注册服务失败，抛出RPC异常
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILED);
        }
    }

}
