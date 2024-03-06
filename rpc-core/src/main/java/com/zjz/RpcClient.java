package com.zjz;

import com.zjz.entity.RpcRequest;
import com.zjz.serializer.CommonSerializer;

public interface RpcClient {
    Object sendRequest(RpcRequest rpcRequest);
    void setSerializer(CommonSerializer serializer);
}
