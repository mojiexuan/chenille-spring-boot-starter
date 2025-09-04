package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 通用线程池配置类
 *
 * <p>用于统一管理线程池的核心参数，适配不同类型的任务场景：
 * <ul>
 *   <li><b>CPU 密集型</b>：强调计算效率，倾向于少线程 + 小队列</li>
 *   <li><b>IO 密集型</b>：强调吞吐能力，倾向于多线程 + 大队列</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleExecutor {
    /**
     * CPU 密集型线程池配置
     */
    @NestedConfigurationProperty
    private ChenilleExecutorCPU cpu = new ChenilleExecutorCPU();
    /**
     * IO 密集型线程池配置
     */
    @NestedConfigurationProperty
    private ChenilleExecutorIO io = new ChenilleExecutorIO();
}
