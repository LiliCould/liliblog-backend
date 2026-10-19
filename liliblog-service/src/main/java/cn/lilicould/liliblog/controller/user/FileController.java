package cn.lilicould.liliblog.controller.user;

import cn.lilicould.liliblog.context.BaseContext;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.query.FileQuery;
import cn.lilicould.liliblog.request.UploadAbandonRequest;
import cn.lilicould.liliblog.request.UploadConfirmRequest;
import cn.lilicould.liliblog.request.UploadTokenRequest;
import cn.lilicould.liliblog.response.FileRecordVO;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.response.UploadTokenVO;
import cn.lilicould.liliblog.result.Result;
import cn.lilicould.liliblog.service.FileService;
import cn.lilicould.liliblog.util.OssUtil;
import cn.lilicould.liliblog.util.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final FileService fileService;

    public FileController(OssUtil ossUtil, FileService fileService) {
        this.ossUtil = ossUtil;
        this.fileService = fileService;
    }

    @PostMapping("/token")
    @Operation(summary = "申请前端直传令牌",
            description = "服务端生成对象key并签发短时效、单key、限大小/类型的上传令牌")
    public Result<UploadTokenVO> issueUploadToken(
            @RequestBody @Validated UploadTokenRequest request,
            HttpServletResponse response) {
        // 令牌属于敏感凭证，禁止任何中间层缓存
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        return Result.success(fileService.issueUploadToken(request));
    }

    @PostMapping("/complete")
    @Operation(summary = "确认前端直传完成",
            description = "服务端通过七牛stat复核对象后落库，重复调用幂等")
    public Result<FileRecordVO> completeUpload(
            @RequestBody @Validated UploadConfirmRequest request) {
        return Result.success(fileService.confirmUpload(request));
    }

    @PostMapping("/abandon")
    @Operation(summary = "放弃前端直传", description = "将待确认记录标记为失败")
    public Result<?> abandonUpload(
            @RequestBody @Validated UploadAbandonRequest request) {
        fileService.abandonUpload(request);
        return Result.success();
    }

    @GetMapping("/my")
    @Operation(summary = "我的上传记录", description = "分页查询当前用户的上传记录")
    public Result<PageInfo<FileRecordVO>> myFiles(
            @ParameterObject @Validated FileQuery query) {
        PageUtil.setDefault(query);
        query.setUserId(BaseContext.getCurrentUserId());
        return Result.success(fileService.list(query));
    }

    /**
     * 旧的后端中转上传接口，保留兼容。
     * <p>上传成功后同步写入 file_record，upload_source=2。</p>
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件（后端中转，兼容旧前端）", description = "文件上传接口")
    @ApiResponse(responseCode = "200", description = "响应成功，登录成功与否看响应状态码")
    public Result<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "上传类型,目前支持：cover,avatar,image,file四个值")
            @RequestParam(value = "type", required = false) String type
    ) {
        if (type == null) {
            type = "file";
        }
        // 如果不是cover,avatar,image,file
        if (!type.equals("cover") && !type.equals("avatar") && !type.equals("image") && !type.equals("file")) {
            type = "file";
        }

        try {
            OssUtil.OssUploadResult result = ossUtil.uploadFileWithResult(
                    file.getInputStream(), file.getOriginalFilename(), type, file.getSize());

            // 记录失败不应影响已成功的上传结果，仅记录日志
            try {
                fileService.recordProxyUpload(result.url(), result.key(),
                        file.getOriginalFilename(), type, file.getSize(), result.mimeType());
            } catch (Exception e) {
                log.error("写入文件记录失败, key={}: {}", result.key(), e.getMessage(), e);
            }

            return Result.success(result.url());
        } catch (IOException e) {
            log.error("读取上传文件失败, originalFilename={}: {}", file.getOriginalFilename(), e.getMessage(), e);
            return Result.error(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件读取异常，请稍后重试");
        }
    }
}
