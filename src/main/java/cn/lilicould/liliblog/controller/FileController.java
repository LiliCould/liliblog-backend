package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.common.util.OssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/file")
@Tag(name = "文件接口")
public class FileController {

    private final OssUtil ossUtil;

    public FileController(OssUtil ossUtil) {
        this.ossUtil = ossUtil;
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文件",description = "文件上传接口")
    @ApiResponse(responseCode = "200",description = "响应成功，登录成功与否看响应状态码")
    public Result<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "上传类型,目前支持：cover,avatar,image,file四个值") @RequestParam(value = "type",required = false) String type
    ) throws IOException {
        if (type == null) {
            type = "file";
        }
        // 如果不是cover,avatar,image,file
        if (!type.equals("cover") && !type.equals("avatar") && !type.equals("image") && !type.equals("file")) {
            type = "file";
        }

        String url = ossUtil.uploadFile(file.getInputStream(), file.getOriginalFilename(),type);

        return Result.success(url);
    }
}
