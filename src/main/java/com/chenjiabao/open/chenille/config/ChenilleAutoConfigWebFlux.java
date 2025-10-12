package com.chenjiabao.open.chenille.config;

import com.chenjiabao.open.chenille.core.*;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.filter.ChenilleAuthFilterFlux;
import com.chenjiabao.open.chenille.filter.ChenilleCorsFilter;
import com.chenjiabao.open.chenille.filter.ChenilleExchangeContextFilter;
import com.chenjiabao.open.chenille.filter.ChenilleMonoErrorFilter;
import com.chenjiabao.open.chenille.handler.ChenilleResponseHandler;
import com.chenjiabao.open.chenille.handler.ChenilleVersionedRMHM;
import com.chenjiabao.open.chenille.model.property.*;
import com.chenjiabao.open.chenille.resolver.ChenilleRequestAttrParamArgumentResolver;
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
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

/**
 * @author ChenJiaBao
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = {
        "org.springframework.web.reactive.DispatcherHandler",
        "org.springframework.web.reactive.config.WebFluxConfigurer"
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableConfigurationProperties(ChenilleProperties.class)
@AutoConfigureAfter(ChenilleAutoConfigCache.class)
public class ChenilleAutoConfigWebFlux implements WebFluxConfigurer {

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new ChenilleRequestAttrParamArgumentResolver());
    }



    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnMissingBean(ChenilleResponseHandler.class)
    public ChenilleResponseHandler chenilleResponseHandler(
            ServerCodecConfigurer configurer,
            RequestedContentTypeResolver resolver,
            ReactiveAdapterRegistry registry) {
        return new ChenilleResponseHandler(
                configurer.getWriters(),
                resolver,
                registry
        );
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.auth", name = "enabled", havingValue = "true")
    public ChenilleAuthFilterFlux authFilterRegistration(
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
        return new ChenilleAuthFilterFlux(
                chenilleProperties.getAuth(),
                chenilleAuthProvider,
                chenilleCheckUtils,
                chenilleJwtUtils
        );
    }

    @Bean(name = "chenilleCorsFilter")
    @ConditionalOnMissingBean(name = "chenilleCorsFilter")
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    public ChenilleCorsFilter corsWebFilter(ChenilleProperties properties) {
        return new ChenilleCorsFilter(properties.getApi());
    }

    @Bean
    @Primary
    public RequestMappingHandlerMapping chenilleRequestMappingHandlerMapping(
            ChenilleProperties chenilleProperties) {
        ChenilleVersionedRMHM versionedRMHM = new ChenilleVersionedRMHM(chenilleProperties.getApi());
        versionedRMHM.setOrder(0);
        return versionedRMHM;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 200)
    public ChenilleExchangeContextFilter chenilleExchangeContextFilter() {
        return new ChenilleExchangeContextFilter();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public ChenilleMonoErrorFilter chenilleMonoErrorFilter() {
        return new ChenilleMonoErrorFilter();
    }

}
