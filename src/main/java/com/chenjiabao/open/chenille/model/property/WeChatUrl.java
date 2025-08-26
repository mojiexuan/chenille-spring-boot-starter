package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeChatUrl {
    private String accessToken = "https://api.weixin.qq.com/cgi-bin/token";
    private String openId = "https://api.weixin.qq.com/sns/jscode2session";
    private String phoneNumber = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";
    private String qrCode = "https://api.weixin.qq.com/wxa/getwxacodeunlimit";
}
