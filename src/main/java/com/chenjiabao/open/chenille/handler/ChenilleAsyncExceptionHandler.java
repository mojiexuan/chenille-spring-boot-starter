package com.chenjiabao.open.chenille.handler;

import com.chenjiabao.open.chenille.core.ChenilleJsonUtils;
import com.chenjiabao.open.chenille.serializer.ChenilleServerResponse;
import com.chenjiabao.open.chenille.enums.ChenilleResponseCode;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 异步流异常处理
 * 捕获 Mono.error() / Flux.error()
 */
@Component
@Order(-100) // 保证优先级高于默认的 DefaultErrorWebExceptionHandler
@Slf4j
public class ChenilleAsyncExceptionHandler extends AbstractErrorWebExceptionHandler {

    private final ChenilleJsonUtils chenilleJsonUtils;

    public ChenilleAsyncExceptionHandler(ErrorAttributes errorAttributes,
                                         WebProperties.Resources resources,
                                         ApplicationContext applicationContext,
                                         @Autowired(required = false) ChenilleJsonUtils chenilleJsonUtils) {
        super(errorAttributes, resources, applicationContext);
        if (chenilleJsonUtils == null) {
            this.chenilleJsonUtils = new ChenilleJsonUtils();
            log.warn("异步流异常处理中 ChenilleJsonUtils 未配置，将使用默认配置");
        } else {
            this.chenilleJsonUtils = chenilleJsonUtils;
            log.warn("异步流异常处理中 ChenilleJsonUtils 已配置，将使用自定义配置");
        }

        // 设置路由优先级和异常处理
        setMessageReaders(ServerCodecConfigurer.create().getReaders());
        setMessageWriters(ServerCodecConfigurer.create().getWriters());
    }


    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        // 捕获所有请求
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request); // 获取异常
        log.error("捕获全局异常 -> {}", error.getMessage(), error);

        // 构建统一响应
        ChenilleServerResponse<Void> response = buildResponse(error);

        // 返回 JSON 响应
        byte[] bytes;
        try {
            bytes = chenilleJsonUtils.toBytes(response);
        } catch (Exception e) {
            log.error("序列化异常响应失败", e);
            bytes = ("{\"code\":\"SYS-0500\",\"message\":\"系统错误\"}").getBytes(StandardCharsets.UTF_8);
        }

        return ServerResponse.status(mapHttpStatus(error))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bytes);
    }

    /**
     * 根据异常类型生成统一响应
     */
    private ChenilleServerResponse<Void> buildResponse(Throwable ex) {
        if (ex instanceof ChenilleChannelException e) {
            return ChenilleServerResponse.fail(e);
        } else if (ex instanceof ServerWebInputException || ex instanceof MethodArgumentNotValidException) {
            return ChenilleServerResponse.fail(ChenilleResponseCode.PARAM_ERROR);
        } else {
            return ChenilleServerResponse.fail(ChenilleResponseCode.SYSTEM_ERROR);
        }
    }

    /**
     * 根据异常映射 HTTP 状态码（可自定义）
     */
    private HttpStatus mapHttpStatus(Throwable ex) {
        if (ex instanceof ChenilleChannelException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof ServerWebInputException || ex instanceof MethodArgumentNotValidException) {
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
