<template>
  <div class="user-center">
    <el-page-header>
      <template #content>
        <span class="text-large font-600 mr-3">用户中心</span>
      </template>
    </el-page-header>

    <el-row :gutter="20" class="mt-4">
      <!-- 左侧用户信息卡片 -->
      <el-col :span="6">
        <el-card class="user-info-card">
          <template #header>
            <div class="card-header">
              <h3>个人信息</h3>
              <el-button type="primary" size="small" text @click="handleEditProfile">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
            </div>
          </template>
          <div class="user-profile">
            <el-avatar :size="80" icon="el-icon-user"></el-avatar>
            <h2 class="username">{{ currentUser?.fullName || '未知用户' }}</h2>
            <div class="user-details">
              <p><el-icon><PhoneOutlined /></el-icon> {{ currentUser?.contactPhone || '未设置' }}</p>
              <p><el-icon><Message /></el-icon> {{ currentUser?.contactEmail || '未设置' }}</p>
              <p><el-icon><Calendar /></el-icon> 注册时间: {{ formatDate(currentUser?.registrationDate) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧内容区域 -->
      <el-col :span="18">
        <!-- 快捷操作卡片 -->
        <el-card class="mb-4">
          <template #header>
            <div class="card-header">
              <h3>快捷操作</h3>
            </div>
          </template>
          <el-row :gutter="20">
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
                @click="$router.push('/repair-orders/user/create')"
              >
                <el-icon><Tools /></el-icon>
                提交维修申请
              </el-button>
            </el-col>
            <el-col :span="8">
              <el-button 
                type="info" 
                size="large" 
                style="width: 100%"
                @click="activeTab = 'repairs'"
              >
                <el-icon><Document /></el-icon>
                查看维修记录
              </el-button>
            </el-col>
          </el-row>
        </el-card>

        <!-- 内容标签页 -->
        <el-card>
          <template #header>
            <div class="card-header">
              <h3>我的数据</h3>
            </div>
          </template>
          <el-tabs v-model="activeTab">
            <!-- 我的车辆 -->
            <el-tab-pane name="vehicles" label="我的车辆">
              <div class="tab-actions mb-3">
                <el-button type="primary" size="small" @click="$router.push('/vehicles/create')">
                  <el-icon><Plus /></el-icon> 添加车辆
                </el-button>
                <el-button size="small" @click="loadUserVehicles">
                  <el-icon><Refresh /></el-icon> 刷新
                </el-button>
              </div>
              
              <el-table 
                :data="userVehicles" 
                border 
                style="width: 100%" 
                v-loading="vehicleLoading"
              >
                <el-table-column prop="licensePlate" label="车牌号" width="120" />
                <el-table-column prop="brand" label="品牌" width="100" />
                <el-table-column prop="model" label="型号" width="120" />
                <el-table-column prop="year" label="年份" width="80" />
                <el-table-column prop="color" label="颜色" width="80" />
                <el-table-column prop="vinNumber" label="车架号" width="180" show-overflow-tooltip />
                <el-table-column label="操作" width="200">
                  <template #default="scope">
                    <el-button 
                      size="small" 
                      type="primary" 
                      link
                      @click="$router.push(`/vehicles/${scope.row.vehicleId}/edit`)"
                    >
                      编辑
                    </el-button>
                    <el-button 
                      size="small" 
                      type="primary" 
                      link
                      @click="handleCreateRepairOrder(scope.row.vehicleId)"
                    >
                      报修
                    </el-button>
                    <el-button 
                      size="small" 
                      type="danger" 
                      link
                      @click="handleDeleteVehicle(scope.row.vehicleId)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <el-empty 
                v-if="userVehicles.length === 0 && !vehicleLoading" 
                description="暂无车辆信息" 
              />
            </el-tab-pane>

            <!-- 维修记录 -->
            <el-tab-pane name="repairs" label="维修记录">
              <div class="tab-actions mb-3">
                <el-button type="primary" size="small" @click="$router.push('/repair-orders/user/create')">
                  <el-icon><Plus /></el-icon> 提交维修申请
                </el-button>
                <el-button size="small" @click="loadUserRepairOrders">
                  <el-icon><Refresh /></el-icon> 刷新
                </el-button>
              </div>
              
              <el-table 
                :data="userRepairOrders" 
                border 
                style="width: 100%" 
                v-loading="repairLoading"
              >
                <el-table-column prop="orderId" label="工单号" width="80" />
                <el-table-column label="车辆信息" width="180">
                  <template #default="scope">
                    {{ scope.row.vehicle ? `${scope.row.vehicle.licensePlate} - ${scope.row.vehicle.brand} ${scope.row.vehicle.model}` : '未知车辆' }}
                  </template>
                </el-table-column>
                <el-table-column prop="descriptionOfIssue" label="问题描述" show-overflow-tooltip />
                <el-table-column label="报修日期" width="180">
                  <template #default="scope">
                    {{ formatDate(scope.row.reportDate) }}
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="120">
                  <template #default="scope">
                    <el-tag :type="getStatusType(scope.row.status)">
                      {{ getStatusText(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="scope">
                    <el-button 
                      size="small" 
                      type="primary" 
                      link
                      @click="$router.push(`/repair-orders/${scope.row.orderId}`)"
                    >
                      详情
                    </el-button>
                    <el-button 
                      size="small" 
                      type="warning" 
                      link
                      v-if="scope.row.status === 'COMPLETED' && !hasFeedback(scope.row.orderId)"
                      @click="openFeedbackDialog(scope.row)"
                    >
                      评价
                    </el-button>
                    <el-button 
                      size="small" 
                      type="danger" 
                      link
                      v-if="canCancelOrder(scope.row.status)"
                      @click="handleCancelOrder(scope.row.orderId)"
                    >
                      取消
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <el-empty 
                v-if="userRepairOrders.length === 0 && !repairLoading" 
                description="暂无维修记录" 
              />
            </el-tab-pane>

            <!-- 账户设置 -->
            <el-tab-pane name="settings" label="账户设置">
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="120px"
              >
                <el-form-item label="姓名" prop="fullName">
                  <el-input v-model="profileForm.fullName" placeholder="请输入姓名" />
                </el-form-item>
                <el-form-item label="联系电话" prop="contactPhone">
                  <el-input v-model="profileForm.contactPhone" placeholder="请输入联系电话" />
                </el-form-item>
                <el-form-item label="电子邮箱" prop="contactEmail">
                  <el-input v-model="profileForm.contactEmail" placeholder="请输入电子邮箱" />
                </el-form-item>
                <el-form-item label="密码" prop="password">
                  <el-input v-model="profileForm.password" type="password" placeholder="留空表示不修改密码" />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="profileForm.confirmPassword" type="password" placeholder="确认新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSaveProfile" :loading="profileLoading">保存</el-button>
                  <el-button @click="resetProfileForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 评价对话框 -->
    <el-dialog
      v-model="feedbackDialogVisible"
      title="提交维修评价"
      width="500px"
    >
      <el-form
        ref="feedbackFormRef"
        :model="feedbackForm"
        :rules="feedbackRules"
        label-width="80px"
      >
        <el-form-item label="维修单号">
          <el-input v-model="feedbackForm.orderId" disabled />
        </el-form-item>
        <el-form-item label="评分" prop="ratingScore">
          <el-rate
            v-model="feedbackForm.ratingScore"
            show-score
            :colors="rateColors"
          />
        </el-form-item>
        <el-form-item label="评价" prop="comments">
          <el-input
            v-model="feedbackForm.comments"
            type="textarea"
            placeholder="请输入您对本次维修服务的评价"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="feedbackDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFeedback" :loading="feedbackLoading">
            提交评价
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Edit, Plus, Refresh, PhoneOutlined, Message, Calendar,
  Van, Tools, Document
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import userApi from '@/api/user'
import vehicleApi from '@/api/vehicle'
import repairOrderApi from '@/api/repairOrder'
import feedbackApi from '@/api/feedback'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('vehicles')

// 用户信息
const currentUser = computed(() => userStore.user)
const userId = computed(() => currentUser.value?.userId)

// 数据加载状态
const vehicleLoading = ref(false)
const repairLoading = ref(false)
const profileLoading = ref(false)

// 用户车辆和维修记录数据
const userVehicles = ref([])
const userRepairOrders = ref([])
const userFeedbacks = ref([])

// 个人信息表单
const profileFormRef = ref(null)
const profileForm = reactive({
  fullName: '',
  contactPhone: '',
  contactEmail: '',
  password: '',
  confirmPassword: ''
})

// 表单验证规则
const profileRules = {
  fullName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在2-50个字符之间', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  contactEmail: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (rule, value, callback) => {
        if (profileForm.password && value !== profileForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 评价对话框
const feedbackDialogVisible = ref(false)
const feedbackLoading = ref(false)
const feedbackFormRef = ref(null)
const feedbackForm = reactive({
  orderId: '',
  repairOrderData: null,
  ratingScore: 5,
  comments: ''
})

const feedbackRules = {
  ratingScore: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  comments: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 5, max: 500, message: '评价内容在5-500个字符之间', trigger: 'blur' }
  ]
}

const rateColors = ['#F56C6C', '#E6A23C', '#909399', '#67C23A', '#409EFF']

// 工单状态映射
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

// 判断是否可以取消工单
const canCancelOrder = (status) => {
  const cancelableStatus = ['PENDING_ASSIGNMENT', 'ASSIGNED']
  return cancelableStatus.includes(status)
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 加载用户车辆列表
const loadUserVehicles = async () => {
  if (!userId.value) return
  
  try {
    vehicleLoading.value = true
    const vehicles = await vehicleApi.getVehiclesByUserId(userId.value)
    userVehicles.value = vehicles || []
  } catch (error) {
    console.error('加载用户车辆失败:', error)
    ElMessage.error('加载车辆信息失败')
  } finally {
    vehicleLoading.value = false
  }
}

// 加载用户维修记录
const loadUserRepairOrders = async () => {
  if (!userId.value) return
  
  try {
    repairLoading.value = true
    const orders = await repairOrderApi.getRepairOrdersByUserId(userId.value)
    userRepairOrders.value = orders || []
    
    // 加载用户评价数据
    await loadUserFeedbacks()
  } catch (error) {
    console.error('加载维修记录失败:', error)
    ElMessage.error('加载维修记录失败')
  } finally {
    repairLoading.value = false
  }
}

// 加载用户评价数据
const loadUserFeedbacks = async () => {
  if (!userId.value) return
  
  try {
    const feedbacks = await feedbackApi.getFeedbacksByUserId(userId.value)
    userFeedbacks.value = feedbacks || []
  } catch (error) {
    console.error('加载评价数据失败:', error)
  }
}

// 检查工单是否已有评价
const hasFeedback = (orderId) => {
  return userFeedbacks.value.some(feedback => feedback.orderId === orderId)
}

// 打开评价对话框
const openFeedbackDialog = (repairOrder) => {
  feedbackForm.orderId = repairOrder.orderId
  feedbackForm.repairOrderData = repairOrder
  feedbackForm.ratingScore = 5
  feedbackForm.comments = ''
  feedbackDialogVisible.value = true
}

// 提交评价
const submitFeedback = async () => {
  if (!feedbackFormRef.value) return
  
  try {
    await feedbackFormRef.value.validate()
    
    feedbackLoading.value = true
    const feedbackData = {
      orderId: feedbackForm.orderId,
      userId: userId.value,
      ratingScore: feedbackForm.ratingScore,
      comments: feedbackForm.comments
    }
    
    await feedbackApi.createFeedback(feedbackData)
    
    ElMessage.success('评价提交成功')
    feedbackDialogVisible.value = false
    
    // 重新加载评价数据
    await loadUserFeedbacks()
  } catch (error) {
    console.error('提交评价失败:', error)
    ElMessage.error('提交评价失败')
  } finally {
    feedbackLoading.value = false
  }
}

// 处理创建维修单
const handleCreateRepairOrder = (vehicleId) => {
  router.push(`/repair-orders/user/create?vehicleId=${vehicleId}`)
}

// 处理删除车辆
const handleDeleteVehicle = (vehicleId) => {
  ElMessageBox.confirm(
    '确定要删除此车辆吗？删除后无法恢复。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await vehicleApi.deleteVehicle(vehicleId)
      ElMessage.success('车辆删除成功')
      loadUserVehicles()
    } catch (error) {
      console.error('删除车辆失败:', error)
      ElMessage.error('删除车辆失败')
    }
  }).catch(() => {})
}

// 处理取消维修单
const handleCancelOrder = (orderId) => {
  ElMessageBox.confirm(
    '确定要取消此维修工单吗？',
    '取消确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '返回',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await repairOrderApi.updateRepairOrderStatus(orderId, 'CANCELLED_BY_USER')
      ElMessage.success('维修工单已取消')
      loadUserRepairOrders()
    } catch (error) {
      console.error('取消维修工单失败:', error)
      ElMessage.error('取消维修工单失败')
    }
  }).catch(() => {})
}

// 编辑个人信息
const handleEditProfile = () => {
  // 填充表单数据
  profileForm.fullName = currentUser.value?.fullName || ''
  profileForm.contactPhone = currentUser.value?.contactPhone || ''
  profileForm.contactEmail = currentUser.value?.contactEmail || ''
  profileForm.password = ''
  profileForm.confirmPassword = ''
  
  // 切换到账户设置标签页
  activeTab.value = 'settings'
}

// 保存个人信息
const handleSaveProfile = async () => {
  if (!profileFormRef.value) return
  
  try {
    await profileFormRef.value.validate()
    
    profileLoading.value = true
    const userData = {
      fullName: profileForm.fullName,
      contactPhone: profileForm.contactPhone,
      contactEmail: profileForm.contactEmail
    }
    
    // 如果输入了密码，则添加到更新数据中
    if (profileForm.password) {
      userData.password = profileForm.password
    }
    
    const updatedUser = await userApi.updateUser(userId.value, userData)
    
    // 更新存储的用户信息
    userStore.setUser(updatedUser)
    
    ElMessage.success('个人信息更新成功')
    resetProfileForm()
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新个人信息失败')
  } finally {
    profileLoading.value = false
  }
}

// 重置个人信息表单
const resetProfileForm = () => {
  if (profileFormRef.value) {
    profileFormRef.value.resetFields()
  }
}

// 页面加载时执行
onMounted(() => {
  if (userId.value) {
    loadUserVehicles()
    loadUserRepairOrders()
  }
})
</script>

<style scoped>
.user-center {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.user-info-card {
  height: 100%;
}

.user-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.username {
  margin: 15px 0 10px;
  font-size: 20px;
}

.user-details {
  width: 100%;
  margin-top: 15px;
}

.user-details p {
  display: flex;
  align-items: center;
  margin: 10px 0;
  color: #606266;
}

.user-details .el-icon {
  margin-right: 8px;
}

.tab-actions {
  display: flex;
  justify-content: flex-start;
  gap: 10px;
}

.mb-3 {
  margin-bottom: 12px;
}

.mb-4 {
  margin-bottom: 20px;
}

.mt-4 {
  margin-top: 20px;
}
</style> 