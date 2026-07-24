package cn.lilicould.liliblog.filter;

import cn.lilicould.liliblog.cache.RedisHelper;
import cn.lilicould.liliblog.constant.RedisPrefixConstant;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.result.Result;
import cn.lilicould.liliblog.util.IpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * ip名单过滤器
 * @author lilicould
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IpListFilter extends OncePerRequestFilter {

    private final IpUtil ipUtil;
    private final RedisHelper redisHelper;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 获取ip
        String ipAddr = ipUtil.getIpAddress(request);

        ipAddr = sanitizeIpForRedis(ipAddr); // 如果有冒号，则替换为 -

        // 获取ip名单的计数key和名单的banner key
        String ipCountKey = RedisPrefixConstant.IP_LIST_COUNT + ipAddr;
        String ipListBannerKey = RedisPrefixConstant.IP_LIST_BANNER + ipAddr;

        // 1. 先检查黑名单
        Boolean banned = redisHelper.get(ipListBannerKey, Boolean.class);
        if (Boolean.TRUE.equals(banned)) {
            log.warn("ip：{}，被禁止访问", ipAddr);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    Result.error(CodeEnum.IP_IS_BANNER)
            ));
            return;
        }

        // 2. 原子累加（key不存在时自动创建为1）
        Long count = redisHelper.increment(ipCountKey);

        // 3. 每次都要刷新过期时间（滑动窗口3秒）
        // 注意：这里假设你的 redisHelper.expire 支持毫秒/秒，按实际封装调整
        redisHelper.expire(ipCountKey, 3, TimeUnit.SECONDS);

        // 4. 判断是否达到阈值
        if (count >= 100) {
            log.warn("ip：{}，访问频繁被封禁", ipAddr);
            redisHelper.set(ipListBannerKey, true,1000*60*60); // 1小时
            redisHelper.delete(ipCountKey);  // 清理计数
            // 返回封禁...
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(
                    Result.error(CodeEnum.IP_IS_BANNER)
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String sanitizeIpForRedis(String ip) {
        if (ip == null || ip.isBlank()) {
            return "unknown";
        }
        // 去掉 IPv6 可能带的方括号，再把冒号换成 -
        return ip.replace("[", "")
                .replace("]", "")
                .replace(":", "-");
    }
}
