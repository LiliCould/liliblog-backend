package cn.lilicould.liliblog.util;

import cn.lilicould.liliblog.config.properties.IpTrustProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * IpUtil客户端IP工具测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
class IpUtilTest {

    @Mock
    private HttpServletRequest request;

    private IpTrustProperties ipTrustProperties;

    private IpUtil ipUtil;

    @BeforeEach
    void setUp() {
        ipTrustProperties = new IpTrustProperties();
        ipUtil = new IpUtil(ipTrustProperties);
    }

    @Test
    void getIpAddressUsesRemoteAddrWhenNotTrustedProxy() {
        when(request.getRemoteAddr()).thenReturn("203.0.113.10");

        assertEquals("203.0.113.10", ipUtil.getIpAddress(request));
    }

    @Test
    void getIpAddressFromTrustedProxyXForwardedFor() {
        ipTrustProperties.setTrustedProxies(List.of("127.0.0.1"));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.7");

        assertEquals("198.51.100.7", ipUtil.getIpAddress(request));
    }

    @Test
    void getIpAddressSkipsInvalidHeaders() {
        ipTrustProperties.setTrustedProxies(List.of("127.0.0.1"));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("X-Real-IP")).thenReturn("198.51.100.9");

        assertEquals("198.51.100.9", ipUtil.getIpAddress(request));
    }

    @Test
    void getIpAddressFallsBackToRemoteAddrWhenAllHeadersInvalid() {
        ipTrustProperties.setTrustedProxies(List.of("127.0.0.1"));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");

        assertEquals("127.0.0.1", ipUtil.getIpAddress(request));
    }

    @Test
    void getIpAddressTakesFirstIpOfCommaSeparatedList() {
        ipTrustProperties.setTrustedProxies(List.of("127.0.0.1"));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.5, 198.51.100.2");

        assertEquals("203.0.113.5", ipUtil.getIpAddress(request));
    }

    @Test
    void getUserAgentReturnsHeaderValue() {
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        assertEquals("Mozilla/5.0", ipUtil.getUserAgent(request));
    }

    @Test
    void getUserAgentReturnsUnknownWhenHeaderMissing() {
        when(request.getHeader("User-Agent")).thenReturn(null);

        assertEquals("Unknown", ipUtil.getUserAgent(request));
    }
}