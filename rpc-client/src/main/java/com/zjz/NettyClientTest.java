package com.zjz;

import com.zjz.netty.client.NettyClient;
import com.zjz.serializer.HessianSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClientTest {
    /**
     * 程序的主入口函数。
     * @param args 命令行传入的参数数组，本程序未使用该参数。
     */
    public static void main(String[] args) {
        // 创建NettyClient实例，用于与服务器建立连接
        NettyClient nettyClient = new NettyClient();

        nettyClient.setSerializer(new HessianSerializer());
        // 通过NettyClient创建RpcClientProxy实例，提供RPC调用能力
        RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyClient);
        // 获取HelloService的代理对象，可通过该对象远程调用服务
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);

        HelloObject  helloObject = new HelloObject(1, "zjz");
        // 调用sayHello方法，并传入一个HelloObject实例作为参数
        String result = helloService.sayHello(helloObject);
        // 打印调用结果
        System.out.println(result);
    }

}
