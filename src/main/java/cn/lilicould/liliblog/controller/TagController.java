package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.query.TagQuery;
import cn.lilicould.liliblog.pojo.dto.request.TagCreateRequest;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.dto.response.TagVO;
import cn.lilicould.liliblog.pojo.entity.Tag;
import cn.lilicould.liliblog.service.TagService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tag")
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签接口", description = "标签接口")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取指定ID的标签", description = "这个接口对管理员和普通用户没有区分")
    public Result<TagVO> getById(@Parameter(description = "标签ID") @PathVariable Long id) {
        Tag tag = tagService.getById(id);

        if (tag == null) {
            return Result.error(CodeEnum.TAG_NOT_FOUND);
        }

        return Result.success(TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .build());
    }

    @GetMapping
    @Operation(summary = "获取标签列表", description = "这个接口对管理员和普通用户没有区分")
    public Result<PageInfo<TagVO>> getList(@ParameterObject TagQuery tagQuery) {

        // 设置默认值
        if (tagQuery.getCurrent() == null) {
            tagQuery.setCurrent(1L);
        }
        if (tagQuery.getSize() == null) {
            tagQuery.setSize(10L);
        }

        PageInfo<TagVO> pageInfo = tagService.getTagList(tagQuery);

        return Result.success(pageInfo);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "添加标签", description = "需要管理员权限")
    public Result<?> addTag(@RequestBody @Validated TagCreateRequest tagCreateRequest) {

        // 如果标签已存在
        if (tagService.exists(new LambdaQueryWrapper<Tag>().eq(Tag::getName, tagCreateRequest.getName()))) {
            return Result.error(CodeEnum.TAG_ALREADY_EXISTS);
        }
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagCreateRequest, tag);
        tagService.save(tag);
        return Result.success();
    }
}
