package com.zjz.netty.client;

import com.zjz.codec.CommonDecoder;
import com.zjz.codec.CommonEncoder;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import com.zjz.serializer.CommonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
@Slf4j
public class ChannelProvider {

    private static EventLoopGroup eventLoopGroup;
    private static Bootstrap bootstrap = initializeBootstrap();

    private static final int MAX_RETRY_COUNT = 5;
    private static Channel channel = null;

    /**
     * 获取与服务端建立的Channel。
     *
     * @param inetSocketAddress 服务端的网络地址。
     * @param serializer 序列化器，用于数据的序列化和反序列化。
     * @return 返回与服务端建立的Channel实例。
     */
    public static Channel get(InetSocketAddress inetSocketAddress, CommonSerializer serializer) {
        // 初始化Netty客户端的ChannelPipeline
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                // 添加自定义的编解码器和客户端处理器
                ch.pipeline().addLast(new CommonEncoder(serializer))
                        .addLast(new CommonDecoder())
                        .addLast(new NettyClientHandler());
            }
        });

        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            // 尝试连接服务端
            connect(bootstrap, inetSocketAddress, countDownLatch);
            countDownLatch.await(); // 等待连接完成
        } catch (InterruptedException e) {
            log.error("获取channel时有错误发生:", e);
        }
        return channel;
    }

    /**
     * 尝试连接服务端。
     *
     * @param bootstrap Netty的启动对象。
     * @param inetSocketAddress 服务端的网络地址。
     * @param countDownLatch 用于计数和同步的 latch。
     */
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, CountDownLatch countDownLatch) {
        connect(bootstrap, inetSocketAddress, MAX_RETRY_COUNT, countDownLatch); // 默认最大重试次数尝试连接
    }

    /**
     * 带有重试机制的连接方法。
     *
     * @param bootstrap Netty的启动对象。
     * @param inetSocketAddress 服务端的网络地址。
     * @param retry 当前重试次数。
     * @param countDownLatch 用于计数和同步的 latch。
     */
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, int retry, CountDownLatch countDownLatch) {
        // 尝试连接并处理连接结果
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端连接成功!");
                channel = future.channel();
                countDownLatch.countDown(); // 连接成功，计数器减一
                return;
            }
            if (retry == 0) {
                log.error("客户端连接失败:重试次数已用完，放弃连接！");
                countDownLatch.countDown(); // 重试次数用完，计数器减一
                throw new RpcException(RpcError.CLIENT_CONNECT_SERVER_FAILURE);
            }
            // 计算下一次重连的延迟时间
            int order = (MAX_RETRY_COUNT - retry) + 1;
            int delay = 1 << order;
            log.error("{}: 连接失败，第 {} 次重连……", new Date(), order);
            // 延迟后再次尝试连接
            bootstrap.config().group().schedule(() -> connect(bootstrap, inetSocketAddress, retry - 1, countDownLatch), delay, TimeUnit
                    .SECONDS);
        });
    }

    /**
     * 初始化Netty的启动对象。
     *
     * @return 返回配置好的Bootstrap实例。
     */
    private static Bootstrap initializeBootstrap() {
        eventLoopGroup = new NioEventLoopGroup(); // 创建事件循环组
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup) // 设置事件循环组
                .channel(NioSocketChannel.class) // 设置使用的通道类
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 设置连接超时时间
                .option(ChannelOption.SO_KEEPALIVE, true) // 开启TCP心跳机制
                .option(ChannelOption.TCP_NODELAY, true); // 禁用Nagle算法
        return bootstrap;
    }

}
