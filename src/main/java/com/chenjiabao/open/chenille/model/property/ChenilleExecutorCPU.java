package com.chenjiabao.open.chenille.model.property;

import com.chenjiabao.open.chenille.enums.ChenilleExecutorRejectedPolicy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CPU 密集型线程池配置
 *
 * <p>用于执行计算密集型任务，线程数与 CPU 核心数相关。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleExecutorCPU {
    /**
     * 线程池是否启用
     */
    private Boolean enabled = false;
    /**
     * 线程池名称前缀
     */
    private String name = "chenille-cpu-executor-";
    /**
     * 核心线程数
     *
     * <p>线程池在空闲时保持的最少线程数量。
     * <ul>
     *   <li>CPU 密集型池：默认 ≈ {@code CPU核心数}，保证最大化利用 CPU</li>
     * </ul>
     */
    private Integer corePoolSize = null;
    /**
     * 最大线程数
     *
     * <p>线程池能够容纳的最大线程数量。
     * <ul>
     *   <li>CPU 密集型池：默认 ≈ {@code CPU核心数 + 1}，略多于核心线程数，用于突发任务</li>
     * </ul>
     */
    private Integer maxPoolSize = null;
    /**
     * 线程池队列容量
     *
     * <p>默认值会根据 JVM 最大堆内存大小推算，避免队列过大导致内存压力。
     * <ul>
     *   <li>CPU 密集型池：倾向于 <b>小队列</b>（如核心数的 2~4 倍），确保快速失败或触发拒绝策略，
     *       防止任务无限堆积。</li>
     * </ul>
     */
    private Integer queueCapacity = null;
    /**
     * 拒绝策略
     *
     * <p>当线程池无法接收新任务时的处理方式。
     * 可根据业务需要选择不同的策略：
     * <ul>
     *   <li>{@link ChenilleExecutorRejectedPolicy#ABORT} —— 直接抛出异常，适合必须执行的任务</li>
     *   <li>{@link ChenilleExecutorRejectedPolicy#CALLER_RUNS} —— 由调用者线程执行任务，适合低负载补偿</li>
     *   <li>{@link ChenilleExecutorRejectedPolicy#DISCARD_OLDEST} —— 丢弃队列中最旧的任务，适合实时性要求高的场景</li>
     *   <li>{@link ChenilleExecutorRejectedPolicy#DISCARD} —— 丢弃当前任务，适合允许部分任务丢弃的场景</li>
     * </ul>
     */
    private ChenilleExecutorRejectedPolicy rejectedPolicy = ChenilleExecutorRejectedPolicy.ABORT;
}
