package com.chenjiabao.open.chenille.config;

import com.chenjiabao.open.chenille.core.ChenilleCheckUtils;
import com.chenjiabao.open.chenille.core.ChenilleJwtUtils;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.filter.ChenilleAuthFilterServlet;
import com.chenjiabao.open.chenille.model.property.ChenilleProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = {
        "jakarta.servlet.Servlet",
        "org.springframework.web.servlet.DispatcherServlet",
        "org.springframework.web.servlet.config.annotation.WebMvcConfigurer"
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ChenilleProperties.class)
@AutoConfigureAfter(ChenilleAutoConfigCache.class)
public class ChenilleAutoConfigWebMvc implements WebMvcConfigurer {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.auth", name = "enabled", havingValue = "true")
    public ChenilleAuthFilterServlet chenilleAuthFilterServlet(
            ChenilleProperties chenilleProperties,
            @Autowired(required = false) ChenilleAuthProvider chenilleAuthProvider,
            @Autowired(required = false) ChenilleCheckUtils chenilleCheckUtils,
            @Autowired(required = false) ChenilleJwtUtils chenilleJwtUtils) {
        if (chenilleAuthProvider == null) {
            log.error("注入 ChenilleAuthFilterFlux Bean 失败 -> 启用 chenille.config.auth 时，你需要实现 ChenilleAuth 接口");
            throw new ChenilleChannelException("注入 ChenilleAuthFilterFlux Bean 失败 -> 启用 chenille.config.auth 时，你需要实现 ChenilleAuth 接口");
        }
        if (chenilleCheckUtils == null) {
            log.error("注入 ChenilleAuthFilterFlux Bean 失败 -> 启用 chenille.config.auth 时，需要同时启用 chenille.config.check");
            throw new ChenilleChannelException("注入 ChenilleCheckUtils Bean 失败 -> 启用 chenille.config.auth 时，需要同时启用 chenille.config.check");
        }
        if (chenilleJwtUtils == null) {
            log.error("注入 ChenilleAuthFilterFlux Bean 失败 -> 启用 chenille.config.auth 时，需要同时启用 chenille.config.jwt");
            throw new ChenilleChannelException("注入 ChenilleJwtUtils Bean 失败 -> 启用 chenille.config.auth 时，需要同时启用 chenille.config.jwt");
        }
        return new ChenilleAuthFilterServlet(
                chenilleProperties.getAuth(),
                chenilleAuthProvider,
                chenilleCheckUtils,
                chenilleJwtUtils
        );
    }

}
