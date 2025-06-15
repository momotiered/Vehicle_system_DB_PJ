<template>
  <div>
    <el-card>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <h2>用户管理</h2>
        <el-button type="primary" @click="handleAdd">添加用户</el-button>
      </div>

      <el-table :data="users" stripe style="width: 100%">
        <el-table-column prop="userId" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="contactPhone" label="联系电话"></el-table-column>
        <el-table-column prop="address" label="地址"></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.userId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="30%">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名" v-if="!isEditMode">
          <el-input v-model="form.username"></el-input>
        </el-form-item>
        <el-form-item label="密码" v-if="!isEditMode">
          <el-input v-model="form.password" type="password"></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactInfo"></el-input>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getAllUsers, createUser, updateUser, deleteUser } from '@/api/admin';

const users = ref([]);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const isEditMode = ref(false);
const form = ref({
  id: null,
  username: '',
  password: '',
  contactInfo: '',
  address: ''
});

const fetchUsers = async () => {
  try {
    const response = await getAllUsers();
    users.value = response.data;
  } catch (error) {
    ElMessage.error('获取用户列表失败');
  }
};

onMounted(fetchUsers);

const resetForm = () => {
  form.value = {
    id: null,
    username: '',
    password: '',
    contactInfo: '',
    address: ''
  };
};

const handleAdd = () => {
  resetForm();
  dialogTitle.value = '添加新用户';
  isEditMode.value = false;
  dialogVisible.value = true;
};

const handleEdit = (user) => {
  resetForm();
  dialogTitle.value = '编辑用户';
  isEditMode.value = true;
  form.value = { 
      id: user.userId, 
      contactInfo: user.contactPhone,
      address: user.address
  };
  dialogVisible.value = true;
};

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该用户吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteUser(id);
      ElMessage.success('删除成功');
      fetchUsers();
    } catch (error) {
      ElMessage.error('删除失败');
    }
  }).catch(() => {
    // a
  });
};

const handleSubmit = async () => {
  try {
    if (isEditMode.value) {
      await updateUser(form.value.id, {
        contactInfo: form.value.contactInfo,
        address: form.value.address
      });
      ElMessage.success('更新成功');
    } else {
      await createUser({
        username: form.value.username,
        password: form.value.password,
        contactInfo: form.value.contactInfo,
        address: form.value.address
      });
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    fetchUsers();
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新失败' : '添加失败');
  }
};
</script> 