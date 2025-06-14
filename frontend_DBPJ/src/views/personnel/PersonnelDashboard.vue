<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <h1>欢迎回来，{{ userName }}</h1>
      <p class="date-display">{{ currentDate }}</p>
    </div>

    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-content">
              <div class="stats-icon pending">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ stats.pendingOrders }}</div>
                <div class="stats-label">待处理工单</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-content">
              <div class="stats-icon progress">
                <el-icon><Loading /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ stats.inProgressOrders }}</div>
                <div class="stats-label">进行中工单</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-content">
              <div class="stats-icon completed">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ stats.completedOrders }}</div>
                <div class="stats-label">本月已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-content">
              <div class="stats-icon rating">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ stats.averageRating }}</div>
                <div class="stats-label">平均评分</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="main-content">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="recent-orders" shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>最近工单</h3>
                <router-link to="/personnel/assignments" class="view-all">查看全部</router-link>
              </div>
            </template>
            <el-table :data="recentOrders" stripe style="width: 100%">
              <el-table-column prop="orderId" label="工单ID" width="80" align="center" />
              <el-table-column prop="description" label="问题描述" show-overflow-tooltip />
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="date" label="日期" width="180" align="center" />
              <el-table-column label="操作" width="120" align="center">
                <template #default="scope">
                  <router-link :to="{ name: 'PersonnelOrderDetail', params: { orderId: scope.row.orderId } }">
                    <el-button size="small" type="primary">查看</el-button>
                  </router-link>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="schedule-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>今日日程</h3>
              </div>
            </template>
            <div v-if="todaySchedule.length > 0">
              <div v-for="(item, index) in todaySchedule" :key="index" class="schedule-item">
                <div class="schedule-time">{{ item.time }}</div>
                <div class="schedule-content">
                  <div class="schedule-title">{{ item.title }}</div>
                  <div class="schedule-desc">{{ item.description }}</div>
                </div>
              </div>
            </div>
            <div v-else class="empty-schedule">
              <el-icon><Calendar /></el-icon>
              <p>今日暂无安排</p>
            </div>
          </el-card>
          
          <el-card class="quick-actions" shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>快捷操作</h3>
              </div>
            </template>
            <div class="action-buttons">
              <router-link to="/personnel/assignments">
                <el-button type="primary" plain icon="Document">查看工单</el-button>
              </router-link>
              <router-link to="/personnel/profile">
                <el-button type="info" plain icon="User">个人信息</el-button>
              </router-link>
              <router-link to="/personnel/payroll">
                <el-button type="success" plain icon="Money">工资记录</el-button>
              </router-link>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Bell, Loading, CircleCheck, Star, Calendar } from '@element-plus/icons-vue';

const userName = ref('技术员');
const currentDate = ref(new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }));

// 模拟数据，实际应从API获取
const stats = ref({
  pendingOrders: 5,
  inProgressOrders: 3,
  completedOrders: 24,
  averageRating: '4.7'
});

const recentOrders = ref([
  { orderId: 123, description: '发动机无法启动，需要检查火花塞', status: 'ASSIGNED', date: '2023-06-15 09:30:00' },
  { orderId: 122, description: '刹车系统异响，需要更换刹车片', status: 'IN_PROGRESS', date: '2023-06-14 14:20:00' },
  { orderId: 121, description: '空调不制冷，需要检查冷媒', status: 'COMPLETED', date: '2023-06-13 11:15:00' },
  { orderId: 120, description: '车灯不亮，需要更换灯泡', status: 'COMPLETED', date: '2023-06-12 16:45:00' }
]);

const todaySchedule = ref([
  { time: '09:00', title: '维修工单 #123', description: '发动机检修 - 车间A' },
  { time: '11:30', title: '团队会议', description: '讨论新的维修流程' },
  { time: '14:00', title: '维修工单 #124', description: '刹车系统维护 - 车间B' }
]);

// 根据状态返回不同的标签类型
const getStatusType = (status) => {
  const statusMap = {
    'PENDING_ASSIGNMENT': 'info',
    'ASSIGNED': 'warning',
    'IN_PROGRESS': 'primary',
    'AWAITING_PARTS': 'danger',
    'COMPLETED': 'success',
    'CANCELLED_BY_USER': 'info',
    'CANNOT_REPAIR': 'danger',
    'PENDING_USER_CONFIRMATION': 'warning'
  };
  return statusMap[status] || 'info';
};

onMounted(() => {
  // 从sessionStorage获取用户信息
  const storedUser = sessionStorage.getItem('user');
  if (storedUser) {
    const user = JSON.parse(storedUser);
    if (user.fullName) {
      userName.value = user.fullName;
    }
  }
  
  // 这里可以添加获取实际数据的API调用
});
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.welcome-section {
  margin-bottom: 30px;
}

.welcome-section h1 {
  color: #303133;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.date-display {
  color: #909399;
  font-size: 16px;
  margin: 0;
}

.stats-section {
  margin-bottom: 30px;
}

.stats-card {
  height: 120px;
  border-radius: 8px;
  transition: transform 0.3s;
}

.stats-card:hover {
  transform: translateY(-5px);
}

.stats-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stats-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stats-icon .el-icon {
  font-size: 28px;
  color: white;
}

.stats-icon.pending {
  background-color: #e6a23c;
}

.stats-icon.progress {
  background-color: #409eff;
}

.stats-icon.completed {
  background-color: #67c23a;
}

.stats-icon.rating {
  background-color: #f56c6c;
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

.main-content {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-weight: 600;
  color: #303133;
}

.view-all {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.view-all:hover {
  text-decoration: underline;
}

.recent-orders {
  margin-bottom: 20px;
}

.schedule-card {
  margin-bottom: 20px;
}

.schedule-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.schedule-item:last-child {
  border-bottom: none;
}

.schedule-time {
  font-weight: bold;
  color: #409eff;
  width: 60px;
}

.schedule-content {
  flex: 1;
}

.schedule-title {
  font-weight: 600;
  margin-bottom: 5px;
}

.schedule-desc {
  color: #909399;
  font-size: 13px;
}

.empty-schedule {
  text-align: center;
  padding: 30px 0;
  color: #909399;
}

.empty-schedule .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.quick-actions {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-buttons .el-button {
  width: 100%;
  justify-content: flex-start;
}
</style> 