package com.chenjiabao.open.chenille.config;

import com.chenjiabao.open.chenille.model.property.ChenilleProperties;
import com.chenjiabao.open.chenille.model.property.ChenilleTransaction;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 响应式事务自动配置
 *
 * <p>基于 R2DBC 与 WebFlux 的响应式事务自动装配。
 * 支持回滚、超时、只读策略。
 * 用户无需额外配置即可获得 TransactionalOperator。</p>
 *
 * <p>配置示例：
 * <pre>
 * chenille:
 *   config:
 *     transaction:
 *       enabled: true
 *       default-timeout: 30s
 *       read-only: false
 * </pre>
 * </p>
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(ChenilleProperties.class)
@EnableTransactionManagement
@AutoConfigureAfter(ChenilleAutoConfigWebFlux.class)
@ConditionalOnProperty(prefix = "chenille.config.transaction", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChenilleAutoConfigTransaction {

    /**
     * 创建响应式事务管理器（R2DBC）
     *
     * @param connectionFactory 数据库连接工厂
     * @return ReactiveTransactionManager
     */
    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    @ConditionalOnMissingBean(ReactiveTransactionManager.class)
    public ReactiveTransactionManager chenilleReactiveTransactionManager(ConnectionFactory connectionFactory) {
        log.info("[Chenille] ▶ 初始化响应式事务管理器（R2DBC）");
        return new R2dbcTransactionManager(connectionFactory);
    }

    /**
     * 创建响应式事务操作器 TransactionalOperator
     *
     * @param manager ReactiveTransactionManager
     * @param chenilleProperties 配置属性
     * @return TransactionalOperator
     */
    @Bean
    @ConditionalOnBean(ReactiveTransactionManager.class)
    @ConditionalOnMissingBean(TransactionalOperator.class)
    public TransactionalOperator chenilleTransactionalOperator(ReactiveTransactionManager manager,
                                                               ChenilleProperties chenilleProperties) {
        ChenilleTransaction tx = chenilleProperties.getTransaction();
        if (tx == null) {
            log.warn("[Chenille] ⚠ 未检测到 chenille.config.transaction 配置，使用默认事务参数");
            tx = new ChenilleTransaction();
        }

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setReadOnly(Boolean.TRUE.equals(tx.getReadOnly()));
        def.setTimeout((int) tx.getDefaultTimeout().getSeconds());
        def.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);

        TransactionalOperator operator = TransactionalOperator.create(manager, def);

        log.info("[Chenille] ▶ 事务操作器就绪 (timeout={}s, readOnly={})",
                tx.getDefaultTimeout().getSeconds(), tx.getReadOnly());

        return operator;
    }

}
