package com.chenjiabao.open.chenille.validator;

import com.chenjiabao.open.chenille.annotation.ChenilleChinaPhone;
import com.chenjiabao.open.chenille.core.ChenilleCheckUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 中国手机号校验器
 */
public record ChenilleChinaPhoneValidator(
        ChenilleCheckUtils chenilleCheckUtils)
        implements ConstraintValidator<ChenilleChinaPhone, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return chenilleCheckUtils.isValidChinaPhoneNumber(value);
    }
}
