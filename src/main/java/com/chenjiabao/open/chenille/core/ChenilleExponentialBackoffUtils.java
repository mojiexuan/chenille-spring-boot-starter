package com.chenjiabao.open.chenille.core;

import lombok.Getter;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ⚡ Reactive 异步指数退避工具类（适用于 WebFlux / Reactor）
 *
 * <p>特性：
 * <ul>
 *     <li>支持异步延迟（Mono.delay）</li>
 *     <li>指数退避 + 抖动</li>
 *     <li>线程安全，可多实例复用</li>
 *     <li>无阻塞等待</li>
 * </ul>
 *
 * <p>示例：
 * <pre>{@code
 * ChenilleExponentialBackoffUtils backoff = ChenilleExponentialBackoffUtils.builder()
 *         .baseDelay(Duration.ofMillis(200))
 *         .maxDelay(Duration.ofSeconds(10))
 *         .enableJitter(true)
 *         .maxAttempts(6)
 *         .build();
 *
 * Flux.range(1, backoff.getMaxAttempts())
 *     .flatMap(attempt ->
 *         doRemoteCall()
 *             .retryWhen(companion -> companion
 *                 .zipWith(Flux.range(1, backoff.getMaxAttempts()), (error, i) -> i)
 *                 .flatMap(retryCount -> backoff.nextDelayMono(retryCount))
 *             )
 *     );
 * }</pre>
 */
public final class ChenilleExponentialBackoffUtils {
    /** 初始延迟 */
    private final Duration baseDelay;

    /** 最大延迟 */
    private final Duration maxDelay;

    /** 是否启用随机抖动 */
    private final boolean enableJitter;

    /** 最大重试次数（<=0 表示无限） */
    @Getter
    private final int maxAttempts;

    /** 随机数生成器 */
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private ChenilleExponentialBackoffUtils(Duration baseDelay,
                                       Duration maxDelay,
                                       boolean enableJitter,
                                       int maxAttempts) {
        if (baseDelay.isZero() || baseDelay.isNegative())
            throw new IllegalArgumentException("baseDelay必须为大于0的正整数");
        if (maxDelay.compareTo(baseDelay) < 0)
            throw new IllegalArgumentException("maxDelay必须大于等于baseDelay");

        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
        this.enableJitter = enableJitter;
        this.maxAttempts = maxAttempts;
    }

    /**
     * 根据重试次数生成异步延迟 Mono
     * @param attempt 当前重试次数（从1开始）
     * @return Mono 延迟完成
     */
    public Mono<Long> nextDelayMono(int attempt) {
        Duration delay = nextDelay(attempt);
        return Mono.delay(delay);
    }

    /**
     * 计算下一次延迟时间（同步版本）
     * @param attempt 当前重试次数（从1开始）
     * @return Duration 延迟时长
     */
    public Duration nextDelay(int attempt) {
        // 防止指数溢出
        long expMillis = (long) (baseDelay.toMillis() * Math.pow(2, Math.min(attempt - 1, 30)));
        long delayMillis = Math.min(expMillis, maxDelay.toMillis());

        if (enableJitter) {
            long jitter = RANDOM.nextLong(delayMillis / 2 + 1);
            delayMillis = delayMillis / 2 + jitter;
        }

        return Duration.ofMillis(delayMillis);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 模式配置
     */
    public static final class Builder {
        private Duration baseDelay = Duration.ofMillis(100);
        private Duration maxDelay = Duration.ofSeconds(10);
        private boolean enableJitter = true;
        private int maxAttempts = 0;

        public Builder baseDelay(Duration baseDelay) {
            this.baseDelay = baseDelay;
            return this;
        }

        public Builder maxDelay(Duration maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }

        public Builder enableJitter(boolean enableJitter) {
            this.enableJitter = enableJitter;
            return this;
        }

        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public ChenilleExponentialBackoffUtils build() {
            return new ChenilleExponentialBackoffUtils(baseDelay, maxDelay, enableJitter, maxAttempts);
        }
    }
}
