package com.chenjiabao.open.chenille.enums;

import lombok.Getter;

/**
 * 内部会用到的枚举
 */
public sealed interface ChenilleInternalEnum permits ChenilleInternalEnum.CommonKey,
        ChenilleInternalEnum.ExecutorType,ChenilleInternalEnum.UserCacheKey {
    @Getter
    enum CommonKey implements ChenilleInternalEnum{
        /**
         * 响应链中存储 ServerWebExchange 上下文
         */
        EXCHANGE_CONTEXT("CHENILLE:EXCHANGE_CONTEXT");

        private final String value;

        CommonKey(String value) {
            this.value = value;
        }
    }
    /**
     * 任务类型枚举
     */
    enum ExecutorType implements ChenilleInternalEnum {
        CPU, IO
    }

    /**
     * 用户缓存键枚举
     */
    @Getter
    enum UserCacheKey implements  ChenilleInternalEnum{
        USER_KEY_TOKEN("CHENILLE:USER:TOKEN:%s"),
        USER_KEY_INFO("CHENILLE:USER:INFO:%s");

        private final String value;

        UserCacheKey(String value) {
            this.value = value;
        }

    }
}
