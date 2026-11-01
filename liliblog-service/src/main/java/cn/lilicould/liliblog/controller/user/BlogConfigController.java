package cn.lilicould.liliblog.controller.user;

import cn.lilicould.liliblog.response.BlogConfigVO;
import cn.lilicould.liliblog.result.Result;
import cn.lilicould.liliblog.service.BlogConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站点公开配置接口
 */
@RestController
@RequestMapping("/api/blog-config")
@RequiredArgsConstructor
@Tag(name = "站点配置接口")
public class BlogConfigController {

    private final BlogConfigService blogConfigService;

    @GetMapping
    @Operation(summary = "获取站点公开配置", description = "首页 Hero、关于作者以及后续扩展的分组配置")
    public Result<BlogConfigVO> getBlogConfig() {
        return Result.success(blogConfigService.getPublicConfig());
    }
}
