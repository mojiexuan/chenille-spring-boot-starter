package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleTransactionMonitoring {
    /**
     * 是否启用事务监控
     * 默认值：true
     */
    private boolean enabled = true;

    /**
     * 监控采样率（0.0-1.0）
     * 默认值：1.0（全部采样）
     */
    private double sampleRate = 1.0;
}
