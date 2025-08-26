package com.chenjiabao.open.chenille;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * DelayedTaskExecutor
 * 任务调度器
 * @author 陈佳宝 mail@chenjiabao.com
 * @version 1.0
 * @description TODO
 * @date 2025/1/19 20:17
 */
public class DelayedTaskExecutor {

    private final ScheduledExecutorService scheduler;

    public DelayedTaskExecutor() {
        // 使用单线程调度器
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 在指定的延迟时间后执行任务
     *
     * @param delaySeconds 延迟的时间，单位为秒
     * @param task         需要执行的任务
     */
    public void executeAfterDelay(long delaySeconds, Runnable task) {
        // 计划任务
        scheduler.schedule(task, delaySeconds, TimeUnit.SECONDS);
    }

    // 可选的：优雅地关闭调度器
    public void shutdown() {
        scheduler.shutdown();
    }
}
