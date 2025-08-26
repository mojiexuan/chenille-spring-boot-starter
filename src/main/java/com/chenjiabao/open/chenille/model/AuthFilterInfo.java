package com.chenjiabao.open.chenille.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限过滤信息
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthFilterInfo {
    /**
     * 路径
     */
    private String path;
    /**
     * jwt令牌
     */
    private String jwtToken;
    /**
     * 负载
     */
    private String subject;
}
