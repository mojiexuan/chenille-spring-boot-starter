package com.chenjiabao.open.chenille.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法执行前查缓存，命中则返回缓存值；未命中则执行方法，再把结果存缓存
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChenilleCacheable {
    /**
     * 缓存名称
     */
    String cacheName() default "";

    /**
     * 缓存key，支持SpEL表达式
     */
    String key();

    /**
     * 基础过期时间（分钟）,小于 1 时使用默认过期时间
     */
    long ttl() default 0;

    /**
     * 随机时间范围（分钟），实际过期时间 = ttl + random(0, randomRange)
     */
    long randomRange() default 0;

    /**
     * 是否同步到二级缓存
     */
    boolean syncToSecondary() default true;
}
