package com.enterprise.sales.service;

import java.io.InputStream;

/**
 * 阿里云OSS服务接口
 */
public interface OSSService {
    
    /**
     * 上传文件到OSS
     * @param fileName 文件名
     * @param inputStream 文件输入流
     * @return 文件访问URL
     */
    String uploadFile(String fileName, InputStream inputStream);
    
    /**
     * 删除OSS文件
     * @param fileName 文件名
     * @return 是否删除成功
     */
    boolean deleteFile(String fileName);
    
    /**
     * 获取文件访问URL
     * @param fileName 文件名
     * @return 文件访问URL
     */
    String getFileUrl(String fileName);
}
