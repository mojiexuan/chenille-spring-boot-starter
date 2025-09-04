package com.chenjiabao.open.chenille.filter;

import com.chenjiabao.open.chenille.enums.ChenilleInternalEnum;
import lombok.NonNull;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 上下文传递
 */
public class ChenilleExchangeContextFilter implements WebFilter {

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange,
                             @NonNull WebFilterChain chain) {
        return chain.filter(exchange)
                .contextWrite(ctx->ctx.put(ChenilleInternalEnum.CommonKey.EXCHANGE_CONTEXT.getValue(),
                        exchange));
    }

}
