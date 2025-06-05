<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { 
  Setting, 
  User, 
  Tickets, 
  Van, 
  Tools, 
  CircleCheck, 
  Document,
  Share,
  Box,
  ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 判断是否显示主布局（登录和注册页面不显示）
const showLayout = computed(() => {
  return !['Login', 'Register'].includes(route.name)
})

const currentUser = computed(() => userStore.user)
const isLoggedIn = computed(() => !!userStore.token)
const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
const isTechnician = computed(() => currentUser.value?.role === 'TECHNICIAN')

// 菜单折叠状态
const isCollapse = ref(false)
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 用于侧边栏的路由匹配
const activeMenu = ref('/')
watch(() => route.path, (path) => {
  if (path.startsWith('/users')) {
    activeMenu.value = '/users'
  } else if (path.startsWith('/vehicles')) {
    activeMenu.value = '/vehicles'
  } else if (path.startsWith('/repair-orders')) {
    activeMenu.value = '/repair-orders'
  } else if (path.startsWith('/technician')) {
    activeMenu.value = '/technician/dashboard'
  } else if (path.startsWith('/materials')) {
    activeMenu.value = '/materials'
  } else {
    activeMenu.value = path
  }
}, { immediate: true })

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      // 跳转到个人资料页面
      ElMessage.info('个人资料功能待开发')
      break
    case 'logout':
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
      break
  }
}

// 处理登出
const handleLogout = () => {
  userStore.logout()
}
</script>

<template>
  <div id="app">
    <div v-if="showLayout" class="layout-container">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <h2>车辆维修管理系统</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-icon><User /></el-icon>
              {{ currentUser?.fullName || '用户' }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <!-- 侧边栏 -->
        <el-aside width="200px" class="sidebar" v-if="isLoggedIn">
          <div class="sidebar-header">
            <h1 class="app-title" v-if="!isCollapse">车辆维修管理</h1>
            <h1 class="app-title-collapsed" v-else>车修</h1>
            <el-icon class="toggle-btn" @click="toggleSidebar">
              <Share />
            </el-icon>
          </div>
          
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            :collapse="isCollapse"
            :collapse-transition="false"
            router
          >
            <el-menu-item index="/dashboard">
              <el-icon><Document /></el-icon>
              <template #title>仪表板</template>
            </el-menu-item>

            <el-menu-item index="/user-center">
              <el-icon><User /></el-icon>
              <template #title>用户中心</template>
            </el-menu-item>
            
            <el-menu-item index="/vehicles" v-if="isAdmin">
              <el-icon><Van /></el-icon>
              <template #title>车辆管理</template>
            </el-menu-item>
            
            <el-menu-item index="/repair-orders">
              <el-icon><Tools /></el-icon>
              <template #title>维修工单</template>
            </el-menu-item>
            
            <el-menu-item index="/technician/dashboard" v-if="isTechnician">
              <el-icon><Setting /></el-icon>
              <template #title>技术人员工作台</template>
            </el-menu-item>
            
            <el-menu-item index="/materials" v-if="isAdmin">
              <el-icon><Box /></el-icon>
              <template #title>维修材料管理</template>
            </el-menu-item>
            
            <el-menu-item index="/users" v-if="isAdmin">
              <el-icon><User /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区域 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </div>

    <!-- 登录/注册页面不显示布局 -->
    <div v-else class="auth-container">
      <router-view />
    </div>
  </div>
</template>

<style>
/* 全局样式重置 */
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

/* 覆盖Element Plus的一些默认样式 */
:deep(.el-card) {
  overflow: visible;
}
</style>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #409eff;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #ebeef5;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: white;
  display: flex;
  align-items: center;
  gap: 5px;
}

.sidebar {
  background-color: #545c64;
  border-right: 1px solid #ebeef5;
}

.main-content {
  padding: 20px;
  background-color: #f5f5f5;
  overflow: auto;
}

.auth-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.el-menu-vertical {
  border-right: none;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  color: white;
}

.app-title {
  font-size: 18px;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-title-collapsed {
  font-size: 18px;
  margin: 0;
}

.toggle-btn {
  cursor: pointer;
  font-size: 18px;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-nav {
  height: 60px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-info span {
  margin-left: 8px;
}

.view-container {
  flex: 1;
  overflow: auto;
  background-color: #f0f2f5;
}

/* 响应式处理 */
@media (max-width: 768px) {
  .sidebar {
    width: 64px;
  }
  
  .app-title {
    display: none;
  }
  
  .app-title-collapsed {
    display: block;
  }
}
</style>
