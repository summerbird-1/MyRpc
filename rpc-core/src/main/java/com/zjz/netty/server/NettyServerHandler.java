package com.zjz.netty.server;

import com.zjz.RequestHandler;
import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import com.zjz.factory.SingletonFactory;
import com.zjz.factory.ThreadPoolFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private  RequestHandler requestHandler;
    private static final String THREAD_NAME_PREFIX = "netty-server-handler";
    private  final ExecutorService threadPool;
    public NettyServerHandler() {
        this.requestHandler = SingletonFactory.getInstance(RequestHandler.class);
        this.threadPool = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);
    }


    /**
     * 当通道读取到数据时的处理逻辑。具体步骤包括：
     * 1. 使用线程池执行请求处理，避免阻塞IO线程。
     * 2. 日志记录请求信息。
     * 3. 调用请求处理器处理请求，并构造响应。
     * 4. 写出响应并关闭连接。
     * 5. 无论处理成功与否，最后释放消息体资源。
     *
     * @param ctx 通道上下文，用于进行通道读写操作。
     * @param msg 接收到的RPC请求消息。
     * @throws Exception 抛出异常时，可能会关闭连接。
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        // 使用线程池异步处理请求，避免阻塞当前IO线程
        threadPool.execute(() -> {
            try {
                // 记录接收到的请求日志
                log.info("服务器接收到请求: {}", msg);

                // 处理请求，并获取处理结果
                Object result = requestHandler.handle(msg);

                // 构造响应并写出到通道，成功时关闭连接
                ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result, msg.getRequestId()));

                // 为写出操作添加监听器，确保在写操作失败时关闭通道
                future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } finally {
                // 释放RPC请求消息资源，避免内存泄漏
                ReferenceCountUtil.release(msg);
            }
        });
    }



    /**
     * 当在处理ChannelHandlerContext时捕获到异常，此方法会被调用。
     * 主要用于记录错误日志，并关闭对应的连接。
     *
     * @param ctx ChannelHandlerContext，通道的上下文，用于进行I/O操作和获取通道的状态。
     * @param cause Throwable，抛出的异常对象，包含了错误的详细信息。
     * @throws Exception 如果处理过程中发生异常，则可能抛出Exception。
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 记录错误日志
        log.error("处理过程调用时有错误发生:");
        // 打印异常栈信息，便于调试
        cause.printStackTrace();
        // 关闭通道，终止连接
        ctx.close();
    }

}
