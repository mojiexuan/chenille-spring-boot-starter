package com.chenjiabao.open.chenille.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 清除缓存中的指定数据
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChenilleCacheEvict {
    /**
     * 缓存名称
     */
    String cacheName() default "";

    /**
     * 缓存key，支持SpEL表达式
     */
    String key();

    /**
     * 是否清除二级缓存
     */
    boolean evictSecondary() default true;
}
