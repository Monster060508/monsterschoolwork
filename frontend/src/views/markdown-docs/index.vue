<template>
  <div class="markdown-docs-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>Markdown文档管理</h2>
      <p>管理智能问答模块的RAG知识库文档（仅支持Markdown格式）</p>
    </div>
    
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showUploadDialog">
        <el-icon><Upload /></el-icon>
        上传文档
      </el-button>
      
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文档标题..."
        style="width: 300px; margin-left: 20px;"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>
    
    <!-- 文档列表 -->
    <el-table :data="documents" style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="文档标题" />
      <el-table-column prop="fileName" label="文件名" />
      <el-table-column prop="fileSize" label="文件大小" width="120">
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="上传时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="viewDocument(row)">查看</el-button>
          <el-button size="small" type="primary" @click="editDocument(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteDocument(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传Markdown文档" width="600px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="文档标题" required>
          <el-input v-model="uploadForm.title" placeholder="请输入文档标题" />
        </el-form-item>
        
        <el-form-item label="选择文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".md"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择Markdown文件</el-button>
            <template #tip>
              <div class="el-upload__tip">只能上传 .md 文件</div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="文档内容">
          <el-input
            v-model="uploadForm.content"
            type="textarea"
            :rows="10"
            placeholder="或直接输入Markdown内容"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleUpload">确认上传</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑Markdown文档" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="文档标题" required>
          <el-input v-model="editForm.title" placeholder="请输入文档标题" />
        </el-form-item>
        
        <el-form-item label="文档内容">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="15"
            placeholder="请输入Markdown内容"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleEdit">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 查看对话框 -->
    <el-dialog v-model="viewDialogVisible" title="查看Markdown文档" width="800px">
      <div class="document-viewer">
        <h3>{{ viewDocument.title }}</h3>
        <div class="document-meta">
          <span>文件名: {{ viewDocument.fileName }}</span>
          <span>上传时间: {{ viewDocument.createTime }}</span>
        </div>
        <div class="document-content">
          <pre>{{ viewDocument.content }}</pre>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { markdownDocumentApi } from '@/api'

// 文档列表数据
const documents = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

// 对话框状态
const uploadDialogVisible = ref(false)
const editDialogVisible = ref(false)
const viewDialogVisible = ref(false)

// 表单数据
const uploadForm = ref({
  title: '',
  content: '',
  file: null as File | null
})

const editForm = ref({
  id: 0,
  title: '',
  content: ''
})

const viewDocument = ref<any>({})

// 获取文档列表
const fetchDocuments = async () => {
  try {
    const response = await markdownDocumentApi.getDocuments({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value
    })
    
    if (response.code === 200) {
      documents.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    console.error('获取文档列表失败:', error)
    ElMessage.error('获取文档列表失败')
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchDocuments()
}

// 分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchDocuments()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchDocuments()
}

// 显示上传对话框
const showUploadDialog = () => {
  uploadForm.value = {
    title: '',
    content: '',
    file: null
  }
  uploadDialogVisible.value = true
}

// 文件选择
const handleFileChange = (file: any) => {
  uploadForm.value.file = file.raw
  
  // 读取文件内容
  const reader = new FileReader()
  reader.onload = (e) => {
    uploadForm.value.content = e.target?.result as string
  }
  reader.readAsText(file.raw)
}

// 上传文档
const handleUpload = async () => {
  if (!uploadForm.value.title) {
    ElMessage.warning('请输入文档标题')
    return
  }
  
  if (!uploadForm.value.content) {
    ElMessage.warning('请选择文件或输入内容')
    return
  }
  
  try {
    // 这里应该先上传文件到OSS，获取URL
    // 简化处理，直接使用模拟URL
    const ossUrl = `https://oss-cn-hangzhou.aliyuncs.com/monster060508/markdown/${Date.now()}.md`
    
    const response = await markdownDocumentApi.uploadDocument({
      title: uploadForm.value.title,
      fileName: uploadForm.value.file?.name || 'untitled.md',
      ossUrl: ossUrl,
      content: uploadForm.value.content,
      fileSize: uploadForm.value.file?.size || 0,
      uploadUserId: 1 // 应该从用户状态获取
    })
    
    if (response.code === 200) {
      ElMessage.success('上传成功')
      uploadDialogVisible.value = false
      fetchDocuments()
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败')
  }
}

// 查看文档
const viewDocument = async (row: any) => {
  try {
    const response = await markdownDocumentApi.getDocument(row.id)
    if (response.code === 200) {
      viewDocument.value = response.data
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取文档详情失败:', error)
    ElMessage.error('获取文档详情失败')
  }
}

// 编辑文档
const editDocument = async (row: any) => {
  try {
    const response = await markdownDocumentApi.getDocument(row.id)
    if (response.code === 200) {
      editForm.value = {
        id: response.data.id,
        title: response.data.title,
        content: response.data.content
      }
      editDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取文档详情失败:', error)
    ElMessage.error('获取文档详情失败')
  }
}

// 提交编辑
const handleEdit = async () => {
  if (!editForm.value.title) {
    ElMessage.warning('请输入文档标题')
    return
  }
  
  try {
    const response = await markdownDocumentApi.updateDocument(editForm.value.id, {
      title: editForm.value.title,
      content: editForm.value.content
    })
    
    if (response.code === 200) {
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      fetchDocuments()
    }
  } catch (error) {
    console.error('修改失败:', error)
    ElMessage.error('修改失败')
  }
}

// 删除文档
const deleteDocument = (row: any) => {
  ElMessageBox.confirm('确定要删除这个文档吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await markdownDocumentApi.deleteDocument(row.id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        fetchDocuments()
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 初始化
onMounted(() => {
  fetchDocuments()
})
</script>

<style scoped>
.markdown-docs-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.action-bar {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.document-viewer {
  max-height: 600px;
  overflow-y: auto;
}

.document-viewer h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.document-meta {
  margin-bottom: 20px;
  color: #909399;
  font-size: 14px;
}

.document-meta span {
  margin-right: 20px;
}

.document-content {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  font-family: 'Courier New', Courier, monospace;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>