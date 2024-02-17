package com.bx.api.bean.service;

import com.bx.api.domain.dto.session.UserSession;
import io.github.stylesmile.annotation.Service;

/*
 * @Author Stylesmile
 */
@Service
public class SessionService {
    public static ThreadLocal<UserSession> userSessionThreadLocal = new ThreadLocal<>();

    public static UserSession getSession() {
        return userSessionThreadLocal.get();
    }

    public static void getSession(UserSession userSession) {
        userSessionThreadLocal.set(userSession);
    }

}
