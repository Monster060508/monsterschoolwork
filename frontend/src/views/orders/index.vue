<template>
  <div class="orders-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>订单管理</h2>
      <p>管理订单信息、状态和下载PDF订单</p>
    </div>
    
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        新建订单
      </el-button>
      
      <el-input
        v-model="searchKeyword"
        placeholder="搜索订单编号..."
        style="width: 300px; margin-left: 20px;"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      
      <el-select v-model="statusFilter" placeholder="订单状态" style="width: 150px; margin-left: 20px;" @change="handleSearch">
        <el-option label="全部状态" value="" />
        <el-option label="待处理" value="PENDING" />
        <el-option label="进行中" value="IN_PROGRESS" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
    </div>
    
    <!-- 订单列表 -->
    <el-table :data="orders" style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="orderNo" label="订单编号" width="180" />
      <el-table-column prop="customerName" label="客户名称" />
      <el-table-column prop="salespersonName" label="销售人员" />
      <el-table-column prop="productName" label="商品名称" />
      <el-table-column prop="totalAmount" label="订单金额" width="120">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ row.totalAmount?.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getOrderStatusType(row.status)">
            {{ getOrderStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button size="small" @click="viewOrder(row)">查看</el-button>
          <el-button size="small" type="primary" @click="editOrder(row)">编辑</el-button>
          <el-button size="small" type="success" @click="downloadOrderPdf(row)">下载PDF</el-button>
          <el-button 
            v-if="row.status === 'PENDING' || row.status === 'PROCESSING'"
            size="small" 
            type="warning" 
            @click="updateOrderStatus(row)"
          >
            更新状态
          </el-button>
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
    
    <!-- 添加/编辑订单对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'add' ? '新建订单' : '编辑订单'"
      width="900px"
    >
      <el-form :model="orderForm" :rules="rules" ref="orderFormRef" label-width="120px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="orderForm.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        
        <el-form-item label="销售人员" prop="salesId">
          <el-select v-model="orderForm.salesId" placeholder="请选择销售人员" style="width: 100%;" filterable>
            <el-option 
              v-for="user in salesUsers" 
              :key="user.id" 
              :label="user.name" 
              :value="user.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="商品" prop="productId">
          <el-select v-model="orderForm.productId" placeholder="请选择商品" style="width: 100%;" filterable @change="handleProductChange">
            <el-option 
              v-for="product in products" 
              :key="product.id" 
              :label="`${product.name} (¥${product.price})`" 
              :value="product.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="商品数量" prop="quantity">
          <el-input-number v-model="orderForm.quantity" :min="1" :max="currentProduct.stock || 999" style="width: 100%;" />
        </el-form-item>
        
        <el-form-item label="订单金额" prop="amount">
          <el-input v-model="orderForm.amount" placeholder="自动计算" disabled>
            <template #prepend>¥</template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="订单状态" prop="status">
          <el-select v-model="orderForm.status" placeholder="请选择订单状态" style="width: 100%;">
            <el-option label="待处理" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input v-model="orderForm.remark" type="textarea" :rows="3" placeholder="请输入订单备注" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 查看订单详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="订单详情" width="800px">
      <div class="order-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">订单编号:</span>
              <span class="value">{{ currentOrder.orderNo }}</span>
            </div>
            <div class="detail-item">
              <span class="label">客户名称:</span>
              <span class="value">{{ currentOrder.customerName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">销售人员:</span>
              <span class="value">{{ currentOrder.salespersonName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">商品名称:</span>
              <span class="value">{{ currentOrder.productName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">订单金额:</span>
              <span class="value" style="color: #f56c6c; font-weight: bold;">¥{{ currentOrder.totalAmount?.toFixed(2) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">订单状态:</span>
              <span class="value">
                <el-tag :type="getOrderStatusType(currentOrder.status)">
                  {{ getOrderStatusText(currentOrder.status) }}
                </el-tag>
              </span>
            </div>
            <div class="detail-item">
              <span class="label">下单时间:</span>
              <span class="value">{{ currentOrder.createTime }}</span>
            </div>
          </div>
        </div>
        
        <div class="detail-section">
          <h4>备注信息</h4>
          <p class="remark">{{ currentOrder.remark || '无备注' }}</p>
        </div>
        
        <div class="detail-section">
          <h4>操作记录</h4>
          <div class="timeline">
            <div v-for="(log, index) in currentOrder.logs" :key="index" class="timeline-item">
              <div class="timeline-time">{{ log.time }}</div>
              <div class="timeline-content">{{ log.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 更新状态对话框 -->
    <el-dialog v-model="statusDialogVisible" title="更新订单状态" width="500px">
      <el-form :model="statusForm" label-width="100px">
        <el-form-item label="订单编号">
          <el-input v-model="statusForm.orderNo" disabled />
        </el-form-item>
        
        <el-form-item label="当前状态">
          <el-tag :type="getOrderStatusType(statusForm.currentStatus)">
            {{ getOrderStatusText(statusForm.currentStatus) }}
          </el-tag>
        </el-form-item>
        
        <el-form-item label="新状态">
          <el-select v-model="statusForm.newStatus" placeholder="请选择新状态" style="width: 100%;">
            <el-option 
              v-for="status in availableStatuses" 
              :key="status.value" 
              :label="status.label" 
              :value="status.value" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态说明">
          <el-input v-model="statusForm.remark" type="textarea" :rows="3" placeholder="请输入状态变更说明" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleStatusSubmit" :loading="statusSubmitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { orderApi, userApi, productApi } from '@/api'

// 数据
const orders = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框相关
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const statusDialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const submitLoading = ref(false)
const statusSubmitLoading = ref(false)
const orderFormRef = ref<FormInstance>()

// 用户和商品数据
const salesUsers = ref([])
const products = ref([])
const currentProduct = ref<any>({})
const currentOrder = ref<any>({})

// 订单表单
const orderForm = reactive({
  id: null as number | null,
  customerName: '',
  salesId: null as number | null,
  productId: null as number | null,
  quantity: 1,
  amount: 0,
  status: 'PENDING',
  remark: ''
})

// 状态表单
const statusForm = reactive({
  orderId: null as number | null,
  orderNo: '',
  currentStatus: '',
  newStatus: '',
  remark: ''
})

// 表单验证规则
const rules: FormRules = {
  customerName: [
    { required: true, message: '请输入客户名称', trigger: 'blur' }
  ],
  salesId: [
    { required: true, message: '请选择销售人员', trigger: 'change' }
  ],
  productId: [
    { required: true, message: '请选择商品', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入商品数量', trigger: 'blur' }
  ]
}

// 可选状态
const availableStatuses = computed(() => {
  const current = statusForm.currentStatus
  const statuses = [
    { value: 'PENDING', label: '待处理' },
    { value: 'IN_PROGRESS', label: '进行中' },
    { value: 'COMPLETED', label: '已完成' },
    { value: 'CANCELLED', label: '已取消' }
  ]
  
  // 根据当前状态过滤可选状态
  switch (current) {
    case 'PENDING':
      return statuses.filter(s => ['IN_PROGRESS', 'CANCELLED'].includes(s.value))
    case 'IN_PROGRESS':
      return statuses.filter(s => ['COMPLETED', 'CANCELLED'].includes(s.value))
    default:
      return []
  }
})

// 获取订单列表
const fetchOrders = async () => {
  try {
    loading.value = true
    
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
      status: statusFilter.value
    }
    
    const response = await orderApi.getOrders(params)
    
    if (response.code === 200) {
      orders.value = response.data.records
      total.value = response.data.total
    } else {
      ElMessage.error(response.message || '获取订单列表失败')
    }
  } catch (error: any) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败: ' + (error.message || '请检查网络连接'))
  } finally {
    loading.value = false
  }
}

// 获取销售人员
const fetchSalesUsers = async () => {
  try {
    const response = await userApi.getUsers({ role: 'SALES' })
    if (response.code === 200) {
      salesUsers.value = response.data.records
    }
  } catch (error: any) {
    console.error('获取销售人员失败:', error)
  }
}

// 获取商品列表
const fetchProducts = async () => {
  try {
    const response = await productApi.getProducts({ status: 1 })
    if (response.code === 200) {
      products.value = response.data.records
    }
  } catch (error: any) {
    console.error('获取商品列表失败:', error)
  }
}

// 显示添加对话框
const showAddDialog = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 显示编辑对话框
const editOrder = (row: any) => {
  dialogType.value = 'edit'
  // 只映射表单需要的字段，避免字段名不匹配
  orderForm.id = row.id
  orderForm.customerName = row.customerName
  orderForm.salesId = row.salespersonId
  orderForm.productId = row.productId || null
  orderForm.quantity = row.quantity || 1
  orderForm.amount = row.totalAmount || 0
  orderForm.status = row.status
  orderForm.remark = row.remark || ''
  dialogVisible.value = true
}

// 查看订单
const viewOrder = async (row: any) => {
  try {
    const response = await orderApi.getOrder(row.id)
    if (response.code === 200) {
      currentOrder.value = response.data
      viewDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取订单详情失败')
    }
  } catch (error: any) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败: ' + (error.message || '请检查网络连接'))
  }
}

// 下载订单PDF
const downloadOrderPdf = async (row: any) => {
  try {
    ElMessage.info('正在生成PDF订单...')
    
    const response = await orderApi.downloadOrderPdf(row.id)
    
    if (response.code === 200) {
      // 创建下载链接
      const blob = new Blob([response.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `订单_${row.orderNo}.pdf`
      link.click()
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('PDF订单生成成功')
    } else {
      ElMessage.error(response.message || 'PDF订单生成失败')
    }
  } catch (error: any) {
    console.error('PDF订单生成失败:', error)
    ElMessage.error('PDF订单生成失败: ' + (error.message || '请检查网络连接'))
  }
}

// 更新订单状态
const updateOrderStatus = (row: any) => {
  statusForm.orderId = row.id
  statusForm.orderNo = row.orderNo
  statusForm.currentStatus = row.status
  statusForm.newStatus = ''
  statusForm.remark = ''
  statusDialogVisible.value = true
}

// 商品变更
const handleProductChange = (productId: number) => {
  const product = products.value.find((p: any) => p.id === productId)
  if (product) {
    currentProduct.value = product
    orderForm.amount = product.price * orderForm.quantity
  }
}

// 监听数量变化
watch(() => orderForm.quantity, (newVal) => {
  if (currentProduct.value.price) {
    orderForm.amount = currentProduct.value.price * newVal
  }
})

// 提交订单表单
const handleSubmit = async () => {
  try {
    await orderFormRef.value.validate()
    
    submitLoading.value = true
    
    let response
    if (dialogType.value === 'add') {
      // 构造后端期望的请求格式
      const requestData = {
        customerName: orderForm.customerName,
        salespersonId: orderForm.salesId,
        items: [
          {
            productId: orderForm.productId,
            quantity: orderForm.quantity
          }
        ]
      }
      response = await orderApi.createOrder(requestData)
    } else {
      const requestData = {
        customerName: orderForm.customerName,
        salespersonId: orderForm.salesId
      }
      response = await orderApi.updateOrder(orderForm.id, requestData)
    }
    
    if (response.code === 200) {
      ElMessage.success(dialogType.value === 'add' ? '创建成功' : '更新成功')
      dialogVisible.value = false
      fetchOrders()
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

// 提交状态更新
const handleStatusSubmit = async () => {
  try {
    if (!statusForm.newStatus) {
      ElMessage.error('请选择新状态')
      return
    }
    
    statusSubmitLoading.value = true
    
    const response = await orderApi.updateOrderStatus(statusForm.orderId, {
      status: statusForm.newStatus,
      remark: statusForm.remark
    })
    
    if (response.code === 200) {
      ElMessage.success('状态更新成功')
      statusDialogVisible.value = false
      fetchOrders()
    } else {
      ElMessage.error(response.message || '状态更新失败')
    }
  } catch (error: any) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败: ' + (error.message || '请检查网络连接'))
  } finally {
    statusSubmitLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  orderForm.id = null
  orderForm.customerName = ''
  orderForm.salesId = null
  orderForm.productId = null
  orderForm.quantity = 1
  orderForm.amount = 0
  orderForm.status = 'PENDING'
  orderForm.remark = ''
  currentProduct.value = {}
  
  if (orderFormRef.value) {
    orderFormRef.value.resetFields()
  }
}

// 订单状态文本
const getOrderStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': '待处理',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 订单状态类型
const getOrderStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'PENDING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchOrders()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchOrders()
}

onMounted(() => {
  fetchOrders()
  fetchSalesUsers()
  fetchProducts()
})
</script>

<style scoped>
.orders-container {
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

.order-detail {
  padding: 20px;
}

.detail-section {
  margin-bottom: 30px;
}

.detail-section h4 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 16px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
}

.detail-item .label {
  width: 100px;
  color: #666;
  font-size: 14px;
}

.detail-item .value {
  flex: 1;
  color: #333;
  font-size: 14px;
}

.remark {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.timeline-item {
  display: flex;
  align-items: flex-start;
}

.timeline-time {
  width: 150px;
  color: #999;
  font-size: 12px;
}

.timeline-content {
  flex: 1;
  color: #333;
  font-size: 14px;
}
</style>