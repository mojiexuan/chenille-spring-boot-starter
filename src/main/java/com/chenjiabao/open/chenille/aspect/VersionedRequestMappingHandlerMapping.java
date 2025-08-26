package com.chenjiabao.open.chenille.aspect;

import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * 用于公开两个方法，勿改，用于接口版本化管理
 * @author ChenJiaBao
 */
public class VersionedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    /**
     * 公开getMappingForMethod方法
     */
    public RequestMappingInfo getMappingForMethodPublic(Method method, Class<?> handlerType) {
        return super.getMappingForMethod(method, handlerType);
    }

    /**
     * 公开注册方法
     */
    public void registerMappingPublic(RequestMappingInfo mapping, Object handler, Method method) {
        super.registerMapping(mapping, handler, method);
    }

}
