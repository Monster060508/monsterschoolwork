-- 更新向量文档表结构
-- 添加分块索引和源文档ID字段

-- 检查并添加chunk_index字段
ALTER TABLE vector_document ADD COLUMN IF NOT EXISTS chunk_index INT DEFAULT 0 COMMENT '块索引';

-- 检查并添加source_doc_id字段
ALTER TABLE vector_document ADD COLUMN IF NOT EXISTS source_doc_id BIGINT COMMENT '源文档ID';

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_source_doc_id ON vector_document(source_doc_id);

-- 显示更新后的表结构
DESCRIBE vector_document;