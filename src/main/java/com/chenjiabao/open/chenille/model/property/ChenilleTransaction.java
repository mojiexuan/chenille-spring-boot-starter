package com.chenjiabao.open.chenille.model.property;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleTransaction {
    /**
     * 事务配置是否开启
     */
    private boolean enabled = true;

    /**
     * 默认事务超时时间
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration defaultTimeout = Duration.ofSeconds(30);

    /**
     * 默认事务是否在提交失败时回滚
     */
    private Boolean rollbackOnCommitFailure = true;

    /**
     * 默认事务是否只读
     */
    private Boolean readOnly = false;

    /**
     * 事务健康检查配置
     */
    @NestedConfigurationProperty
    private ChenilleTransactionHealth health = new ChenilleTransactionHealth();

    /**
     * 事务监控配置
     */
    @NestedConfigurationProperty
    private ChenilleTransactionMonitoring monitoring = new ChenilleTransactionMonitoring();

    /**
     * 初始化后根据父级 enabled 状态调整子模块
     */
    @PostConstruct
    public void applyInheritance() {
        if (!enabled) {
            health.setEnabled(false);
            monitoring.setEnabled(false);
        }
    }
}
