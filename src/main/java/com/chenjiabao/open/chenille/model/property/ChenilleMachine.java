package com.chenjiabao.open.chenille.model.property;

import lombok.*;

/**
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleMachine {
    private boolean enabled = true;
    // 分布式机器id
    private Long id = 1L;
    // CPU核心数
    private Integer cpu = 1;
}
