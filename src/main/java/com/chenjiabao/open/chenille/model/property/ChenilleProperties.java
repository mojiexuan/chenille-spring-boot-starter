package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author ChenJiaBao
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "chenille.config")
public class ChenilleProperties {
    /**
     * 认证配置
     */
    @NestedConfigurationProperty
    private ChenilleAuth auth = new ChenilleAuth();

    /**
     * 分布式机器信息
     */
    @NestedConfigurationProperty
    private ChenilleMachine machine = new ChenilleMachine();

    /**
     * 胡椒值
     */
    @NestedConfigurationProperty
    private ChenilleHash hash = new ChenilleHash();

    /**
     * 邮件配置
     */
    @NestedConfigurationProperty
    private ChenilleMail mail = new ChenilleMail();

    /**
     * JWT配置
     */
    @NestedConfigurationProperty
    private ChenilleJwt jwt = new ChenilleJwt();

    /**
     * 文件上传配置
     */
    @NestedConfigurationProperty
    private ChenilleFile file = new ChenilleFile();

    /**
     * Api配置
     */
    @NestedConfigurationProperty
    private ChenilleApi api = new ChenilleApi();

    /**
     * 静态资源
     */
    @NestedConfigurationProperty
    private ChenilleAssets assets = new ChenilleAssets();

    /**
     * 开放Api文档
     */
    @NestedConfigurationProperty
    private ChenilleDoc docs = new ChenilleDoc();

    /**
     * 检查器
     */
    @NestedConfigurationProperty
    private ChenilleCheck check = new ChenilleCheck();

    /**
     * 时间
     */
    @NestedConfigurationProperty
    private ChenilleTime time = new ChenilleTime();

    /**
     * 微信
     */
    @NestedConfigurationProperty
    private ChenilleWeChat wechat = new ChenilleWeChat();

    /**
     * 缓存
     */
    @NestedConfigurationProperty
    private ChenilleCache cache = new ChenilleCache();

    /**
     * 线程池配置
     */
    @NestedConfigurationProperty
    private ChenilleExecutor executor = new ChenilleExecutor();

    /**
     * jackson 工具类配置
     */
    @NestedConfigurationProperty
    private ChenilleJackson jackson = new ChenilleJackson();
}
