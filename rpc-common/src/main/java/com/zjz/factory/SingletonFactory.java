package com.zjz.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * 单例工厂类，用于创建并管理各个单例对象。
 * 通过维护一个静态的objectMap来存储已经创建的单例对象，保证每个类只被实例化一次。
 */
public class SingletonFactory {

    // 静态的map用于存储单例对象
    private static Map<Class, Object> objectMap = new HashMap<>();

    // 私有构造方法，防止外部实例化
    private SingletonFactory() {}

    /**
     * 获取指定类的单例实例。
     * 如果该类的实例尚未创建，则创建一个新的实例并存储在objectMap中。
     *
     * @param clazz 需要获取单例实例的类
     * @param <T> 泛型参数，指定需要获取实例的类型
     * @return 返回该类的单例实例
     */
    public static <T> T getInstance(Class<T> clazz) {
        Object instance = objectMap.get(clazz); // 尝试从map中获取实例
        synchronized (clazz) { // 对类对象加锁，保证线程安全
            if(instance == null) { // 如果尚未实例化，则创建新实例
                try {
                    instance = clazz.newInstance(); // 使用无参构造方法创建实例
                    objectMap.put(clazz, instance); // 将新实例存储到map中
                } catch (IllegalAccessException | InstantiationException e) {
                    // 如果创建实例过程中出现异常，则抛出运行时异常
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return clazz.cast(instance); // 将获取的实例转换为泛型T并返回
    }

}
