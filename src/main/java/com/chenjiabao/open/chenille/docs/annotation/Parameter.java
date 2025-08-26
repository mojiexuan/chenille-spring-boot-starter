package com.chenjiabao.open.chenille.docs.annotation;

import java.lang.annotation.*;

/**
 * 描述Api参数
 * @author ChenJiaBao
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
    String name() default "";
    String description() default "";
    boolean required() default false;
}
