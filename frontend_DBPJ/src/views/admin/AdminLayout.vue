<template>
  <el-container class="admin-layout">
    <el-aside width="200px" class="sidebar">
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical-demo"
        router
      >
        <div class="sidebar-header">
          <h3>管理员后台</h3>
        </div>
        <el-menu-item index="/admin/monitor">
          <i class="el-icon-data-analysis"></i>
          <span>监控面板</span>
        </el-menu-item>
        <el-menu-item index="/admin/assignment">
          <i class="el-icon-document-checked"></i>
          <span>工单分配</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <i class="el-icon-user"></i>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/personnel">
          <i class="el-icon-coordinate"></i>
          <span>人员管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div>欢迎您，管理员！</div>
        <el-button type="danger" @click="logout">退出登录</el-button>
      </el-header>
      <el-main class="content">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();

const activeMenu = computed(() => route.path);

const logout = () => {
  sessionStorage.clear();
  router.push('/login');
  ElMessage.success('已成功退出登录');
};
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
.sidebar-header {
  padding: 20px;
  text-align: center;
  font-size: 18px;
  color: #333;
  border-bottom: 1px solid #e6e6e6;
}
.sidebar {
  border-right: 1px solid #e6e6e6;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e6e6e6;
}
.content {
  background-color: #f4f7f9;
  padding: 20px;
  display: flex;
  flex-direction: column;
}
</style> 