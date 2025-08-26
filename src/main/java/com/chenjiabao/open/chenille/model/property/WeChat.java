package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeChat {
    private Boolean enabled = false;
    private String appId;
    private String appSecret;
    @NestedConfigurationProperty
    private WeChatUrl url = new WeChatUrl();
    @NestedConfigurationProperty
    private WeChatPay pay = new WeChatPay();
}
