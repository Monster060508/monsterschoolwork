package com.enterprise.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.sales.entity.VectorDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VectorDocumentMapper extends BaseMapper<VectorDocument> {
    
    @Select("SELECT * FROM vector_document WHERE source_doc_id = #{sourceDocId} ORDER BY chunk_index")
    List<VectorDocument> findBySourceDocId(@Param("sourceDocId") Long sourceDocId);
    
    @Select("SELECT * FROM vector_document WHERE title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')")
    List<VectorDocument> searchByKeyword(@Param("keyword") String keyword);
    
    @Select("SELECT * FROM vector_document")
    List<VectorDocument> findAll();
}