<template>
  <div class="order-list-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>工单列表</span>
          <el-input
            v-model="searchQuery"
            placeholder="搜索工单..."
            style="width: 200px"
            clearable
            @clear="handleSearch"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </template>

      <el-table
        :data="filteredOrders"
        v-loading="loading"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="orderId" label="工单ID" width="80" align="center" />
        <el-table-column prop="user.username" label="用户" width="120" />
        <el-table-column prop="vehicle.plateNumber" label="车牌号" width="120" />
        <el-table-column prop="descriptionOfIssue" label="问题描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reportDate" label="报修时间" width="180">
          <template #default="scope">
            {{ new Date(scope.row.reportDate).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="维修人员" width="200">
          <template #default="scope">
            <div v-if="scope.row.status !== 'PENDING_ASSIGNMENT' && scope.row.repairAssignments && scope.row.repairAssignments.length > 0">
              <el-tag 
                v-for="assignment in scope.row.repairAssignments" 
                :key="assignment.id"
                :type="getAssignmentStatusType(assignment.status)"
                class="mx-1"
              >
                {{ assignment.repairPersonnel.fullName }}
              </el-tag>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              link
              @click="viewOrderDetails(scope.row.orderId)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 工单详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="工单详情"
      width="70%"
      destroy-on-close
    >
      <div v-if="selectedOrder" class="order-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工单ID">{{ selectedOrder.orderId }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ selectedOrder.user?.username }}</el-descriptions-item>
          <el-descriptions-item label="车牌号">{{ selectedOrder.vehicle?.plateNumber }}</el-descriptions-item>
          <el-descriptions-item label="报修时间">
            {{ new Date(selectedOrder.reportDate).toLocaleString() }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedOrder.status)">
              {{ getStatusLabel(selectedOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="问题描述" :span="2">
            {{ selectedOrder.descriptionOfIssue }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 维修人员信息 -->
        <div v-if="selectedOrder.repairAssignments && selectedOrder.repairAssignments.length > 0" class="mt-4">
          <h4>维修人员</h4>
          <el-table :data="selectedOrder.repairAssignments" border style="width: 100%">
            <el-table-column prop="repairPersonnel.fullName" label="姓名" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="getAssignmentStatusType(scope.row.status)">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="hoursWorked" label="工时" />
            <el-table-column prop="laborCostForPersonnel" label="费用">
              <template #default="scope">
                ¥{{ scope.row.laborCostForPersonnel?.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 材料使用记录 -->
        <div v-if="selectedOrder.orderMaterialsUsed && selectedOrder.orderMaterialsUsed.length > 0" class="mt-4">
          <h4>材料使用记录</h4>
          <el-table :data="selectedOrder.orderMaterialsUsed" border style="width: 100%">
            <el-table-column prop="material.materialName" label="材料名称" />
            <el-table-column prop="quantityUsed" label="使用数量" />
            <el-table-column prop="pricePerUnitAtTimeOfUse" label="单价">
              <template #default="scope">
                ¥{{ scope.row.pricePerUnitAtTimeOfUse?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="小计">
              <template #default="scope">
                ¥{{ (scope.row.quantityUsed * scope.row.pricePerUnitAtTimeOfUse)?.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 费用汇总 -->
        <div class="mt-4">
          <h4>费用汇总</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="材料费用">
              ¥{{ selectedOrder.totalMaterialCost?.toFixed(2) }}
            </el-descriptions-item>
            <el-descriptions-item label="工时费用">
              ¥{{ selectedOrder.totalLaborCost?.toFixed(2) }}
            </el-descriptions-item>
            <el-descriptions-item label="总费用">
              ¥{{ selectedOrder.grandTotalCost?.toFixed(2) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';

const loading = ref(false);
const orders = ref([]);
const searchQuery = ref('');
const detailsDialogVisible = ref(false);
const selectedOrder = ref(null);

// 获取工单列表
const fetchOrders = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/repair-orders');
    orders.value = response.data;
  } catch (error) {
    ElMessage.error('获取工单列表失败');
    console.error('Error fetching orders:', error);
  } finally {
    loading.value = false;
  }
};

// 搜索过滤
const filteredOrders = computed(() => {
  if (!searchQuery.value) return orders.value;
  
  const query = searchQuery.value.toLowerCase();
  return orders.value.filter(order => 
    order.orderId.toString().includes(query) ||
    order.user?.username?.toLowerCase().includes(query) ||
    order.vehicle?.plateNumber?.toLowerCase().includes(query) ||
    order.descriptionOfIssue?.toLowerCase().includes(query)
  );
});

// 查看工单详情
const viewOrderDetails = async (orderId) => {
  try {
    const response = await axios.get(`/api/repair-orders/${orderId}`);
    selectedOrder.value = response.data;
    detailsDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取工单详情失败');
    console.error('Error fetching order details:', error);
  }
};

// 获取状态标签
const getStatusLabel = (status) => {
  const statusMap = {
    'PENDING_ASSIGNMENT': '待分配',
    'ASSIGNED': '已分配',
    'IN_PROGRESS': '进行中',
    'AWAITING_PARTS': '待配件',
    'COMPLETED': '已完成',
    'CANCELLED_BY_USER': '用户取消',
    'CANNOT_REPAIR': '无法维修',
    'PENDING_USER_CONFIRMATION': '待用户确认'
  };
  return statusMap[status] || status;
};

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING_ASSIGNMENT': 'info',
    'ASSIGNED': 'warning',
    'IN_PROGRESS': 'primary',
    'AWAITING_PARTS': 'danger',
    'COMPLETED': 'success',
    'CANCELLED_BY_USER': 'info',
    'CANNOT_REPAIR': 'danger',
    'PENDING_USER_CONFIRMATION': 'warning'
  };
  return typeMap[status] || 'info';
};

// 获取分配状态类型
const getAssignmentStatusType = (status) => {
  const typeMap = {
    'Assigned': 'info',
    'Accepted': 'primary',
    'Work_Completed': 'success',
    'Rejected': 'danger'
  };
  return typeMap[status] || 'info';
};

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑已通过计算属性实现
};

onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
.order-list-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mt-4 {
  margin-top: 1rem;
}

.order-details {
  padding: 20px;
}

:deep(.el-descriptions__label) {
  width: 120px;
  justify-content: flex-end;
}

.mx-1 {
  margin: 0 4px;
}
</style> 