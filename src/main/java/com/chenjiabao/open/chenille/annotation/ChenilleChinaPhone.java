package com.chenjiabao.open.chenille.annotation;

import com.chenjiabao.open.chenille.validator.ChenilleChinaPhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ChenilleChinaPhoneValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChenilleChinaPhone {
    String message() default "手机号格式错误";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
