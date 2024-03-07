package com.zjz.factory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
@Slf4j
public class ThreadPoolFactory {
    /**
     * 线程池核心参数定义
     */
    private static final int CORE_POOL_SIZE = 10; // 核心线程数
    private static final int MAXIMUM_POOL_SIZE_SIZE = 100; // 最大线程数
    private static final int KEEP_ALIVE_TIME = 1; // 空闲线程存活时间
    private static final int BLOCKING_QUEUE_CAPACITY = 100; // 队列容量

    // 线程池映射，用于存储线程池实例
    private static Map<String, ExecutorService> threadPollsMap = new ConcurrentHashMap<>();
    private ThreadPoolFactory() {
        // 私有构造函数，防止实例化
    }

    /**
     * 创建默认线程池
     *
     * @param threadNamePrefix 线程名字前缀
     * @return ExecutorService 线程池实例
     */
    public static ExecutorService createDefaultThreadPool(String threadNamePrefix) {
        return createDefaultThreadPool(threadNamePrefix, false);
    }

    /**
     * 创建自定义是否为守护线程的默认线程池
     *
     * @param threadNamePrefix 线程名字前缀
     * @param daemon           是否为守护线程
     * @return ExecutorService 线程池实例
     */
    public static ExecutorService createDefaultThreadPool(String threadNamePrefix, Boolean daemon) {
        // 如果线程池不存在或者已经关闭，则重新创建
        ExecutorService pool = threadPollsMap.computeIfAbsent(threadNamePrefix, k -> createThreadPool(threadNamePrefix, daemon));
        if (pool.isShutdown() || pool.isTerminated()) {
            threadPollsMap.remove(threadNamePrefix);
            pool = createThreadPool(threadNamePrefix, daemon);
            threadPollsMap.put(threadNamePrefix, pool);
        }
        return pool;
    }

    /**
     * 内部方法，用于创建线程池
     *
     * @param threadNamePrefix 线程名字前缀
     * @param daemon           是否为守护线程
     * @return ExecutorService 线程池实例
     */
    private static ExecutorService createThreadPool(String threadNamePrefix, Boolean daemon) {
        // 配置线程池基本参数
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE_SIZE, KEEP_ALIVE_TIME, TimeUnit.MINUTES, workQueue, threadFactory);
    }

    /**
     * 关闭所有线程池
     */
    public static void shutDownAll() {
        log.info("关闭所有线程池...");
        // 并行流处理，关闭所有线程池并等待其终止
        threadPollsMap.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("关闭线程池 [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                // 等待线程池完全关闭，超时则中断
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                log.error("关闭线程池失败！");
                executorService.shutdownNow(); // 中断所有任务
            }
        });
    }

    /**
     * 创建自定义的ThreadFactory
     *
     * @param threadNamePrefix 作为创建的线程名字的前缀
     * @param daemon           指定是否为 Daemon Thread(守护线程)
     * @return ThreadFactory
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        // 根据前缀和守护状态构建ThreadFactory
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }

        // 若无前缀，则使用默认的ThreadFactory
        return Executors.defaultThreadFactory();
    }

}
