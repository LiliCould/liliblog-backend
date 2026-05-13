package cn.lilicould.liliblog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/auth")
@Tag(name = "OAuth2", description = "OAuth2 相关接口")
public class OAuth2Controller {

    @GetMapping("/login/github")
    @Operation(summary = "跳转到 GitHub 登录页面")
    public String redirectToGithubAuth() {
        return "redirect:/oauth2/authorization/github";
    }
}
