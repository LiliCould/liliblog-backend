package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.entity.FileRecord;
import cn.lilicould.liliblog.query.FileQuery;
import cn.lilicould.liliblog.request.UploadAbandonRequest;
import cn.lilicould.liliblog.request.UploadConfirmRequest;
import cn.lilicould.liliblog.request.UploadTokenRequest;
import cn.lilicould.liliblog.response.BackfillResultVO;
import cn.lilicould.liliblog.response.FileRecordVO;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.response.UploadTokenVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 文件上传记录 Service
 */
public interface FileService extends IService<FileRecord> {

    /**
     * 申请前端直传令牌：创建待确认记录，返回绑定单个 key 的短时效令牌
     */
    UploadTokenVO issueUploadToken(UploadTokenRequest request);

    /**
     * 确认前端直传完成：服务端通过 stat 复核对象并落库
     */
    FileRecordVO confirmUpload(UploadConfirmRequest request);

    /**
     * 主动放弃前端直传
     */
    void abandonUpload(UploadAbandonRequest request);

    /**
     * 分页查询文件记录
     */
    PageInfo<FileRecordVO> list(FileQuery query);

    /**
     * 管理员删除文件：删除对象并逻辑删除记录
     */
    void deleteFile(Long id);

    /**
     * 记录一次后端中转上传
     */
    void recordProxyUpload(String url, String key, String originalName,
                           String type, long fileSize, String mimeType);

    /**
     * 一次性回填历史文件：
     * <ul>
     *     <li>列举桶内对象并补齐 file_record</li>
     *     <li>为每个历史对象建立 LEGACY_KEEP 保护引用</li>
     *     <li>扫描文章/头像，按 URL 补建正常引用</li>
     * </ul>
     *
     * @param prefix     对象 key 前缀，空串表示全桶
     * @param dryRun     只统计不写入
     * @param maxObjects 本次最多处理的对象数
     */
    BackfillResultVO backfillLegacyFiles(String prefix, boolean dryRun, int maxObjects);
}
