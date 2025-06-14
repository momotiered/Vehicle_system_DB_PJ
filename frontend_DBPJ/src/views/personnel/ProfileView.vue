<template>
  <div class="profile-container">
    <el-card class="profile-card" v-if="profile">
      <template #header>
        <div class="card-header">
          <h2>个人资料</h2>
          <el-tag :type="profile.isActive ? 'success' : 'danger'" size="large">
            {{ profile.isActive ? '在职' : '离职' }}
          </el-tag>
        </div>
      </template>
      
      <div class="profile-content">
        <div class="profile-avatar">
          <el-avatar :size="100" icon="UserFilled"></el-avatar>
          <h3 class="profile-name">{{ profile.fullName }}</h3>
          <div class="profile-role">{{ profile.workType }}</div>
        </div>
        
        <el-divider />
        
        <el-descriptions :column="1" border class="profile-details">
          <el-descriptions-item label="用户名" label-class-name="label-bold">
            <div class="description-content">{{ profile.username }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="时薪" label-class-name="label-bold">
            <div class="description-content price">¥{{ profile.hourlyRate }}/小时</div>
          </el-descriptions-item>
          <el-descriptions-item label="联系电话" label-class-name="label-bold">
            <div class="description-content">{{ profile.contactPhone }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="入职日期" label-class-name="label-bold">
            <div class="description-content">{{ new Date(profile.hireDate).toLocaleDateString() }}</div>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="profile-stats">
          <div class="stat-card">
            <div class="stat-value">{{ completedOrders }}</div>
            <div class="stat-label">已完成工单</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ totalHours }}</div>
            <div class="stat-label">总工时</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ averageRating.toFixed(1) }}</div>
            <div class="stat-label">平均评分</div>
          </div>
        </div>
      </div>
    </el-card>
    
    <div v-else class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getPersonnelProfile } from '@/api/repairPersonnel.js';

const router = useRouter();
const profile = ref(null);
const userInfo = ref(null);

// 模拟数据，实际应从API获取
const completedOrders = ref(24);
const totalHours = ref(128);
const averageRating = ref(4.7);

onMounted(async () => {
  const storedUser = sessionStorage.getItem('user');
  if (storedUser) {
    userInfo.value = JSON.parse(storedUser);
    if (!userInfo.value || !userInfo.value.personnelId) {
      ElMessage.error('无法获取维修人员ID，请重新登录。');
      router.push('/login');
      return;
    }
    try {
      profile.value = await getPersonnelProfile(userInfo.value.personnelId);
    } catch (error) {
      ElMessage.error(error.message);
    }
  } else {
    ElMessage.error('请先登录');
    router.push('/login');
  }
});
</script>

<style scoped>
.profile-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
  font-weight: 600;
}

.profile-content {
  padding: 20px 0;
}

.profile-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.profile-name {
  margin: 15px 0 5px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.profile-role {
  color: #909399;
  font-size: 16px;
}

.profile-details {
  margin: 20px 0;
}

:deep(.label-bold) {
  font-weight: 600;
  background-color: #f5f7fa;
  width: 120px;
}

.description-content {
  padding: 10px 0;
  font-size: 16px;
}

.price {
  color: #f56c6c;
  font-weight: 600;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 30px;
  padding: 20px 0;
  background-color: #f0f9eb;
  border-radius: 8px;
}

.stat-card {
  text-align: center;
  padding: 10px 20px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #67c23a;
  margin-bottom: 5px;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

.loading-container {
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-width: 800px;
  margin: 0 auto;
}
</style> 