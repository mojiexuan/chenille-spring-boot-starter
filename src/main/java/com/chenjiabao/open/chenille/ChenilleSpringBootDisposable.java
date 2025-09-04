package com.chenjiabao.open.chenille;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ChenilleSpringBootDisposable implements DisposableBean {

    @Override
    public void destroy(){
        log.info("服务已销毁");
    }
}
