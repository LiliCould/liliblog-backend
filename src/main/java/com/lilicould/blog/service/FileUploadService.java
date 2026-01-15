package com.lilicould.blog.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传接口
 */
public interface FileUploadService {
    String upload(MultipartFile file,String type);
}
