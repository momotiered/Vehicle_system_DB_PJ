<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>欢迎登录</span>
        </div>
      </template>

      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @submit.prevent="handleLogin">
        <el-form-item label="登录身份" prop="role">
          <el-radio-group v-model="loginForm.role">
            <el-radio label="user">用户</el-radio>
            <el-radio label="personnel">维修人员</el-radio>
            <el-radio label="admin">系统管理员</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%;">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="footer-links">
        <router-link to="/register">没有账户? 立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

// 使用 useRouter
const router = useRouter();

// 登录表单数据
const loginForm = reactive({
  role: 'user', // 默认选择'普通用户'
  username: '',
  password: '',
});

// 登录表单引用和加载状态
const loginFormRef = ref(null);
const loading = ref(false);

// 表单验证规则
const loginRules = {
  role: [{ required: true, message: '请选择登录身份', trigger: 'change' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

// 登录处理函数
const handleLogin = async () => {
  if (!loginFormRef.value) return;
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        if (loginForm.role === 'user') {
          // 根据用户类型确定API端点
          const endpoint = '/api/users/login';

          const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              username: loginForm.username,
              password: loginForm.password,
            }),
          });

          const data = await response.json();

          if (response.ok) {
            ElMessage.success('登录成功!');
            // 这里可以根据userType将不同用户信息存入Pinia
            // 例如: const userStore = useUserStore(); userStore.setUser(data);
            sessionStorage.setItem('user', JSON.stringify(data)); // 临时使用sessionStorage
            
            // 根据角色跳转到不同页面
            const redirectPath = '/dashboard'; // 假设普通用户的dashboard路径
            router.push(redirectPath);
          } else {
            ElMessage.error(data.message || '登录失败，请检查您的凭证。');
          }
        } else if (loginForm.role === 'personnel') {
          // 根据用户类型确定API端点
          const endpoint = '/api/personnel/login';

          const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              username: loginForm.username,
              password: loginForm.password,
            }),
          });

          const data = await response.json();

          if (response.ok) {
            ElMessage.success('登录成功!');
            // 这里可以根据userType将不同用户信息存入Pinia
            // 例如: const userStore = useUserStore(); userStore.setUser(data);
            sessionStorage.setItem('user', JSON.stringify(data)); // 临时使用sessionStorage
            
            // 根据角色跳转到不同页面
            const redirectPath = '/personnel/dashboard'; // 假设维修人员的dashboard路径
            router.push(redirectPath);
          } else {
            ElMessage.error(data.message || '登录失败，请检查您的凭证。');
          }
        } else if (loginForm.role === 'admin') {
          // 假设管理员登录成功后跳转到特定仪表盘
          // 注意：后端需要实现管理员登录接口
          ElMessage.success('管理员登录成功（模拟）');
          sessionStorage.setItem('user', JSON.stringify({ id: 0, role: 'admin', fullName: '管理员' })); // 模拟管理员信息
          router.push('/admin/assignment');
        }
      } catch (error) {
        console.error('登录请求失败:', error);
        ElMessage.error('无法连接到服务器，请稍后再试。');
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.login-card {
  width: 400px;
}

.card-header {
  text-align: center;
  font-size: 20px;
}

.user-type-radio {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.footer-links {
  margin-top: 15px;
  text-align: center;
}
</style> 