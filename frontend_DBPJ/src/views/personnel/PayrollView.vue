<template>
  <div class="payroll-container">
    <h2>薪资记录</h2>
    <el-table :data="records" v-loading="loading" style="width: 100%">
      <el-table-column prop="paymentDate" label="发放日期" width="180">
        <template #default="scope">
          {{ new Date(scope.row.paymentDate).toLocaleDateString() }}
        </template>
      </el-table-column>
      <el-table-column prop="periodStartDate" label="周期开始" width="180">
         <template #default="scope">
          {{ new Date(scope.row.periodStartDate).toLocaleDateString() }}
        </template>
      </el-table-column>
      <el-table-column prop="periodEndDate" label="周期结束" width="180">
         <template #default="scope">
          {{ new Date(scope.row.periodEndDate).toLocaleDateString() }}
        </template>
      </el-table-column>
      <el-table-column prop="totalHoursWorked" label="总工时" />
      <el-table-column prop="totalAmountPaid" label="发放总额">
        <template #default="scope">
          ¥{{ scope.row.totalAmountPaid.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="notes" label="备注" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getPayrollRecords } from '@/api/repairPersonnel.js';

const router = useRouter();
const records = ref([]);
const loading = ref(false);
const userInfo = ref(null);

onMounted(async () => {
  const storedUser = sessionStorage.getItem('user');
  if (storedUser) {
    userInfo.value = JSON.parse(storedUser);
    if (!userInfo.value || !userInfo.value.personnelId) {
      ElMessage.error('无法获取维修人员ID，请重新登录。');
      router.push('/login');
      return;
    }
    loading.value = true;
    try {
      records.value = await getPayrollRecords(userInfo.value.personnelId);
    } catch (error) {
      ElMessage.error(error.message);
    } finally {
      loading.value = false;
    }
  } else {
    ElMessage.error('请先登录');
    router.push('/login');
  }
});
</script>

<style scoped>
.payroll-container {
  padding: 20px;
}
</style> 