package com.chenjiabao.open.chenille.handler;

import com.chenjiabao.open.chenille.core.ChenilleServerResponse;
import com.chenjiabao.open.chenille.annotation.ChenilleIgnoreResponse;
import com.chenjiabao.open.chenille.enums.ChenilleResponseCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 在控制层之后执行，用于包装返回值
 */
@Slf4j
public class ChenilleResponseHandler extends ResponseBodyResultHandler {

    public ChenilleResponseHandler(List<HttpMessageWriter<?>> writers,
                                   RequestedContentTypeResolver resolver,
                                   ReactiveAdapterRegistry registry) {
        super(writers, resolver, registry);
    }

    @Override
    public boolean supports(@NonNull HandlerResult result) {
        log.info("检查支持");

        ResolvableType returnType = result.getReturnType();

        if (returnType.resolve() == Mono.class) {
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

        log.info("开始处理返回值");
        Object value = result.getReturnValue();
        Object body;
        if(value == null){
            body = Mono.just(buildResponse(null));
        }else if (value instanceof Mono<?> mono) {
            body = mono.map(o->{
                if (o instanceof ChenilleServerResponse<?> serverResponse) {
                    return ChenilleServerResponse.builder()
                            .setCode(serverResponse.getCode())
                            .setData(serverResponse.getData())
                            .setMessage(serverResponse.getMessage())
                            .getResponseEntity();
                }else {
                    return buildResponse(o);
                }
            });
        }else if (value instanceof Flux<?> flux) {
            body = flux.collectList()
                    .map(this::buildResponse)
                    .defaultIfEmpty(buildResponse(null));
        }else if (value instanceof ResponseEntity<?> resp) {
            exchange.getResponse().getHeaders().putAll(resp.getHeaders());
            Object b = resp.getBody();
            if (b instanceof ChenilleServerResponse<?> serverResponse) {
                body = Mono.just(
                        ChenilleServerResponse.builder()
                                .setCode(serverResponse.getCode())
                                .setData(serverResponse.getData())
                                .setMessage(serverResponse.getMessage())
                                .getResponseEntity()
                );
            }else {
                body = Mono.just(
                        ResponseEntity
                                .status(resp.getStatusCode())
                                .headers(resp.getHeaders())
                                .body(buildResponse(b, resp.getStatusCode()))
                );
            }
        }else {
            body = Mono.just(buildResponse(value));
        }

        return writeBody(body, result.getReturnTypeSource(), exchange);
    }

    /**
     * 构建响应
     *
     * @param body 响应体
     * @return 响应
     */
    private ResponseEntity<ChenilleServerResponse<Object>> buildResponse(Object body) {
        return buildResponse(body, HttpStatus.OK);
    }

    /**
     * 构建响应
     *
     * @param body 响应体
     * @param status 状态码
     * @return 响应
     */
    private ResponseEntity<ChenilleServerResponse<Object>> buildResponse(Object body, HttpStatusCode status) {
        return ChenilleServerResponse.builder()
                .setData(body)
                .setCode(ChenilleResponseCode.getResponseCode(status))
                .getResponseEntity();
    }
}
