package com.zjz.netty.server;

import com.zjz.RpcServer;
import com.zjz.codec.CommonDecoder;
import com.zjz.codec.CommonEncoder;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import com.zjz.hook.ShutdownHook;
import com.zjz.provider.ServiceProviderImpl;
import com.zjz.registry.NacosServiceRegistry;
import com.zjz.provider.ServiceProvider;
import com.zjz.registry.ServiceRegistry;
import com.zjz.serializer.CommonSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Netty实现的RPC服务器类。
 */
@Slf4j
public class NettyServer implements RpcServer {


    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;

    private final String host;
    private final int port;
    private final CommonSerializer serializer;
    /**
     * 构造函数，指定服务器监听的主机和端口。
     *
     * @param host 服务器监听的主机。
     * @param port 服务器监听的端口。
     */
    public NettyServer(String host, int port) {
       this(host, port, DEFAULT_SERIALIZER);
    }
    public NettyServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        this.serializer = CommonSerializer.getByCode(serializer);
    }
    /**
     * 启动服务器
     * 该方法不接受参数，也不返回任何值。
     * 它通过使用NIO（非阻塞I/O）来创建和管理服务器，监听指定的主机和端口，
     * 并处理传入的连接和IO操作。
     */
    @Override
    public void start() {
        ShutdownHook.getShutdownHook().addClearAllHook();
        // 创建NIO事件循环组，用于处理连接接受和IO操作
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            // 配置服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class) // 指定使用的NIO通道类
                    .handler(new LoggingHandler(LogLevel.INFO)) // 添加日志处理器，记录服务器操作日志
                    .option(ChannelOption.SO_BACKLOG,256) // 设置连接队列大小，控制同时等待连接的最大数量
                    .option(ChannelOption.SO_KEEPALIVE,true) // 启用TCP KeepAlive，检查连接是否有效
                    .childOption(ChannelOption.TCP_NODELAY, true) // 启用TCP NoDelay，减少数据包延迟
                    // 配置新建立连接的处理器，初始化每个连接的ChannelPipeline
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // 添加编解码器和自定义处理器到通道管道，处理数据的编码、解码和业务逻辑
                            pipeline.addLast(new CommonEncoder(serializer));
                            pipeline.addLast(new CommonDecoder());
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            // 绑定端口并启动服务器，同步等待端口绑定成功
            ChannelFuture future = serverBootstrap.bind(host, port).sync();

            // 等待服务器关闭，确保所有连接都关闭
            future.channel().closeFuture().sync();
        }catch (InterruptedException e){
          // 记录服务启动异常，如果线程被中断
          log.error("服务启动失败",e);
        }finally {
            // 关闭事件循环组，释放资源，确保在服务停止时正确清理
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    /**
     * 发布服务到服务注册中心。
     * @param service 要发布的服务实例。
     * @param serviceClass 服务的类对象，用于指定服务的类型。
     * @param <T> 服务的类型。
     * @throws RpcException 如果序列化器未设置，则抛出异常。
     */
    @Override
    public <T> void publishService(T service, Class<T> serviceClass) {
        // 检查序列化器是否已设置，未设置则抛出异常
        if(serializer == null) {
            log.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        // 将服务添加到服务提供者列表
        serviceProvider.addServiceProvider(service, serviceClass);
        // 向服务注册中心注册服务
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        // 启动服务
        start();
    }

}
