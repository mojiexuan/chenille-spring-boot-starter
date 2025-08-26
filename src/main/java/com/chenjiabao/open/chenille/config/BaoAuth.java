package com.chenjiabao.open.chenille.config;

import com.chenjiabao.open.chenille.dto.AuthStatus;
import com.chenjiabao.open.chenille.model.AuthFilterInfo;

/**
 * @author ChenJiaBao
 */
public interface BaoAuth {
    /**
     * 认证
     * @param authFilterInfo 认证信息
     * @return 认证状态,auth=true时认证通过
     */
    default AuthStatus auth(AuthFilterInfo authFilterInfo){
        return new AuthStatus();
    }
}
