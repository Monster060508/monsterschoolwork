package com.enterprise.sales.service.impl;

import com.enterprise.sales.service.DocumentChunkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DocumentChunkServiceImpl implements DocumentChunkService {
    
    @Override
    public List<String> splitDocument(String content, int chunkSize, int overlap) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> chunks = new ArrayList<>();
        int length = content.length();
        int start = 0;
        
        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            
            // 尝试在句号、换行符处断开
            if (end < length) {
                int lastPeriod = content.lastIndexOf('。', end);
                int lastNewline = content.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastPeriod, lastNewline);
                
                if (breakPoint > start + chunkSize / 2) {
                    end = breakPoint + 1;
                }
            }
            
            String chunk = content.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            
            start = end - overlap;
            if (start >= length) break;
        }
        
        log.info("文档分块完成: 原文{}字符, 分成{}块", content.length(), chunks.size());
        return chunks;
    }
    
    @Override
    public List<String> smartSplit(String content, int maxChunkSize) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> chunks = new ArrayList<>();
        
        // 先按段落分割
        String[] paragraphs = content.split("\n\n+|\r\n\r\n+");
        
        StringBuilder currentChunk = new StringBuilder();
        
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) continue;
            
            // 如果当前段落本身就超过最大大小，需要进一步分割
            if (paragraph.length() > maxChunkSize) {
                // 先保存当前累积的内容
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                }
                
                // 按句子分割长段落
                String[] sentences = paragraph.split("[。！？.!?]");
                for (String sentence : sentences) {
                    sentence = sentence.trim();
                    if (sentence.isEmpty()) continue;
                    
                    if (currentChunk.length() + sentence.length() + 1 > maxChunkSize) {
                        if (currentChunk.length() > 0) {
                            chunks.add(currentChunk.toString().trim());
                            currentChunk = new StringBuilder();
                        }
                    }
                    currentChunk.append(sentence).append("。");
                }
            } else if (currentChunk.length() + paragraph.length() + 2 > maxChunkSize) {
                // 当前块加上新段落会超限，保存当前块
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                }
                currentChunk.append(paragraph);
            } else {
                // 累积段落
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            }
        }
        
        // 保存最后一块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        log.info("智能分块完成: 原文{}字符, 分成{}块", content.length(), chunks.size());
        return chunks;
    }
}