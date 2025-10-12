package com.chenjiabao.open.chenille.filter;

import com.chenjiabao.open.chenille.model.property.ChenilleApi;
import lombok.NonNull;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 跨域过滤器
 *
 * @author ChenJiaBao
 */
public record ChenilleCorsFilter(ChenilleApi api) implements WebFilter, Ordered {

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange,
                             @NonNull WebFilterChain chain) {
        var response = exchange.getResponse();
        var request = exchange.getRequest();

        // 设置 CORS 头
        response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, api.getAccessControlAllowOrigin());
        response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, api.getAccessControlAllowMethods());
        response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(api.getAccessControlMaxAge()));
        response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, api.getAccessControlAllowHeaders());

        // **预检请求 (OPTIONS) 直接返回**
        if ("OPTIONS".equalsIgnoreCase(request.getMethod().name())) {
            response.setStatusCode(HttpStatus.OK);
            return Mono.empty();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
