<template>
  <div class="products-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>商品管理</h2>
      <p>管理商品信息、库存和图片</p>
    </div>
    
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
      
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品名称..."
        style="width: 300px; margin-left: 20px;"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      
      <el-select v-model="categoryFilter" placeholder="商品分类" style="width: 150px; margin-left: 20px;" @change="handleSearch">
        <el-option label="全部分类" value="" />
        <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
      </el-select>
    </div>
    
    <!-- 商品列表 -->
    <el-table :data="products" style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="商品图片" width="120">
        <template #default="{ row }">
          <el-image 
            :src="row.image || 'https://via.placeholder.com/80'" 
            :preview-src-list="[row.image]"
            fit="cover"
            style="width: 80px; height: 80px; border-radius: 4px;"
          />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="price" label="价格" width="120">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ row.price?.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100">
        <template #default="{ row }">
          <el-tag :type="row.stock < 10 ? 'danger' : 'success'">
            {{ row.stock }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="viewProduct(row)">查看</el-button>
          <el-button size="small" type="primary" @click="editProduct(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteProduct(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 添加/编辑商品对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'add' ? '添加商品' : '编辑商品'"
      width="800px"
    >
      <el-form :model="productForm" :rules="rules" ref="productFormRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="productForm.category" placeholder="请选择商品分类" style="width: 100%;">
            <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="productForm.price" :precision="2" :min="0" :max="999999" style="width: 100%;" />
        </el-form-item>
        
        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" :max="999999" style="width: 100%;" />
        </el-form-item>
        
        <el-form-item label="商品图片" prop="image">
          <div class="image-upload">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleImageSuccess"
              :before-upload="beforeImageUpload"
              :headers="uploadHeaders"
            >
              <img v-if="productForm.image" :src="productForm.image" class="avatar">
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="image-tip">建议尺寸: 800x800 像素，大小不超过 2MB</div>
          </div>
        </el-form-item>
        
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" :rows="4" placeholder="请输入商品描述" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 查看商品详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="商品详情" width="600px">
      <div class="product-detail">
        <div class="detail-image">
          <img :src="currentProduct.image || 'https://via.placeholder.com/300'" alt="商品图片">
        </div>
        <div class="detail-info">
          <h3>{{ currentProduct.name }}</h3>
          <p class="price">¥{{ currentProduct.price?.toFixed(2) }}</p>
          <p class="stock">库存: {{ currentProduct.stock }}</p>
          <p class="category">分类: {{ currentProduct.category }}</p>
          <p class="description">{{ currentProduct.description }}</p>
          <p class="time">创建时间: {{ currentProduct.createTime }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { productApi, uploadApi } from '@/api'

// 数据
const products = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const categoryFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框相关
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const submitLoading = ref(false)
const productFormRef = ref<FormInstance>()

// 商品分类
const categories = ref(['电子产品', '服装鞋帽', '食品饮料', '家居用品', '办公用品', '其他'])

// 当前查看的商品
const currentProduct = ref<any>({})

// 商品表单
const productForm = reactive({
  id: null as number | null,
  name: '',
  category: '',
  price: 0,
  stock: 0,
  image: '',
  description: '',
  status: 1
})

// 表单验证规则
const rules: FormRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' }
  ]
}

// 上传相关
const uploadUrl = computed(() => {
  return '/api/upload/image'
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${localStorage.getItem('token')}`
  }
})

// 获取商品列表
const fetchProducts = async () => {
  try {
    loading.value = true
    
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
      category: categoryFilter.value
    }
    
    const response = await productApi.getProducts(params)
    
    if (response.code === 200) {
      products.value = response.data.records
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '获取商品列表失败')
    }
  } catch (error: any) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败: ' + (error.message || '请检查网络连接'))
  } finally {
    loading.value = false
  }
}

// 显示添加对话框
const showAddDialog = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 显示编辑对话框
const editProduct = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(productForm, row)
  dialogVisible.value = true
}

// 查看商品
const viewProduct = (row: any) => {
  currentProduct.value = row
  viewDialogVisible.value = true
}

// 删除商品
const deleteProduct = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await productApi.deleteProduct(row.id)
    
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchProducts()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '请检查网络连接'))
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await productFormRef.value.validate()
    
    submitLoading.value = true
    
    let response
    if (dialogType.value === 'add') {
      response = await productApi.createProduct(productForm)
    } else {
      response = await productApi.updateProduct(productForm.id, productForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
      dialogVisible.value = false
      fetchProducts()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败: ' + (error.message || '请检查网络连接'))
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  productForm.id = null
  productForm.name = ''
  productForm.category = ''
  productForm.price = 0
  productForm.stock = 0
  productForm.image = ''
  productForm.description = ''
  productForm.status = 1
  
  if (productFormRef.value) {
    productFormRef.value.resetFields()
  }
}

// 图片上传成功
const handleImageSuccess = (response: any) => {
  if (response.code === 200) {
    productForm.image = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// 图片上传前检查
const beforeImageUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传图片只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

// 状态变更
const handleStatusChange = async (row: any) => {
  try {
    const response = await productApi.updateProductStatus(row.id, row.status)
    
    if (response.code === 200) {
      ElMessage.success('状态更新成功')
    } else {
      // 恢复原状态
      row.status = row.status === 1 ? 0 : 1
      ElMessage.error(response.message || '状态更新失败')
    }
  } catch (error: any) {
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败: ' + (error.message || '请检查网络连接'))
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchProducts()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchProducts()
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.products-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.action-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.image-upload {
  display: flex;
  flex-direction: column;
}

.avatar-uploader {
  width: 178px;
  height: 178px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}

.image-tip {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}

.product-detail {
  display: flex;
  gap: 30px;
}

.detail-image {
  flex: 1;
}

.detail-image img {
  width: 100%;
  max-width: 300px;
  height: auto;
  border-radius: 8px;
}

.detail-info {
  flex: 1;
}

.detail-info h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 24px;
}

.detail-info p {
  margin: 0 0 15px 0;
  color: #666;
  line-height: 1.6;
}

.detail-info .price {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
}

.detail-info .stock {
  font-size: 16px;
}

.detail-info .category {
  font-size: 14px;
  color: #999;
}

.detail-info .description {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
}

.detail-info .time {
  font-size: 12px;
  color: #999;
}
</style>