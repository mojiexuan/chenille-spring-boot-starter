package com.chenjiabao.open.chenille.filter;

import com.chenjiabao.open.chenille.model.property.Api;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 跨域过滤器
 * @author ChenJiaBao
 */
@WebFilter(urlPatterns = "/*")
public class CorsFilter implements Filter {

    private final Api api;

    public CorsFilter(Api api) {
        this.api = api;
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        // 设置 CORS 头
        response.setHeader("Access-Control-Allow-Origin", api.getAccessControlAllowOrigin());
        response.setHeader("Access-Control-Allow-Methods", api.getAccessControlAllowMethods());
        response.setHeader("Access-Control-Max-Age", String.valueOf(api.getAccessControlMaxAge()));
        response.setHeader("Access-Control-Allow-Headers", api.getAccessControlAllowHeaders());

        // **预检请求 (OPTIONS) 直接返回**
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
