package com.zjz.netty.client;
import com.zjz.serializer.HessianSerializer;
import com.zjz.serializer.KryoSerializer;
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

/**
 * Netty实现的RPC客户端。
 */
@Slf4j
public class NettyClient implements RpcClient {
    private String host; // 服务器主机地址
    private int port; // 服务器端口
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
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new CommonDecoder())
//                                .addLast(new CommonEncoder(new JsonSerializer()))
//                                .addLast(new CommonEncoder(new KryoSerializer()))
                                .addLast(new CommonEncoder(new HessianSerializer()))
                                .addLast(new NettyClientHandler());
                    }
                });
    }
    /**
     * 发送RPC请求。
     *
     * @param rpcRequest RPC请求对象
     * @return 返回RPC响应的数据部分
     */
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        try{
            // 连接到服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            log.info("客户端连接到服务器{}：{}",host,port);
            Channel channel = future.channel();
            if(channel != null){
                // 写入并刷新请求到通道
                channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                    if(future1.isSuccess()){
                        log.info((String.format("客户端发送消息：%s",rpcRequest.toString())));
                    }else{
                        log.error("发送消息有错误发生：",future1.cause());
                    }
                });
                channel.closeFuture().sync();
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                RpcResponse rpcResponse = channel.attr(key).get();
                return rpcResponse.getData();
            }
        }catch (InterruptedException e){
            log.error("发送消息时有错误发生：",e);
        }
        return null;
    }
}
