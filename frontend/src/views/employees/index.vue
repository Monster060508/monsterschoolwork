<template>
  <div class="employees-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>员工管理</h2>
      <p>管理员工信息、角色和照片</p>
    </div>
    
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加员工
      </el-button>
      
      <el-input
        v-model="searchKeyword"
        placeholder="搜索员工姓名..."
        style="width: 300px; margin-left: 20px;"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      
      <el-select v-model="roleFilter" placeholder="角色筛选" style="width: 150px; margin-left: 20px;" @change="handleSearch">
        <el-option label="全部角色" value="" />
        <el-option label="管理员" value="ADMIN" />
        <el-option label="销售" value="SALES" />
      </el-select>
    </div>
    
    <!-- 员工列表 -->
    <el-table :data="employees" style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="照片" width="100">
        <template #default="{ row }">
          <el-avatar :size="50" :src="row.photoUrl">{{ row.name?.charAt(0) }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
            {{ row.role === 'ADMIN' ? '管理员' : '销售' }}
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
      <el-table-column prop="createTime" label="入职时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="viewEmployee(row)">查看</el-button>
          <el-button size="small" type="primary" @click="editEmployee(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteEmployee(row)">删除</el-button>
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
    
    <!-- 添加/编辑员工对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'add' ? '添加员工' : '编辑员工'"
      width="800px"
    >
      <el-form :model="employeeForm" :rules="rules" ref="employeeFormRef" label-width="100px">
        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="employeeForm.name" placeholder="请输入员工姓名" />
        </el-form-item>
        
        <el-form-item label="用户名" prop="username">
          <el-input v-model="employeeForm.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="employeeForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="employeeForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="employeeForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="employeeForm.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="销售" value="SALES" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="员工照片" prop="photoUrl">
          <div class="avatar-upload">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders"
            >
              <img v-if="employeeForm.photoUrl" :src="employeeForm.photoUrl" class="avatar">
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="avatar-tip">建议尺寸: 200x200 像素，大小不超过 1MB</div>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 查看员工详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="员工详情" width="600px">
      <div class="employee-detail">
        <div class="detail-header">
          <el-avatar :size="100" :src="currentEmployee.photoUrl">{{ currentEmployee.name?.charAt(0) }}</el-avatar>
          <div class="detail-title">
            <h3>{{ currentEmployee.name }}</h3>
            <el-tag :type="currentEmployee.role === 'ADMIN' ? 'danger' : 'primary'" size="large">
              {{ currentEmployee.role === 'ADMIN' ? '管理员' : '销售' }}
            </el-tag>
          </div>
        </div>
        
        <div class="detail-info">
          <div class="info-item">
            <span class="label">用户名:</span>
            <span class="value">{{ currentEmployee.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机号:</span>
            <span class="value">{{ currentEmployee.phone }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱:</span>
            <span class="value">{{ currentEmployee.email }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态:</span>
            <span class="value">
              <el-tag :type="currentEmployee.status === 1 ? 'success' : 'danger'">
                {{ currentEmployee.status === 1 ? '在职' : '离职' }}
              </el-tag>
            </span>
          </div>
          <div class="info-item">
            <span class="label">入职时间:</span>
            <span class="value">{{ currentEmployee.createTime }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { userApi, uploadApi } from '@/api'

// 数据
const employees = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const roleFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框相关
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const submitLoading = ref(false)
const employeeFormRef = ref<FormInstance>()

// 当前查看的员工
const currentEmployee = ref<any>({})

// 员工表单
const employeeForm = reactive({
  id: null as number | null,
  name: '',
  username: '',
  password: '',
  phone: '',
  email: '',
  role: 'SALES',
  photoUrl: '',
  status: 1
})

// 表单验证规则
const rules: FormRules = {
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度在 4 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
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

// 获取员工列表
const fetchEmployees = async () => {
  try {
    loading.value = true
    
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
      role: roleFilter.value
    }
    
    const response = await userApi.getUsers(params)
    
    if (response.code === 200) {
      employees.value = response.data.records
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '获取员工列表失败')
    }
  } catch (error: any) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败: ' + (error.message || '请检查网络连接'))
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
const editEmployee = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(employeeForm, row)
  employeeForm.password = '' // 编辑时不清空密码
  dialogVisible.value = true
}

// 查看员工
const viewEmployee = (row: any) => {
  currentEmployee.value = row
  viewDialogVisible.value = true
}

// 删除员工
const deleteEmployee = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个员工吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    console.log('开始删除员工，ID:', row.id)
    const response = await userApi.deleteUser(row.id)
    console.log('删除响应:', response)
    
    if (response.code === 200) {
      ElMessage.success('删除成功')
      await fetchEmployees()
    } else {
      console.error('删除失败，服务器返回:', response)
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除员工失败:', error)
      // 显示更详细的错误信息
      let errorMsg = '删除失败'
      if (error.response) {
        // 服务器返回了错误响应
        errorMsg += ': ' + (error.response.data?.message || error.response.statusText || '服务器错误')
      } else if (error.message) {
        errorMsg += ': ' + error.message
      }
      ElMessage.error(errorMsg)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await employeeFormRef.value.validate()
    
    submitLoading.value = true
    
    let response
    if (dialogType.value === 'add') {
      response = await userApi.createUser(employeeForm)
    } else {
      response = await userApi.updateUser(employeeForm.id, employeeForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
      dialogVisible.value = false
      fetchEmployees()
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
  employeeForm.id = null
  employeeForm.name = ''
  employeeForm.username = ''
  employeeForm.password = ''
  employeeForm.phone = ''
  employeeForm.email = ''
  employeeForm.role = 'SALES'
  employeeForm.photoUrl = ''
  employeeForm.status = 1
  
  if (employeeFormRef.value) {
    employeeFormRef.value.resetFields()
  }
}

// 头像上传成功
const handleAvatarSuccess = (response: any) => {
  if (response.code === 200) {
    employeeForm.photoUrl = response.data.url
    ElMessage.success('照片上传成功')
  } else {
    ElMessage.error(response.message || '照片上传失败')
  }
}

// 头像上传前检查
const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt1M = file.size / 1024 / 1024 < 1

  if (!isJPG) {
    ElMessage.error('上传照片只能是 JPG/PNG 格式!')
  }
  if (!isLt1M) {
    ElMessage.error('上传照片大小不能超过 1MB!')
  }
  return isJPG && isLt1M
}

// 状态变更
const handleStatusChange = async (row: any) => {
  try {
    const response = await userApi.updateUserStatus(row.id, row.status)
    
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
  fetchEmployees()
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchEmployees()
}

// 页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchEmployees()
}

// 初始化
onMounted(() => {
  fetchEmployees()
})
</script>

<style scoped>
.employees-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
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
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-uploader {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.employee-detail {
  padding: 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.detail-title {
  margin-left: 20px;
}

.detail-title h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
}

.detail-info {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item .label {
  width: 100px;
  color: #909399;
  font-size: 14px;
}

.info-item .value {
  flex: 1;
  color: #303133;
  font-size: 14px;
}
</style>
