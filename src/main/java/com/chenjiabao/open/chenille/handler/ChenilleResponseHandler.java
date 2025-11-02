package com.chenjiabao.open.chenille.handler;

import com.chenjiabao.open.chenille.serializer.ChenilleServerResponse;
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

    @SuppressWarnings("unused")
    private ChenilleServerResponse<Object> __chenilleResponseDummy() {
        return null;
    }

    @Override
    public boolean supports(@NonNull HandlerResult result) {
        ResolvableType returnType = result.getReturnType();

        if (returnType.resolve() == Mono.class
                || returnType.resolve() == ResponseEntity.class
                || returnType.resolve() == Flux.class) {
            ResolvableType genericType = returnType.getGeneric(0);
            if (genericType.resolve() == ResponseEntity.class ||
                    genericType.resolve() == Mono.class ||
                    genericType.resolve() == Flux.class ||
                    genericType.resolve() == ChenilleServerResponse.class) {
                return false;
            }
        }

        MethodParameter returnTypeSource = result.getReturnTypeSource();
        return returnTypeSource.getMethodAnnotation(ChenilleIgnoreResponse.class) == null &&
                !returnTypeSource.getContainingClass().isAnnotationPresent(ChenilleIgnoreResponse.class);
    }

    @Override
    @NonNull
    public Mono<Void> handleResult(@NonNull ServerWebExchange exchange,
                                   @NonNull HandlerResult result) {

        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Object returnValue = result.getReturnValue();
        Mono<?> body;

        switch (returnValue) {
            case null -> body = Mono.just(buildResponse(null));
            case ChenilleServerResponse<?> serverResponse -> {
                exchange.getResponse().setStatusCode(serverResponse.getCode().getStatus());
                body = Mono.just(serverResponse);
            }
            case Mono<?> mono -> {
                body = mono.flatMap(o -> {
                    if (o instanceof ChenilleServerResponse<?> sr) {
                        exchange.getResponse().setStatusCode(sr.getCode().getStatus());
                        return Mono.just(sr);
                    } else {
                        return Mono.just(buildResponse(o));
                    }
                });
            }
            case Flux<?> flux -> {
                String accept = exchange.getRequest().getHeaders().getFirst(HttpHeaders.ACCEPT);
                boolean isStream = accept != null && accept.contains("text/event-stream");
                if (isStream) {
                    body = flux.map(this::buildResponse)
                            .defaultIfEmpty(buildResponse(null))
                            .collectList()
                            .map(this::buildResponse);
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
                if (b instanceof ChenilleServerResponse<?> sr) {
                    body = Mono.just(sr);
                } else {
                    body = Mono.just(buildResponse(b, resp.getStatusCode()));
                }
            }
            default -> {
                body = Mono.just(buildResponse(returnValue));
            }
        }

        MethodParameter bodyParameter;
        try {
            Method dummy = ChenilleResponseHandler.class.getDeclaredMethod("__chenilleResponseDummy");
            bodyParameter = new MethodParameter(dummy, -1);
        } catch (NoSuchMethodException e) {
            throw ChenilleChannelException.builder()
                    .logMessage("数据包装失败")
                    .build();
        }

        MethodParameter actualParam = result.getReturnTypeSource();
        return writeBody(body, bodyParameter, actualParam, exchange);
    }

    private ChenilleServerResponse<Object> buildResponse(Object body) {
        return buildResponse(body, HttpStatus.OK);
    }

    private ChenilleServerResponse<Object> buildResponse(Object body, HttpStatusCode status) {
        return ChenilleServerResponse.builder()
                .setData(body)
                .setCode(ChenilleResponseCode.getResponseCode(status));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
