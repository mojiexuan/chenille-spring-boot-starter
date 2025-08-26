package com.chenjiabao.open.chenille.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AccessToken
 *
 * @author 陈佳宝 mail@chenjiabao.com
 * @version 1.0
 * @description TODO
 * @date 2025/1/19 20:06
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {
    private String access_token;
    private int expires_in;
}
