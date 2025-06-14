<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo-container">
        <h3>维修工工作台</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical-demo"
        router
      >
        <el-menu-item index="/personnel/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/personnel/assignments">
          <el-icon><Document /></el-icon>
          <span>我的工单</span>
        </el-menu-item>
        <el-menu-item index="/personnel/profile">
          <el-icon><User /></el-icon>
          <span>个人资料</span>
        </el-menu-item>
        <el-menu-item index="/personnel/payroll">
          <el-icon><Money /></el-icon>
          <span>薪资记录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div>欢迎您，王师傅！</div>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const activeMenu = computed(() => {
  return route.path;
});

const handleLogout = () => {
  // 清除登录信息
  sessionStorage.removeItem('user');
  sessionStorage.removeItem('personnel');
  router.push('/login');
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
  color: #fff;
}

.logo-container {
  padding: 20px;
  text-align: center;
  color: #fff;
}

.el-menu {
  border-right: none;
  background-color: #304156;
}

.el-menu-item {
  color: #bfcbd9;
}

.el-menu-item:hover,
.el-menu-item.is-active {
  background-color: #263445 !important;
  color: #409EFF !important;
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
}
</style> 