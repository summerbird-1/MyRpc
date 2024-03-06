package com.zjz.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjz.entity.RpcRequest;
import com.zjz.enums.SerializerCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * JsonSerializer类实现了CommonSerializer接口，
 * 提供了基于JSON的序列化和反序列化功能。
 */
@Slf4j
public class JsonSerializer implements CommonSerializer {

    // ObjectMapper实例用于处理JSON序列化和反序列化
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将对象序列化为JSON字节数组。
     *
     * @param obj 需要被序列化的对象。
     * @return 序列化后的JSON字节数组，如果发生错误则返回null。
     */
    @Override
    public byte[] serialize(Object obj) {
        try {
            log.info("JSON序列化中。。。");
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化时有错误发生: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将JSON字节数组反序列化为对象。
     *
     * @param bytes 需要被反序列化的JSON字节数组。
     * @param clazz 目标对象的类类型。
     * @return 反序列化后的对象，如果发生错误则返回null。
     */
    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try {
            Object obj = objectMapper.readValue(bytes, clazz);
            if(obj instanceof RpcRequest) {
                // 如果反序列化得到的是RpcRequest类型，则对其进行特殊处理
                obj = handleRequest(obj);
            }
            log.info("JSON反序列化中。。。");
            return obj;
        } catch (IOException e) {
            log.error("反序列化时有错误发生: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 处理RpcRequest类型的对象，确保其参数类型与实际值匹配。
     *
     * @param obj 需要处理的RpcRequest对象。
     * @return 处理后的RpcRequest对象。
     * @throws IOException 如果处理过程中发生序列化或反序列化错误。
     */
    private Object handleRequest(Object obj) throws IOException {
        RpcRequest rpcRequest = (RpcRequest) obj;
        for(int i = 0; i < rpcRequest.getParamTypes().length; i ++) {
            Class<?> clazz = rpcRequest.getParamTypes()[i];
            // 如果参数的实际类型与期望类型不匹配，则重新进行反序列化以保证类型正确
            if(!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
                rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, clazz);
            }
        }
        return rpcRequest;
    }

    /**
     * 获取序列化器的代码标识。
     *
     * @return 序列化器的代码标识。
     */
    @Override
    public int getCode() {
        return SerializerCode.valueOf("JSON").getCode();
    }

}
