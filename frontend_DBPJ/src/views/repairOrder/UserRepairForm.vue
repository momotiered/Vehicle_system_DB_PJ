<template>
  <div class="user-repair-form">
    <el-page-header @back="$router.go(-1)">
      <template #content>
        <span class="text-large font-600 mr-3">提交维修申请</span>
      </template>
    </el-page-header>

    <el-card class="form-card">
      <el-form
        ref="repairFormRef"
        :model="repairForm"
        :rules="repairRules"
        label-width="120px"
        @submit.prevent="handleSubmit"
      >
        <!-- 车辆选择 -->
        <el-form-item label="选择车辆" prop="vehicleId">
          <el-select
            v-model="repairForm.vehicleId"
            placeholder="请选择需要维修的车辆"
            filterable
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="vehicle in userVehicles"
              :key="vehicle.vehicleId"
              :label="`${vehicle.licensePlate} - ${vehicle.brand} ${vehicle.model}`"
              :value="vehicle.vehicleId"
            />
          </el-select>
          
          <!-- 未添加车辆时的提示 -->
          <div v-if="userVehicles.length === 0" class="mt-2 text-warning">
            <el-alert
              title="您还没有添加车辆信息"
              type="warning"
              show-icon
              :closable="false"
            >
              <template #default>
                请先 <el-link type="primary" @click="$router.push('/vehicles/create')">添加车辆</el-link> 后再提交维修申请
              </template>
            </el-alert>
          </div>
        </el-form-item>

        <!-- 问题描述 -->
        <el-form-item label="问题描述" prop="descriptionOfIssue">
          <el-input
            v-model="repairForm.descriptionOfIssue"
            type="textarea"
            placeholder="请详细描述车辆故障问题，例如：何时发生、什么情况下出现、有什么症状等"
            :rows="4"
          />
        </el-form-item>

        <!-- 紧急程度 -->
        <el-form-item label="紧急程度" prop="urgencyLevel">
          <el-radio-group v-model="repairForm.urgencyLevel">
            <el-radio value="LOW">一般</el-radio>
            <el-radio value="MEDIUM">紧急</el-radio>
            <el-radio value="HIGH">非常紧急</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 期望维修时间 -->
        <el-form-item label="期望维修时间" prop="expectedServiceDate">
          <el-date-picker
            v-model="repairForm.expectedServiceDate"
            type="date"
            placeholder="选择期望的维修日期"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 补充说明 -->
        <el-form-item label="补充说明">
          <el-input
            v-model="repairForm.notes"
            type="textarea"
            placeholder="任何其他需要说明的情况（选填）"
            :rows="2"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
            :disabled="userVehicles.length === 0"
          >
            {{ loading ? '提交中...' : '提交申请' }}
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="$router.go(-1)">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import repairOrderApi from '@/api/repairOrder'
import vehicleApi from '@/api/vehicle'

const router = useRouter()
const userStore = useUserStore()
const repairFormRef = ref()
const loading = ref(false)
const userVehicles = ref([])

// 获取当前用户ID
const userId = userStore.user?.userId

// 维修表单数据
const repairForm = reactive({
  vehicleId: null,
  descriptionOfIssue: '',
  urgencyLevel: 'MEDIUM',
  expectedServiceDate: null,
  notes: ''
})

// 表单验证规则
const repairRules = {
  vehicleId: [
    { required: true, message: '请选择车辆', trigger: 'change' }
  ],
  descriptionOfIssue: [
    { required: true, message: '请描述车辆问题', trigger: 'blur' },
    { min: 10, message: '问题描述至少10个字符', trigger: 'blur' }
  ],
  urgencyLevel: [
    { required: true, message: '请选择紧急程度', trigger: 'change' }
  ]
}

// 加载用户车辆列表
const loadUserVehicles = async () => {
  try {
    if (!userId) {
      ElMessage.warning('未能获取用户信息，请重新登录')
      return
    }
    
    loading.value = true
    const vehicles = await vehicleApi.getVehiclesByUserId(userId)
    userVehicles.value = vehicles || []
    
    // 如果有URL查询参数中的vehicleId，自动选择该车辆
    const urlParams = new URLSearchParams(window.location.search)
    const preselectedVehicleId = urlParams.get('vehicleId')
    
    if (preselectedVehicleId && userVehicles.value.some(v => v.vehicleId == preselectedVehicleId)) {
      repairForm.vehicleId = parseInt(preselectedVehicleId)
    }
  } catch (error) {
    console.error('加载用户车辆失败:', error)
    ElMessage.error('加载车辆信息失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!repairFormRef.value) return

  try {
    await repairFormRef.value.validate()
    
    loading.value = true
    
    // 构建维修工单数据
    const repairOrderData = {
      vehicleId: repairForm.vehicleId,
      userId: userId,
      descriptionOfIssue: repairForm.descriptionOfIssue,
      urgencyLevel: repairForm.urgencyLevel,
      expectedServiceDate: repairForm.expectedServiceDate ? 
        new Date(repairForm.expectedServiceDate).toISOString().split('T')[0] : null,
      notes: repairForm.notes || null,
      status: 'PENDING_ASSIGNMENT' // 默认状态为待分配
    }
    
    console.log('提交的数据:', repairOrderData) // 添加调试日志
    
    // 调用API创建维修工单
    await repairOrderApi.createUserRepairRequest(repairOrderData)
    
    ElMessage.success('维修申请提交成功')
    router.push('/user-center')
  } catch (error) {
    console.error('提交维修申请失败:', error)
    // 显示更详细的错误信息
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.response?.status === 400) {
      ElMessage.error('请求数据格式错误，请检查输入')
    } else if (error.response?.status === 403) {
      ElMessage.error('权限不足或车辆不属于当前用户')
    } else {
      ElMessage.error('提交维修申请失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 重置表单
const handleReset = () => {
  if (repairFormRef.value) {
    repairFormRef.value.resetFields()
  }
}

// 组件挂载时加载用户车辆数据
onMounted(() => {
  loadUserVehicles()
})
</script>

<style scoped>
.user-repair-form {
  padding: 20px;
}

.form-card {
  margin-top: 20px;
}

.text-warning {
  color: #e6a23c;
  font-size: 14px;
}

.mt-2 {
  margin-top: 8px;
}
</style> 