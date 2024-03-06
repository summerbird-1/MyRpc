package com.zjz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
/**
 * 序列化器编码枚举，用于定义不同序列化器的标识代码。
 */
public enum SerializerCode {
    // 使用Kryo序列化器的标识代码
    KRYO(0),
    // 使用JSON序列化器的标识代码
    JSON(1);

    // 序列化器的标识码
    private final int code;

}
