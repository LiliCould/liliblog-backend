package com.lilicould.blog.config;

import com.lilicould.blog.dao.UserMapper;
import com.lilicould.blog.entity.User;
import com.lilicould.blog.util.JwtUtil;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 配置类
 */
@Configuration
@Slf4j
public class WebSocketConfig {

    /**
     * 创建 ServerEndpointExporter 对象，用于自动注册 WebSocket 端点
     * @return ServerEndpointExporter 对象
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 创建 WebSocketHandshakeInterceptor 拦截器，用于处理 WebSocket 握手请求
     */
    @Component
    public static class WebSocketHandshakeInterceptor extends ServerEndpointConfig.Configurator {

        private static JwtUtil jwtUtil;
        private static UserMapper userMapper;

        @Autowired
        public void setJwtUtil(JwtUtil jwtUtil) {
            WebSocketHandshakeInterceptor.jwtUtil = jwtUtil;
        }

        /**
         * 修改 WebSocket 握手请求，添加用户ID属性
         * @param sec 端点配置对象
         * @param request 握手请求对象
         * @param response 握手响应对象
         */
        @Override
        public void modifyHandshake(ServerEndpointConfig sec,
                                    HandshakeRequest request,
                                    HandshakeResponse response) {

            List<String> tokens = request.getParameterMap().get("token");
            if (tokens != null && !tokens.isEmpty()) {
                String token = tokens.getFirst();

                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    User user = userMapper.selectByUsername(username);
                    Long userId = null;
                    if (user.getId() != null) {
                        userId = user.getId();
                    }

                    // 获取用户真实IP
                    String ipAddress = getRealIpAddress(request);

                    // 存入配置的用户属性中
                    sec.getUserProperties().put("userId", userId);
                    sec.getUserProperties().put("ipAddress", ipAddress);
                    log.info("WebSocket 握手成功，用户ID: {},用户ip：{}", userId, ipAddress);
                } else {
                    log.warn("WebSocket 握手失败：Token 无效");
                }
            }
        }

        /**
         * 获取用户真实IP地址
         * @param request 握手请求对象
         * @return 用户真实IP地址
         */
        private String getRealIpAddress(HandshakeRequest request) {
            Map<String, List<String>> headers = request.getHeaders();

            String ip = getHeaderFirstValue(headers, "X-Forwarded-For");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }

            ip = getHeaderFirstValue(headers, "X-Real-IP");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }

            ip = getHeaderFirstValue(headers, "Proxy-Client-IP");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }

            ip = getHeaderFirstValue(headers, "WL-Proxy-Client-IP");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }

            ip = getHeaderFirstValue(headers, "HTTP_CLIENT_IP");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }

            ip = getHeaderFirstValue(headers, "HTTP_X_FORWARDED_FOR");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }

            // 获取远程地址
            List<?> remoteAddr = request.getHeaders().get("remote-addr");
            if (remoteAddr != null) {
                if (!remoteAddr.isEmpty()) {
                    return remoteAddr.getFirst().toString();
                }
            }

            return "unknown";
        }

        /**
         * 获取请求头中指定名称的第一个值
         * @param headers 请求头
         * @param headerName 请求头名称
         * @return 请求头值
         */
        private String getHeaderFirstValue(Map<String, List<String>> headers, String headerName) {
            List<String> values = headers.get(headerName.toLowerCase());
            if (values != null && !values.isEmpty()) {
                return values.getFirst();
            }
            return null;
        }

        @Autowired
        public void setUserMapper(UserMapper userMapper) {
            this.userMapper = userMapper;
        }
    }
}
