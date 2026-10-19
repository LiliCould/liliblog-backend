package cn.lilicould.liliblog.controller.admin;

import cn.lilicould.liliblog.query.FileQuery;
import cn.lilicould.liliblog.response.FileRecordVO;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.result.Result;
import cn.lilicould.liliblog.service.FileService;
import cn.lilicould.liliblog.util.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/file")
@Tag(name = "后台-文件管理")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFileController {

    private final FileService fileService;

    @GetMapping
    @Operation(summary = "分页文件记录", description = "管理员查询所有上传记录")
    public Result<PageInfo<FileRecordVO>> list(
            @ParameterObject @Validated FileQuery query) {
        PageUtil.setDefault(query);
        return Result.success(fileService.list(query));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文件", description = "删除七牛对象并逻辑删除文件记录")
    public Result<?> delete(
            @Parameter(description = "文件记录ID", example = "1") @PathVariable Long id) {
        fileService.deleteFile(id);
        return Result.success();
    }
}
