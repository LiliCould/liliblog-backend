package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.entity.BlogConfig;
import cn.lilicould.liliblog.response.BlogConfigSectionVO;
import cn.lilicould.liliblog.response.BlogConfigVO;
import com.baomidou.mybatisplus.extension.service.IService;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * @author Lili_Could
 * @description 针对表【blog_config(站点配置表)】的数据库操作Service
 */
public interface BlogConfigService extends IService<BlogConfig> {

    /**
     * 获取公开的站点配置（只包含已启用的分组）
     *
     * @return 组装后的站点配置
     */
    BlogConfigVO getPublicConfig();

    /**
     * 管理端：获取全部分组
     *
     * @return 分组列表
     */
    List<BlogConfigSectionVO> listSections();

    /**
     * 管理端：获取指定分组
     *
     * @param key 分组键
     * @return 分组内容
     */
    BlogConfigSectionVO getSection(String key);

    /**
     * 新增或更新一个分组
     *
     * @param key     分组键
     * @param value   分组内容
     * @param enabled 是否启用，null 表示不修改
     * @param remark  备注，null 表示不修改
     */
    void upsertSection(String key, JsonNode value, Integer enabled, String remark);

    /**
     * 删除一个分组
     *
     * @param key 分组键
     */
    void deleteSection(String key);
}
