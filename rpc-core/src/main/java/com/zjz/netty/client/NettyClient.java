package com.zjz.netty.client;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import com.zjz.registry.NacosServiceDiscovery;
import com.zjz.registry.NacosServiceRegistry;
import com.zjz.registry.ServiceDiscovery;
import com.zjz.registry.ServiceRegistry;
import com.zjz.serializer.CommonSerializer;
import com.zjz.serializer.HessianSerializer;
import com.zjz.serializer.KryoSerializer;
import com.zjz.util.RpcMessageChecker;
import io.netty.bootstrap.Bootstrap;
import com.zjz.RpcClient;
import com.zjz.codec.CommonDecoder;
import com.zjz.codec.CommonEncoder;
import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import com.zjz.serializer.JsonSerializer;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Netty实现的RPC客户端。
 */
@Slf4j
public class NettyClient implements RpcClient {

    private static final Bootstrap bootstrap; // Netty的启动引导类
    private static final EventLoopGroup group;


    // 静态初始化块，用于初始化Netty的连接设置
    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);
    }

    private final ServiceDiscovery serviceDiscovery;
    private final CommonSerializer serializer;
    public NettyClient() {
        this(DEFAULT_SERIALIZER);
    }
    public NettyClient(Integer serializer) {
        this.serviceDiscovery = new NacosServiceDiscovery();
        this.serializer = CommonSerializer.getByCode(serializer);
    }
    /**
     * 发送RPC（远程过程调用）请求。
     * <p>
     * 本方法负责通过序列化器将RPC请求对象发送给服务端，并接收响应，处理异常情况。
     * 使用客户端连接池和重试机制以确保连接的稳定性和效率。
     * </p>
     *
     * @param rpcRequest RPC请求对象，包含调用的服务接口名和请求数据
     * @return 返回RPC响应的数据部分，若连接失败或异常发生则可能返回null
     */
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        // 检查序列化器是否已设置，未设置则抛出异常
        if(serializer == null) {
            log.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }

        // 用于存储RPC调用结果的原子引用
        AtomicReference<Object> result = new AtomicReference<>(null);

        try{
            // 通过服务发现获取服务地址，并尝试连接到服务端
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);

            // 如果连接不可用，则关闭连接并返回null
            if (!channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }

            // 将RPC请求写入并刷新到通道，同时监听操作结果
            channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                if(future1.isSuccess()){
                    log.info((String.format("客户端发送消息：%s",rpcRequest.toString())));
                }else{
                    log.error("发送消息有错误发生：",future1.cause());
                }
            });

            // 等待通道关闭，确保请求被发送
            channel.closeFuture().sync();

            // 从通道的属性中获取RPC响应，进行响应检查，并存储结果
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
            RpcResponse rpcResponse = channel.attr(key).get();
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            result.set(rpcResponse.getData());

        }catch (InterruptedException e){
            // 记录中断异常日志，并重新设置中断状态
            log.error("发送消息时有错误发生：",e);
            Thread.currentThread().interrupt();
        }

        // 返回RPC调用结果
        return result.get();
    }

}
