<template>
  <div class="repair-order-detail">
    <el-page-header @back="$router.go(-1)">
      <template #content>
        <span class="text-large font-600 mr-3">维修单详情</span>
      </template>
      <template #extra>
        <el-button
          type="primary"
          @click="$router.push(`/repair-orders/${repairOrderId}/edit`)"
          v-if="repairOrder.status !== 'COMPLETED'"
        >
          编辑维修单
        </el-button>
      </template>
    </el-page-header>

    <div v-loading="loading">
      <el-row :gutter="20">
        <!-- 基本信息 -->
        <el-col :span="16">
          <el-card class="detail-card">
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
                <el-tag :type="getStatusType(repairOrder.status)" v-if="repairOrder.status">
                  {{ getStatusText(repairOrder.status) }}
                </el-tag>
              </div>
            </template>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="维修单号">
                {{ repairOrder.repairOrderId || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDate(repairOrder.createDate) }}
              </el-descriptions-item>
              <el-descriptions-item label="车牌号">
                {{ repairOrder.vehicle?.licensePlate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="车主">
                {{ repairOrder.user?.fullName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="车辆品牌">
                {{ repairOrder.vehicle?.brand || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="车辆型号">
                {{ repairOrder.vehicle?.model || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="车辆年份">
                {{ repairOrder.vehicle?.year || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="车辆颜色">
                {{ repairOrder.vehicle?.color || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                {{ repairOrder.user?.contactPhone || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="联系邮箱">
                {{ repairOrder.user?.contactEmail || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 维修信息 -->
          <el-card class="detail-card">
            <template #header>
              <span>维修信息</span>
            </template>

            <el-descriptions :column="1" border>
              <el-descriptions-item label="问题描述">
                <div class="description-text">
                  {{ repairOrder.problemDescription || '-' }}
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="维修备注" v-if="repairOrder.repairNotes">
                <div class="description-text">
                  {{ repairOrder.repairNotes }}
                </div>
              </el-descriptions-item>
            </el-descriptions>

            <el-descriptions :column="2" border style="margin-top: 20px;">
              <el-descriptions-item label="预估费用">
                <span class="cost-text">¥{{ repairOrder.estimatedCost || 0 }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="实际费用">
                <span class="cost-text" :class="{ 'actual-cost': repairOrder.actualCost }">
                  ¥{{ repairOrder.actualCost || 0 }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="维修开始时间">
                {{ formatDate(repairOrder.repairStartDate) }}
              </el-descriptions-item>
              <el-descriptions-item label="维修完成时间">
                {{ formatDate(repairOrder.repairEndDate) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <!-- 侧边栏信息 -->
        <el-col :span="8">
          <!-- 状态时间线 -->
          <el-card class="detail-card">
            <template #header>
              <span>状态跟踪</span>
            </template>

            <el-timeline>
              <el-timeline-item
                timestamp="创建时间"
                :timestamp-value="formatDate(repairOrder.createDate)"
                type="primary"
              >
                维修单已创建
              </el-timeline-item>
              
              <el-timeline-item
                v-if="repairOrder.repairStartDate"
                timestamp="开始维修"
                :timestamp-value="formatDate(repairOrder.repairStartDate)"
                type="warning"
              >
                开始维修作业
              </el-timeline-item>
              
              <el-timeline-item
                v-if="repairOrder.repairEndDate"
                timestamp="维修完成"
                :timestamp-value="formatDate(repairOrder.repairEndDate)"
                type="success"
              >
                维修作业完成
              </el-timeline-item>
              
              <el-timeline-item
                v-if="repairOrder.lastModified && repairOrder.lastModified !== repairOrder.createDate"
                timestamp="最后更新"
                :timestamp-value="formatDate(repairOrder.lastModified)"
                type="info"
              >
                信息已更新
              </el-timeline-item>
            </el-timeline>
          </el-card>

          <!-- 费用统计 -->
          <el-card class="detail-card" v-if="repairOrder.estimatedCost || repairOrder.actualCost">
            <template #header>
              <span>费用统计</span>
            </template>

            <div class="cost-stats">
              <div class="cost-item">
                <span class="cost-label">预估费用：</span>
                <span class="cost-value estimated">¥{{ repairOrder.estimatedCost || 0 }}</span>
              </div>
              <div class="cost-item">
                <span class="cost-label">实际费用：</span>
                <span class="cost-value actual">¥{{ repairOrder.actualCost || 0 }}</span>
              </div>
              <div class="cost-item" v-if="repairOrder.estimatedCost && repairOrder.actualCost">
                <span class="cost-label">费用差额：</span>
                <span
                  class="cost-value"
                  :class="costDifference >= 0 ? 'positive' : 'negative'"
                >
                  {{ costDifference >= 0 ? '+' : '' }}¥{{ Math.abs(costDifference) }}
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import repairOrderApi from '@/api/repairOrder'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const loading = ref(false)

const repairOrderId = computed(() => parseInt(route.params.id))

const repairOrder = reactive({
  repairOrderId: null,
  vehicle: {},
  user: {},
  problemDescription: '',
  status: '',
  estimatedCost: null,
  actualCost: null,
  repairStartDate: null,
  repairEndDate: null,
  repairNotes: '',
  createDate: null,
  lastModified: null
})

// 费用差额计算
const costDifference = computed(() => {
  if (!repairOrder.estimatedCost || !repairOrder.actualCost) return 0
  return repairOrder.actualCost - repairOrder.estimatedCost
})

// 状态相关方法
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'IN_PROGRESS': 'info',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待维修',
    'IN_PROGRESS': '维修中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 加载维修单详情
const loadRepairOrderDetail = async () => {
  try {
    loading.value = true
    const data = await repairOrderApi.getRepairOrderById(repairOrderId.value)
    
    Object.assign(repairOrder, data)
  } catch (error) {
    console.error('加载维修单详情失败:', error)
    ElMessage.error('加载维修单详情失败')
    router.go(-1)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (repairOrderId.value) {
    loadRepairOrderDetail()
  }
})
</script>

<style scoped>
.repair-order-detail {
  padding: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.description-text {
  white-space: pre-wrap;
  line-height: 1.6;
}

.cost-text {
  font-weight: bold;
  font-size: 16px;
}

.actual-cost {
  color: #e6a23c;
}

.cost-stats {
  padding: 10px 0;
}

.cost-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.cost-label {
  font-weight: 500;
  color: #606266;
}

.cost-value {
  font-weight: bold;
  font-size: 16px;
}

.cost-value.estimated {
  color: #909399;
}

.cost-value.actual {
  color: #e6a23c;
}

.cost-value.positive {
  color: #f56c6c;
}

.cost-value.negative {
  color: #67c23a;
}
</style> 