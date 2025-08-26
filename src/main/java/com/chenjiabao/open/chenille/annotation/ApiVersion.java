package com.chenjiabao.open.chenille.annotation;

import java.lang.annotation.*;

/**
 * 版本控制注解
 * @author ChenJiaBao
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {
    int value() default 1;
}
