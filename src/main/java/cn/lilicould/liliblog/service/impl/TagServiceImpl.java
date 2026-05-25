package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.common.annotation.Audit;
import cn.lilicould.liliblog.common.constant.OrderConstant;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.mapper.TagMapper;
import cn.lilicould.liliblog.model.dto.query.TagQuery;
import cn.lilicould.liliblog.model.dto.request.TagCreateRequest;
import cn.lilicould.liliblog.model.dto.request.TagUpdateRequest;
import cn.lilicould.liliblog.model.dto.response.PageInfo;
import cn.lilicould.liliblog.model.dto.response.TagVO;
import cn.lilicould.liliblog.model.entity.Tag;
import cn.lilicould.liliblog.service.TagService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author Lili_Could
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:41
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

    /**
     * 获取标签列表
     * @param tagQuery 查询参数
     * @return 标签列表
     */
    @Override
    public PageInfo<TagVO> getTagList(TagQuery tagQuery) {
        // 创建分页对象
        Page<Tag> page = Page.of(tagQuery.getCurrent(), tagQuery.getSize());
        page.setOrders(OrderItem.descs(OrderConstant.CREATE_TIME, OrderConstant.UPDATE_TIME));

        // 构建查询条件
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like( tagQuery.getName() != null, Tag::getName, tagQuery.getName())
                .ge( tagQuery.getStartTime() != null, Tag::getCreateTime, tagQuery.getStartTime())
                .le( tagQuery.getEndTime() != null, Tag::getCreateTime, tagQuery.getEndTime());

        // 查询
        Page<Tag> tagPage = page(page, queryWrapper);

        // 转换为VO
        List<TagVO> records = tagPage.getRecords().stream().map(tag -> TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .createTime(tag.getCreateTime())
                .updateTime(tag.getUpdateTime())
                .build()).toList();

        // 转换为PageInfo对象返回
        Page<TagVO> tagVOPage = Page.of(tagPage.getCurrent(), tagPage.getSize(), tagPage.getTotal());
        tagVOPage.setRecords(records);
        return PageInfo.of(tagVOPage);
    }

    /**
     * 新增标签
     * @param tagCreateRequest 新增参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    @Audit(
            module = "tag",
            operation = "CREATE",
            description = "'新增标签:' + #tagCreateRequest.getName()",
            targetType = "TAG",
            target = "#tagCreateRequest.getName()"
    )
    public void save(TagCreateRequest tagCreateRequest) {
        // 检查名称是否已存在
        if (this.exists(new LambdaQueryWrapper<Tag>().eq(Tag::getName, tagCreateRequest.getName()))) {
            throw new BusinessException(CodeEnum.TAG_ALREADY_EXISTS);
        }

        Tag tag = new Tag();
        BeanUtils.copyProperties(tagCreateRequest, tag);
        this.save(tag);
    }

    /**
     * 更新标签
     * @param id 标签ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    @Audit(
            module = "tag",
            operation = "UPDATE",
            description = "'更新标签:' + #id",
            targetType = "TAG",
            target = "#id"
    )
    public void update(Long id, TagUpdateRequest tagUpdateRequest) {
        // 如果标签不存在
        if (!this.exists(new LambdaQueryWrapper<Tag>().eq(Tag::getId, id))) {
            throw new BusinessException(CodeEnum.TAG_NOT_FOUND);
        }

        // 要修改的名称未被其他标签使用
        if (this.exists(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, tagUpdateRequest.getName())
                .ne(Tag::getId, id))
        ) {
            throw new BusinessException(CodeEnum.TAG_ALREADY_EXISTS);
        }
        // 更新
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagUpdateRequest, tag);
        tag.setId(id);
        this.updateById(tag);
    }

    /**
     * 删除标签
     * @param id 标签ID
     */
    @Override
    @Audit(
            module = "tag",
            operation = "DELETE",
            description = "'删除标签:' + #id",
            targetType = "TAG",
            target = "#id"
    )
    public void delete(Long id) {
        // 判断标签是否存在，保持代码健壮性
        if (!this.exists(new LambdaQueryWrapper<Tag>().eq(Tag::getId, id))) {
            throw new BusinessException(CodeEnum.TAG_NOT_FOUND);
        }

        this.removeById(id);
    }

    /**
     * 批量删除标签
     * @param ids 标签ID列表
     */
    @Override
    @Audit(
            module = "tag",
            operation = "DELETE",
            description = "'批量删除标签:' + #ids.size()",
            targetType = "TAG",
            target = "#ids"
    )
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(CodeEnum.PARAM_MISSING);
        }
        this.removeByIds(ids);
    }
}




