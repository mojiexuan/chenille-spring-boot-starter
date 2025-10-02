package com.chenjiabao.open.chenille.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;

@AutoConfiguration
@Slf4j
@AutoConfigureAfter({
        ChenilleAutoConfigBase.class,
        ChenilleAutoConfigWeb.class,
        ChenilleAutoConfigCache.class,
        ChenilleAutoConfigWebFlux.class
})
public class ChenilleAutoConfigLifecycle {

    /**
     * 监听应用启动完成事件（兼容 WebMVC 和 WebFlux）
     * 使用者可以通过定义同名 Bean 覆盖此实现
     */
    @Bean
    @ConditionalOnMissingBean(name = "chenilleApplicationReadyListener")
    public ApplicationListener<ApplicationReadyEvent> applicationReadyListener() {
        return event -> log.info("🚀 Chenille: 服务已启动完成");
    }

    /**
     * 监听应用关闭事件
     * 使用者可以通过定义同名 Bean 覆盖此实现
     */
    @Bean
    @ConditionalOnMissingBean(name = "chenilleContextClosedListener")
    public ApplicationListener<ContextClosedEvent> contextClosedListener() {
        return event -> log.info("🛑 Chenille: 服务正在关闭");
    }

    /**
     * 监听应用停止事件
     */
    @Bean
    @ConditionalOnMissingBean(name = "chenilleContextStoppedListener")
    public ApplicationListener<ContextStoppedEvent> contextStoppedListener() {
        return event -> log.info("⏹️ Chenille: 服务已停止");
    }

}
