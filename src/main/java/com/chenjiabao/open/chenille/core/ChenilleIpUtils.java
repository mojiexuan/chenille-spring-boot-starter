package com.chenjiabao.open.chenille.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP工具类
 *
 * @author ChenJiaBao
 */
@Slf4j
public class ChenilleIpUtils {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "CF-Connecting-IP"
    };

    /**
     * 从 WebFlux 的 ServerHttpRequest 获取用户公网 IP
     *
     * @param request 请求
     * @return IP（可能为 null）
     */
    public String getPublicIp(ServerHttpRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeaders().getFirst(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                ip = ip.split(",")[0].trim();
                if (isPublicIp(ip)) {
                    return ip;
                }
            }
        }
        if (request.getRemoteAddress() != null) {
            String ip = request.getRemoteAddress().getAddress().getHostAddress();
            return isPublicIp(ip) ? ip : null;
        }
        return null;
    }

    /**
     * 获取用户 IP，如果没有公网 IP，就兜底返回 内网地址
     */
    public String getIpOrFallback(ServerHttpRequest request) {
        String ip = getPublicIp(request);
        if (ip != null) {
            return ip;
        }
        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : null;
    }

    /**
     * 判断是否是公网 IP
     *
     * @param ip IP
     * @return 是否是公网 IP
     */
    public boolean isPublicIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return !(addr.isAnyLocalAddress()
                    || addr.isLoopbackAddress()
                    || addr.isSiteLocalAddress()
                    || isPrivateIPv6(addr));
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 检查 IPv6 地址是否属于私有地址段
     *
     * @param addr 网络地址
     * @return 是否内网地址
     */
    public boolean isPrivateIPv6(InetAddress addr) {
        String host = addr.getHostAddress();
        return host.startsWith("fc") || host.startsWith("fd"); // fc00::/7 内网 IPv6
    }

}
