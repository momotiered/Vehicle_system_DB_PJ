<template>
  <div class="order-assignment-view">
    <div class="page-header">
      <h1>工单分配</h1>
      <p>在此页面，您可以查看所有待处理的维修工单，并将其分配给合适的维修人员。</p>
    </div>

    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>待分配工单 ({{ pendingOrders.length }})</span>
        </div>
      </template>
      <el-table :data="pendingOrders" style="width: 100%; height: 100%" v-loading="loading.orders">
        <el-table-column prop="orderId" label="工单ID" width="100" />
        <el-table-column prop="descriptionOfIssue" label="问题描述" />
        <el-table-column prop="reportDate" label="报修日期" width="200">
          <template #default="scope">
            {{ new Date(scope.row.reportDate).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="分配维修工" width="350">
          <template #default="scope">
            <el-select
              v-model="scope.row.selectedPersonnel"
              multiple
              filterable
              placeholder="请选择维修人员"
              style="width: 100%;"
            >
              <el-option
                v-for="person in availablePersonnel"
                :key="person.personnelId"
                :label="`${person.fullName} (${person.workType})`"
                :value="person.personnelId"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleAssign(scope.row)"
              :disabled="!scope.row.selectedPersonnel || scope.row.selectedPersonnel.length === 0"
            >
              分配
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import { getPendingOrders, getAvailablePersonnel, assignOrder } from '@/api/admin.js';

const pendingOrders = ref([]);
const availablePersonnel = ref([]);

const loading = reactive({
  orders: false,
  personnel: false,
});

const loadData = async () => {
  loading.orders = true;
  loading.personnel = true;
  try {
    const [ordersRes, personnelRes] = await Promise.all([
      getPendingOrders(),
      getAvailablePersonnel()
    ]);
    pendingOrders.value = ordersRes.map(order => ({ ...order, selectedPersonnel: [] }));
    availablePersonnel.value = personnelRes;
  } catch (error) {
    ElMessage.error('加载数据失败，请稍后重试。');
    console.error(error);
  } finally {
    loading.orders = false;
    loading.personnel = false;
  }
};

onMounted(loadData);

const handleAssign = async (order) => {
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在分配...',
    background: 'rgba(0, 0, 0, 0.7)',
  });
  
  try {
    await assignOrder(order.orderId, order.selectedPersonnel);
    ElMessage.success(`工单 #${order.orderId} 分配成功！`);
    // 分配成功后，重新加载待处理列表
    await loadData();
  } catch (error) {
    const message = error.response?.data?.message || '分配失败，请检查网络或联系技术支持。';
    ElMessage.error(message);
    console.error(error);
  } finally {
    loadingInstance.close();
  }
};
</script>

<style scoped>
.order-assignment-view {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  flex-shrink: 0;
  margin-bottom: 20px;
}

.box-card {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

:deep(.el-card__body) {
  flex-grow: 1;
  padding: 0;
}

:deep(.el-table) {
  height: 100%;
}

h1, p {
  margin: 0;
}
p {
  color: #606266;
  font-size: 14px;
  margin-top: 8px;
}

.card-header {
  font-weight: bold;
}
</style> 