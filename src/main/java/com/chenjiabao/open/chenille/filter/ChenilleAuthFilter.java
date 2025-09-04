package com.chenjiabao.open.chenille.filter;

import com.chenjiabao.open.chenille.core.ChenilleCheckUtils;
import com.chenjiabao.open.chenille.core.ChenilleJwtUtils;
import com.chenjiabao.open.chenille.dto.ChenilleAuthStatus;
import com.chenjiabao.open.chenille.model.ChenilleAuthFilterInfo;
import com.chenjiabao.open.chenille.model.property.ChenilleAuth;
import lombok.NonNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * @author ChenJiaBao
 */
public class ChenilleAuthFilter implements WebFilter {

    private final ChenilleAuth auth;
    private final com.chenjiabao.open.chenille.config.ChenilleAuth chenilleAuth;
    private final ChenilleCheckUtils chenilleCheckUtils;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ChenilleJwtUtils chenilleJwtUtils;

    public ChenilleAuthFilter(
            ChenilleAuth auth,
            com.chenjiabao.open.chenille.config.ChenilleAuth chenilleAuth,
            ChenilleCheckUtils chenilleCheckUtils,
            ChenilleJwtUtils chenilleJwtUtils) {
        this.auth = auth;
        this.chenilleAuth = chenilleAuth;
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

        // 如果路径需要身份验证
        if (shouldAuthenticate(path)) {
            // 获取令牌
            String jwtToken = extractJwtToken(exchange);
            if (jwtToken != null && chenilleJwtUtils.validateToken(jwtToken)) {
                // 提取负载
                String subject = chenilleJwtUtils.parseToken(jwtToken).getSubject();
                // 认证
                ChenilleAuthStatus chenilleAuthStatus = chenilleAuth.auth(new ChenilleAuthFilterInfo(
                        path,
                        jwtToken,
                        subject
                ));
                if(chenilleAuthStatus.isAuth()){
                    chenilleAuthStatus.getAttributes().forEach((k,v)->{
                        exchange.getAttributes().put(k,v);
                    });
                    return chain.filter(exchange);
                }else {
                    return unauthorized(exchange);
                }
            }else {
                return unauthorized(exchange);
            }
        }else {
            return chain.filter(exchange);
        }

    }

    /**
     * 判断路径是否需要身份验证
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
     * @param exchange HTTP响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange){
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"code\":\"AUTH-4003\",\"message\":\"权限不足，禁止访问\"}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
