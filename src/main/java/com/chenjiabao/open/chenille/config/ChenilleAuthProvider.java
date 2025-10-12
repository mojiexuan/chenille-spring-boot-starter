package com.chenjiabao.open.chenille.config;

import com.chenjiabao.open.chenille.dto.ChenilleAuthStatus;
import com.chenjiabao.open.chenille.model.ChenilleAuthFilterInfo;

/**
 * @author ChenJiaBao
 */
public interface ChenilleAuthProvider {
    /**
     * 认证
     * @param chenilleAuthFilterInfo 认证信息
     * @return 认证状态,auth=true时认证通过
     */
    default ChenilleAuthStatus auth(ChenilleAuthFilterInfo chenilleAuthFilterInfo){
        return new ChenilleAuthStatus();
    }
}
