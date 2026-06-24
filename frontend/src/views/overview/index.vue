<template>
  <div class="overview-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>销售总览</h2>
      <p>查看销售数据统计和关键指标</p>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon" style="background-color: #409eff;">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ stats.totalSales?.toLocaleString() || '0' }}</div>
            <div class="stat-label">总销售额</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon" style="background-color: #67c23a;">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalOrders || '0' }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon" style="background-color: #e6a23c;">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalCustomers || '0' }}</div>
            <div class="stat-label">总客户数</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon" style="background-color: #f56c6c;">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalProducts || '0' }}</div>
            <div class="stat-label">商品种类</div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-section">
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>销售趋势</span>
            <el-radio-group v-model="salesChartPeriod" size="small">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
              <el-radio-button label="year">本年</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <div class="chart-container" ref="salesChartRef"></div>
      </el-card>
      
      <el-card class="chart-card">
        <template #header>
          <span>订单状态分布</span>
        </template>
        <div class="chart-container" ref="orderStatusChartRef"></div>
      </el-card>
    </div>
    
    <!-- 详细信息区域 -->
    <div class="details-section">
      <!-- 销冠排行 -->
      <el-card class="detail-card">
        <template #header>
          <span>销售排行榜</span>
        </template>
        <div class="ranking-list">
          <div v-for="(item, index) in topSales" :key="index" class="ranking-item">
            <div class="ranking-index" :class="{ 'top-three': index < 3 }">
              {{ index + 1 }}
            </div>
            <div class="ranking-avatar">
              <el-avatar :size="40" :src="item.photo_url">{{ item.salesperson_name?.charAt(0) }}</el-avatar>
            </div>
            <div class="ranking-info">
              <div class="ranking-name">{{ item.salesperson_name }}</div>
              <div class="ranking-value">销售额: ¥{{ item.total_sales?.toLocaleString() || '0' }}</div>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 热门商品 -->
      <el-card class="detail-card">
        <template #header>
          <span>热门商品TOP5</span>
        </template>
        <div class="product-list">
          <div v-for="(item, index) in hotProducts" :key="index" class="product-item">
            <div class="product-index">{{ index + 1 }}</div>
            <div class="product-image">
              <img :src="item.image_url || 'https://via.placeholder.com/50'" alt="商品图片">
            </div>
            <div class="product-info">
              <div class="product-name">{{ item.product_name }}</div>
              <div class="product-stats">
                <span>销量: {{ item.sales_quantity }}</span>
                <span>库存: {{ item.stock_quantity }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 最近订单 -->
    <el-card class="recent-orders-card">
      <template #header>
        <div class="card-header">
          <span>最近订单</span>
          <el-button type="primary" link @click="viewAllOrders">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentOrders" style="width: 100%;">
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="customerName" label="客户名称" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="amount" label="订单金额">
          <template #default="{ row }">
            ¥{{ row.amount?.toLocaleString() || '0' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.status)">
              {{ getOrderStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" />
      </el-table>
    </el-card>
    
    <!-- 打印报表按钮 -->
    <div class="print-section">
      <el-button type="primary" size="large" @click="printReport">
        <el-icon><Printer /></el-icon>
        打印总览PDF报表
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { overviewApi } from '@/api'
import * as echarts from 'echarts'

const router = useRouter()

// 统计数据
const stats = reactive({
  totalSales: 0,
  totalOrders: 0,
  totalCustomers: 0,
  totalProducts: 0
})

// 图表数据
const salesChartPeriod = ref('month')
const salesChartRef = ref()
const orderStatusChartRef = ref()

// 排行数据
const topSales = ref([])
const hotProducts = ref([])
const recentOrders = ref([])

// 图表实例
let salesChart: echarts.ECharts | null = null
let orderStatusChart: echarts.ECharts | null = null

// 获取数据
const fetchData = async () => {
  try {
    // 获取统计数据
    const statsResponse = await overviewApi.getStats()
    if (statsResponse.code === 200) {
      Object.assign(stats, statsResponse.data)
    }
    
    // 获取销售排行榜
    const topSalesResponse = await overviewApi.getTopSales()
    if (topSalesResponse.code === 200) {
      topSales.value = topSalesResponse.data
    }
    
    // 获取热门商品
    const hotProductsResponse = await overviewApi.getHotProducts()
    if (hotProductsResponse.code === 200) {
      hotProducts.value = hotProductsResponse.data
    }
    
    // 获取最近订单
    const recentOrdersResponse = await overviewApi.getRecentOrders()
    if (recentOrdersResponse.code === 200) {
      recentOrders.value = recentOrdersResponse.data
    }
    
    // 初始化图表
    await initCharts()
  } catch (error: any) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败: ' + (error.message || '请检查网络连接'))
  }
}

// 初始化图表
const initCharts = async () => {
  try {
    // 销售趋势图表
    if (salesChartRef.value) {
      salesChart = echarts.init(salesChartRef.value)
      
      const salesDataResponse = await overviewApi.getSalesTrend(salesChartPeriod.value)
      if (salesDataResponse.code === 200) {
        const rawData = salesDataResponse.data
        // 后端返回 [{date, order_count, sales_amount}]，转换为 {dates, values}
        const dates = rawData.map((item: any) => item.date)
        const values = rawData.map((item: any) => Number(item.sales_amount) || 0)
        
        const salesOption = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: [
            {
              type: 'category',
              data: dates,
              axisTick: {
                alignWithLabel: true
              }
            }
          ],
          yAxis: [
            {
              type: 'value',
              axisLabel: {
                formatter: '¥{value}'
              }
            }
          ],
          series: [
            {
              name: '销售额',
              type: 'bar',
              barWidth: '60%',
              data: values,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#83bff6' },
                  { offset: 0.5, color: '#188df0' },
                  { offset: 1, color: '#188df0' }
                ])
              }
            }
          ]
        }
        
        salesChart.setOption(salesOption)
      }
    }
    
    // 订单状态图表
    if (orderStatusChartRef.value) {
      orderStatusChart = echarts.init(orderStatusChartRef.value)
      
      const orderStatusResponse = await overviewApi.getOrderStatusDistribution()
      if (orderStatusResponse.code === 200) {
        const rawData = orderStatusResponse.data
        // 后端返回 {status: count} 格式，转换为 {name, value} 格式
        const statusNameMap: Record<string, string> = {
          'PENDING': '待处理',
          'IN_PROGRESS': '进行中',
          'COMPLETED': '已完成',
          'CANCELLED': '已取消'
        }
        const orderStatusData = Object.entries(rawData).map(([status, count]) => ({
          name: statusNameMap[status] || status,
          value: count
        }))
        
        const orderStatusOption = {
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 10,
            data: orderStatusData.map((item: any) => item.name)
          },
          series: [
            {
              name: '订单状态',
              type: 'pie',
              radius: ['50%', '70%'],
              avoidLabelOverlap: false,
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '30',
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: orderStatusData
            }
          ]
        }
        
        orderStatusChart.setOption(orderStatusOption)
      }
    }
  } catch (error: any) {
    console.error('初始化图表失败:', error)
  }
}

// 打印报表
const printReport = async () => {
  try {
    ElMessage.info('正在生成PDF报表...')
    
    const response = await overviewApi.printReport()
    
    if (response.code === 200) {
      // 创建下载链接
      const blob = new Blob([response.data], { type: 'application/pdf' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `销售总览报表_${new Date().toISOString().slice(0, 10)}.pdf`
      link.click()
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('报表生成成功')
    } else {
      ElMessage.error(response.message || '报表生成失败')
    }
  } catch (error: any) {
    console.error('报表生成失败:', error)
    ElMessage.error('报表生成失败: ' + (error.message || '请检查网络连接'))
  }
}

// 查看所有订单
const viewAllOrders = () => {
  router.push('/orders')
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

// 监听销售趋势周期变化
watch(salesChartPeriod, () => {
  initCharts()
})

// 窗口大小变化时重绘图表
const handleResize = () => {
  salesChart?.resize()
  orderStatusChart?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

// 组件卸载时移除事件监听
import { onUnmounted } from 'vue'
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  salesChart?.dispose()
  orderStatusChart?.dispose()
})
</script>

<style scoped>
.overview-container {
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

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
  font-size: 24px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
}

.chart-container {
  height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.details-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.detail-card {
  border-radius: 8px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}

.ranking-index {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #ccc;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 15px;
}

.ranking-index.top-three {
  background: linear-gradient(135deg, #ffd700 0%, #ff8c00 100%);
}

.ranking-avatar {
  margin-right: 15px;
}

.ranking-info {
  flex: 1;
}

.ranking-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.ranking-value {
  font-size: 14px;
  color: #666;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}

.product-index {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 15px;
}

.product-image {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 15px;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.product-stats {
  font-size: 14px;
  color: #666;
  display: flex;
  gap: 20px;
}

.recent-orders-card {
  border-radius: 8px;
  margin-bottom: 20px;
}

.print-section {
  text-align: center;
  margin-top: 20px;
}
</style>