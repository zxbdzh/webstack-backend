package com.zxb.webstackbackend.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Author: Administrator
 * CreateTime: 2024/7/10
 * Project: webstack-backend
 */
public class FileUtil {

    public static File convertMultipartFileToFile(MultipartFile file) throws IOException {
        // 创建一个临时文件
        File tempFile = File.createTempFile("temp", null);

        // 将 MultipartFile 写入临时文件
        file.transferTo(tempFile);

        return tempFile;
    }
}