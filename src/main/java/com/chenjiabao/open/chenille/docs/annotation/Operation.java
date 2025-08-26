package com.chenjiabao.open.chenille.docs.annotation;

import java.lang.annotation.*;

/**
 * 描述Api操作
 * @author ChenJiaBao
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Operation {
    String summary() default "";
    String description() default "";
    boolean deprecated() default false;
}
