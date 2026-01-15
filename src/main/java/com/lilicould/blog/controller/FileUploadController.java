package com.lilicould.blog.controller;

import com.lilicould.blog.service.FileUploadService;
import com.lilicould.blog.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/file")
public class FileUploadController {
    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 上传文件
     * @param file 文件
     * @param type 类型
     * @return 结果
     */
    @PostMapping
    public ResultVO<Void> upload(@RequestBody MultipartFile file,String type) {
        String result = fileUploadService.upload(file,type);
        return ResultVO.success(result);
    }
}
