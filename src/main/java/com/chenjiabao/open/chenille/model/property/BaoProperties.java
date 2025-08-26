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
public class BaoProperties {
    /**
     * 认证配置
     */
    @NestedConfigurationProperty
    private Auth auth = new Auth();

    // 分布式机器信息
    @NestedConfigurationProperty
    private Machine machine = new Machine();

    // 胡椒值
    @NestedConfigurationProperty
    private Hash hash = new Hash();

    // 邮件配置
    @NestedConfigurationProperty
    private Mail mail = new Mail();

    // JWT配置
    @NestedConfigurationProperty
    private Jwt jwt = new Jwt();

    // 文件上传配置
    @NestedConfigurationProperty
    private File file = new File();

    // Api配置
    @NestedConfigurationProperty
    private Api api = new Api();

    // 静态资源
    @NestedConfigurationProperty
    private Assets assets = new Assets();

    // 开放Api文档
    @NestedConfigurationProperty
    private Document docs = new Document();

    // 检查器
    @NestedConfigurationProperty
    private Check check = new Check();

    // 时间
    @NestedConfigurationProperty
    private Time time = new Time();

    // 微信
    @NestedConfigurationProperty
    private WeChat wechat = new WeChat();

    /**
     *  redis
     */
    @NestedConfigurationProperty
    private Redis redis = new Redis();
}
