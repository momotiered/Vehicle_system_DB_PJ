<template>
  <div>
    <el-card>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <h2>车辆管理</h2>
      </div>

      <el-table :data="vehicles" stripe style="width: 100%">
        <el-table-column prop="vehicleId" label="ID" width="80"></el-table-column>
        <el-table-column prop="licensePlate" label="车牌号"></el-table-column>
        <el-table-column prop="make" label="品牌"></el-table-column>
        <el-table-column prop="model" label="型号"></el-table-column>
        <el-table-column prop="yearOfManufacture" label="年份"></el-table-column>
        <el-table-column prop="color" label="颜色"></el-table-column>
        <el-table-column prop="user.fullName" label="车主"></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.vehicleId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑车辆信息" width="30%">
      <el-form :model="form" label-width="80px">
        <el-form-item label="车牌号">
          <el-input v-model="form.licensePlate"></el-input>
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="form.make"></el-input>
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.model"></el-input>
        </el-form-item>
        <el-form-item label="年份">
          <el-input-number v-model="form.yearOfManufacture" :min="1900" :max="2024"></el-input-number>
        </el-form-item>
        <el-form-item label="颜色">
          <el-input v-model="form.color"></el-input>
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
import { getAllVehicles, updateVehicle, deleteVehicle } from '@/api/admin';

const vehicles = ref([]);
const dialogVisible = ref(false);
const form = ref({
  vehicleId: null,
  licensePlate: '',
  make: '',
  model: '',
  yearOfManufacture: new Date().getFullYear(),
  color: ''
});

const fetchVehicles = async () => {
  try {
    const response = await getAllVehicles();
    vehicles.value = response.data;
  } catch (error) {
    ElMessage.error('获取车辆列表失败');
  }
};

onMounted(fetchVehicles);

const resetForm = () => {
  form.value = {
    vehicleId: null,
    licensePlate: '',
    make: '',
    model: '',
    yearOfManufacture: new Date().getFullYear(),
    color: ''
  };
};

const handleEdit = (vehicle) => {
  resetForm();
  form.value = {
    vehicleId: vehicle.vehicleId,
    licensePlate: vehicle.licensePlate,
    make: vehicle.make,
    model: vehicle.model,
    yearOfManufacture: vehicle.yearOfManufacture,
    color: vehicle.color
  };
  dialogVisible.value = true;
};

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该车辆吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteVehicle(id);
      ElMessage.success('删除成功');
      fetchVehicles();
    } catch (error) {
      ElMessage.error('删除失败');
    }
  }).catch(() => {
    // 取消删除
  });
};

const handleSubmit = async () => {
  try {
    await updateVehicle(form.value.vehicleId, {
      licensePlate: form.value.licensePlate,
      make: form.value.make,
      model: form.value.model,
      yearOfManufacture: form.value.yearOfManufacture,
      color: form.value.color
    });
    ElMessage.success('更新成功');
    dialogVisible.value = false;
    fetchVehicles();
  } catch (error) {
    ElMessage.error('更新失败');
  }
};
</script>

<style scoped>
.el-input-number {
  width: 100%;
}
</style> 