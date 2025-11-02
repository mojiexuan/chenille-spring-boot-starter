package com.chenjiabao.open.chenille.filter;

import com.chenjiabao.open.chenille.config.ChenilleAuthProvider;
import com.chenjiabao.open.chenille.core.ChenilleCheckUtils;
import com.chenjiabao.open.chenille.core.ChenilleJwtUtils;
import com.chenjiabao.open.chenille.enums.ChenilleResponseCode;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.model.ChenilleAuthFilterInfo;
import com.chenjiabao.open.chenille.model.property.ChenilleAuth;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Locale;

/**
 * @author ChenJiaBao
 */
@Slf4j
public class ChenilleAuthFilterFlux implements WebFilter, Ordered {

    private final ChenilleAuth auth;
    private final ChenilleAuthProvider chenilleAuthProvider;
    private final ChenilleCheckUtils chenilleCheckUtils;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ChenilleJwtUtils chenilleJwtUtils;

    public ChenilleAuthFilterFlux(
            ChenilleAuth auth,
            @Autowired(required = false) ChenilleAuthProvider chenilleAuthProvider,
            ChenilleCheckUtils chenilleCheckUtils,
            ChenilleJwtUtils chenilleJwtUtils) {
        this.auth = auth;
        this.chenilleAuthProvider = chenilleAuthProvider;
        this.chenilleCheckUtils = chenilleCheckUtils;
        this.chenilleJwtUtils = chenilleJwtUtils;
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange,
                             @NonNull WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        // 请求方式
        String method = exchange.getRequest().getMethod().name().toLowerCase(Locale.ROOT);
        if ("OPTIONS".equals(method)) {
            return chain.filter(exchange);
        }

        // 判断是否需要鉴权
        if (!shouldAuthenticate(path)) {
            return chain.filter(exchange);
        }

        // 提取JWT
        String jwtToken = extractJwtToken(exchange);
        if (jwtToken == null) {
            return unauthorized();
        }

        return chenilleJwtUtils
                .isTokenValid(jwtToken)
                .flatMap(valid -> {
                    if (!valid) {
                        return unauthorized();
                    }
                    // 解析负载
                    return chenilleJwtUtils.parseClaims(jwtToken)
                            .flatMap(claims -> {
                                String subject = claims.getSubject();
                                if (chenilleAuthProvider == null) {
                                    return chain.filter(exchange);
                                }

                                // 调用外部认证逻辑（响应式）
                                return chenilleAuthProvider.auth(new ChenilleAuthFilterInfo(
                                                path,
                                                jwtToken,
                                                subject
                                        ))
                                        .flatMap(authStatus -> {
                                            if (authStatus.isAuth()) {
                                                // 将认证属性注入请求上下文
                                                authStatus.getAttributes().forEach((k, v) ->
                                                        exchange.getAttributes().put(k, v));
                                                return chain.filter(exchange);
                                            } else {
                                                return unauthorized();
                                            }
                                        });
                            });
                })
                .onErrorResume(e -> {
                    log.error("鉴权过程发生异常: {}", e.getMessage(), e);
                    return unauthorized();
                });

    }

    /**
     * 判断路径是否需要身份验证
     *
     * @param path 请求路径
     * @return 是否需要身份验证
     */
    private boolean shouldAuthenticate(String path) {
        // 首先检查是否匹配任何包含路径
        boolean shouldInclude = auth.getIncludePaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        // 如果匹配包含路径，再检查是否匹配任何排除路径
        if (shouldInclude) {
            boolean shouldExclude = auth.getExcludePaths().stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, path));
            return !shouldExclude;
        }

        return false;
    }

    /**
     * 从请求中提取JWT令牌
     *
     * @param exchange HTTP请求
     * @return JWT令牌，如果不存在或格式不正确则返回null
     */
    private String extractJwtToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length());
            return chenilleCheckUtils.isValidEmptyParam(token) ? null : token;
        }
        return null;
    }

    /**
     * 发送未授权响应
     */
    private Mono<Void> unauthorized() {
        return Mono.error(ChenilleChannelException.builder()
                        .logMessage("权限不足，禁止访问")
                        .code(ChenilleResponseCode.FORBIDDEN)
                        .build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
