package com.chenjiabao.open.chenille.docs.config;

import com.chenjiabao.open.chenille.docs.enums.ChenilleApiServerEnv;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChenilleOpenApiServer {
    private ChenilleApiServerEnv serverEnv;
    private String url;
}
