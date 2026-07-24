package cn.lilicould.liliblog.controller.admin;

import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.result.Result;
import cn.lilicould.liliblog.util.PageUtil;
import cn.lilicould.liliblog.query.TagQuery;
import cn.lilicould.liliblog.request.TagCreateRequest;
import cn.lilicould.liliblog.request.TagUpdateRequest;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.response.TagVO;
import cn.lilicould.liliblog.entity.Tag;
import cn.lilicould.liliblog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tag")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理接口")
public class AdminTagController {

    private final TagService tagService;

    @GetMapping
    @Operation(summary = "分页获取标签列表")
    public Result<PageInfo<TagVO>> list(@ParameterObject TagQuery tagQuery) {

        // 设置分页默认值
        PageUtil.setDefault(tagQuery);

        // 获取标签列表
        PageInfo<TagVO> pageInfo = tagService.getTagList(tagQuery);

        return Result.success(pageInfo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取标签详情")
    public Result<TagVO> get(@Parameter(description = "标签ID", required = true) @PathVariable Long id) {

        // 查询
        Tag tag = tagService.getById(id);

        // 判空
        if (tag == null) {
            throw new BusinessException(CodeEnum.TAG_NOT_FOUND);
        }

        // 转换VO返回
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag, tagVO);
        return Result.success(tagVO);
    }

    @PostMapping
    @Operation(summary = "新增标签")
    public Result<?> save(@RequestBody @Validated TagCreateRequest tagCreateRequest) {

        tagService.save(tagCreateRequest);

        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标签")
    public Result<?> update(@Parameter(description = "标签ID", required = true) @PathVariable Long id,
                            @RequestBody @Validated TagUpdateRequest tagUpdateRequest) {

        tagService.update(id, tagUpdateRequest);

        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签", description = "根据ID删除标签，也可使用批量删除接口")
    public Result<?> deleteTag(@Parameter(description = "标签ID") @PathVariable Long id) {

        tagService.delete(id);

        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量删除标签")
    public Result<?> deleteTags(@RequestBody @Validated List<Long> ids) {

        // 批量删除
        tagService.delete(ids);

        return Result.success();
    }

}
