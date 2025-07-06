package com.shirongbao.timenest.service.oss.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.shirongbao.timenest.config.OssProperties;
import com.shirongbao.timenest.service.oss.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author: ShiRongbao
 * @date: 2025-05-16
 * @description: oss服务实现类
 */
@Service("ossService")
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OssProperties ossProperties;

    private final OSS ossClient;

    @Override
    public String uploadAvatar(MultipartFile file) throws IOException {
        return uploadFile(file, ossProperties.getAvatar());
    }

    @Override
    public String uploadImageNest(MultipartFile file) throws IOException{
        return uploadFile(file, ossProperties.getNest());
    }

    // 上传文件,到指定文件夹下
    private String uploadFile(MultipartFile file, String path) throws IOException {
        String originalFilename = file.getOriginalFilename();

        // 获取文件扩展名
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 计算文件内容的 MD5
        String fileMD5 = DigestUtils.md5DigestAsHex(file.getInputStream());

        // 构造唯一文件名
        String uniqueFileName = fileMD5 + fileExtension;
        String fileName = path + uniqueFileName;

        // 检查是否已存在该文件（可选）
        if (ossClient.doesObjectExist(ossProperties.getBucketName(), fileName)) {
            return ossProperties.getDomain() + "/" + fileName; // 已存在，直接返回 URL
        }

        // 获取文件的 MIME 类型
        String contentType = file.getContentType();

        // 创建 ObjectMetadata 并设置 Content-Type
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        // 上传文件
        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(ossProperties.getBucketName(), fileName, inputStream, metadata);
        }

        // 设置 Object 为公共读
        ossClient.setObjectAcl(ossProperties.getBucketName(), fileName, CannedAccessControlList.PublicRead);

        // 返回访问 URL
        return ossProperties.getDomain() + "/" + fileName;
    }

}
