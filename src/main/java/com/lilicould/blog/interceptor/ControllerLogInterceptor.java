package com.lilicould.blog.interceptor;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.Enumeration;

/**
 * 拦截器 - 控制器日志
 */
public class ControllerLogInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ControllerLogInterceptor.class);
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        startTime.set(System.currentTimeMillis());

        if (handler instanceof HandlerMethod handlerMethod) {
            String className = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            logger.debug("[Controller] 开始执行 - {}类的{}方法", className, methodName);
        }

        // 记录请求基本信息
        logRequestStart(request);

        // 记录请求头信息（DEBUG级别）
        logRequestHeaders(request);

        // 记录请求参数
        logRequestParams(request);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 可以在这里记录响应前的信息
        // 比如记录ModelAndView的内容
        if (modelAndView != null) {
            logger.debug("[HTTP] 视图信息 - 视图名: {}, Model: {}",
                    modelAndView.getViewName(), modelAndView.getModel());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long executionTime = System.currentTimeMillis() - startTime.get();
        startTime.remove();

        // 记录请求完成信息
        logRequestCompletion(request, response, executionTime, ex);
    }

    /**
     * 记录请求开始信息
     */
    private void logRequestStart(HttpServletRequest request) {
        StringBuilder logMsg = new StringBuilder();
        logMsg.append("[HTTP] 请求开始-")
                .append("请求方式:").append(request.getMethod()).append(" ")
                .append(" 请求URI:").append(request.getRequestURI());

        // 添加请求参数
        String queryString = request.getQueryString();
        if (queryString != null) {
            logMsg.append("?").append(queryString);
        }

        logMsg.append(" - IP地址:").append(getClientIP(request))
                .append(" - User-Agent: ").append(getShortUserAgent(request));

        logger.info(logMsg.toString());
    }

    /**
     * 记录请求头信息
     */
    private void logRequestHeaders(HttpServletRequest request) {
        // 如果是DEBUG级别，则记录请求头信息
        if (logger.isDebugEnabled()) {
            // 获取请求头信息
            StringBuilder headers = new StringBuilder();
            Enumeration<String> headerNames = request.getHeaderNames();

            // 如果存在请求头信息，则记录
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.append(headerName).append(": ").append(headerValue).append("; ");
            }

            if (!headers.isEmpty()) {
                logger.debug("[HTTP] 请求头 - {}", headers);
            }
        }
    }

    /**
     * 记录请求参数
     */
    private void logRequestParams(HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            StringBuilder params = new StringBuilder();
            Enumeration<String> paramNames = request.getParameterNames();

            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                String[] paramValues = request.getParameterValues(paramName);
                params.append(paramName).append("=");

                if (paramValues.length == 1) {
                    params.append(paramValues[0]);
                } else {
                    params.append(Arrays.toString(paramValues));
                }
                params.append("; ");
            }

            if (!params.isEmpty()) {
                logger.debug("[HTTP] 请求参数 - {}", params);
            }
        }
    }

    /**
     * 记录请求完成信息
     */
    private void logRequestCompletion(HttpServletRequest request, HttpServletResponse response,
                                      long executionTime, Exception ex) {
        StringBuilder logMsg = new StringBuilder();
        logMsg.append("[HTTP] 请求完成 - ")
                .append(request.getMethod()).append(" ")
                .append(request.getRequestURI())
                .append(" - 状态码: ").append(response.getStatus())
                .append(" - 耗时: ").append(executionTime).append("ms");

        // 记录响应内容类型
        String contentType = response.getContentType();
        if (contentType != null) {
            logMsg.append(" - 类型: ").append(contentType);
        }

        if (ex != null) {
            logMsg.append(" - 异常: ").append(ex.getMessage());
            logger.error(logMsg.toString(), ex);
        } else {
            if (executionTime > 1000) {
                logger.warn("{} (慢请求)", logMsg);
            } else {
                logger.info(logMsg.toString());
            }
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIP(HttpServletRequest request) {
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
    private String getShortUserAgent(HttpServletRequest request) {
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
