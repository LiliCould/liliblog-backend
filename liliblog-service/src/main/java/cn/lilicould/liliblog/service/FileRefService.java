package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.entity.FileRef;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 文件引用关系 Service
 */
public interface FileRefService extends IService<FileRef> {

    /**
     * 回填引用的结果：created=建立引用数，missing=URL 找不到文件记录数
     */
    record BackfillRefResult(int created, int missing) {
    }

    /**
     * 同步文章引用：封面 + 正文中引用的本站文件
     *
     * @param articleId  文章ID
     * @param ownerId    文章作者ID，用于限定只能引用自己上传的文件
     * @param coverImage 封面URL
     * @param content    Markdown正文
     */
    void syncArticleRefs(Long articleId, Long ownerId, String coverImage, String content);

    /**
     * 删除文章的全部文件引用
     */
    void removeArticleRefs(Long articleId);

    /**
     * 同步用户头像引用
     */
    void syncUserAvatar(Long userId, String avatarUrl);

    /**
     * 删除用户的头像引用
     */
    void removeUserRefs(Long userId);

    /**
     * 历史文件回填：不限制文件上传者，按 URL 建立文章引用。
     * <p>用于导入历史数据时，file_record.user_id 可能为空的场景。</p>
     */
    BackfillRefResult backfillArticleRefs(Long articleId, Long ownerId,
                                          String coverImage, String content);

    /**
     * 历史文件回填：不限制文件上传者，按 URL 建立头像引用。
     */
    BackfillRefResult backfillUserAvatar(Long userId, String avatarUrl);
}
