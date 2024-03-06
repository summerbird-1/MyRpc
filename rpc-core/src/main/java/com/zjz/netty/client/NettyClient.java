package com.zjz.netty.client;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
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
    private String host; // 服务器主机地址
    private int port; // 服务器端口

    private CommonSerializer serializer;
    private static final Bootstrap bootstrap; // Netty的启动引导类
    /**
     * NettyClient构造函数。
     *
     * @param host 服务器主机地址
     * @param port 服务器端口
     */
    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    // 静态初始化块，用于初始化Netty的连接设置
    static {
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);
    }
    /**
     * 发送RPC请求。
     *
     * @param rpcRequest RPC请求对象
     * @return 返回RPC响应的数据部分
     */
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if(serializer == null) {
            log.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        AtomicReference<Object> result = new AtomicReference<>(null);
        try{
            // 连接到服务器,修改方案，提供了客户端连接失败重试机制
            Channel channel = ChannelProvider.get(new InetSocketAddress(host, port), serializer);
            if(channel.isActive()) {
                // 写入并刷新请求到通道
                channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                    if(future1.isSuccess()){
                        log.info((String.format("客户端发送消息：%s",rpcRequest.toString())));
                    }else{
                        log.error("发送消息有错误发生：",future1.cause());
                    }
                });
                channel.closeFuture().sync();
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                RpcResponse rpcResponse = channel.attr(key).get();
                RpcMessageChecker.check(rpcRequest, rpcResponse);
                result.set(rpcResponse.getData());
            }else {
                System.exit(0);
            }
        }catch (InterruptedException e){
            log.error("发送消息时有错误发生：",e);
        }
        return result.get();
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
