package com.chenjiabao.open.chenille.core;

import com.chenjiabao.open.chenille.enums.ChenilleExecutorRejectedPolicy;
import com.chenjiabao.open.chenille.enums.ChenilleInternalEnum;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 硬件相关工具类
 *
 * @author 陈佳宝
 * @since 2025/09/02
 */
public class ChenilleHardwareUtils {

    /**
     * 获取 CPU 核心数
     */
    public int availableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * 获取最大可用堆内存
     */
    public long maxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * 根据任务类型计算默认核心线程数
     *
     * @param type CPU 或 IO
     */
    public int defaultCoreThreads(ChenilleInternalEnum.ExecutorType type) {
        int cores = availableProcessors();
        return type == ChenilleInternalEnum.ExecutorType.CPU ? cores : cores * 2;
    }

    /**
     * 根据任务类型计算默认最大线程数
     *
     * @param type CPU 或 IO
     */
    public int defaultMaxThreads(ChenilleInternalEnum.ExecutorType type) {
        int cores = availableProcessors();
        return type == ChenilleInternalEnum.ExecutorType.CPU ? cores + 1 : cores * 4;
    }

    /**
     * 根据任务类型和最大堆内存计算默认队列容量
     *
     * @param type CPU 或 IO
     */
    public int defaultQueueCapacity(ChenilleInternalEnum.ExecutorType type) {
        int cores = availableProcessors();
        if (type == ChenilleInternalEnum.ExecutorType.CPU) {
            return cores * 2; // CPU 密集型小队列
        }
        // IO 密集型大队列，根据堆内存计算
        long maxHeap = maxMemory(); // 字节
        long estimatedTasks = (long) ((maxHeap * 0.2) / (50 * 1024)); // 每个任务约 50KB
        return (int) Math.min(estimatedTasks, 50_000);
    }

    public ThreadPoolTaskExecutor buildExecutor(ChenilleInternalEnum.ExecutorType type,
                                                String name,
                                                Integer corePoolSize,
                                                Integer maxPoolSize,
                                                Integer queueCapacity,
                                                ChenilleExecutorRejectedPolicy rejectedPolicy) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 线程名前缀
        executor.setThreadNamePrefix(name);

        // 核心线程数
        executor.setCorePoolSize(
                corePoolSize != null ? corePoolSize : this.defaultCoreThreads(type)
        );

        // 最大线程数
        executor.setMaxPoolSize(
                maxPoolSize != null ? maxPoolSize : this.defaultMaxThreads(type)
        );

        // 队列容量
        executor.setQueueCapacity(
                queueCapacity != null ? queueCapacity : this.defaultQueueCapacity(type)
        );

        // 拒绝策略
        executor.setRejectedExecutionHandler(rejectedPolicy.toHandler());

        executor.initialize();
        return executor;
    }

}
