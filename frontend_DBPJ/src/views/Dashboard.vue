<template>
  <div class="dashboard">
    <el-page-header>
      <template #content>
        <span class="text-large font-600 mr-3">仪表板</span>
      </template>
    </el-page-header>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <el-icon size="30"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalUsers }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon vehicle-icon">
              <el-icon size="30"><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalVehicles }}</div>
              <div class="stat-label">车辆总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon order-icon">
              <el-icon size="30"><Tools /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalRepairOrders }}</div>
              <div class="stat-label">维修工单</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending-icon">
              <el-icon size="30"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.pendingOrders }}</div>
              <div class="stat-label">待处理工单</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card class="quick-actions-card">
      <template #header>
        <span>快捷操作</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-button 
            type="primary" 
            size="large" 
            style="width: 100%"
            @click="$router.push('/users/create')"
          >
            <el-icon><UserFilled /></el-icon>
            添加用户
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button 
            type="success" 
            size="large" 
            style="width: 100%"
            @click="$router.push('/vehicles/create')"
          >
            <el-icon><Van /></el-icon>
            添加车辆
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button 
            type="warning" 
            size="large" 
            style="width: 100%"
            @click="$router.push('/repair-orders/create')"
          >
            <el-icon><Tools /></el-icon>
            创建工单
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 最近活动 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="recent-card">
          <template #header>
            <span>最近的维修工单</span>
            <el-link type="primary" style="float: right" @click="$router.push('/repair-orders')">
              查看全部
            </el-link>
          </template>
          <el-table :data="recentOrders" style="width: 100%" stripe>
            <el-table-column prop="orderId" label="工单ID" width="80" />
            <el-table-column prop="descriptionOfIssue" label="问题描述" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="recent-card">
          <template #header>
            <span>系统信息</span>
          </template>
          <div class="system-info">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
              <el-descriptions-item label="数据库状态">
                <el-tag type="success">正常</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="最后更新">{{ new Date().toLocaleString() }}</el-descriptions-item>
              <el-descriptions-item label="在线用户">{{ stats.onlineUsers }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, Van, Tools, Clock, UserFilled } from '@element-plus/icons-vue'
import userApi from '@/api/user'
import repairOrderApi from '@/api/repairOrder'

const stats = ref({
  totalUsers: 0,
  totalVehicles: 0,
  totalRepairOrders: 0,
  pendingOrders: 0,
  onlineUsers: 1
})

const recentOrders = ref([])

const getStatusType = (status) => {
  const typeMap = {
    'PENDING_ASSIGNMENT': 'warning',
    'ASSIGNED': 'info',
    'IN_PROGRESS': 'primary',
    'AWAITING_PARTS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED_BY_USER': 'danger',
    'CANNOT_REPAIR': 'danger',
    'PENDING_USER_CONFIRMATION': 'warning'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING_ASSIGNMENT': '待分配',
    'ASSIGNED': '已分配',
    'IN_PROGRESS': '进行中',
    'AWAITING_PARTS': '等待配件',
    'COMPLETED': '已完成',
    'CANCELLED_BY_USER': '用户取消',
    'CANNOT_REPAIR': '无法维修',
    'PENDING_USER_CONFIRMATION': '待用户确认'
  }
  return textMap[status] || status
}

const loadDashboardData = async () => {
  try {
    // 加载用户统计
    const users = await userApi.getAllUsers()
    stats.value.totalUsers = users.length || 0

    // 模拟其他数据
    stats.value.totalVehicles = 25
    stats.value.totalRepairOrders = 48
    stats.value.pendingOrders = 12

    // 模拟最近工单数据
    recentOrders.value = [
      { orderId: 1001, descriptionOfIssue: '发动机异响', status: 'IN_PROGRESS' },
      { orderId: 1002, descriptionOfIssue: '刹车片更换', status: 'PENDING_ASSIGNMENT' },
      { orderId: 1003, descriptionOfIssue: '空调维修', status: 'COMPLETED' },
      { orderId: 1004, descriptionOfIssue: '轮胎更换', status: 'AWAITING_PARTS' },
      { orderId: 1005, descriptionOfIssue: '变速箱维修', status: 'ASSIGNED' }
    ]
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stats-row {
  margin: 20px 0;
}

.stat-card {
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  color: white;
}

.user-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.vehicle-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.order-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.pending-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.quick-actions-card {
  margin: 20px 0;
}

.recent-card {
  margin-top: 20px;
  min-height: 300px;
}

.system-info {
  padding: 10px 0;
}
</style> 