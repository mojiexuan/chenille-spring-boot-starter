package com.chenjiabao.open.chenille.annotation;

import java.lang.annotation.*;

/**
 * 忽略响应处理
 * 用于Controller方法，忽略ChenilleResponseAdvice的处理
 * @author 陈佳宝 mail@chenjiabao.com
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ChenilleIgnoreResponse {
}
