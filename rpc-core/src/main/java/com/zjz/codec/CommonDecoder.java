package com.zjz.codec;

import com.zjz.entity.RpcRequest;
import com.zjz.entity.RpcResponse;
import com.zjz.enums.PackageType;
import com.zjz.enums.RpcError;
import com.zjz.exception.RpcException;
import com.zjz.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
/**
 * 通用解码器，用于解析RPC框架中的网络数据包。
 * 继承自ReplayingDecoder，利用ReplayingDecoder的帧解析能力，实现对RPC数据包的解码。
 */
@Slf4j
public class CommonDecoder extends ReplayingDecoder {

    private static final int MAGIC_NUMBER = 0xCAFEBABE; // 协议约定的魔数，用于识别数据包是否为本框架处理的RPC包。

    /**
     * 解码方法，负责解析ByteBuf中的数据，并将其转换为Java对象添加到out列表中。
     *
     * @param ctx 通道上下文，用于获取通道相关信息和操作通道。
     * @param in 输入的ByteBuf，包含需要解码的数据。
     * @param out 存放解码结果的列表。
     * @throws Exception 解码过程中发生的任何异常。
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 读取并校验魔法数字
        int magic = in.readInt();
        if(magic != MAGIC_NUMBER) {
            log.error("不识别的协议包: {}", magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }

        // 读取并解析包类型代码
        int packageCode = in.readInt();
        Class<?> packageClass;
        // 根据包类型代码，确定具体的包类型
        if(packageCode == PackageType.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if(packageCode == PackageType.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            log.error("不识别的数据包: {}", packageCode);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }

        // 读取并解析序列化器代码
        int serializerCode = in.readInt();
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if(serializer == null) {
            log.error("不识别的反序列化器: {}", serializerCode);
            throw new RpcException(RpcError.UNKNOWN_SERIALIZER);
        }

        // 读取并解析数据体长度及数据体
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        // 使用反序列化器将字节数据转换为Java对象
        Object obj = serializer.deserialize(bytes, packageClass);
        // 将解码后的对象添加到结果列表中
        out.add(obj);
    }
}
