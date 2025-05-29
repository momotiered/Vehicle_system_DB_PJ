<template>
  <div class="user-form">
    <el-page-header @back="$router.go(-1)">
      <template #content>
        <span class="text-large font-600 mr-3">{{ isEdit ? '编辑用户' : '添加用户' }}</span>
      </template>
    </el-page-header>

    <el-card class="form-card">
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="100px"
        @submit.prevent="handleSubmit"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="userForm.username"
                placeholder="请输入用户名"
                :disabled="isEdit"
                clearable
              />
              <div v-if="isEdit" class="form-tip">用户名不可修改</div>
            </el-form-item>
          </el-col>
          
          <el-col :span="12" v-if="!isEdit">
            <el-form-item label="密码" prop="passwordHash">
              <el-input
                v-model="userForm.passwordHash"
                type="password"
                placeholder="请输入密码"
                show-password
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="fullName">
              <el-input
                v-model="userForm.fullName"
                placeholder="请输入真实姓名"
                clearable
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="手机号" prop="contactPhone">
              <el-input
                v-model="userForm.contactPhone"
                placeholder="请输入手机号"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="contactEmail">
              <el-input
                v-model="userForm.contactEmail"
                placeholder="请输入邮箱地址"
                clearable
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="注册时间" v-if="isEdit">
              <el-input
                :value="formatDate(userForm.registrationDate)"
                disabled
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="地址" prop="address">
          <el-input
            v-model="userForm.address"
            type="textarea"
            placeholder="请输入地址"
            :rows="3"
          />
        </el-form-item>

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
import userApi from '@/api/user'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userFormRef = ref()
const loading = ref(false)

// 判断是否为编辑模式
const isEdit = computed(() => !!route.params.id)
const userId = computed(() => parseInt(route.params.id))

const userForm = reactive({
  username: '',
  passwordHash: '',
  fullName: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  registrationDate: null
})

// 验证手机号
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(value)) {
    callback(new Error('请输入正确的手机号码'))
  } else {
    callback()
  }
}

// 验证邮箱
const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

const userRules = computed(() => {
  const rules = {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
    ],
    fullName: [
      { required: true, message: '请输入姓名', trigger: 'blur' }
    ],
    contactPhone: [
      { validator: validatePhone, trigger: 'blur' }
    ],
    contactEmail: [
      { validator: validateEmail, trigger: 'blur' }
    ]
  }
  
  // 新增用户时密码必填
  if (!isEdit.value) {
    rules.passwordHash = [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '密码长度至少为 6 位', trigger: 'blur' }
    ]
  }
  
  return rules
})

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 加载用户数据（编辑模式）
const loadUserData = async () => {
  if (!isEdit.value) return
  
  try {
    loading.value = true
    const userData = await userApi.getUserById(userId.value)
    
    Object.keys(userForm).forEach(key => {
      if (userData[key] !== undefined) {
        userForm[key] = userData[key]
      }
    })
  } catch (error) {
    console.error('加载用户数据失败:', error)
    ElMessage.error('加载用户数据失败')
    router.go(-1)
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!userFormRef.value) return

  try {
    const valid = await userFormRef.value.validate()
    if (!valid) return

    loading.value = true
    
    const userData = {
      username: userForm.username,
      fullName: userForm.fullName,
      contactPhone: userForm.contactPhone || null,
      contactEmail: userForm.contactEmail || null,
      address: userForm.address || null
    }
    
    // 新增用户时添加密码
    if (!isEdit.value) {
      userData.passwordHash = userForm.passwordHash
    }

    if (isEdit.value) {
      await userApi.updateUser(userId.value, userData)
      ElMessage.success('用户更新成功')
    } else {
      await userApi.createUser(userData)
      ElMessage.success('用户创建成功')
    }
    
    router.push('/users')
  } catch (error) {
    console.error('保存用户失败:', error)
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
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  
  if (isEdit.value) {
    loadUserData()
  }
}

onMounted(() => {
  if (isEdit.value) {
    loadUserData()
  }
})
</script>

<style scoped>
.user-form {
  padding: 20px;
}

.form-card {
  margin-top: 20px;
  max-width: 800px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style> 