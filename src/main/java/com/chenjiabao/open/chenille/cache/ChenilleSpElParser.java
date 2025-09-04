package com.chenjiabao.open.chenille.cache;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供 SpEL 表达式解析工具，支持缓存，提高性能
 * @author Chen Jiabao
 */
public class ChenilleSpElParser {
    // 定义一个表达式解析器
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    // 表达式缓存：method -> (keyExpression -> Expression)
    private static final Map<Method, Map<String, Expression>> CACHE = new ConcurrentHashMap<>();

    /**
     * 解析 SpEL 表达式，生成缓存 key
     * @param keyExpression SpEL 表达式
     * @param joinPoint 连接点
     * @return 解析后的 key
     */
    public static String parseKey(String keyExpression, JoinPoint joinPoint) {
        // 如果没有表达式，使用默认的 key 生成规则
        if (keyExpression == null || keyExpression.isEmpty()) {
            return generateDefaultKey(joinPoint);
        }

        // 获取当前方法的签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取该方法对应的缓存
        Map<String, Expression> methodCache =
                CACHE.computeIfAbsent(method, m -> new ConcurrentHashMap<>());

        // 如果该表达式没有缓存，则解析并存入缓存
        Expression expression = methodCache.computeIfAbsent(
                keyExpression,
                PARSER::parseExpression
        );

        // 创建 SpEL 上下文，绑定参数和目标对象
        EvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(),
                method,
                joinPoint.getArgs(),
                new DefaultParameterNameDiscoverer()
        );

        // 执行表达式，获取最终 key
        return expression.getValue(context, String.class);
    }

    /**
     * 默认 key 的生成规则：类名 + 方法名 + 参数值
     * @param joinPoint 连接点
     * @return 生成的默认 key
     */
    private static String generateDefaultKey(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 拼接 类名.方法名(
        StringBuilder sb = new StringBuilder(method.getDeclaringClass().getName())
                .append(".").append(method.getName()).append("(");

        // 拼接参数列表
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                sb.append(arg != null ? arg.toString() : "null").append(",");
            }
            // 删除最后一个逗号
            sb.setLength(sb.length() - 1);
        }
        sb.append(")");
        return sb.toString();
    }
}
