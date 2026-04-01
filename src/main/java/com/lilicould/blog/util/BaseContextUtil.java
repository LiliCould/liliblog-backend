package com.lilicould.blog.util;

public class BaseContextUtil {
    private static final ThreadLocal<String> usernameThreadLocal = new ThreadLocal<>();

    public static void set(String username) {
        usernameThreadLocal.set(username);
    }

    public static String get() {
        return usernameThreadLocal.get();
    }
}
