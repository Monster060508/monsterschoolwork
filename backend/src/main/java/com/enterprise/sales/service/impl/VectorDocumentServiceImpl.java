package com.enterprise.sales.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.sales.entity.VectorDocument;
import com.enterprise.sales.mapper.VectorDocumentMapper;
import com.enterprise.sales.service.EmbeddingService;
import com.enterprise.sales.service.VectorDocumentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorDocumentServiceImpl extends ServiceImpl<VectorDocumentMapper, VectorDocument> implements VectorDocumentService {
    
    private final VectorDocumentMapper vectorDocumentMapper;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    @Transactional
    public List<VectorDocument> storeDocumentChunks(VectorDocument document, List<String> chunks, List<List<Float>> embeddings) {
        List<VectorDocument> storedDocs = new ArrayList<>();
        
        for (int i = 0; i < chunks.size(); i++) {
            VectorDocument chunkDoc = new VectorDocument();
            chunkDoc.setTitle(document.getTitle());
            chunkDoc.setContent(chunks.get(i));
            chunkDoc.setChunkIndex(i);
            chunkDoc.setSourceDocId(document.getSourceDocId());
            chunkDoc.setMetadata(document.getMetadata());
            chunkDoc.setCreateTime(LocalDateTime.now());
            chunkDoc.setUpdateTime(LocalDateTime.now());
            
            // 将向量转换为JSON存储
            if (embeddings != null && i < embeddings.size()) {
                try {
                    chunkDoc.setEmbeddingJson(objectMapper.writeValueAsString(embeddings.get(i)));
                } catch (JsonProcessingException e) {
                    log.error("向量序列化失败", e);
                }
            }
            
            vectorDocumentMapper.insert(chunkDoc);
            storedDocs.add(chunkDoc);
        }
        
        log.info("存储文档块完成: 文档ID={}, 块数={}", document.getSourceDocId(), chunks.size());
        return storedDocs;
    }
    
    @Override
    public List<VectorDocument> semanticSearch(List<Float> queryEmbedding, int topK) {
        // 获取所有文档块
        List<VectorDocument> allDocs = vectorDocumentMapper.findAll();
        
        // 计算相似度并排序
        List<VectorDocument> results = new ArrayList<>();
        
        for (VectorDocument doc : allDocs) {
            if (doc.getEmbeddingJson() == null || doc.getEmbeddingJson().isEmpty()) {
                continue;
            }
            
            try {
                List<Float> docEmbedding = objectMapper.readValue(
                    doc.getEmbeddingJson(), 
                    new TypeReference<List<Float>>() {}
                );
                
                double similarity = embeddingService.cosineSimilarity(queryEmbedding, docEmbedding);
                doc.setSimilarityScore(similarity);
                results.add(doc);
                
            } catch (JsonProcessingException e) {
                log.warn("解析文档向量失败, docId={}", doc.getId());
            }
        }
        
        // 按相似度降序排序，取前topK个
        return results.stream()
            .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
            .limit(topK)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteBySourceDocId(Long sourceDocId) {
        List<VectorDocument> docs = vectorDocumentMapper.findBySourceDocId(sourceDocId);
        for (VectorDocument doc : docs) {
            vectorDocumentMapper.deleteById(doc.getId());
        }
        log.info("删除文档块完成: 源文档ID={}, 删除{}块", sourceDocId, docs.size());
    }
    
    @Override
    public List<VectorDocument> findBySourceDocId(Long sourceDocId) {
        return vectorDocumentMapper.findBySourceDocId(sourceDocId);
    }
    
    @Override
    public List<VectorDocument> findAll() {
        return vectorDocumentMapper.findAll();
    }
}