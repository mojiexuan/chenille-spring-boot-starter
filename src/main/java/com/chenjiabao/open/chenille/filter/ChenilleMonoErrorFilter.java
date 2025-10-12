package com.chenjiabao.open.chenille.filter;

import com.chenjiabao.open.chenille.enums.ChenilleResponseCode;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import lombok.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 处理 Mono 错误的 WebFilter
 */
public class ChenilleMonoErrorFilter implements WebFilter, Ordered {

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange,
                             @NonNull WebFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(ex -> {
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                    String json;

                    if(ex instanceof ChenilleChannelException e){
                        exchange.getResponse().setStatusCode(e.getCode().getStatus());
                        json = String.format("{\"code\":\"%s\",\"message\":\"%s\"}",
                                e.getCode().getCode(), e.getMessage());
                    } else if (ex instanceof IllegalArgumentException) {
                        exchange.getResponse().setStatusCode(ChenilleResponseCode.PARAM_ERROR.getStatus());
                        json = String.format("{\"code\":\"%s\",\"message\":\"%s\"}",
                                ChenilleResponseCode.PARAM_ERROR.getCode(),
                                ChenilleResponseCode.PARAM_ERROR.getMessage());
                    } else {
                        exchange.getResponse().setStatusCode(ChenilleResponseCode.SYSTEM_ERROR.getStatus());
                        json = String.format("{\"code\":\"%s\",\"message\":\"%s\"}",
                                ChenilleResponseCode.SYSTEM_ERROR.getCode(),
                                ChenilleResponseCode.SYSTEM_ERROR.getMessage());
                    }

                    DataBuffer buffer = exchange.getResponse()
                            .bufferFactory()
                            .wrap(json.getBytes(StandardCharsets.UTF_8));

                    return exchange.getResponse().writeWith(Mono.just(buffer));
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
