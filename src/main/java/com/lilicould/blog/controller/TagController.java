package com.lilicould.blog.controller;

import com.lilicould.blog.dto.TagCreateDTO;
import com.lilicould.blog.dto.TagUpdateDTO;
import com.lilicould.blog.entity.Tag;
import com.lilicould.blog.service.TagService;
import com.lilicould.blog.vo.ResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制类
 */
@RestController
@RequestMapping("/api/tag")
public class TagController {
    @Autowired
    private TagService tagService;

    /**
     * 获取所有标签
     * @return 所有标签
     */
    @GetMapping("/list")
    public ResultVO<List<Tag>> list() {
        return ResultVO.success(tagService.getAllTags());
    }

    /**
     * 添加标签
     * @param tag 标签
     * @return 添加结果
     */
    @PostMapping
    public ResultVO<Void> add(@Valid @RequestBody TagCreateDTO tag) {
        tagService.addTag(tag);
        return ResultVO.success("添加标签成功");
    }

    /**
     * 删除标签
     * @param tagName 标签名称
     * @return 删除结果
     */
    @DeleteMapping("/{tagName}")
    public ResultVO<Void> delete(@PathVariable("tagName") String tagName) {
        tagService.deleteTag(tagName);
        return ResultVO.success("删除标签成功");
    }

    /**
     * 更新标签
     * @param tag 标签
     * @return 更新结果
     */
    @PutMapping
    public ResultVO<Void> update(@Valid @RequestBody TagUpdateDTO tag) {
        tagService.updateTag(tag);
        return ResultVO.success("更新标签成功");
    }

    /**
     * 获取标签
     * @param tagName 标签名称
     * @return 标签
     */
    @GetMapping("/{tagName}")
    public ResultVO<Tag> get(@PathVariable("tagName") String tagName) {
        tagService.getTag(tagName);
        return ResultVO.success(tagService.getTag(tagName));
    }
}
