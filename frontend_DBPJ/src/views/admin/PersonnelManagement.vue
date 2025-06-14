<template>
  <div>
    <el-card>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <h2>维修人员管理</h2>
        <el-button type="primary" @click="handleAdd">添加人员</el-button>
      </div>

      <el-table :data="personnel" stripe style="width: 100%">
        <el-table-column prop="personnelId" label="ID" width="80"></el-table-column>
        <el-table-column prop="fullName" label="姓名"></el-table-column>
        <el-table-column prop="workType" label="工种"></el-table-column>
        <el-table-column prop="hourlyRate" label="时薪"></el-table-column>
        <el-table-column prop="contactPhone" label="联系电话"></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.personnelId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="30%">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名" v-if="!isEditMode">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="密码" v-if="!isEditMode">
          <el-input v-model="form.password" type="password"></el-input>
        </el-form-item>
        <el-form-item label="工种">
          <el-input v-model="form.specialization"></el-input>
        </el-form-item>
        <el-form-item label="时薪">
          <el-input-number v-model="form.hourlyRate" :precision="2" :step="10"></el-input-number>
        </el-form-item>
         <el-form-item label="联系电话">
          <el-input v-model="form.contactInfo"></el-input>
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
import { getAllPersonnel, createPersonnel, updatePersonnel, deletePersonnel } from '@/api/admin';

const personnel = ref([]);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const isEditMode = ref(false);
const form = ref({
  id: null,
  name: '',
  password: '',
  specialization: '',
  hourlyRate: 0,
  contactInfo: ''
});

const fetchPersonnel = async () => {
  try {
    const response = await getAllPersonnel();
    personnel.value = response.data;
  } catch (error) {
    ElMessage.error('获取维修人员列表失败');
  }
};

onMounted(fetchPersonnel);

const resetForm = () => {
  form.value = {
    id: null,
    name: '',
    password: '',
    specialization: '',
    hourlyRate: 0,
    contactInfo: ''
  };
};

const handleAdd = () => {
  resetForm();
  dialogTitle.value = '添加新人员';
  isEditMode.value = false;
  dialogVisible.value = true;
};

const handleEdit = (p) => {
  resetForm();
  dialogTitle.value = '编辑人员信息';
  isEditMode.value = true;
  form.value = {
    id: p.personnelId,
    specialization: p.workType,
    hourlyRate: p.hourlyRate,
    contactInfo: p.contactPhone
  };
  dialogVisible.value = true;
};

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该人员吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deletePersonnel(id);
      ElMessage.success('删除成功');
      fetchPersonnel();
    } catch (error) {
      ElMessage.error('删除失败');
    }
  });
};

const handleSubmit = async () => {
  try {
    if (isEditMode.value) {
      await updatePersonnel(form.value.id, {
        specialization: form.value.specialization,
        hourlyRate: form.value.hourlyRate,
        contactInfo: form.value.contactInfo,
      });
      ElMessage.success('更新成功');
    } else {
      await createPersonnel({
        name: form.value.name,
        password: form.value.password,
        specialization: form.value.specialization,
        hourlyRate: form.value.hourlyRate,
      });
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    fetchPersonnel();
  } catch (error) {
    ElMessage.error(isEditMode.value ? '更新失败' : '添加失败');
  }
};
</script> 