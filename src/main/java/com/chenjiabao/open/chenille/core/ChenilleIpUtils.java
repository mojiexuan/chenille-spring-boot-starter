package com.chenjiabao.open.chenille.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
     * @return 公网 IP（可能为 null）
     */
    public String getPublicIp(ServerHttpRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeaders().getFirst(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
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
     * 获取用户 IP，如果没有公网 IP，就兜底返回内网地址
     *
     * @param request 请求
     * @return 公网或内网 IP（可能为 null）
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
     * @return true 表示公网 IP
     */
    public boolean isPublicIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return !(addr.isAnyLocalAddress()
                    || addr.isLoopbackAddress()
                    || addr.isSiteLocalAddress()
                    || isPrivateIPv6(addr));
        } catch (UnknownHostException e) {
            log.debug("无法解析 IP 地址: {}", ip, e);
            return false;
        }
    }

    /**
     * 判断 IPv6 地址是否属于私有地址段（fc00::/7 或 ::1 loopback）
     *
     * @param addr 网络地址
     * @return 是否内网地址
     */
    public boolean isPrivateIPv6(InetAddress addr) {
        if (addr == null) {
            return false;
        }
        String host = addr.getHostAddress();
        return host.startsWith("fc") || host.startsWith("fd") || host.equals("::1");
    }

    /**
     * 判断任意 IP 是否为内网 IP（IPv4 或 IPv6）
     *
     * @param ip IP 地址
     * @return true 表示内网 IP
     */
    public boolean isPrivateIp(String ip) {
        if (ip == null || ip.isBlank()) return false;
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isAnyLocalAddress()
                    || addr.isLoopbackAddress()
                    || addr.isSiteLocalAddress()
                    || isPrivateIPv6(addr);
        } catch (UnknownHostException e) {
            log.debug("无法解析 IP 地址: {}", ip, e);
            return false;
        }
    }

    /**
     * 从请求头解析所有候选 IP（X-Forwarded-For 等），按顺序返回
     *
     * @param request 请求对象
     * @return IP 列表，顺序为头部顺序
     */
    public List<String> parseForwardedIps(ServerHttpRequest request) {
        List<String> ips = new ArrayList<>();
        for (String header : IP_HEADER_CANDIDATES) {
            String value = request.getHeaders().getFirst(header);
            if (value != null && !value.isBlank()) {
                for (String ip : value.split(",")) {
                    ips.add(ip.trim());
                }
            }
        }
        return ips;
    }

    /**
     * 获取公网 IP Optional 包装（方便调用链使用）
     *
     * @param request 请求对象
     * @return Optional 包装的公网 IP
     */
    public Optional<String> getPublicIpOptional(ServerHttpRequest request) {
        return Optional.ofNullable(getPublicIp(request));
    }
}
