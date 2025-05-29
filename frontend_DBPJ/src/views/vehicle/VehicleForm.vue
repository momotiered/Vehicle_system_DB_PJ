<template>
  <div class="vehicle-form">
    <el-page-header @back="$router.go(-1)">
      <template #content>
        <span class="text-large font-600 mr-3">{{ isEdit ? '编辑车辆' : '添加车辆' }}</span>
      </template>
    </el-page-header>

    <el-card class="form-card">
      <el-form
        ref="vehicleFormRef"
        :model="vehicleForm"
        :rules="vehicleRules"
        label-width="100px"
        @submit.prevent="handleSubmit"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="车牌号" prop="licensePlate">
              <el-input
                v-model="vehicleForm.licensePlate"
                placeholder="请输入车牌号"
                clearable
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="车主" prop="userId">
              <el-select
                v-model="vehicleForm.userId"
                placeholder="请选择车主"
                filterable
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="user in userOptions"
                  :key="user.userId"
                  :label="`${user.fullName} (${user.username})`"
                  :value="user.userId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="品牌" prop="brand">
              <el-input
                v-model="vehicleForm.brand"
                placeholder="请输入品牌"
                clearable
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="型号" prop="model">
              <el-input
                v-model="vehicleForm.model"
                placeholder="请输入型号"
                clearable
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="年份" prop="year">
              <el-input-number
                v-model="vehicleForm.year"
                :min="1900"
                :max="currentYear"
                placeholder="请输入年份"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="颜色" prop="color">
              <el-input
                v-model="vehicleForm.color"
                placeholder="请输入颜色"
                clearable
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12" v-if="isEdit">
            <el-form-item label="注册时间">
              <el-input
                :value="formatDate(vehicleForm.registrationDate)"
                disabled
              />
            </el-form-item>
          </el-col>
        </el-row>

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
import vehicleApi from '@/api/vehicle'
import userApi from '@/api/user'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const vehicleFormRef = ref()
const loading = ref(false)
const userOptions = ref([])

// 判断是否为编辑模式
const isEdit = computed(() => !!route.params.id)
const vehicleId = computed(() => parseInt(route.params.id))
const currentYear = new Date().getFullYear()

const vehicleForm = reactive({
  licensePlate: '',
  userId: null,
  brand: '',
  model: '',
  year: null,
  color: '',
  registrationDate: null
})

// 验证车牌号
const validateLicensePlate = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入车牌号'))
    return
  }
  // 简单的车牌号验证（中国车牌号格式）
  const plateRegex = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$/
  if (!plateRegex.test(value)) {
    callback(new Error('请输入正确的车牌号格式'))
  } else {
    callback()
  }
}

const vehicleRules = {
  licensePlate: [
    { validator: validateLicensePlate, trigger: 'blur' }
  ],
  userId: [
    { required: true, message: '请选择车主', trigger: 'change' }
  ],
  brand: [
    { required: true, message: '请输入品牌', trigger: 'blur' }
  ],
  model: [
    { required: true, message: '请输入型号', trigger: 'blur' }
  ],
  year: [
    { required: true, message: '请输入年份', trigger: 'blur' },
    { type: 'number', min: 1900, max: currentYear, message: `年份必须在1900-${currentYear}之间`, trigger: 'blur' }
  ],
  color: [
    { required: true, message: '请输入颜色', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 加载用户选项
const loadUserOptions = async () => {
  try {
    const users = await userApi.getAllUsers()
    userOptions.value = users || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  }
}

// 加载车辆数据（编辑模式）
const loadVehicleData = async () => {
  if (!isEdit.value) return
  
  try {
    loading.value = true
    const vehicleData = await vehicleApi.getVehicleById(vehicleId.value)
    
    Object.keys(vehicleForm).forEach(key => {
      if (vehicleData[key] !== undefined) {
        vehicleForm[key] = vehicleData[key]
      }
    })
    
    // 如果车辆有关联的用户对象，提取用户ID
    if (vehicleData.user) {
      vehicleForm.userId = vehicleData.user.userId
    }
  } catch (error) {
    console.error('加载车辆数据失败:', error)
    ElMessage.error('加载车辆数据失败')
    router.go(-1)
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!vehicleFormRef.value) return

  try {
    const valid = await vehicleFormRef.value.validate()
    if (!valid) return

    loading.value = true
    
    const vehicleData = {
      licensePlate: vehicleForm.licensePlate,
      userId: vehicleForm.userId,
      brand: vehicleForm.brand,
      model: vehicleForm.model,
      year: vehicleForm.year,
      color: vehicleForm.color
    }

    if (isEdit.value) {
      await vehicleApi.updateVehicle(vehicleId.value, vehicleData)
      ElMessage.success('车辆更新成功')
    } else {
      await vehicleApi.createVehicle(vehicleData)
      ElMessage.success('车辆创建成功')
    }
    
    router.push('/vehicles')
  } catch (error) {
    console.error('保存车辆失败:', error)
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
  if (vehicleFormRef.value) {
    vehicleFormRef.value.resetFields()
  }
  
  if (isEdit.value) {
    loadVehicleData()
  }
}

onMounted(() => {
  loadUserOptions()
  if (isEdit.value) {
    loadVehicleData()
  }
})
</script>

<style scoped>
.vehicle-form {
  padding: 20px;
}

.form-card {
  margin-top: 20px;
  max-width: 800px;
}
</style> 