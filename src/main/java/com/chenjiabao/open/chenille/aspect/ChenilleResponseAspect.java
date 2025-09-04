package com.chenjiabao.open.chenille.aspect;

import com.chenjiabao.open.chenille.core.ChenilleServerResponse;
import com.chenjiabao.open.chenille.enums.ChenilleInternalEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Aspect
public class ChenilleResponseAspect {

    @Around("(@within(org.springframework.web.bind.annotation.RestController) " +
            "|| @within(org.springframework.web.bind.annotation.ResponseBody) " +
            "|| (@within(org.springframework.stereotype.Controller) && @annotation(org.springframework.web.bind.annotation.ResponseBody))) " +
            "&& !@within(com.chenjiabao.open.chenille.annotation.ChenilleIgnoreResponse) " +
            "&& !@annotation(com.chenjiabao.open.chenille.annotation.ChenilleIgnoreResponse)")
    public Mono<ChenilleServerResponse<Object>> wrapResponse(ProceedingJoinPoint pjp)
            throws Throwable {

        Object result = pjp.proceed();

        if (result == null) {
            return wrapWithContext(null);
        }

        if (result instanceof Mono<?> mono) {
            return mono.flatMap(this::wrapWithContext);
        }

        if (result instanceof Flux<?> flux) {
            return flux.collectList().flatMap(this::wrapWithContext);
        }

        if (result instanceof ResponseEntity<?> resp) {
            return wrapWithContext(resp.getBody());
        }

        // 普通对象
        log.warn("这是不规范的返回值，所有返回值必须使用 Mono/Flux 包裹起来！");
        return wrapWithContext(result);
    }

    /**
     * 包装响应体
     * @param body 响应体
     * @return 响应体
     */
    private Mono<ChenilleServerResponse<Object>> wrapWithContext(Object body) {
        return Mono.deferContextual(ctx -> {
            ServerWebExchange exchange = ctx.get(
                    ChenilleInternalEnum.CommonKey.EXCHANGE_CONTEXT.getValue()
            );
            return Mono.just(wrapWithExchange(body, exchange));
        });
    }

    /**
     * 包装响应体
     * @param body 响应体
     * @return 响应体
     */
    private ChenilleServerResponse<Object> wrapWithExchange(Object body){
        return Mono.deferContextual(ctx->{
            ServerWebExchange exchange = ctx.get(
                    ChenilleInternalEnum.CommonKey.EXCHANGE_CONTEXT.getValue()
            );
            return Mono.just(wrapWithExchange(body, exchange));
        }).block();
    }

    /**
     * 包装响应体
     * @param body 响应体
     * @param exchange 交换体
     * @return 响应体
     */
    private ChenilleServerResponse<Object> wrapWithExchange(Object body, ServerWebExchange exchange) {
        ChenilleServerResponse<Object> result = wrapResponse(body);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().setStatusCode(result.getCode().getStatus());
        return result;
    }

    /**
     * 包装响应体
     * @param body 响应体
     * @return 响应体
     */
    private ChenilleServerResponse<Object> wrapResponse(Object body) {
        ChenilleServerResponse<Object> result = null;
        if(body instanceof ChenilleServerResponse<?> serverResponse){
            result = ChenilleServerResponse.builder()
                    .setCode(serverResponse.getCode())
                    .setMessage(serverResponse.getMessage())
                    .setData(serverResponse.getData())
                    .build();
        }
        if(result == null){
            result = ChenilleServerResponse.builder()
                    .setData(body)
                    .build();
        }
        return result;
    }
}
