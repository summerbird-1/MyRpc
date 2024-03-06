package com.zjz;

import com.zjz.serializer.CommonSerializer;

public interface RpcServer {
    void start(int port);
    void setSerializer(CommonSerializer serializer);
}
