package com.chenjiabao.open.chenille.handler;

import com.chenjiabao.open.chenille.annotation.ChenilleApiVersion;
import com.chenjiabao.open.chenille.model.property.ChenilleApi;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import java.lang.reflect.Method;

@Slf4j
public class ChenilleVersionedRMHM extends RequestMappingHandlerMapping {
    private final ChenilleApi chenilleApi;

    public ChenilleVersionedRMHM(ChenilleApi chenilleApi) {
        this.chenilleApi = chenilleApi;
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(@NonNull Method method,
                                                     @NonNull Class<?> handlerType) {

        RequestMappingInfo mappingInfo = super.getMappingForMethod(method, handlerType);
        if (mappingInfo == null) return null;

        ChenilleApiVersion methodVersion = method.getAnnotation(ChenilleApiVersion.class);
        ChenilleApiVersion classVersion = handlerType.getAnnotation(ChenilleApiVersion.class);
        int version = (methodVersion != null) ? methodVersion.value() :
                (classVersion != null) ? classVersion.value() : 1;

        String versionPrefix = "/" + chenilleApi.getPrefix() + "/v" + version;

        // 给原有 mapping 添加前缀
        RequestMappingInfo versionMapping = RequestMappingInfo
                .paths(versionPrefix)
                .build();

        return versionMapping.combine(mappingInfo);
    }
}
