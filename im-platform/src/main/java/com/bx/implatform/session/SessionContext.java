package com.bx.implatform.session;

/*
 * @Author Stylesmile
 */
public class SessionContext {
    public static ThreadLocal<UserSession> userSessionThreadLocal = new ThreadLocal<>();

    public static UserSession getSession() {
        return userSessionThreadLocal.get();
    }

    public static void getSession(UserSession userSession) {
        userSessionThreadLocal.set(userSession);
    }

}
