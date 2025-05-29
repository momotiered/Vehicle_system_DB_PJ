<template>
  <div class="user-list">
    <el-page-header>
      <template #content>
        <span class="text-large font-600 mr-3">用户管理</span>
      </template>
    </el-page-header>

    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.username"
            placeholder="搜索用户名"
            :prefix-icon="Search"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="searchForm.phone"
            placeholder="搜索手机号"
            :prefix-icon="Phone"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
        <el-col :span="6" style="text-align: right">
          <el-button type="primary" @click="$router.push('/users/create')">
            <el-icon><Plus /></el-icon>
            添加用户
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 用户表格 -->
    <el-card>
      <el-table
        :data="userList"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="fullName" label="姓名" />
        <el-table-column prop="contactPhone" label="手机号" />
        <el-table-column prop="contactEmail" label="邮箱" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="registrationDate" label="注册时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.registrationDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
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
import { Search, Phone, Plus } from '@element-plus/icons-vue'
import userApi from '@/api/user'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const userList = ref([])

const searchForm = reactive({
  username: '',
  phone: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    const response = await userApi.getAllUsers()
    userList.value = response || []
    pagination.total = userList.value.length
    
    // 客户端分页
    const start = (pagination.currentPage - 1) * pagination.pageSize
    const end = start + pagination.pageSize
    userList.value = userList.value.slice(start, end)
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadUserList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.username = ''
  searchForm.phone = ''
  handleSearch()
}

// 编辑用户
const handleEdit = (user) => {
  router.push(`/users/${user.userId}/edit`)
}

// 删除用户
const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await userApi.deleteUser(user.userId)
    ElMessage.success('删除成功')
    loadUserList()
  } catch (error) {
    if (error === 'cancel') {
      ElMessage.info('已取消删除')
    } else {
      console.error('删除用户失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadUserList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadUserList()
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 