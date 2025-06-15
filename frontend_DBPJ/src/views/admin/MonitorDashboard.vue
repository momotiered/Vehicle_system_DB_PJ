<template>
  <div class="monitor-dashboard">
    <div class="dashboard-header">
      <h1>监控面板</h1>
      <el-button type="primary" @click="refreshAll" :loading="loading">
        <i class="el-icon-refresh"></i>
        刷新数据
      </el-button>
    </div>

    <!-- 实时运营概览 -->
    <el-card class="overview-card" shadow="hover">
      <div slot="header" class="card-header">
        <span>实时运营概览</span>
        <el-tag type="info">{{ getCurrentTime() }}</el-tag>
      </div>
      <div class="overview-grid">
        <div class="stat-item">
          <div class="stat-number">{{ overviewStats.todayOrders || 0 }}</div>
          <div class="stat-label">今日工单</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ overviewStats.pendingOrders || 0 }}</div>
          <div class="stat-label">待分配</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ overviewStats.assignedOrders || 0 }}</div>
          <div class="stat-label">已分配</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ overviewStats.inProgressOrders || 0 }}</div>
          <div class="stat-label">进行中</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ overviewStats.completedOrders || 0 }}</div>
          <div class="stat-label">已完成</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ overviewStats.totalUsers || 0 }}</div>
          <div class="stat-label">总用户</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ overviewStats.activePersonnel || 0 }}</div>
          <div class="stat-label">在职人员</div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" class="dashboard-row">
      <!-- 工单状态分布 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>工单状态分布</span>
          </div>
          <div class="status-chart">
            <div v-for="(value, key) in orderStatusStats" :key="key" class="status-item">
              <div class="status-bar">
                <div class="status-label">{{ getStatusLabel(key) }}</div>
                <div class="status-progress">
                  <div 
                    class="status-fill" 
                    :style="{ width: getStatusPercentage(value) + '%', backgroundColor: getStatusColor(key) }"
                  ></div>
                </div>
                <div class="status-count">{{ value }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 财务统计 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>财务统计</span>
            <el-tag type="success">本月</el-tag>
          </div>
          <div class="financial-stats">
            <div class="financial-item">
              <div class="financial-label">总收入</div>
              <div class="financial-value">¥{{ formatCurrency(financialStats.totalRevenue) }}</div>
              <div class="financial-change">+12%</div>
            </div>
            <div class="financial-item">
              <div class="financial-label">材料费收入</div>
              <div class="financial-value">¥{{ formatCurrency(financialStats.materialRevenue) }}</div>
              <div class="financial-percent">{{ getMaterialPercentage() }}%</div>
            </div>
            <div class="financial-item">
              <div class="financial-label">人工费收入</div>
              <div class="financial-value">¥{{ formatCurrency(financialStats.laborRevenue) }}</div>
              <div class="financial-percent">{{ getLaborPercentage() }}%</div>
            </div>
            <div class="financial-item">
              <div class="financial-label">平均工单价值</div>
              <div class="financial-value">¥{{ formatCurrency(financialStats.avgOrderValue) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-row">
      <!-- 库存预警 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>库存预警</span>
            <el-badge :value="inventoryAlerts.length" type="danger">
              <i class="el-icon-warning"></i>
            </el-badge>
          </div>
          <div class="inventory-alerts">
            <div v-if="inventoryAlerts.length === 0" class="no-alerts">
              <i class="el-icon-success"></i>
              <p>库存状况良好</p>
            </div>
            <div v-else>
              <div v-for="alert in inventoryAlerts" :key="alert.materialId" class="alert-item">
                <div class="alert-icon">
                  <i class="el-icon-warning" :class="getAlertClass(alert.alertLevel)"></i>
                </div>
                <div class="alert-content">
                  <div class="alert-name">{{ alert.materialName }}</div>
                  <div class="alert-info">库存: {{ alert.stockQuantity }} | 安全库存: {{ alert.safeStock }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 人员统计 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>维修人员统计</span>
          </div>
          <div class="personnel-stats">
            <div class="work-type-distribution">
              <h4>工种分布</h4>
              <div v-for="(count, workType) in personnelStats.workTypeDistribution" :key="workType" class="work-type-item">
                <div class="work-type-label">{{ workType }}</div>
                <div class="work-type-bar">
                  <div class="work-type-fill" :style="{ width: getWorkTypePercentage(count) + '%' }"></div>
                </div>
                <div class="work-type-count">{{ count }}人</div>
              </div>
            </div>
            <div class="personnel-summary">
              <div class="summary-item">
                <div class="summary-number">{{ personnelStats.totalActivePersonnel || 0 }}</div>
                <div class="summary-label">总在职人员</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 客户满意度 -->
    <el-card shadow="hover">
      <div slot="header" class="card-header">
        <span>客户满意度</span>
      </div>
      <div class="satisfaction-stats">
        <div class="satisfaction-overview">
          <div class="avg-rating">
            <div class="rating-number">{{ satisfactionStats.avgRating || '--' }}</div>
            <div class="rating-stars">
              <i v-for="n in 5" :key="n" class="el-icon-star-on" :class="{ active: n <= Math.round(satisfactionStats.avgRating) }"></i>
            </div>
            <div class="rating-label">平均评分</div>
          </div>
          <div class="feedback-count">
            <div class="count-number">{{ satisfactionStats.totalFeedback || 0 }}</div>
            <div class="count-label">本月反馈数</div>
          </div>
        </div>
        <div class="rating-distribution">
          <div v-for="(count, rating) in satisfactionStats.ratingDistribution" :key="rating" class="rating-item">
            <div class="rating-label">{{ rating }}星</div>
            <div class="rating-bar">
              <div class="rating-fill" :style="{ width: getRatingPercentage(count) + '%' }"></div>
            </div>
            <div class="rating-count">{{ count }}</div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { 
  getOverviewStats, 
  getOrderStatusStats, 
  getFinancialStats, 
  getInventoryAlerts, 
  getPersonnelStats, 
  getSatisfactionStats 
} from '@/api/admin';

// 响应式数据
const loading = ref(false);
const overviewStats = reactive({});
const orderStatusStats = reactive({});
const financialStats = reactive({});
const inventoryAlerts = ref([]);
const personnelStats = reactive({});
const satisfactionStats = reactive({});

// 获取当前时间
const getCurrentTime = () => {
  return new Date().toLocaleString('zh-CN');
};

// 获取状态标签
const getStatusLabel = (status) => {
  const statusMap = {
    'PENDING_ASSIGNMENT': '待分配',
    'ASSIGNED': '已分配',
    'IN_PROGRESS': '进行中',
    'AWAITING_PARTS': '待配件',
    'COMPLETED': '已完成',
    'CANCELLED_BY_USER': '用户取消',
    'CANNOT_REPAIR': '无法维修',
    'PENDING_USER_CONFIRMATION': '待用户确认'
  };
  return statusMap[status] || status;
};

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'PENDING_ASSIGNMENT': '#E6A23C',
    'ASSIGNED': '#409EFF',
    'IN_PROGRESS': '#67C23A',
    'AWAITING_PARTS': '#F56C6C',
    'COMPLETED': '#67C23A',
    'CANCELLED_BY_USER': '#909399',
    'CANNOT_REPAIR': '#F56C6C',
    'PENDING_USER_CONFIRMATION': '#E6A23C'
  };
  return colorMap[status] || '#909399';
};

// 获取状态百分比
const getStatusPercentage = (value) => {
  const total = Object.values(orderStatusStats).reduce((sum, val) => sum + val, 0);
  return total > 0 ? (value / total * 100).toFixed(1) : 0;
};

// 格式化金额
const formatCurrency = (amount) => {
  if (!amount) return '0.00';
  return parseFloat(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2 });
};

// 获取材料费百分比
const getMaterialPercentage = () => {
  const total = (financialStats.materialRevenue || 0) + (financialStats.laborRevenue || 0);
  return total > 0 ? Math.round((financialStats.materialRevenue || 0) / total * 100) : 0;
};

// 获取人工费百分比
const getLaborPercentage = () => {
  const total = (financialStats.materialRevenue || 0) + (financialStats.laborRevenue || 0);
  return total > 0 ? Math.round((financialStats.laborRevenue || 0) / total * 100) : 0;
};

// 获取预警级别样式
const getAlertClass = (level) => {
  return {
    'alert-critical': level === '紧急',
    'alert-warning': level === '警告',
    'alert-normal': level === '正常'
  };
};

// 获取工种百分比
const getWorkTypePercentage = (count) => {
  const total = Object.values(personnelStats.workTypeDistribution || {}).reduce((sum, val) => sum + val, 0);
  return total > 0 ? (count / total * 100).toFixed(1) : 0;
};

// 获取评分百分比
const getRatingPercentage = (count) => {
  const total = Object.values(satisfactionStats.ratingDistribution || {}).reduce((sum, val) => sum + val, 0);
  return total > 0 ? (count / total * 100).toFixed(1) : 0;
};

// 加载所有数据
const loadAllData = async () => {
  loading.value = true;
  try {
    const [
      overviewRes,
      orderStatusRes,
      financialRes,
      inventoryRes,
      personnelRes,
      satisfactionRes
    ] = await Promise.all([
      getOverviewStats().catch(() => ({ data: {} })),
      getOrderStatusStats().catch(() => ({ data: {} })),
      getFinancialStats().catch(() => ({ data: {} })),
      getInventoryAlerts().catch(() => ({ data: [] })),
      getPersonnelStats().catch(() => ({ data: {} })),
      getSatisfactionStats().catch(() => ({ data: {} }))
    ]);

    Object.assign(overviewStats, overviewRes.data);
    Object.assign(orderStatusStats, orderStatusRes.data);
    Object.assign(financialStats, financialRes.data);
    inventoryAlerts.value = inventoryRes.data;
    Object.assign(personnelStats, personnelRes.data);
    Object.assign(satisfactionStats, satisfactionRes.data);

  } catch (error) {
    console.error('加载监控数据失败:', error);
    ElMessage.error('加载监控数据失败');
  } finally {
    loading.value = false;
  }
};

// 刷新所有数据
const refreshAll = () => {
  loadAllData();
  ElMessage.success('数据已刷新');
};

// 组件挂载时加载数据
onMounted(() => {
  loadAllData();
});
</script>

<style scoped>
.monitor-dashboard {
  padding: 20px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dashboard-header h1 {
  margin: 0;
  color: #303133;
}

.dashboard-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 实时概览样式 */
.overview-card {
  margin-bottom: 20px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

/* 工单状态样式 */
.status-chart {
  padding: 10px 0;
}

.status-item {
  margin-bottom: 15px;
}

.status-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-label {
  min-width: 80px;
  font-size: 14px;
  color: #606266;
}

.status-progress {
  flex: 1;
  height: 20px;
  background: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
}

.status-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.status-count {
  min-width: 30px;
  font-weight: bold;
  color: #303133;
}

/* 财务统计样式 */
.financial-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.financial-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.financial-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.financial-value {
  font-size: 24px;
  font-weight: bold;
  color: #67C23A;
  margin-bottom: 4px;
}

.financial-change {
  font-size: 12px;
  color: #67C23A;
}

.financial-percent {
  font-size: 12px;
  color: #909399;
}

/* 库存预警样式 */
.inventory-alerts {
  max-height: 300px;
  overflow-y: auto;
}

.no-alerts {
  text-align: center;
  padding: 40px;
  color: #67C23A;
}

.no-alerts i {
  font-size: 48px;
  margin-bottom: 10px;
}

.alert-item {
  display: flex;
  align-items: center;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 6px;
  background: #fff2f0;
  border-left: 4px solid #F56C6C;
}

.alert-icon {
  margin-right: 10px;
  font-size: 20px;
}

.alert-critical {
  color: #F56C6C;
}

.alert-warning {
  color: #E6A23C;
}

.alert-normal {
  color: #67C23A;
}

.alert-content {
  flex: 1;
}

.alert-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.alert-info {
  font-size: 12px;
  color: #606266;
}

/* 人员统计样式 */
.personnel-stats {
  display: flex;
  gap: 20px;
}

.work-type-distribution {
  flex: 1;
}

.work-type-distribution h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
}

.work-type-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}

.work-type-label {
  min-width: 60px;
  font-size: 14px;
  color: #606266;
}

.work-type-bar {
  flex: 1;
  height: 16px;
  background: #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

.work-type-fill {
  height: 100%;
  background: #409EFF;
  transition: width 0.3s ease;
}

.work-type-count {
  min-width: 40px;
  font-size: 14px;
  color: #303133;
}

.personnel-summary {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-width: 120px;
}

.summary-item {
  text-align: center;
}

.summary-number {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.summary-label {
  font-size: 14px;
  color: #606266;
}

/* 客户满意度样式 */
.satisfaction-stats {
  display: flex;
  gap: 40px;
}

.satisfaction-overview {
  display: flex;
  gap: 40px;
}

.avg-rating {
  text-align: center;
}

.rating-number {
  font-size: 48px;
  font-weight: bold;
  color: #E6A23C;
  margin-bottom: 8px;
}

.rating-stars {
  margin-bottom: 8px;
}

.rating-stars i {
  font-size: 20px;
  color: #DCDFE6;
  margin: 0 2px;
}

.rating-stars i.active {
  color: #E6A23C;
}

.rating-label {
  font-size: 14px;
  color: #606266;
}

.feedback-count {
  text-align: center;
}

.count-number {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.count-label {
  font-size: 14px;
  color: #606266;
}

.rating-distribution {
  flex: 1;
}

.rating-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}

.rating-bar {
  flex: 1;
  height: 20px;
  background: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
}

.rating-fill {
  height: 100%;
  background: #E6A23C;
  transition: width 0.3s ease;
}

.rating-count {
  min-width: 30px;
  font-weight: bold;
  color: #303133;
}
</style> 