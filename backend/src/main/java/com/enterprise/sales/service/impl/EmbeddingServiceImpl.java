package com.enterprise.sales.service.impl;

import com.enterprise.sales.service.EmbeddingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class EmbeddingServiceImpl implements EmbeddingService {
    
    @Value("${langchain4j.open-ai.embedding-model.base-url}")
    private String embeddingBaseUrl;
    
    @Value("${langchain4j.open-ai.embedding-model.api-key}")
    private String embeddingApiKey;
    
    @Value("${langchain4j.open-ai.embedding-model.model-name}")
    private String embeddingModelName;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public List<Float> embed(String text) {
        try {
            List<String> texts = Collections.singletonList(text);
            List<List<Float>> results = embedBatch(texts);
            return results.isEmpty() ? Collections.emptyList() : results.get(0);
        } catch (Exception e) {
            log.error("文本向量化失败: {}", e.getMessage(), e);
            throw new RuntimeException("文本向量化失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<List<Float>> embedBatch(List<String> texts) {
        try {
            String url = embeddingBaseUrl + "/embeddings";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + embeddingApiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", embeddingModelName);
            requestBody.put("input", texts);
            
            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            
            log.info("调用Embedding API, 文本数量: {}, 模型: {}", texts.size(), embeddingModelName);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                JsonNode dataArray = responseJson.get("data");
                
                List<List<Float>> embeddings = new ArrayList<>();
                for (JsonNode item : dataArray) {
                    JsonNode embeddingArray = item.get("embedding");
                    List<Float> embedding = new ArrayList<>();
                    for (JsonNode value : embeddingArray) {
                        embedding.add(value.floatValue());
                    }
                    embeddings.add(embedding);
                }
                
                log.info("向量化成功, 生成{}个向量, 维度: {}", embeddings.size(), 
                         embeddings.isEmpty() ? 0 : embeddings.get(0).size());
                return embeddings;
            } else {
                throw new RuntimeException("Embedding API返回错误: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("批量向量化失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量向量化失败: " + e.getMessage());
        }
    }
    
    @Override
    public double cosineSimilarity(List<Float> vec1, List<Float> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}