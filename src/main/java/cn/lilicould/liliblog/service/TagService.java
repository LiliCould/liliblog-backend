package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.model.dto.query.TagQuery;
import cn.lilicould.liliblog.model.dto.request.TagCreateRequest;
import cn.lilicould.liliblog.model.dto.request.TagUpdateRequest;
import cn.lilicould.liliblog.model.dto.response.PageInfo;
import cn.lilicould.liliblog.model.dto.response.TagVO;
import cn.lilicould.liliblog.model.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Lili_Could
* @description 针对表【tag(标签表)】的数据库操作Service
* @createDate 2026-05-08 16:58:41
*/
public interface TagService extends IService<Tag> {

    /**
     * 获取标签列表
     * @param tagQuery 查询参数
     * @return 标签列表
     */
    PageInfo<TagVO> getTagList(TagQuery tagQuery);

    /**
     * 新增标签
     * @param tagCreateRequest 新增参数
     */
    void save(TagCreateRequest tagCreateRequest);

    /**
     * 更新标签
     * @param id 标签ID
     * @param tagUpdateRequest 更新参数
     */
    void update(Long id, TagUpdateRequest tagUpdateRequest);


    /**
     * 删除标签
     * @param id 标签ID
     */
    void delete(Long id);

    /**
     * 批量删除标签
     * @param ids 标签ID列表
     */
    void delete(List<Long> ids);
}
