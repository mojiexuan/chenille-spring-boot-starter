package com.chenjiabao.open.chenille.filter;

import com.chenjiabao.open.chenille.CheckUtils;
import com.chenjiabao.open.chenille.JwtUtils;
import com.chenjiabao.open.chenille.config.BaoAuth;
import com.chenjiabao.open.chenille.dto.AuthStatus;
import com.chenjiabao.open.chenille.model.AuthFilterInfo;
import com.chenjiabao.open.chenille.model.property.Auth;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Map;

/**
 * @author ChenJiaBao
 */
public class AuthFilter implements Filter {

    private final Auth auth;
    private final BaoAuth baoAuth;
    private final CheckUtils checkUtils;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtUtils jwtUtils;

    public AuthFilter(
            Auth auth,
            BaoAuth baoAuth,
            CheckUtils checkUtils,
            JwtUtils jwtUtils) {
        this.auth = auth;
        this.baoAuth = baoAuth;
        this.checkUtils = checkUtils;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI();
        // 请求方式
        String method = request.getMethod();
        // 如果是OPTIONS请求，直接放行
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 如果路径需要身份验证
        if (shouldAuthenticate(path)) {
            // 获取令牌
            String jwtToken = extractJwtToken(request);
            if(jwtToken != null){

                // 验证令牌是否有效
                if (!jwtUtils.validateToken(jwtToken)) {
                    sendUnauthorizedResponse(response);
                    return;
                }

                // 提取负载
                String subject = jwtUtils.parseToken(jwtToken).getSubject();

                // 认证
                AuthStatus authStatus = baoAuth.auth(new AuthFilterInfo(
                        path,
                        jwtToken,
                        subject
                ));
                if(authStatus.isAuth()){
                    // 设置请求属性
                    if(!authStatus.getAttributes().isEmpty()){
                        setRequestAttribute(request,authStatus.getAttributes());
                    }
                    // 继续执行过滤器链
                    filterChain.doFilter(request, response);
                }else {
                    sendUnauthorizedResponse(response);
                }
            }else {
                sendUnauthorizedResponse(response);
            }
        }else {
            filterChain.doFilter(request, response);
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
     * @param request HTTP请求
     * @return JWT令牌，如果不存在或格式不正确则返回null
     */
    private String extractJwtToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length());
            return checkUtils.isValidEmptyParam(token) ? null : token;
        }
        return null;
    }

    /**
     * 设置请求属性
     * @param request HTTP请求
     * @param attributes 属性映射
     */
    private void setRequestAttribute(HttpServletRequest request, Map<String, Object> attributes) {
        attributes.forEach(request::setAttribute);
    }

    /**
     * 发送未授权响应
     * @param response HTTP响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.getWriter().write("{\"code\":\"AUTH-4001\",\"message\":\"无权访问\"}");
    }
}
