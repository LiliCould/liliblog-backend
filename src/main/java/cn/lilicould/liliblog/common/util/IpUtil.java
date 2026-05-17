package cn.lilicould.liliblog.common.util;


import cn.lilicould.liliblog.config.properties.IpTrustProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IpUtil {
    private final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private final IpTrustProperties ipTrustProperties;

    /**
     * 获取客户端真实IP（安全版本）
     */
    public String getIpAddress(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        // 如果直连 IP 不在信任列表中，直接使用（防止伪造）
        if (!isTrustedProxy(remoteAddr)) {
            log.debug("非信任代理直连，使用 RemoteAddr: {}", remoteAddr);
            return remoteAddr;
        }

        // 从可信代理获取真实 IP
        String ip = null;
        for (String header : IP_HEADERS) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                break;
            }
        }

        // 如果 Header 中没有有效 IP，使用 RemoteAddr
        if (!isValidIp(ip)) {
            ip = remoteAddr;
        }

        // 处理多个 IP 的情况，取第一个（最接近客户端的）
        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        log.debug("获取到客户端IP: {}", ip);
        return ip;
    }

    /**
     * 检查是否为可信代理
     */
    private boolean isTrustedProxy(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        // 支持 IPv4 / IPv6
        return ipTrustProperties.getTrustedProxies().contains(ip);
    }

    /**
     * 验证IP是否有效
     */
    private boolean isValidIp(String ip) {
        return ip != null
                && !ip.isEmpty()
                && !"unknown".equalsIgnoreCase(ip)
                && !"null".equalsIgnoreCase(ip);
    }

    /**
     * 获取 User-Agent
     */
    public String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown";
    }
}
