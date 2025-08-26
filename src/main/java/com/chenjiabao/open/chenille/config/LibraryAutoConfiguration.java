package com.chenjiabao.open.chenille.config;

import com.chenjiabao.open.chenille.*;
import com.chenjiabao.open.chenille.aspect.ApiVersionAspect;
import com.chenjiabao.open.chenille.aspect.VersionedRequestMappingHandlerMapping;
import com.chenjiabao.open.chenille.common.WeChatCommon;
import com.chenjiabao.open.chenille.controller.JiaBaoDocController;
import com.chenjiabao.open.chenille.controller.BaoAssetsController;
import com.chenjiabao.open.chenille.docs.scanner.ApiScanner;
import com.chenjiabao.open.chenille.filter.AuthFilter;
import com.chenjiabao.open.chenille.filter.CorsFilter;
import com.chenjiabao.open.chenille.model.property.BaoProperties;
import com.chenjiabao.open.chenille.resolver.RequestAttributeParamArgumentResolver;
import com.chenjiabao.open.chenille.serializer.JsonRedisSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

/**
 * @author ChenJiaBao
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(BaoProperties.class)
public class LibraryAutoConfiguration implements WebMvcConfigurer {

    @Bean
    @ConditionalOnMissingBean
    public PriceUtils priceUtils(){
        return  new PriceUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.redis", name = "enabled", havingValue = "true")
    public RedisUtils redisUtils(
            StringRedisTemplate stringRedisTemplate,
            RedisTemplate<String, Object> redisTemplate){
        return new RedisUtils(stringRedisTemplate, redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.redis", name = "enabled", havingValue = "true")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JsonRedisSerializer<>(Object.class));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.redis", name = "enabled", havingValue = "true")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.redis", name = "enabled", havingValue = "true")
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.wechat", name = "enabled", havingValue = "true")
    public WeChatCommon weChatCommon(BaoProperties baoProperties){
        return new WeChatCommon(baoProperties.getWechat());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.auth", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<AuthFilter> authFilterRegistration(
            BaoProperties baoProperties,
            BaoAuth baoAuth,
            CheckUtils checkUtils,
            JwtUtils jwtUtils){
        AuthFilter authFilter = new AuthFilter(
                baoProperties.getAuth(),
                baoAuth,
                checkUtils,
                jwtUtils
        );
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(authFilter);
        registration.addUrlPatterns("/*");
        registration.setName("authFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<CorsFilter> corsFilter(BaoProperties properties) {
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        // 注册 CorsConfigFilter 过滤器
        registrationBean.setFilter(new CorsFilter(properties.getApi()));
        // 设置需要处理跨域的 URL 模式
        registrationBean.addUrlPatterns("/*");
        // 设置过滤器的执行顺序，0 是优先级最高
        registrationBean.setOrder(0);
        return registrationBean;
    }

    @Bean
    @ConditionalOnMissingBean
    public JiaBaoDocController jiaBaoDocController(){
        return new JiaBaoDocController();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiScanner apiScanner(){
        return new ApiScanner();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.assets", name = "enabled", havingValue = "true")
    public BaoAssetsController jiabaoAssetsController(BaoProperties properties) {
        return new BaoAssetsController(properties.getAssets());
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(requestAttributeParamArgumentResolver());
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestAttributeParamArgumentResolver requestAttributeParamArgumentResolver() {
        return new RequestAttributeParamArgumentResolver();
    }


    @Bean
    @ConditionalOnMissingBean
    public VersionedRequestMappingHandlerMapping versionedRequestMappingHandlerMapping() {
        VersionedRequestMappingHandlerMapping handlerMapping = new VersionedRequestMappingHandlerMapping();
        handlerMapping.setOrder(0);
        return handlerMapping;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.api", name = "enabled", havingValue = "true")
    public ApiVersionAspect apiVersionAspect(
            BaoProperties properties,
            ApplicationContext applicationContext,
            VersionedRequestMappingHandlerMapping versionedRequestMappingHandlerMapping){
        return new ApiVersionAspect(properties, applicationContext, versionedRequestMappingHandlerMapping);
    }

    @Bean
    @ConditionalOnMissingBean // 仅当不存在该类型的bean时，才会创建该bean
    @ConditionalOnProperty(prefix = "chenille.config.hash", name = "enabled", havingValue = "true")
    public HashUtils hashUtils(BaoProperties properties) {
        HashUtils hashUtils = new HashUtils();
        if (properties.getHash().getPepper() != null) {
            hashUtils.setHashPepper(properties.getHash().getPepper());
        }
        return hashUtils;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.jwt", name = "enabled", havingValue = "true")
    public JwtUtils jwtUtils(BaoProperties properties) {
        JwtUtils jwtUtils = new JwtUtils();
        if (properties.getJwt().getSecret() != null) {
            jwtUtils.setJwtSecret(properties.getJwt().getSecret());
        }
        jwtUtils.setExpires(properties.getJwt().getExpires());
        return jwtUtils;
    }

    @Bean
    @ConditionalOnMissingBean
    public DelayedTaskExecutor delayedTaskExecutor() {
        return new DelayedTaskExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public SnowflakeUtils snowflakeUtils(BaoProperties properties) {
        return new SnowflakeUtils(properties.getMachine().getId());
    }

    @Bean
    @ConditionalOnMissingBean
    public SensitiveWordUtils sensitiveWordUtils() {
        return SensitiveWordUtils.builder();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.mail", name = "enabled", havingValue = "true")
    public MailUtils.Builder mailUtilsBuilder(CheckUtils checkUtils, BaoProperties properties) {
        MailUtils.Builder builder = new MailUtils.Builder(checkUtils);
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
    public MailUtils mailUtils(MailUtils.Builder builder, BaoProperties properties) {
        if(properties.getMail().getUsername() != null){
            return builder.build().setFrom(properties.getMail().getUsername());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.check", name = "enabled", havingValue = "true")
    public CheckUtils checkUtils(BaoProperties properties) {
        return new CheckUtils(properties.getCheck());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.file", name = "enabled", havingValue = "true")
    public FilesUtils filesUtils(BaoProperties properties,
                                 TimeUtils timeUtils,
                                 StringUtils stringUtils){
        return  new FilesUtils(properties.getFile(),timeUtils,stringUtils);
    }

    @Bean
    @ConditionalOnMissingBean
    public TimeUtils timeUtils(BaoProperties properties) {
        return new TimeUtils(properties.getTime());
    }

    @Bean
    @ConditionalOnMissingBean
    public StringUtils stringUtils() {
        return new StringUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionUtils collectionUtils(){
        return new CollectionUtils();
    }

}
