package com.chenjiabao.open.chenille.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OpenId
 *
 * @author 陈佳宝 mail@chenjiabao.com
 * @version 1.0
 * @description TODO
 * @date 2025/1/20 12:59
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenId {
    private int errcode;
    private String openid;
    private String errmsg;
    private String session_key;
    private String unionid;
}
