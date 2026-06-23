package com.enterprise.sales.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.enterprise.sales.service.OSSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.InputStream;

@Slf4j
@Service
public class OSSServiceImpl implements OSSService {
    
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    
    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;
    
    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;
    
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;
    
    private OSS ossClient;
    
    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
    
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
    
    @Override
    public String uploadFile(String fileName, InputStream inputStream) {
        try {
            ossClient.putObject(bucketName, fileName, inputStream);
            
            // 构建文件访问URL
            String url = "https://" + bucketName + "." + endpoint.replace("https://", "") + "/" + fileName;
            
            log.info("文件上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteFile(String fileName) {
        try {
            ossClient.deleteObject(bucketName, fileName);
            log.info("文件删除成功: {}", fileName);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return false;
        }
    }
    
    @Override
    public String getFileUrl(String fileName) {
        return "https://" + bucketName + "." + endpoint.replace("https://", "") + "/" + fileName;
    }
}
