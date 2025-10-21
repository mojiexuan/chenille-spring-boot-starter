package com.chenjiabao.open.chenille.annotation;

import com.chenjiabao.open.chenille.validator.ChenilleNumericStringValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ChenilleNumericStringValidator.class)
@Target({
        ElementType.FIELD,          // 字段
        ElementType.PARAMETER,      // 方法参数
        ElementType.METHOD,         // 可选（用于返回值验证）
        ElementType.ANNOTATION_TYPE // 允许被其他注解组合使用
})
@Retention(RetentionPolicy.RUNTIME)
public @interface NumericString {
    String message() default "字符串格式错误";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
