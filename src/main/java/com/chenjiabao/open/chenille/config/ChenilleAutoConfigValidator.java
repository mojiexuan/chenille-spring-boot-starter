package com.chenjiabao.open.chenille.config;

import com.chenjiabao.open.chenille.core.ChenilleCheckUtils;
import com.chenjiabao.open.chenille.model.property.ChenilleProperties;
import com.chenjiabao.open.chenille.validator.ChenilleChinaPhoneValidator;
import com.chenjiabao.open.chenille.validator.ChenilleNumericStringValidator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConditionalOnWebApplication
@ConditionalOnClass(name = "jakarta.validation.ConstraintValidator")
@EnableConfigurationProperties(ChenilleProperties.class)
@ConditionalOnProperty(prefix = "chenille.config.check", name = "enabled", havingValue = "true")
@AutoConfigureAfter(ChenilleAutoConfigWeb.class)
public class ChenilleAutoConfigValidator {

    @Bean
    @ConditionalOnMissingBean
    public ChenilleChinaPhoneValidator chenilleChinaPhoneValidator(ChenilleCheckUtils chenilleCheckUtils) {
        return new ChenilleChinaPhoneValidator(chenilleCheckUtils);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleNumericStringValidator chenilleNumericStringValidator(ChenilleCheckUtils chenilleCheckUtils) {
        return new ChenilleNumericStringValidator(chenilleCheckUtils);
    }

}
