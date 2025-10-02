package com.chenjiabao.open.chenille.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.ClassUtils;

/**
 * chenille web 环境工具类
 */
@Getter
@Setter
public class ChenilleWebEnvUtils {
    /**
     * 是否是 webflux 环境
     */
    private boolean isWebFlux;

    public ChenilleWebEnvUtils() {
        isWebFlux = ClassUtils.isPresent(
                "org.springframework.web.reactive.DispatcherHandler",
                ChenilleWebEnvUtils.class.getClassLoader());
    }
}
