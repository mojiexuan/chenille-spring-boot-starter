package com.chenjiabao.open.chenille.annotation;

import com.chenjiabao.open.chenille.validator.ChenilleChinaPhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ChenilleChinaPhoneValidator.class)
@Target({
        ElementType.FIELD,          // 字段
        ElementType.PARAMETER,      // 方法参数
        ElementType.METHOD,         // 可选（用于返回值验证）
        ElementType.ANNOTATION_TYPE // 允许被其他注解组合使用
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChinaPhone {
    String message() default "手机号格式错误";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
