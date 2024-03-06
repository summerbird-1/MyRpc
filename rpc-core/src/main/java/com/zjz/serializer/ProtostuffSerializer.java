package com.zjz.serializer;

import com.zjz.enums.SerializerCode;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static io.protostuff.runtime.RuntimeSchema.getSchema;

/**
 * 使用Protostuff库实现的通用序列化器类，继承自CommonSerializer接口。
 */
public class ProtostuffSerializer implements CommonSerializer{
    // 分配一个链式缓冲区，用于序列化和反序列化过程
    private LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    // 使用ConcurrentHashMap作为缓存，存储类与对应的Schema
    private Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    /**
     * 序列化方法，将对象转换为字节数组。
     *
     * @param obj 需要被序列化的对象。
     * @return obj的字节数组表示。
     */
    @Override
    public byte[] serialize(Object obj) {
        Class clazz = obj.getClass(); // 获取对象的类
        Schema schema = getSchema(clazz); // 获取对应的Schema
        byte[] data;
        try {
            // 使用ProtostuffIOUtil将对象序列化为字节数组
            data = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            // 清空缓冲区，以便下次使用
            buffer.clear();
        }
        return data;
    }

    /**
     * 反序列化方法，将字节数组转换为对象。
     *
     * @param bytes 需要被反序列化的字节数组。
     * @param clazz 目标对象的类。
     * @return 反序列化后的对象。
     */
    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        Schema schema = getSchema(clazz); // 获取对应的Schema
        Object obj = schema.newMessage(); // 创建目标类的实例
        // 使用ProtostuffIOUtil将字节数组反序列化为对象
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }

    /**
     * 获取序列化器的代码标识。
     *
     * @return 序列化器的代码标识。
     */
    @Override
    public int getCode() {
        return SerializerCode.valueOf("PROTOBUF").getCode();
    }

    /**
     * 根据类获取其Schema。如果缓存中不存在，则通过RuntimeSchema进行懒创建并缓存。
     *
     * @param clazz 需要获取Schema的类。
     * @return 对应类的Schema。
     */
    private Schema getSchema(Class clazz) {
        Schema schema = schemaCache.get(clazz); // 尝试从缓存获取Schema
        if (Objects.isNull(schema)) {
            // 通过RuntimeSchema.getSchema()懒加载并缓存Schema，确保线程安全
            schema = RuntimeSchema.getSchema(clazz);
            if (Objects.nonNull(schema)) {
                schemaCache.put(clazz, schema); // 将新获取的Schema加入缓存
            }
        }
        return schema;
    }
}
