package com.chenjiabao.open.chenille.validator;

import com.chenjiabao.open.chenille.annotation.NumericString;
import com.chenjiabao.open.chenille.core.ChenilleCheckUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 数字字符串校验器
 */
public record ChenilleNumericStringValidator(
        ChenilleCheckUtils chenilleCheckUtils) implements ConstraintValidator<NumericString, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return chenilleCheckUtils.isValidNumberString(value);
    }
}
