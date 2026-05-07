package com.god.file.controller;

import com.god.common.context.UserContext;
import com.god.common.exception.BusinessException;
import com.god.common.result.Result;
import com.god.common.result.ResultCode;
import com.god.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "文件上传", description = "图片上传到阿里云 OSS")
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传单张图片", description = "上传一张图片到 OSS，返回访问 URL。支持 jpg/png/webp/gif，最大 5MB。需登录")
    @PostMapping("/upload/image")
    public Result<String> uploadImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file) {
        checkLogin();
        String url = fileService.uploadImage(file);
        return Result.success(url);
    }

    @Operation(summary = "批量上传图片", description = "一次上传多张图片（最多 9 张），返回 URL 列表。需登录")
    @PostMapping("/upload/batch")
    public Result<List<String>> uploadBatch(
            @Parameter(description = "图片文件列表") @RequestParam("files") List<MultipartFile> files) {
        checkLogin();
        List<String> urls = fileService.uploadImages(files);
        return Result.success(urls);
    }

    private void checkLogin() {
        if (UserContext.getUserId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
    }
}
