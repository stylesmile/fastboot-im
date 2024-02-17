package com.bx.implatform.bean.service;

import com.bx.implatform.session.UserSession;
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
