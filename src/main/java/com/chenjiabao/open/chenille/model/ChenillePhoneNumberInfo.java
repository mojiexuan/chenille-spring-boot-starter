package com.chenjiabao.open.chenille.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 微信返回的手机号信息
 * @author ChenJiaBao
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChenillePhoneNumberInfo {
    // 用户绑定的手机号（国外手机号会有区号）
    private String phoneNumber;
    // 没有区号的手机号
    private String purePhoneNumber;
    // 区号
    private String countryCode;
}
