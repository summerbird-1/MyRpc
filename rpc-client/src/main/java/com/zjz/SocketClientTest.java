package com.zjz;

import com.zjz.serializer.CommonSerializer;
import com.zjz.serializer.HessianSerializer;
import com.zjz.serializer.KryoSerializer;
import com.zjz.socket.client.SocketClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketClientTest {
    public static void main(String[] args) {

        SocketClient socketClient = new SocketClient(CommonSerializer.KRYO_SERIALIZER);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(socketClient);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject hello = new HelloObject(1, "hello");
        String s = helloService.sayHello(hello);
        System.out.println(s);
    }
}
