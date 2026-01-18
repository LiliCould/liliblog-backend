package com.lilicould.blog.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {
    /**
     * 获取客户端IP地址
     */
    public static String getClientIP(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "X-Real-IP"};

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 获取简化的User-Agent信息
     */
    public static String getShortUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown";
        }

        // 简化User-Agent显示
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";
        if (userAgent.contains("Postman")) return "Postman";
        if (userAgent.contains("curl")) return "curl";

        return "Other";
    }
}
