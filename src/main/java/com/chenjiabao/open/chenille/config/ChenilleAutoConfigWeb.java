package com.chenjiabao.open.chenille.config;

import com.chenjiabao.open.chenille.common.ChenilleWeChatCommon;
import com.chenjiabao.open.chenille.controller.ChenilleAssetsController;
import com.chenjiabao.open.chenille.core.*;
import com.chenjiabao.open.chenille.docs.config.ChenilleOpenApi;
import com.chenjiabao.open.chenille.docs.render.ChenilleApiDocRender;
import com.chenjiabao.open.chenille.docs.scanner.ChenilleApiScanner;
import com.chenjiabao.open.chenille.enums.ChenilleInternalEnum;
import com.chenjiabao.open.chenille.html.ChenilleApiDocsTemplate;
import com.chenjiabao.open.chenille.model.property.ChenilleExecutorCPU;
import com.chenjiabao.open.chenille.model.property.ChenilleExecutorIO;
import com.chenjiabao.open.chenille.model.property.ChenilleProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@ConditionalOnWebApplication
@EnableConfigurationProperties(ChenilleProperties.class)
@AutoConfigureAfter(ChenilleAutoConfigBase.class)
public class ChenilleAutoConfigWeb{

    /**
     * CPU 密集型线程池
     */
    @Bean(name = "chenilleCpuExecutor")
    @ConditionalOnMissingBean(name = "chenilleCpuExecutor")
    @ConditionalOnProperty(prefix = "chenille.config.executor.cpu", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor cpuExecutor(ChenilleProperties chenilleProperties,
                                              @Autowired(required = false) ChenilleHardwareUtils chenilleHardwareUtils) {
        if (chenilleHardwareUtils == null) {
            chenilleHardwareUtils = new ChenilleHardwareUtils();
        }

        ChenilleExecutorCPU cpu = chenilleProperties.getExecutor().getCpu();
        return chenilleHardwareUtils.buildExecutor(ChenilleInternalEnum.ExecutorType.CPU,
                cpu.getName(),
                cpu.getCorePoolSize(),
                cpu.getMaxPoolSize(),
                cpu.getQueueCapacity(),
                cpu.getRejectedPolicy());
    }

    /**
     * IO 密集型线程池
     */
    @Bean(name = "chenilleIoExecutor")
    @ConditionalOnMissingBean(name = "chenilleIoExecutor")
    @ConditionalOnProperty(prefix = "chenille.config.executor.io", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor ioExecutor(ChenilleProperties chenilleProperties,
                                             @Autowired(required = false) ChenilleHardwareUtils chenilleHardwareUtils) {
        if (chenilleHardwareUtils == null) {
            chenilleHardwareUtils = new ChenilleHardwareUtils();
        }

        ChenilleExecutorIO io = chenilleProperties.getExecutor().getIo();
        return chenilleHardwareUtils.buildExecutor(ChenilleInternalEnum.ExecutorType.IO,
                io.getName(),
                io.getCorePoolSize(),
                io.getMaxPoolSize(),
                io.getQueueCapacity(),
                io.getRejectedPolicy());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.check", name = "enabled", havingValue = "true")
    public ChenilleCheckUtils chenilleCheckUtils(ChenilleProperties properties) {
        return new ChenilleCheckUtils(properties.getCheck());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.jackson.xml", name = "enabled", havingValue = "true")
    public ChenilleXmlUtils chenilleXmlUtils() {
        return new ChenilleXmlUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.jackson.json", name = "enabled", havingValue = "true",matchIfMissing = true)
    public ChenilleJsonUtils chenilleJsonUtils() {
        return new ChenilleJsonUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.wechat", name = "enabled", havingValue = "true")
    public ChenilleWeChatCommon chenilleWeChatCommon(ChenilleProperties chenilleProperties) {
        return new ChenilleWeChatCommon(chenilleProperties.getWechat());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.docs", name = "enabled", havingValue = "true")
    public ChenilleApiDocRender chenilleApiDocRender(
            ChenilleProperties chenilleProperties,
            @Autowired(required = false) ChenilleOpenApi chenilleOpenApi){
        return new ChenilleApiDocRender(
                chenilleProperties.getDocs(),
                new ChenilleApiDocsTemplate(),
                chenilleOpenApi
        );
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.docs", name = "enabled", havingValue = "true")
    public ChenilleApiScanner chenilleApiScanner(
            @Autowired(required = false) ChenilleOpenApi chenilleOpenApi) {
        return new ChenilleApiScanner(chenilleOpenApi);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.assets", name = "enabled", havingValue = "true")
    public ChenilleAssetsController chenilleAssetsController(ChenilleProperties properties) {
        return new ChenilleAssetsController(properties.getAssets());
    }

    @Bean
    @ConditionalOnMissingBean // 仅当不存在该类型的bean时，才会创建该bean
    @ConditionalOnProperty(prefix = "chenille.config.hash", name = "enabled", havingValue = "true")
    public ChenilleHashUtils chenilleHashUtils(ChenilleProperties properties) {
        ChenilleHashUtils chenilleHashUtils = new ChenilleHashUtils();
        if (properties.getHash().getPepper() != null) {
            chenilleHashUtils.setHashPepper(properties.getHash().getPepper());
        }
        return chenilleHashUtils;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.jwt", name = "enabled", havingValue = "true")
    public ChenilleJwtUtils chenilleJwtUtils(ChenilleProperties properties,
                                             @Autowired(required = false) ChenilleJsonUtils chenilleJsonUtils) {
        ChenilleJwtUtils chenilleJwtUtils = new ChenilleJwtUtils(properties.getJwt(), chenilleJsonUtils);
        if (properties.getJwt().getSecret() != null) {
            chenilleJwtUtils.setJwtSecret(properties.getJwt().getSecret());
        }
        chenilleJwtUtils.setExpires(properties.getJwt().getExpires());
        return chenilleJwtUtils;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.mail", name = "enabled", havingValue = "true")
    public ChenilleMailUtils.Builder chenilleMailUtilsBuilder(
            ChenilleProperties properties,
            @Autowired(required = false) ChenilleCheckUtils chenilleCheckUtils) {
        if(chenilleCheckUtils == null) {
            chenilleCheckUtils = new ChenilleCheckUtils(properties.getCheck());
        }

        ChenilleMailUtils.Builder builder = new ChenilleMailUtils.Builder(chenilleCheckUtils);
        if (properties.getMail().getHost() != null) {
            builder = builder.setHost(properties.getMail().getHost());
        }
        builder = builder.setPort(properties.getMail().getPort());
        builder = builder.setSsl(properties.getMail().getSsl());
        builder = builder.setAuth(properties.getMail().getAuth());
        if (properties.getMail().getUsername() != null) {
            builder = builder.setUsername(properties.getMail().getUsername());
        }
        if (properties.getMail().getPassword() != null) {
            builder = builder.setPassword(properties.getMail().getPassword());
        }
        builder = builder.setProtocol(properties.getMail().getProtocol());

        return builder;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.mail", name = "enabled", havingValue = "true")
    public ChenilleMailUtils chenilleMailUtils(ChenilleProperties properties,
                                       ChenilleMailUtils.Builder builder) {
        if (properties.getMail().getUsername() != null) {
            return builder.build().setFrom(properties.getMail().getUsername());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(ChenilleFilesUtils.class)
    @ConditionalOnProperty(prefix = "chenille.config.file", name = "enabled", havingValue = "true")
    public ChenilleFilesUtils chenilleFilesUtils(ChenilleProperties properties,
                                         @Autowired(required = false) ChenilleTimeUtils chenilleTimeUtils,
                                         @Autowired(required = false) ChenilleRandomUtils chenilleRandomUtils) {
        if(chenilleTimeUtils == null) {
            chenilleTimeUtils = new ChenilleTimeUtils(properties.getTime());
        }
        if(chenilleRandomUtils == null) {
            chenilleRandomUtils = new ChenilleRandomUtils();
        }

        return new ChenilleFilesUtils(properties.getFile(), chenilleTimeUtils, chenilleRandomUtils);
    }

}
