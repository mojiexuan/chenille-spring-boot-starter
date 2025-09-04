package com.chenjiabao.open.chenille.enums;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池拒绝策略
 */
public enum ChenilleExecutorRejectedPolicy {
    /**
     * 直接抛出 {@link RejectedExecutionException}
     */
    ABORT{
        @Override
        public RejectedExecutionHandler toHandler() {
            return new ThreadPoolExecutor.AbortPolicy();
        }
    },
    /**
     * 调用者运行策略，在调用者线程中执行任务
     */
    CALLER_RUNS{
        @Override
        public RejectedExecutionHandler toHandler() {
            return new ThreadPoolExecutor.CallerRunsPolicy();
        }
    },
    /**
     * 丢弃旧任务，执行新任务
     */
    DISCARD_OLDEST{
        @Override
        public RejectedExecutionHandler toHandler() {
            return new ThreadPoolExecutor.DiscardOldestPolicy();
        }
    },
    /**
     * 丢弃当前任务
     */
    DISCARD{
        @Override
        public RejectedExecutionHandler toHandler() {
            return new ThreadPoolExecutor.DiscardPolicy();
        }
    };

    public abstract RejectedExecutionHandler toHandler();
}
