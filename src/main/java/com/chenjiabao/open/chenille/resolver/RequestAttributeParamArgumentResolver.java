package com.chenjiabao.open.chenille.resolver;

import com.chenjiabao.open.chenille.annotation.RequestAttributeParam;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 处理 @RequestAttributeParam 注解
 * @author ChenJiaBao
 */
public class RequestAttributeParamArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 检查是否有 @RequestAttributeParam 注解
        return parameter.hasParameterAnnotation(RequestAttributeParam.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            @Nonnull NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {
        // 获取注解
        RequestAttributeParam annotation = parameter.getParameterAnnotation(RequestAttributeParam.class);
        if (annotation == null) {
            return null;
        }

        // 获取请求属性名
        String attributeName = annotation.value();
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        // 获取请求属性值
        Object attributeValue = request.getAttribute(attributeName);

        // 如果参数类型不匹配，可以抛出异常或返回 null
        if (attributeValue != null && !parameter.getParameterType().isInstance(attributeValue)) {
            throw new IllegalArgumentException(
                    "请求属性 '" + attributeName + "' 的类型是 " + attributeValue.getClass() +
                            "， 但预期类型是 " + parameter.getParameterType()
            );
        }

        return attributeValue;
    }
}
