package com.chenjiabao.open.chenille.docs.scanner;

import com.chenjiabao.open.chenille.annotation.ChenilleApiVersion;
import com.chenjiabao.open.chenille.docs.annotation.Operation;
import com.chenjiabao.open.chenille.docs.annotation.Parameter;
import com.chenjiabao.open.chenille.docs.annotation.Tag;
import com.chenjiabao.open.chenille.docs.config.ChenilleOpenApi;
import com.chenjiabao.open.chenille.docs.model.ChenilleApiDefinition;
import com.chenjiabao.open.chenille.docs.model.ChenilleApiGroup;
import com.chenjiabao.open.chenille.docs.model.ChenilleApiParameter;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 接口扫描器
 *
 * @author ChenJiaBao
 */
public class ChenilleApiScanner implements ApplicationListener<ApplicationReadyEvent> {

    private final Logger log = LoggerFactory.getLogger(ChenilleApiScanner.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ChenilleOpenApi chenilleOpenApi;
    @Getter
    private static volatile List<ChenilleApiGroup> chenilleApiGroups = Collections.emptyList();

    private static final List<Class<? extends Annotation>> HTTP_METHOD_ANNOTATIONS = Arrays.asList(
            RequestMapping.class,
            GetMapping.class,
            PostMapping.class,
            PutMapping.class,
            DeleteMapping.class
//            PatchMapping.class
    );

    private static final Set<Class<?>> WRAPPER_TYPES = new HashSet<>(Arrays.asList(
            Integer.class, Long.class, Double.class, Float.class,
            Boolean.class, Character.class, Byte.class, Short.class
    ));

    public ChenilleApiScanner(@Autowired(required = false) ChenilleOpenApi chenilleOpenApi) {
        this.chenilleOpenApi = chenilleOpenApi;
    }

    public static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    @Override
    public void onApplicationEvent(@Nonnull ApplicationReadyEvent event) {
        // 应用启动完成后启动扫描线程
        executor.submit(()->{
            log.info("接口文档：开始扫描接口");
            try {
                chenilleApiGroups = scanControllers(event.getApplicationContext());
                if(chenilleOpenApi != null){
                    ChenilleApiGroup overview = new ChenilleApiGroup();
                    overview.setName("概览");
                    // 概览接口
                    ChenilleApiDefinition overviewApi = new ChenilleApiDefinition();
                    overviewApi.setPath("/");
                    overviewApi.setSummary("概览");
                    overviewApi.setUrlCode("/");
                    overview.addApi(overviewApi);
                    chenilleApiGroups.addFirst(overview);
                }
            } catch (RuntimeException e) {
                log.error("接口文档：扫描接口失败", e);
            }
            log.info("接口文档：扫描接口完成");
        });
    }

    /**
     * 扫描控制器
     *
     * @param context 应用上下文
     * @return 接口组列表
     */
    private List<ChenilleApiGroup> scanControllers(ApplicationContext context) {
        List<ChenilleApiGroup> result = new ArrayList<>();
        // 获取所有RestController
        Map<String, Object> controllers = context.getBeansWithAnnotation(RestController.class);
        controllers.values().forEach(controller -> {
            // 类
            Class<?> clazz = controller.getClass();
            ChenilleApiGroup chenilleApiGroup = resolveGroup(clazz);
            if(clazz.isAnnotationPresent(ChenilleApiVersion.class)){
                ChenilleApiVersion chenilleApiVersion = clazz.getAnnotation(ChenilleApiVersion.class);
                if(chenilleApiVersion.value() > 0){
                    chenilleApiGroup.setVersion(String.valueOf(chenilleApiVersion.value()));
                }
            }
            // 扫描方法
            Arrays.stream(clazz.getDeclaredMethods())
                    .filter(method -> HTTP_METHOD_ANNOTATIONS.stream()
                            .anyMatch(method::isAnnotationPresent))
                    .forEach(method -> {
                        ChenilleApiDefinition chenilleApiDefinition = parseApiDefinition(method);
                        chenilleApiGroup.addApi(chenilleApiDefinition);
                        if(method.isAnnotationPresent(ChenilleApiVersion.class)){
                            ChenilleApiVersion chenilleApiVersion = clazz.getAnnotation(ChenilleApiVersion.class);
                            if(chenilleApiVersion.value() > 0){
                                chenilleApiDefinition.setVersion(String.valueOf(chenilleApiVersion.value()));
                            }else {
                                chenilleApiDefinition.setVersion(chenilleApiGroup.getVersion());
                            }
                        }
                    });
            result.add(chenilleApiGroup);
        });

        return result;
    }

    /**
     * 解析分组信息
     * @param clazz 类
     * @return 分组名称
     */
    private ChenilleApiGroup resolveGroup(Class<?> clazz) {
        ChenilleApiGroup chenilleApiGroup = new ChenilleApiGroup();
        //  优先检查是否有自定义注解
        if (clazz.isAnnotationPresent(Tag.class)) {
            Tag tag = clazz.getAnnotation(Tag.class);
            chenilleApiGroup.setName(tag.name());
            chenilleApiGroup.setDescription(tag.description());
        }else {
            // 获取类的简单名称（不含包名）
            String simpleName = clazz.getSimpleName();
            if (simpleName.endsWith("Controller")) {
                chenilleApiGroup.setName(simpleName.substring(0,simpleName.length() - "Controller".length()));
            }
            chenilleApiGroup.setName(simpleName);
        }

        // 检查是否有 @RequestMapping 的 value
        if (clazz.isAnnotationPresent(RequestMapping.class)) {
            String[] values = clazz.getAnnotation(RequestMapping.class).value();
            if (values.length > 0 && !values[0].isEmpty()) {
                chenilleApiGroup.setPath(values[0]);
            }
        }

        return chenilleApiGroup;
    }

    /**
     * 解析接口定义
     */
    private ChenilleApiDefinition parseApiDefinition(Method method){
        // 解析方法或参数
        ChenilleApiDefinition chenilleApiDefinition = resolveHttpMethod(method);
        // 检查并处理Operation注解
        Operation operation = AnnotationUtils.findAnnotation(method, Operation.class);
        if (operation != null) {
            chenilleApiDefinition.setSummary(operation.summary());
            chenilleApiDefinition.setDescription(operation.description());
            chenilleApiDefinition.setDeprecated(operation.deprecated());
        }

        chenilleApiDefinition.setSummary(method.getName());

        // 解析参数
        parseParameters(chenilleApiDefinition,method);

        return chenilleApiDefinition;
    }

    /**
     * 解析方法的HTTP请求方法及接口路径
     * @param method 控制器方法
     * @return ApiDefinition
     */
    private ChenilleApiDefinition resolveHttpMethod(Method method) {
        ChenilleApiDefinition chenilleApiDefinition = new ChenilleApiDefinition();
        String[] paths = {"*"};
        chenilleApiDefinition.setMethod("*");
        if (method.isAnnotationPresent(GetMapping.class)) {
            chenilleApiDefinition.setMethod("GET");
            paths = method.getAnnotation(GetMapping.class).value();
        }
        if (method.isAnnotationPresent(PostMapping.class)) {
            chenilleApiDefinition.setMethod("POST");
            paths = method.getAnnotation(PostMapping.class).value();
        }
        if (method.isAnnotationPresent(PutMapping.class)) {
            chenilleApiDefinition.setMethod("PUT");
            paths = method.getAnnotation(PutMapping.class).value();
        }
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            chenilleApiDefinition.setMethod("DELETE");
            paths = method.getAnnotation(DeleteMapping.class).value();
        }
        if (method.isAnnotationPresent(PatchMapping.class)) {
            chenilleApiDefinition.setMethod("PATCH");
            paths = method.getAnnotation(PatchMapping.class).value();
        }
        // 处理@RequestMapping
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            if (requestMapping.method().length > 0) {
                // 从@RequestMapping的method属性获取
                chenilleApiDefinition.setMethod(requestMapping.method()[0].name().toUpperCase());
            }
            chenilleApiDefinition.setMethod("GET");
            paths = method.getAnnotation(RequestMapping.class).value();
        }
        chenilleApiDefinition.setPath(paths.length > 0 ? paths[0] : "");
        return chenilleApiDefinition;
    }

    /**
     * 解析参数
     * @param method 方法
     */
    private void parseParameters(ChenilleApiDefinition chenilleApiDefinition, Method method) {
        Arrays.stream(method.getParameters())
                .filter(this::shouldIncludeParameter)
                .forEach(parameter -> {
                    if(parameter.isAnnotationPresent(RequestBody.class)){
                        ChenilleApiParameter apiParam = parseBodyParameter(parameter.getType());
                        if(parameter.isAnnotationPresent(Parameter.class)){
                            Parameter param = parameter.getAnnotation(Parameter.class);
                            if(apiParam.getName() == null){
                                apiParam.setName(param.name());
                            }
                            if(apiParam.getDescription() == null){
                                apiParam.setDescription(param.description());
                            }
                            apiParam.setRequired(param.required());
                        }
                        chenilleApiDefinition.addBodyParam(apiParam);
                    }else {
                        ChenilleApiParameter apiParam = parseParameter(parameter);
                        if("query".equals(apiParam.getIn())){
                            chenilleApiDefinition.addQueryParam(apiParam);
                        }else if("path".equals(apiParam.getIn())){
                            chenilleApiDefinition.addPathParam(apiParam);
                        }
                    }
                });
    }

    /**
     * 排除特定类型参数
     * @param parameter 参数
     */
    private boolean shouldIncludeParameter(java.lang.reflect.Parameter parameter) {
        // 排除特定类型的参数
        return !parameter.getType().equals(ServerWebExchange.class);
    }

    /**
     * 解析单个参数
     * @param parameter 参数
     * @return ApiParameter
     */
    private ChenilleApiParameter parseParameter(java.lang.reflect.Parameter parameter) {
        ChenilleApiParameter apiParam = new ChenilleApiParameter();
        apiParam.setIn("query");

        // 检查@Parameter注解
        if(parameter.isAnnotationPresent(Parameter.class)){
            Parameter param = parameter.getAnnotation(Parameter.class);
            apiParam.setName(param.name());
            apiParam.setDescription(param.description());
            apiParam.setRequired(param.required());
        }
        // 检查@PathVariable注解
        if (parameter.isAnnotationPresent(PathVariable.class)) {
            PathVariable ann = parameter.getAnnotation(PathVariable.class);
            if (!ann.value().isEmpty()) {
                if(apiParam.getName() == null){
                    apiParam.setName(ann.value());
                }
            }
            apiParam.setIn("path");
        }

        // @NotNull
        if (parameter.isAnnotationPresent(Nonnull.class)) {
            apiParam.setRequired(true);
        }

        // 类型
        apiParam.setType(parameter.getType().getSimpleName());
        if(apiParam.getName() == null){
            // 使用参数实际名称（需要编译时保留参数名）
            if (parameter.isNamePresent()) {
                apiParam.setName(parameter.getName());
            }
        }

        return apiParam;
    }

    /**
     * 解析请求体参数
     * @param type 类
     * @return List<ApiParameter>
     */
    private ChenilleApiParameter parseBodyParameter(Class<?> type) {
        ChenilleApiParameter parameter = new ChenilleApiParameter();
        parameter.setIn("body");

        // 基础类型
        if (type.isPrimitive() || isWrapperType(type)) {
            parameter.setType(type.getTypeName());
        }

        // 集合类型
        if (Collection.class.isAssignableFrom(type)) {
            return parseCollectionSchema(type);
        }

        // 数组类型
        if (type.isArray()) {
            parameter.setType(getTypeName(type.getComponentType()) + "[]");
            return parameter;
        }

        // 复杂对象
        return parseComplexSchema(type);
    }

    /**
     * 获取类型的规范化名称
     * @param type Java类型
     * @return 类型名称字符串
     */
    public String getTypeName(Class<?> type) {
        if (type == null) {
            return "object";
        }
        // 处理基本类型
        if (type.isPrimitive()) {
            return type.getSimpleName();
        }

        // 处理包装类型
        if (isWrapperType(type)) {
            return type.getSimpleName().toLowerCase();
        }

        // 处理数组
        if (type.isArray()) {
            return getTypeName(type.getComponentType()) + "[]";
        }

        // 默认返回简单类名
        return type.getSimpleName();
    }

    /**
     * 解析集合类型Schema
     * @param collectionType 集合类型
     * @return Schema对象
     */
    private ChenilleApiParameter parseCollectionSchema(Class<?> collectionType) {
        ChenilleApiParameter chenilleApiParameter = new ChenilleApiParameter();
        chenilleApiParameter.setType("array");

        // 获取集合元素的类型，并递归解析
        Type genericType = collectionType.getGenericSuperclass();
        if (genericType instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            if (typeArguments.length > 0) {
                Class<?> elementType = (Class<?>) typeArguments[0];
                chenilleApiParameter.addProperty("items",parseBodyParameter(elementType));
            }
        }

        return chenilleApiParameter;
    }

    /**
     * 解析请求体的复杂对象
     * @param type class
     * @return ApiParameter
     */
    private ChenilleApiParameter parseComplexSchema(Class<?> type){
        ChenilleApiParameter parameter = new ChenilleApiParameter();
        Arrays.stream(type.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .forEach(field -> {
                    ChenilleApiParameter fieldParameter = this.parseBodyParameter(field.getType());
                    parameter.addProperty(field.getName(),fieldParameter);
                });
        return parameter;
    }

}
