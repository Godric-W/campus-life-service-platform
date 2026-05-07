package com.god.file.service.impl;

import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.god.common.exception.BusinessException;
import com.god.file.config.OssProperties;
import com.god.file.service.FileService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final OssProperties ossProperties;
    private OSS ossClient;

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final int MAX_BATCH_COUNT = 9;

    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
        log.info("OSS client initialized, bucket: {}", ossProperties.getBucketName());
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        validateFile(file);
        return doUpload(file);
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(400, "文件列表不能为空");
        }
        if (files.size() > MAX_BATCH_COUNT) {
            throw new BusinessException(400, "单次最多上传 " + MAX_BATCH_COUNT + " 张图片");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file);
        }
        for (MultipartFile file : files) {
            urls.add(doUpload(file));
        }
        return urls;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(400, "图片大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(400, "不支持的图片格式，仅支持 jpg/png/webp/gif");
        }
    }

    private String doUpload(MultipartFile file) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectKey = "images/" + datePath + "/" + IdUtil.fastSimpleUUID() + extension;

        try {
            ossClient.putObject(ossProperties.getBucketName(), objectKey, file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException(500, "文件上传失败");
        }

        String baseUrl = ossProperties.getBaseUrl();
        if (baseUrl != null && !baseUrl.isBlank()) {
            return baseUrl + "/" + objectKey;
        }
        return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + objectKey;
    }
}
