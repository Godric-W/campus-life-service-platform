package com.god.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    String uploadImage(MultipartFile file);
    List<String> uploadImages(List<MultipartFile> files);
}
