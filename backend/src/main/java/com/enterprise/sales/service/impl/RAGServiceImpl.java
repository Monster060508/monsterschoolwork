package com.enterprise.sales.service.impl;

import com.enterprise.sales.entity.VectorDocument;
import com.enterprise.sales.service.*;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RAGServiceImpl implements RAGService {
    
    private final EmbeddingService embeddingService;
    private final DocumentChunkService documentChunkService;
    private final VectorDocumentService vectorDocumentService;
    private final ChatLanguageModel chatModel;
    
    // 文档分块配置
    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_OVERLAP = 50;
    
    @Override
    @Transactional
    public Map<String, Object> uploadAndProcessDocument(String title, String content, String metadata) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始处理文档: 标题={}, 内容长度={}", title, content.length());
            
            // 1. 文档分块
            List<String> chunks = documentChunkService.smartSplit(content, DEFAULT_CHUNK_SIZE);
            log.info("文档分块完成: {}块", chunks.size());
            
            // 2. 批量向量化
            List<List<Float>> embeddings = embeddingService.embedBatch(chunks);
            log.info("向量化完成: {}个向量", embeddings.size());
            
            // 3. 存储向量文档
            VectorDocument docTemplate = new VectorDocument();
            docTemplate.setTitle(title);
            docTemplate.setSourceDocId(System.currentTimeMillis()); // 使用时间戳作为临时ID
            docTemplate.setMetadata(metadata);
            
            List<VectorDocument> storedDocs = vectorDocumentService.storeDocumentChunks(docTemplate, chunks, embeddings);
            
            result.put("success", true);
            result.put("title", title);
            result.put("sourceDocId", docTemplate.getSourceDocId());
            result.put("totalChunks", chunks.size());
            result.put("totalEmbeddings", embeddings.size());
            result.put("embeddingDimension", embeddings.isEmpty() ? 0 : embeddings.get(0).size());
            result.put("storedDocuments", storedDocs.size());
            result.put("chunks", chunks.stream().map(c -> c.substring(0, Math.min(100, c.length())) + "...").collect(Collectors.toList()));
            result.put("timestamp", LocalDateTime.now());
            
            log.info("文档处理完成: 标题={}, 块数={}, 向量维度={}", 
                     title, chunks.size(), embeddings.isEmpty() ? 0 : embeddings.get(0).size());
            
        } catch (Exception e) {
            log.error("文档处理失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> semanticQuestionAnswer(String question, int topK) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始语义问答: 问题={}", question);
            
            // 1. 问题向量化
            List<Float> questionEmbedding = embeddingService.embed(question);
            log.info("问题向量化完成: 维度={}", questionEmbedding.size());
            
            // 2. 语义检索
            List<VectorDocument> relevantDocs = vectorDocumentService.semanticSearch(questionEmbedding, topK);
            log.info("语义检索完成: 找到{}个相关文档块", relevantDocs.size());
            
            // 3. 构建上下文
            StringBuilder contextBuilder = new StringBuilder();
            List<Map<String, Object>> retrievedDocs = new ArrayList<>();
            
            for (VectorDocument doc : relevantDocs) {
                contextBuilder.append(doc.getContent()).append("\n\n");
                
                Map<String, Object> docInfo = new HashMap<>();
                docInfo.put("id", doc.getId());
                docInfo.put("title", doc.getTitle());
                docInfo.put("content", doc.getContent().substring(0, Math.min(200, doc.getContent().length())) + "...");
                docInfo.put("chunkIndex", doc.getChunkIndex());
                docInfo.put("similarity", Math.round(doc.getSimilarityScore() * 10000.0) / 10000.0);
                retrievedDocs.add(docInfo);
            }
            
            String context = contextBuilder.toString().trim();
            
            // 4. 大模型生成回答
            String answer = generateAnswerWithLLM(question, context);
            
            result.put("success", true);
            result.put("question", question);
            result.put("answer", answer);
            result.put("retrievedDocuments", retrievedDocs);
            result.put("contextLength", context.length());
            result.put("path", "RAG_SEMANTIC");
            result.put("timestamp", LocalDateTime.now());
            
            log.info("语义问答完成: 问题={}, 检索文档数={}, 回答长度={}", 
                     question, relevantDocs.size(), answer.length());
            
        } catch (Exception e) {
            log.error("语义问答失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getDocumentChunks(Long sourceDocId) {
        List<VectorDocument> chunks = vectorDocumentService.findBySourceDocId(sourceDocId);
        
        return chunks.stream().map(chunk -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", chunk.getId());
            info.put("title", chunk.getTitle());
            info.put("content", chunk.getContent());
            info.put("chunkIndex", chunk.getChunkIndex());
            info.put("hasEmbedding", chunk.getEmbeddingJson() != null && !chunk.getEmbeddingJson().isEmpty());
            info.put("createTime", chunk.getCreateTime());
            return info;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteDocument(Long sourceDocId) {
        vectorDocumentService.deleteBySourceDocId(sourceDocId);
        log.info("文档删除完成: 源文档ID={}", sourceDocId);
    }
    
    /**
     * 使用大模型生成回答
     */
    private String generateAnswerWithLLM(String question, String context) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            
            // 系统提示
            String systemPrompt = "你是企业销售管理系统的智能助手。请根据提供的参考文档内容，准确回答用户的问题。" +
                "如果文档中没有相关信息，请如实说明。回答要简洁、专业、准确。";
            messages.add(new SystemMessage(systemPrompt));
            
            // 构建用户消息（包含上下文）
            String userMessage = String.format(
                "参考文档内容：\n%s\n\n用户问题：%s\n\n请根据参考文档回答上述问题。", 
                context, question
            );
            messages.add(new UserMessage(userMessage));
            
            // 调用大模型
            Response<AiMessage> response = chatModel.generate(messages);
            return response.content().text();
            
        } catch (Exception e) {
            log.error("大模型生成回答失败", e);
            return "抱歉，生成回答时出现错误：" + e.getMessage();
        }
    }
}