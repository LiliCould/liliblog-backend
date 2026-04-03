package com.lilicould.blog.service.impl;

import com.lilicould.blog.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Slf4j
@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    @Value("${server.base-url:https://lilicould.cn:8888}")
    private String baseUrl;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    public String upload(MultipartFile file, String type) {
        // 1. 参数校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 2. 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 10MB");
        }

        // 3. 文件类型校验
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 提取文件扩展名
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(lastDotIndex);
        }

        String newName = UUID.randomUUID() + extension;
        try {
            // 获取 JAR 包所在目录
            String jarDir = System.getProperty("user.dir");

            // 构建完整路径 - 相对于 JAR 包目录
            Path targetDir = Paths.get(jarDir, uploadPath);
            File dir = targetDir.toFile();
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    log.info("创建目录：{}", targetDir);
                }
            }

            File target = new File(dir, newName);
            file.transferTo(target);

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }

        // 返回完整的访问 URL
        return baseUrl + "/uploads/" + newName;
    }
}
