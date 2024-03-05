package com.zjz;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloServiceImpl implements HelloService{
    @Override
    public String sayHello(HelloObject helloObject) {
        log.info("接收到：{}", helloObject.getMessage());
        return "这是调用的返回值，id= " + helloObject.getId();
    }
}
