package com.zjz;

import com.zjz.serializer.CommonSerializer;

public interface RpcServer {
    void start();
    void setSerializer(CommonSerializer serializer);
    <T> void publishService(T service, Class<T> serviceClass);
}
