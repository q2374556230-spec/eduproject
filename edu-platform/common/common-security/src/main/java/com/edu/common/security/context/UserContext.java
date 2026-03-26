package com.edu.common.security.context;

import com.edu.common.security.model.LoginUser;

/**
 * 用户上下文 - 利用 ThreadLocal 存储当前请求的用户信息
 * 由网关解析 JWT 后将用户信息放入 Header，各服务从 Header 中取出并存入此上下文
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {}

    public static void setCurrentUser(LoginUser user) {
        USER_HOLDER.set(user);
    }

    public static LoginUser getCurrentUser() {
        return USER_HOLDER.get();
    }

    public static Long getCurrentUserId() {
        LoginUser user = USER_HOLDER.get();
        return user != null ? user.getUserId() : null;
    }

    public static String getCurrentUsername() {
        LoginUser user = USER_HOLDER.get();
        return user != null ? user.getUsername() : null;
    }

    public static String getCurrentRole() {
        LoginUser user = USER_HOLDER.get();
        return user != null ? user.getRole() : null;
    }

    /** 判断当前用户是否是管理员 */
    public static boolean isAdmin() {
        return "admin".equals(getCurrentRole());
    }

    /** 请求结束后清理，防止内存泄漏 */
    public static void clear() {
        USER_HOLDER.remove();
    }
}
