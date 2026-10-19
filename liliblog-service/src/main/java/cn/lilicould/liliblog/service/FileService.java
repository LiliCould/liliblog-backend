package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.entity.FileRecord;
import cn.lilicould.liliblog.query.FileQuery;
import cn.lilicould.liliblog.request.UploadAbandonRequest;
import cn.lilicould.liliblog.request.UploadConfirmRequest;
import cn.lilicould.liliblog.request.UploadTokenRequest;
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
}
