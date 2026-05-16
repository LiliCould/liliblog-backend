package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.pojo.dto.query.TagQuery;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.dto.response.TagVO;
import cn.lilicould.liliblog.pojo.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
