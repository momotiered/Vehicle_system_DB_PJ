<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 判断是否显示主布局（登录和注册页面不显示）
const showLayout = computed(() => {
  return !['Login', 'Register'].includes(route.name)
})

const currentUser = computed(() => userStore.currentUser)

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
        <el-aside width="200px" class="sidebar">
          <el-menu
            :default-active="$route.path"
            router
            class="el-menu-vertical"
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
          >
            <el-menu-item index="/dashboard">
              <el-icon><House /></el-icon>
              <span>仪表板</span>
            </el-menu-item>
            
            <el-sub-menu index="users">
              <template #title>
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </template>
              <el-menu-item index="/users">用户列表</el-menu-item>
              <el-menu-item index="/users/create">添加用户</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="vehicles">
              <template #title>
                <el-icon><Van /></el-icon>
                <span>车辆管理</span>
              </template>
              <el-menu-item index="/vehicles">车辆列表</el-menu-item>
              <el-menu-item index="/vehicles/create">添加车辆</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="repair-orders">
              <template #title>
                <el-icon><Tools /></el-icon>
                <span>维修工单</span>
              </template>
              <el-menu-item index="/repair-orders">工单列表</el-menu-item>
              <el-menu-item index="/repair-orders/create">创建工单</el-menu-item>
            </el-sub-menu>
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
</style>
