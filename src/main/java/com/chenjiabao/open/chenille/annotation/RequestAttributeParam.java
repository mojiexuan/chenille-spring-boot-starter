package com.chenjiabao.open.chenille.annotation;

import java.lang.annotation.*;

/**
 * 从请求中获取指定属性名的值
 * @author ChenJiaBao
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestAttributeParam {
    //指定要从请求属性（request attribute）中获取的属性名
    String value();
}
