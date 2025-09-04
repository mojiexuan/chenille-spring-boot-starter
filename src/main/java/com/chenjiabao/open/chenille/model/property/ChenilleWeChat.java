package com.chenjiabao.open.chenille.model.property;

import lombok.*;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleWeChat {
    private Boolean enabled = false;
    private String appId;
    private String appSecret;
    @NestedConfigurationProperty
    private ChenilleWeChatUrl url = new ChenilleWeChatUrl();
    @NestedConfigurationProperty
    private ChenilleWeChatPay pay = new ChenilleWeChatPay();
}
