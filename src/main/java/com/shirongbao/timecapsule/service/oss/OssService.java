package com.shirongbao.timecapsule.service.oss;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author: ShiRongbao
 * @date: 2025-05-16
 * @description:
 */
public interface OssService {

    String uploadAvatar(MultipartFile file) throws IOException;

}
