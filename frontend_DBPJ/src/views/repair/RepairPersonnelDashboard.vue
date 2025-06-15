<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.pendingOrders || 0 }}</div>
            <div class="stat-label">待处理工单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.inProgressOrders || 0 }}</div>
            <div class="stat-label">进行中工单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.monthlyCompletedOrders || 0 }}</div>
            <div class="stat-label">本月完成工单</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt-4">
      <template #header>
        <div class="card-header">
          <span>我的工单</span>
          <el-radio-group v-model="activeTab" size="small">
            <el-radio-button label="pending">待处理</el-radio-button>
            <el-radio-button label="inProgress">进行中</el-radio-button>
            <el-radio-button label="completed">已完成</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      // ... existing code ...
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';

const stats = ref({
  pendingOrders: 0,
  inProgressOrders: 0,
  monthlyCompletedOrders: 0
});

const activeTab = ref('pending');

const fetchStats = async () => {
  try {
    const response = await axios.get('/api/repair-personnel/stats');
    stats.value = response.data;
  } catch (error) {
    ElMessage.error('获取统计数据失败');
    console.error('Error fetching stats:', error);
  }
};

onMounted(() => {
  fetchStats();
});
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stat-card {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  text-align: center;
  width: 100%;
}

.stat-number {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 16px;
  color: #606266;
  white-space: nowrap;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mt-4 {
  margin-top: 1rem;
}

:deep(.el-radio-button__inner) {
  padding: 8px 15px;
  min-width: 80px;
  text-align: center;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #409EFF;
  border-color: #409EFF;
  box-shadow: -1px 0 0 0 #409EFF;
}
</style> 