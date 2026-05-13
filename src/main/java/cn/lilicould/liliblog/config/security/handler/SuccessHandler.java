package cn.lilicould.liliblog.config.security.handler;

import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.domain.security.OAuth2SecurityUser;
import cn.lilicould.liliblog.pojo.dto.response.LoginVO;
import cn.lilicould.liliblog.service.impl.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class SuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

    @Override
    @NullMarked
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {


        log.info("OAuth2 登录成功");

        OAuth2SecurityUser oauth2User = (OAuth2SecurityUser) authentication.getPrincipal();

        // 生成token返回
        LoginVO loginVO;
        if (oauth2User != null) {
            loginVO = tokenService.createLoginResponse(oauth2User.toUser(),response);
            // 返回 JSON 响应
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8);
            response.getWriter().write(objectMapper.writeValueAsString(Result.success(loginVO)));
        }
    }
}
