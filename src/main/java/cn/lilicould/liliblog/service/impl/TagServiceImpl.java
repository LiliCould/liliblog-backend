package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.common.constant.OrderConstant;
import cn.lilicould.liliblog.mapper.TagMapper;
import cn.lilicould.liliblog.pojo.dto.query.TagQuery;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.dto.response.TagVO;
import cn.lilicould.liliblog.pojo.entity.Tag;
import cn.lilicould.liliblog.service.TagService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
        queryWrapper.like( tagQuery.getName() != null, Tag::getName, tagQuery.getName());

        // 查询
        Page<Tag> tagPage = page(page, queryWrapper);

        // 转换为VO
        List<TagVO> records = tagPage.getRecords().stream().map(tag -> TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .build()).toList();

        // 转换为PageInfo对象返回
        Page<TagVO> tagVOPage = Page.of(tagPage.getCurrent(), tagPage.getSize(), tagPage.getTotal());
        tagVOPage.setRecords(records);
        return PageInfo.of(tagVOPage);
    }
}




