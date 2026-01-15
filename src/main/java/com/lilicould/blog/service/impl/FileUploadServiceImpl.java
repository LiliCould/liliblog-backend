package com.lilicould.blog.service.impl;

import com.lilicould.blog.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path:classpath:static/avatar/}")
    private String uploadPath;

    // 文件大小限制（默认10MB）
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    public String upload(MultipartFile file, String type) {
        // 1. 参数校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 2. 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }

        // 3. 文件类型校验
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String newName = UUID.randomUUID() + file.getOriginalFilename();
        try {
            File target = new File(uploadPath,newName);
            file.transferTo(target);

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
        return newName;
    }
}