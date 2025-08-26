package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证配置
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auth {

    /**
     * 认证开关
     */
    private boolean enabled = true;

    /**
     * 需要进行身份验证的路径
     */
    private List<String> includePaths = new ArrayList<>();

    /**
     * 不需要进行身份验证的路径（优先级高于includePaths）
     */
    private List<String> excludePaths = new ArrayList<>();
}
