package cn.lilicould.liliblog.controller.admin;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.common.util.PageUtil;
import cn.lilicould.liliblog.model.dto.query.TagQuery;
import cn.lilicould.liliblog.model.dto.request.TagCreateRequest;
import cn.lilicould.liliblog.model.dto.request.TagUpdateRequest;
import cn.lilicould.liliblog.model.dto.response.PageInfo;
import cn.lilicould.liliblog.model.dto.response.TagVO;
import cn.lilicould.liliblog.model.entity.Tag;
import cn.lilicould.liliblog.service.TagService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    public Result<?> save(@RequestBody @Validated TagCreateRequest tagUpdateRequest) {

        // 检查名称是否已存在
        if (tagService.exists(new LambdaQueryWrapper<Tag>().eq(Tag::getName, tagUpdateRequest.getName()))) {
            throw new BusinessException(CodeEnum.TAG_ALREADY_EXISTS);
        }

        Tag tag = new Tag();
        BeanUtils.copyProperties(tagUpdateRequest, tag);
        tagService.save(tag);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标签")
    public Result<?> update(@Parameter(description = "标签ID", required = true) @PathVariable Long id,
                            @RequestBody @Validated TagUpdateRequest tagUpdateRequest) {
        // 如果标签不存在
        if (!tagService.exists(new LambdaQueryWrapper<Tag>().eq(Tag::getId, id))) {
            throw new BusinessException(CodeEnum.TAG_NOT_FOUND);
        }

        // 要修改的名称未被其他标签使用
        if (tagService.exists(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, tagUpdateRequest.getName())
                .ne(Tag::getId, id))
        ) {
            throw new BusinessException(CodeEnum.TAG_ALREADY_EXISTS);
        }
        // 更新
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagUpdateRequest, tag);
        tag.setId(id);
        tagService.updateById(tag);

        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签", description = "根据ID删除标签，也可使用批量删除接口")
    public Result<?> deleteTag(@Parameter(description = "标签ID") @PathVariable Long id) {
        // 判断标签是否存在，保持代码健壮性
        if (!tagService.exists(new LambdaQueryWrapper<Tag>().eq(Tag::getId, id))) {
            throw new BusinessException(CodeEnum.TAG_NOT_FOUND);
        }

        tagService.removeById(id);

        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量删除标签")
    public Result<?> deleteTags(@RequestBody @Validated List<Long> ids) {

        // 批量删除
        tagService.removeByIds(ids);

        return Result.success();
    }

}
