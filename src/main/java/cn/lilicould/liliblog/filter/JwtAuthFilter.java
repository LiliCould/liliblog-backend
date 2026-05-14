package cn.lilicould.liliblog.filter;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.common.util.JwtUtil;
import cn.lilicould.liliblog.domain.security.SecurityUser;
import cn.lilicould.liliblog.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 认证过滤器
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 获取请求头中的 Authorization 字段
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // 如果没有 Token 或格式不对，直接放行（让后续逻辑处理未认证的情况）
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取 Token（去掉 "Bearer " 前缀）
        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 从 Token 中解析用户名
            String username = jwtUtil.extractUsername(token);

            // 如果当前 SecurityContext 没有认证信息，则进行认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // todo 应该从token中解析而不是每次都从数据库中查
                SecurityUser user = (SecurityUser) userService.loadUserByUsername(username);

                // 验证 Token 有效性并设置认证信息
                if (jwtUtil.validateToken(token, username) && user.isEnabled()) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null, // 凭证：传统模式是密码。由于使用jwt，所以这里设置为空
                            user.getAuthorities()
                    );
                    // 将信息存到Security上下文中
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            log.error("JWT 认证失败", e);

            /*
                不是认证接口且jwt验证失败的直接返回jwt已经过期
                前端收到这个的时候应该立刻去获取新的访问令牌
             */
            if (!request.getRequestURI().startsWith("/auth")) {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8);
                response.getWriter().write(objectMapper.writeValueAsString(Result.error(CodeEnum.TOKEN_EXPIRED)));
                return;
            }
        }

        // 继续执行后续过滤器链
        filterChain.doFilter(request, response);
    }
}
