package com.zjz.netty.server;

import com.zjz.RpcServer;
import com.zjz.codec.CommonDecoder;
import com.zjz.codec.CommonEncoder;
import com.zjz.serializer.JsonSerializer;
import com.zjz.serializer.KryoSerializer;
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

/**
 * Netty实现的RPC服务器类。
 */
@Slf4j
public class NettyServer implements RpcServer {

    /**
     * 启动服务器，监听指定端口。
     *
     * @param port 服务器监听的端口号。
     */
    @Override
    public void start(int port) {
        // 创建NIO事件循环组，用于处理连接接受和IO操作
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            // 配置服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class) // 指定使用的NIO通道类
                    .handler(new LoggingHandler(LogLevel.INFO)) // 添加日志处理器
                    .option(ChannelOption.SO_BACKLOG,256) // 设置连接队列大小
                    .option(ChannelOption.SO_KEEPALIVE,true) // 启用TCP KeepAlive
                    .childOption(ChannelOption.TCP_NODELAY, true) // 启用TCP NoDelay，减少延迟
                    // 配置新建立连接的处理器
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // 添加编解码器和自定义处理器到通道管道
//                            pipeline.addLast(new CommonEncoder(new JsonSerializer()));
                            pipeline.addLast(new CommonEncoder(new KryoSerializer()));
                            pipeline.addLast(new CommonDecoder());
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            // 绑定端口并启动服务器
            ChannelFuture future = serverBootstrap.bind(port).sync();
            // 等待服务器关闭
            future.channel().closeFuture().sync();
        }catch (InterruptedException e){
          // 记录服务启动异常
          log.error("服务启动失败",e);
        }finally {
            // 关闭事件循环组，释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
