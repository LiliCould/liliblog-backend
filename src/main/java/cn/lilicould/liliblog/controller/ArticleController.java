package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/article")
public class ArticleController {

    @GetMapping
    @Secured("ROLE_USER")
    public Result getArticle() {

        log.error("有权限");

        return Result.success();
    }
}
