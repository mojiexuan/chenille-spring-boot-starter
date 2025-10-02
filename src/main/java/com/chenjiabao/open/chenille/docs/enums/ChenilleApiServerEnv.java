package com.chenjiabao.open.chenille.docs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 接口文档 服务环境枚举类
 * @author 陈佳宝
 */
@Getter
@AllArgsConstructor
public enum ChenilleApiServerEnv {

    LOCAL_DEV("本地开发环境"),
    DEV("开发环境"),
    QA("QA / 测试环境"),
    UAT("用户验收测试环境"),
    STAGING("预发布 / 暂存环境"),
    PREPROD("准生产环境"),
    PROD("生产环境"),
    SANDBOX("沙箱 / Mock 环境"),
    DEMO("演示环境"),
    BETA("Beta 测试环境"),
    PERF("性能测试环境"),
    TRAINING("培训环境"),
    DISASTER_RECOVERY("灾备环境");

    private final String description;

}
