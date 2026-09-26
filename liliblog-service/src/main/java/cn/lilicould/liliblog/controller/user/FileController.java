package cn.lilicould.liliblog.controller.user;

import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.result.Result;
import cn.lilicould.liliblog.util.OssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
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
            @Parameter(description = "上传类型,目前支持：cover,avatar,image,file四个值")
            @RequestParam(value = "type",required = false) String type
    ) {
        if (type == null) {
            type = "file";
        }
        // 如果不是cover,avatar,image,file
        if (!type.equals("cover") && !type.equals("avatar") && !type.equals("image") && !type.equals("file")) {
            type = "file";
        }

        try {
            String url = ossUtil.uploadFile(
                    file.getInputStream(), file.getOriginalFilename(), type, file.getSize());
            return Result.success(url);
        } catch (IOException e) {
            log.error("读取上传文件失败, originalFilename={}: {}", file.getOriginalFilename(), e.getMessage(), e);
            return Result.error(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件读取异常，请稍后重试");
        }
    }
}
