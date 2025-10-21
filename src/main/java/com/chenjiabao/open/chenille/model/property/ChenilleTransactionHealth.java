package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleTransactionHealth {
    /**
     * 是否启用事务健康检查
     * 默认值：true
     */
    private boolean enabled = true;

    /**
     * 健康检查超时时间
     * 默认值：500ms
     */
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration timeout = Duration.ofMillis(500);
}
