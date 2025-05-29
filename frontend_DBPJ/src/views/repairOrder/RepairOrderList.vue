<template>
  <div class="repair-order-list">
    <el-page-header>
      <template #content>
        <span class="text-large font-600 mr-3">维修单管理</span>
      </template>
    </el-page-header>

    <el-card class="list-card">
      <!-- 搜索和操作栏 -->
      <div class="search-bar">
        <el-row :gutter="20" align="middle">
          <el-col :span="6">
            <el-input
              v-model="searchQuery"
              placeholder="搜索维修单号、车牌号"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="searchStatus"
              placeholder="选择状态"
              clearable
              @change="handleSearch"
            >
              <el-option label="待维修" value="PENDING" />
              <el-option label="维修中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleSearch"
            />
          </el-col>
          <el-col :span="4">
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
            <el-button type="primary" @click="$router.push('/repair-orders/create')">
              <el-icon><Plus /></el-icon>
              新建维修单
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 维修单表格 -->
      <el-table
        v-loading="loading"
        :data="repairOrderList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="repairOrderId" label="维修单号" width="100" />
        <el-table-column prop="vehicle.licensePlate" label="车牌号" width="120" />
        <el-table-column prop="vehicle.brand" label="品牌" width="100" />
        <el-table-column prop="vehicle.model" label="型号" width="120" />
        <el-table-column prop="user.fullName" label="车主" width="100" />
        <el-table-column prop="problemDescription" label="问题描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="estimatedCost" label="预估费用" width="100">
          <template #default="{ row }">
            ¥{{ row.estimatedCost || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="actualCost" label="实际费用" width="100">
          <template #default="{ row }">
            ¥{{ row.actualCost || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="createDate" label="创建时间" width="150">
          <template #default="{ row }">
            {{ formatDate(row.createDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="handleDetail(row.repairOrderId)"
            >
              详情
            </el-button>
            <el-button
              size="small"
              @click="handleEdit(row.repairOrderId)"
              v-if="row.status !== 'COMPLETED'"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(row)"
              v-if="row.status === 'PENDING'"
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus
} from '@element-plus/icons-vue'
import repairOrderApi from '@/api/repairOrder'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const repairOrderList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const searchStatus = ref('')
const dateRange = ref([])

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
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 加载维修单列表
const loadRepairOrderList = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    if (searchQuery.value) {
      params.search = searchQuery.value
    }
    
    if (searchStatus.value) {
      params.status = searchStatus.value
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const response = await repairOrderApi.getAllRepairOrders(params)
    
    if (response.content) {
      repairOrderList.value = response.content
      total.value = response.totalElements
    } else {
      repairOrderList.value = response || []
      total.value = repairOrderList.value.length
    }
  } catch (error) {
    console.error('加载维修单列表失败:', error)
    ElMessage.error('加载维修单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadRepairOrderList()
}

// 重置搜索
const handleReset = () => {
  searchQuery.value = ''
  searchStatus.value = ''
  dateRange.value = []
  currentPage.value = 1
  loadRepairOrderList()
}

// 查看详情
const handleDetail = (repairOrderId) => {
  router.push(`/repair-orders/${repairOrderId}`)
}

// 编辑维修单
const handleEdit = (repairOrderId) => {
  router.push(`/repair-orders/${repairOrderId}/edit`)
}

// 删除维修单
const handleDelete = async (repairOrder) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除维修单 "${repairOrder.repairOrderId}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await repairOrderApi.deleteRepairOrder(repairOrder.repairOrderId)
    ElMessage.success('删除成功')
    loadRepairOrderList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除维修单失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// 分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadRepairOrderList()
}

// 当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadRepairOrderList()
}

onMounted(() => {
  loadRepairOrderList()
})
</script>

<style scoped>
.repair-order-list {
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