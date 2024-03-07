package com.zjz;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloServiceImpl2 implements HelloService{
    @Override
    public String sayHello(HelloObject helloObject) {
        log.info("接收到消息：{}", helloObject.getMessage());
        return "本次处理来自Socket服务";
    }
}
