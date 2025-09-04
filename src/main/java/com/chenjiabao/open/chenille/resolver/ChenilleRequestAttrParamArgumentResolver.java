package com.chenjiabao.open.chenille.resolver;

import com.chenjiabao.open.chenille.annotation.ChenilleRequestAttributeParam;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 处理 @RequestAttributeParam 注解
 * @author ChenJiaBao
 */
public class ChenilleRequestAttrParamArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 检查是否有 @RequestAttributeParam 注解
        return parameter.hasParameterAnnotation(ChenilleRequestAttributeParam.class);
    }

    @Override
    @NonNull
    public Mono<Object> resolveArgument(MethodParameter parameter,
                                        @NonNull BindingContext bindingContext,
                                        @NonNull ServerWebExchange exchange) {
        ChenilleRequestAttributeParam annotation =
                parameter.getParameterAnnotation(ChenilleRequestAttributeParam.class);
        if (annotation == null) {
            return Mono.empty();
        }

        // 获取请求属性名
        String attributeName = annotation.value();

        // 从 exchange.attributes 中取值
        Object attributeValue = exchange.getAttribute(attributeName);

        // 类型检查
        if (attributeValue != null && !parameter.getParameterType().isInstance(attributeValue)) {
            return Mono.error(new IllegalArgumentException(
                    "请求属性 '" + attributeName + "' 的类型是 " + attributeValue.getClass() +
                            "， 但预期类型是 " + parameter.getParameterType()
            ));
        }

        return Mono.justOrEmpty(attributeValue);
    }
}
