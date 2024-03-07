package com.zjz;

import com.zjz.entity.RpcRequest;
import com.zjz.serializer.CommonSerializer;

public interface RpcClient {
    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;
    Object sendRequest(RpcRequest rpcRequest);
}
