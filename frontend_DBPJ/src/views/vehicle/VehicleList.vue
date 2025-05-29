<template>
  <div class="vehicle-list">
    <el-page-header>
      <template #content>
        <span class="text-large font-600 mr-3">车辆管理</span>
      </template>
    </el-page-header>

    <el-card class="list-card">
      <!-- 搜索和操作栏 -->
      <div class="search-bar">
        <el-row :gutter="20" align="middle">
          <el-col :span="8">
            <el-input
              v-model="searchQuery"
              placeholder="搜索车牌号、品牌或型号"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchUserId"
              placeholder="选择车主"
              clearable
              filterable
              @change="handleSearch"
            >
              <el-option
                v-for="user in userOptions"
                :key="user.userId"
                :label="user.fullName"
                :value="user.userId"
              />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-col>
          <el-col :span="4" class="text-right">
            <el-button type="primary" @click="$router.push('/vehicles/create')">
              <el-icon><Plus /></el-icon>
              添加车辆
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 车辆表格 -->
      <el-table
        v-loading="loading"
        :data="vehicleList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="vehicleId" label="车辆ID" width="80" />
        <el-table-column prop="licensePlate" label="车牌号" width="120" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="color" label="颜色" width="80" />
        <el-table-column prop="user.fullName" label="车主" width="100" />
        <el-table-column prop="user.contactPhone" label="联系电话" width="120" />
        <el-table-column prop="registrationDate" label="注册时间" width="150">
          <template #default="{ row }">
            {{ formatDate(row.registrationDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="handleEdit(row.vehicleId)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus
} from '@element-plus/icons-vue'
import vehicleApi from '@/api/vehicle'
import userApi from '@/api/user'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const vehicleList = ref([])
const userOptions = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const searchUserId = ref('')

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 加载用户选项
const loadUserOptions = async () => {
  try {
    const users = await userApi.getAllUsers()
    userOptions.value = users || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

// 加载车辆列表
const loadVehicleList = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    if (searchQuery.value) {
      params.search = searchQuery.value
    }
    
    if (searchUserId.value) {
      params.userId = searchUserId.value
    }

    const response = await vehicleApi.getAllVehicles(params)
    
    if (response.content) {
      vehicleList.value = response.content
      total.value = response.totalElements
    } else {
      vehicleList.value = response || []
      total.value = vehicleList.value.length
    }
  } catch (error) {
    console.error('加载车辆列表失败:', error)
    ElMessage.error('加载车辆列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadVehicleList()
}

// 重置搜索
const handleReset = () => {
  searchQuery.value = ''
  searchUserId.value = ''
  currentPage.value = 1
  loadVehicleList()
}

// 编辑车辆
const handleEdit = (vehicleId) => {
  router.push(`/vehicles/${vehicleId}/edit`)
}

// 删除车辆
const handleDelete = async (vehicle) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除车辆 "${vehicle.licensePlate}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await vehicleApi.deleteVehicle(vehicle.vehicleId)
    ElMessage.success('删除成功')
    loadVehicleList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除车辆失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// 分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadVehicleList()
}

// 当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadVehicleList()
}

onMounted(() => {
  loadUserOptions()
  loadVehicleList()
})
</script>

<style scoped>
.vehicle-list {
  padding: 20px;
}

.list-card {
  margin-top: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.text-right {
  text-align: right;
}
</style> 