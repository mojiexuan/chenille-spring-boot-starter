package com.chenjiabao.open.chenille.docs.annotation;

import java.lang.annotation.*;

/**
 * 描述模型类
 * @author ChenJiaBao
 */
@Documented
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Schema {
    /** 类型描述 */
    String description() default "";

    /** 示例值 */
    String example() default "";

    /** 是否必须 */
    boolean required() default false;

    /** 引用类型（用于复杂对象） */
    String ref() default "";

    /** 允许的值（枚举） */
    String[] allowedValues() default {};

    /** 最小长度（字符串/集合） */
    int minLength() default -1;

    /** 最大长度（字符串/集合） */
    int maxLength() default -1;

    /** 最小值（数字） */
    String minimum() default "";

    /** 最大值（数字） */
    String maximum() default "";

    /** 格式（如date-time, email等） */
    String format() default "";
}
