package com.chenjiabao.open.chenille.filter;

import com.chenjiabao.open.chenille.config.ChenilleAuthProvider;
import com.chenjiabao.open.chenille.core.ChenilleCheckUtils;
import com.chenjiabao.open.chenille.core.ChenilleJwtUtils;
import com.chenjiabao.open.chenille.dto.ChenilleAuthStatus;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.model.ChenilleAuthFilterInfo;
import com.chenjiabao.open.chenille.model.property.ChenilleAuth;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

/**
 * 认证过滤器
 *
 * @author ChenJiaBao
 */
public class ChenilleAuthFilterServlet implements Filter, Ordered {

    private final ChenilleAuth auth;
    private final ChenilleAuthProvider chenilleAuthProvider;
    private final ChenilleCheckUtils chenilleCheckUtils;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ChenilleJwtUtils chenilleJwtUtils;

    public ChenilleAuthFilterServlet(
            ChenilleAuth auth,
            ChenilleAuthProvider chenilleAuthProvider,
            ChenilleCheckUtils chenilleCheckUtils,
            ChenilleJwtUtils chenilleJwtUtils) {
        this.auth = auth;
        this.chenilleAuthProvider = chenilleAuthProvider;
        this.chenilleCheckUtils = chenilleCheckUtils;
        this.chenilleJwtUtils = chenilleJwtUtils;
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        // 转换为 HttpServletRequest
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        // 转换为 HttpServletResponse
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        // 请求方式
        String method = httpServletRequest.getMethod();

        if ("OPTIONS".equals(method)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 路径
        String path = httpServletRequest.getServletPath();

        if (shouldAuthenticate(path)) {
            String jwtToken = extractJwtToken(httpServletResponse);
            if (jwtToken != null && chenilleJwtUtils.validateToken(jwtToken)) {
                // 提取负载
                String subject = chenilleJwtUtils.parseToken(jwtToken).getSubject();
                // 认证
                ChenilleAuthStatus chenilleAuthStatus = chenilleAuthProvider.auth(new ChenilleAuthFilterInfo(
                        path,
                        jwtToken,
                        subject
                ));
                if(chenilleAuthStatus.isAuth()){
                    chenilleAuthStatus.getAttributes().forEach((k,v)->{
                        httpServletResponse.addHeader(k,String.valueOf(v));
                    });
                    filterChain.doFilter(servletRequest, servletResponse);
                }else {
                    unauthorized(httpServletResponse);
                }
            }else {
                unauthorized(httpServletResponse);
            }
        }else {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    /**
     * 判断路径是否需要身份验证
     * @param path 请求路径
     * @return 是否需要身份验证
     */
    private boolean shouldAuthenticate(String path) {
        // 首先检查是否匹配任何包含路径
        boolean include = auth.getIncludePaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        // 如果匹配包含路径，再检查是否匹配任何排除路径
        if (include) {
            boolean shouldExclude = auth.getExcludePaths().stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, path));
            return !shouldExclude;
        }

        return false;
    }

    /**
     * 从请求中提取JWT令牌
     * @param response HTTP请求
     * @return JWT令牌，如果不存在或格式不正确则返回null
     */
    private String extractJwtToken(HttpServletResponse response) {
        String authHeader = response.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length());
            return chenilleCheckUtils.isValidEmptyParam(token) ? null : token;
        }
        return null;
    }

    /**
     * 发送未授权响应
     * @param response HTTP响应
     */
    private void unauthorized(HttpServletResponse response){
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"code\":\"AUTH-4003\",\"message\":\"权限不足，禁止访问\"}";
        try {
            response.getWriter().write(body);
        } catch (IOException e) {
            throw new ChenilleChannelException();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
