package com.chenjiabao.open.chenille.handler;

import com.chenjiabao.open.chenille.core.ChenilleServerResponse;
import com.chenjiabao.open.chenille.annotation.ChenilleIgnoreResponse;
import com.chenjiabao.open.chenille.enums.ChenilleResponseCode;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.http.*;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 在控制层之后执行，用于包装返回值
 */
@Slf4j
public class ChenilleResponseHandler extends ResponseBodyResultHandler implements Ordered {

    public ChenilleResponseHandler(List<HttpMessageWriter<?>> writers,
                                   RequestedContentTypeResolver resolver,
                                   ReactiveAdapterRegistry registry) {
        super(writers, resolver, registry);
    }

    /**
     * “哑方法”
     * 用于生成带泛型信息的 MethodParameter
     */
    @SuppressWarnings("unused")
    private ChenilleServerResponse<Object> __chenilleResponseDummy() {
        return null;
    }

    @Override
    public boolean supports(@NonNull HandlerResult result) {

        ResolvableType returnType = result.getReturnType();

        if (returnType.resolve() == Mono.class || returnType.resolve() == ResponseEntity.class) {
            ResolvableType genericType = returnType.getGeneric(0);
            if (genericType.resolve() == ResponseEntity.class ||
                    genericType.resolve() == Mono.class ||
                    genericType.resolve() == Flux.class) {
                return false;
            }
        }

        // 忽略 ChenilleIgnoreResponse 注解
        MethodParameter returnTypeSource = result.getReturnTypeSource();
        return returnTypeSource.getMethodAnnotation(ChenilleIgnoreResponse.class) == null &&
                !returnTypeSource.getContainingClass().isAnnotationPresent(ChenilleIgnoreResponse.class);
    }

    @Override
    @NonNull
    public Mono<Void> handleResult(@NonNull ServerWebExchange exchange,
                                   @NonNull HandlerResult result) {
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Object value = result.getReturnValue();
        Object body;
        switch (value) {
            case null -> body = Mono.just(buildResponse(null));
            case Mono<?> mono -> body = mono.map(o -> {
                if (o instanceof ChenilleServerResponse<?> serverResponse) {
                    exchange.getResponse().setStatusCode(serverResponse.getCode().getStatus());
                    return serverResponse;
                } else {
                    return buildResponse(o);
                }
            });
            case Flux<?> flux -> {
                String accept = exchange.getRequest().getHeaders().getFirst(HttpHeaders.ACCEPT);
                boolean isStream = accept != null && accept.contains("text/event-stream");
                if (isStream) {
                    body = flux
                            .map(this::buildResponse)
                            .defaultIfEmpty(buildResponse(null));
                } else {
                    body = flux.collectList()
                            .map(this::buildResponse)
                            .defaultIfEmpty(buildResponse(null));
                }
            }
            case ResponseEntity<?> resp -> {
                exchange.getResponse().getHeaders().putAll(resp.getHeaders());
                exchange.getResponse().setStatusCode(resp.getStatusCode());
                Object b = resp.getBody();
                if (b instanceof ChenilleServerResponse<?> serverResponse) {
                    body = Mono.just(serverResponse);
                } else {
                    body = Mono.just(buildResponse(b, resp.getStatusCode()));
                }
            }
            default -> body = Mono.just(buildResponse(value));
        }

        MethodParameter actualParam = result.getReturnTypeSource();

        MethodParameter bodyParameter;
        try {
            Method dummy = ChenilleResponseHandler.class.getDeclaredMethod("__chenilleResponseDummy");
            bodyParameter = new MethodParameter(dummy, -1); // -1 表示 method return type
        } catch (NoSuchMethodException e) {
            // 这不可能发生，除非上面的哑方法被删掉；抛异常方便排查
            throw new ChenilleChannelException("数据包装失败", e);
        }

        // 使用带 actualParam 的重载，便于编码器推断真实元素类型
        return writeBody(body, bodyParameter, actualParam, exchange);
    }

    /**
     * 构建响应
     *
     * @param body 响应体
     * @return 响应
     */
    private ChenilleServerResponse<Object> buildResponse(Object body) {
        return buildResponse(body, HttpStatus.OK);
    }

    /**
     * 构建响应
     *
     * @param body   响应体
     * @param status 状态码
     * @return 响应
     */
    private ChenilleServerResponse<Object> buildResponse(Object body, HttpStatusCode status) {
        return ChenilleServerResponse.builder()
                .setData(body)
                .setCode(ChenilleResponseCode.getResponseCode(status))
                .build();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
