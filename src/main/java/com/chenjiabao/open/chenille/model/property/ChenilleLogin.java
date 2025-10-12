package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录配置
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleLogin {
    private boolean enabled = false;
    /**
     * 登录过期时间（秒）
     */
    private long expire = 7200;
}
