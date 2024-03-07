package com.zjz;

import com.zjz.serializer.CommonSerializer;

public interface RpcServer {
    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;
    void start();

    <T> void publishService(T service, Class<T> serviceClass);
}
