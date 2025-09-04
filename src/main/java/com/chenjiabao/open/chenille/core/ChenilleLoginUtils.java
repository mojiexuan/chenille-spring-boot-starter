package com.chenjiabao.open.chenille.core;

import lombok.NonNull;

/**
 * 登录工具类
 */
public record ChenilleLoginUtils(ChenilleCacheUtils chenilleCacheUtils) {

    /**
     * 登录
     *
     * @param userid 用户唯一标识
     * @return 登录凭证
     */
    @NonNull
    public String login(@NonNull String userid) {
        return "";
    }

}
