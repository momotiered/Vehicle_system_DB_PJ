<template>
  <div class="repair-order-form">
    <el-page-header @back="$router.go(-1)">
      <template #content>
        <span class="text-large font-600 mr-3">{{ isEdit ? '编辑维修单' : '新建维修单' }}</span>
      </template>
    </el-page-header>

    <el-card class="form-card">
      <el-form
        ref="repairOrderFormRef"
        :model="repairOrderForm"
        :rules="repairOrderRules"
        label-width="120px"
        @submit.prevent="handleSubmit"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择车辆" prop="vehicleId">
              <el-select
                v-model="repairOrderForm.vehicleId"
                placeholder="请选择车辆"
                filterable
                clearable
                style="width: 100%"
                @change="handleVehicleChange"
              >
                <el-option
                  v-for="vehicle in vehicleOptions"
                  :key="vehicle.vehicleId"
                  :label="`${vehicle.licensePlate} - ${vehicle.brand} ${vehicle.model} (${vehicle.user.fullName})`"
                  :value="vehicle.vehicleId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="车主" v-if="selectedVehicle">
              <el-input
                :value="selectedVehicle.user.fullName"
                disabled
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="问题描述" prop="problemDescription">
          <el-input
            v-model="repairOrderForm.problemDescription"
            type="textarea"
            placeholder="请详细描述车辆故障问题"
            :rows="4"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="维修状态" prop="status">
              <el-select
                v-model="repairOrderForm.status"
                placeholder="请选择状态"
                style="width: 100%"
              >
                <el-option label="待维修" value="PENDING" />
                <el-option label="维修中" value="IN_PROGRESS" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="预估费用" prop="estimatedCost">
              <el-input-number
                v-model="repairOrderForm.estimatedCost"
                :min="0"
                :precision="2"
                placeholder="预估费用"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="实际费用" prop="actualCost">
              <el-input-number
                v-model="repairOrderForm.actualCost"
                :min="0"
                :precision="2"
                placeholder="实际费用"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="维修开始时间" prop="repairStartDate">
              <el-date-picker
                v-model="repairOrderForm.repairStartDate"
                type="datetime"
                placeholder="选择维修开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="维修完成时间" prop="repairEndDate">
              <el-date-picker
                v-model="repairOrderForm.repairEndDate"
                type="datetime"
                placeholder="选择维修完成时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="维修备注">
          <el-input
            v-model="repairOrderForm.repairNotes"
            type="textarea"
            placeholder="维修过程记录、注意事项等"
            :rows="3"
          />
        </el-form-item>

        <el-form-item v-if="isEdit">
          <el-row :gutter="20">
            <el-col :span="12">
              <span class="form-label">创建时间：</span>
              <span>{{ formatDate(repairOrderForm.createDate) }}</span>
            </el-col>
            <el-col :span="12">
              <span class="form-label">最后更新：</span>
              <span>{{ formatDate(repairOrderForm.lastModified) }}</span>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ loading ? '保存中...' : '保存' }}
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="$router.go(-1)">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import repairOrderApi from '@/api/repairOrder'
import vehicleApi from '@/api/vehicle'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const repairOrderFormRef = ref()
const loading = ref(false)
const vehicleOptions = ref([])

// 判断是否为编辑模式
const isEdit = computed(() => !!route.params.id)
const repairOrderId = computed(() => parseInt(route.params.id))

const repairOrderForm = reactive({
  vehicleId: null,
  userId: null,
  problemDescription: '',
  status: 'PENDING',
  estimatedCost: null,
  actualCost: null,
  repairStartDate: null,
  repairEndDate: null,
  repairNotes: '',
  createDate: null,
  lastModified: null
})

// 选中的车辆信息
const selectedVehicle = computed(() => {
  return vehicleOptions.value.find(v => v.vehicleId === repairOrderForm.vehicleId)
})

const repairOrderRules = {
  vehicleId: [
    { required: true, message: '请选择车辆', trigger: 'change' }
  ],
  problemDescription: [
    { required: true, message: '请描述车辆问题', trigger: 'blur' },
    { min: 10, message: '问题描述至少10个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择维修状态', trigger: 'change' }
  ],
  estimatedCost: [
    { type: 'number', min: 0, message: '预估费用不能为负数', trigger: 'blur' }
  ],
  actualCost: [
    { type: 'number', min: 0, message: '实际费用不能为负数', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 车辆选择变化处理
const handleVehicleChange = (vehicleId) => {
  const vehicle = vehicleOptions.value.find(v => v.vehicleId === vehicleId)
  if (vehicle) {
    repairOrderForm.userId = vehicle.user.userId
  }
}

// 加载车辆选项
const loadVehicleOptions = async () => {
  try {
    const vehicles = await vehicleApi.getAllVehicles()
    vehicleOptions.value = vehicles || []
  } catch (error) {
    console.error('加载车辆列表失败:', error)
    ElMessage.error('加载车辆列表失败')
  }
}

// 加载维修单数据（编辑模式）
const loadRepairOrderData = async () => {
  if (!isEdit.value) return
  
  try {
    loading.value = true
    const repairOrderData = await repairOrderApi.getRepairOrderById(repairOrderId.value)
    
    Object.keys(repairOrderForm).forEach(key => {
      if (repairOrderData[key] !== undefined) {
        repairOrderForm[key] = repairOrderData[key]
      }
    })
    
    // 如果维修单有关联的车辆和用户对象，提取ID
    if (repairOrderData.vehicle) {
      repairOrderForm.vehicleId = repairOrderData.vehicle.vehicleId
    }
    if (repairOrderData.user) {
      repairOrderForm.userId = repairOrderData.user.userId
    }
  } catch (error) {
    console.error('加载维修单数据失败:', error)
    ElMessage.error('加载维修单数据失败')
    router.go(-1)
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!repairOrderFormRef.value) return

  try {
    const valid = await repairOrderFormRef.value.validate()
    if (!valid) return

    loading.value = true
    
    const repairOrderData = {
      vehicleId: repairOrderForm.vehicleId,
      userId: repairOrderForm.userId,
      problemDescription: repairOrderForm.problemDescription,
      status: repairOrderForm.status,
      estimatedCost: repairOrderForm.estimatedCost,
      actualCost: repairOrderForm.actualCost,
      repairStartDate: repairOrderForm.repairStartDate,
      repairEndDate: repairOrderForm.repairEndDate,
      repairNotes: repairOrderForm.repairNotes || null
    }

    if (isEdit.value) {
      await repairOrderApi.updateRepairOrder(repairOrderId.value, repairOrderData)
      ElMessage.success('维修单更新成功')
    } else {
      await repairOrderApi.createRepairOrder(repairOrderData)
      ElMessage.success('维修单创建成功')
    }
    
    router.push('/repair-orders')
  } catch (error) {
    console.error('保存维修单失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('保存失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 重置表单
const handleReset = () => {
  if (repairOrderFormRef.value) {
    repairOrderFormRef.value.resetFields()
  }
  
  if (isEdit.value) {
    loadRepairOrderData()
  } else {
    // 重置为默认值
    Object.assign(repairOrderForm, {
      vehicleId: null,
      userId: null,
      problemDescription: '',
      status: 'PENDING',
      estimatedCost: null,
      actualCost: null,
      repairStartDate: null,
      repairEndDate: null,
      repairNotes: ''
    })
  }
}

onMounted(() => {
  loadVehicleOptions()
  if (isEdit.value) {
    loadRepairOrderData()
  }
})
</script>

<style scoped>
.repair-order-form {
  padding: 20px;
}

.form-card {
  margin-top: 20px;
  max-width: 1000px;
}

.form-label {
  font-weight: bold;
  color: #606266;
}
</style> 