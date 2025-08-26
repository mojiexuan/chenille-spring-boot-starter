package com.chenjiabao.open.chenille.aspect;

import com.chenjiabao.open.chenille.model.property.BaoProperties;
import com.chenjiabao.open.chenille.annotation.ApiVersion;
import com.chenjiabao.open.chenille.exception.ApiVersionException;
import jakarta.annotation.PostConstruct;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.util.pattern.PathPattern;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Api版本化工具，勿改
 * @author ChenJiaBao
 */
@Aspect
public class ApiVersionAspect {

    private final BaoProperties properties;
    // 依赖注入Spring应用上下文，用于获取所有控制器Bean
    private final ApplicationContext applicationContext;
    // 用于注册版本化路由
    private final VersionedRequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    public ApiVersionAspect(BaoProperties properties,
                            ApplicationContext applicationContext,
                            VersionedRequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.properties = properties;
        this.applicationContext = applicationContext;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @PostConstruct
    public void init() {
        // 获取所有带有@RestController注解的Bean
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
        // 遍历每个控制器
        controllers.forEach((name, controller) -> {
            // 获取控制器的Class对象
            Class<?> controllerClass = AopUtils.getTargetClass(controller);
            // 查找类上的@ApiVersion注解
            ApiVersion classVersion = AnnotationUtils.findAnnotation(controllerClass, ApiVersion.class);

            // 遍历控制器的所有方法
            ReflectionUtils.doWithMethods(controllerClass, method -> {
                // 检查方法是否有@RequestMapping或其派生注解（如@GetMapping）
                if (AnnotationUtils.findAnnotation(method, RequestMapping.class) != null) {
                    // 查找方法上的@ApiVersion注解
                    ApiVersion methodVersion = AnnotationUtils.findAnnotation(method, ApiVersion.class);
                    // 确定版本号：方法注解优先于类注解
                    int version = methodVersion != null ? methodVersion.value() :
                            (classVersion != null ? classVersion.value() : 0);

                    // 如果版本号有效（>0），则注册版本化路径
                    if (version > 0) {
                        registerVersionedPath(controller, method, version);
                    }
                }
            });
        });
    }

    /**
     * 注册带版本前缀的路由
     */
    private void registerVersionedPath(Object controller, Method method, int version) {
        try {

            Class<?> targetClass = AopUtils.getTargetClass(controller);
            Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);

            // 获取原始映射
            RequestMappingInfo originalMapping = requestMappingHandlerMapping
                    .getMappingForMethodPublic(specificMethod, targetClass);

            // 如果存在原始映射
            if (originalMapping != null) {
                // 创建新版路径
                if (originalMapping.getPathPatternsCondition() != null) {
                    Set<String> originalPaths = originalMapping.getPathPatternsCondition()
                            .getPatterns()
                            .stream()
                            .map(PathPattern::getPatternString)
                            .collect(Collectors.toSet());

                    // 创建新的路由映射：
                    // 1. 保留原始映射的所有条件（请求方法、参数等）
                    // 2. 修改路径：添加/**/v{version}前缀
                    String[] versionedPaths = originalPaths.stream()
                            .map(path -> {
                                // 确保路径规范化
                                if (!path.startsWith("/")) {
                                    path = "/" + path;
                                }
                                return "/" + properties.getApi().getPrefix() + "/v" + version + path;
                            })
                            .toArray(String[]::new);

                    RequestMappingInfo versionedMapping = originalMapping.mutate()
                            .paths(versionedPaths)
                            .build();

                    requestMappingHandlerMapping.registerMappingPublic(
                            versionedMapping, controller, specificMethod);

                }
            }
        } catch (Exception e) {
            // 当出现以下情况时会抛出异常：
            // 1. 控制器方法没有有效的@RequestMapping注解
            // 2. 路由注册过程中发生反射异常
            // 3. 版本号无效（≤0）
            throw new ApiVersionException("未能为 "
                    + controller.getClass().getSimpleName()
                    + " 注册版本化路径 -> "
                    + method.getName()
                    + "\n 控制器方法是否存在有效的@RequestMapping注解？"
                    + "\n 版本号是否有效（≥1的整数）？");
        }
    }
}
