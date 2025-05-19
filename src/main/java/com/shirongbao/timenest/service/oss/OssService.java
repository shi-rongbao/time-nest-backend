package com.shirongbao.timenest.service.oss;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author: ShiRongbao
 * @date: 2025-05-16
 * @description: oss服务接口
 */
public interface OssService {

    String uploadAvatar(MultipartFile file) throws IOException;

}
