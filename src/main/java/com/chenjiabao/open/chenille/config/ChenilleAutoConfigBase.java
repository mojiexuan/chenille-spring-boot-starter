package com.chenjiabao.open.chenille.config;

import com.chenjiabao.open.chenille.core.*;
import com.chenjiabao.open.chenille.model.property.ChenilleProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(ChenilleProperties.class)
public class ChenilleAutoConfigBase {

    @Bean
    @ConditionalOnMissingBean
    public ChenilleNumberUtils chenilleNumberUtils(){
        return new ChenilleNumberUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleObjectUtils chenilleObjectUtils(ChenilleNumberUtils chenilleNumberUtils){
        return new ChenilleObjectUtils(chenilleNumberUtils);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleWebEnvUtils chenilleWebEnvUtils(){
        return new ChenilleWebEnvUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleSensitiveWordUtils chenilleSensitiveWordUtils() {
        return ChenilleSensitiveWordUtils.builder();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleHardwareUtils chenilleHardwareUtils() {
        return new ChenilleHardwareUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenillePriceUtils chenillePriceUtils() {
        return new ChenillePriceUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleDelayedTaskExecutor chenilleDelayedTaskExecutor() {
        return new ChenilleDelayedTaskExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleSnowflakeUtils chenilleSnowflakeUtils(ChenilleProperties properties) {
        return new ChenilleSnowflakeUtils(properties.getMachine().getId());
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleTimeUtils chenilleTimeUtils(ChenilleProperties properties) {
        return new ChenilleTimeUtils(properties.getTime());
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleStringUtils chenilleStringUtils() {
        return new ChenilleStringUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleCollectionUtils collectionUtils() {
        return new ChenilleCollectionUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleRandomUtils chenilleRandomUtils() {
        return new ChenilleRandomUtils();
    }
}
