<template>
  <div v-if="order" class="order-detail-container">
    <el-page-header @back="goBack" :content="`工单详情 - #${order.orderId}`" class="page-header"/>

    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h3>工单信息</h3>
          <el-tag :type="getStatusType(order.status)" size="large">{{ order.status }}</el-tag>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报修用户" label-class-name="label-bold">
          <div class="description-content">{{ order.user.fullName }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="车牌号" label-class-name="label-bold">
          <div class="description-content">{{ order.vehicle.licensePlate }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="车辆型号" label-class-name="label-bold">
          <div class="description-content">{{ order.vehicle.make }} {{ order.vehicle.model }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="报修日期" label-class-name="label-bold">
          <div class="description-content">{{ new Date(order.reportDate).toLocaleString() }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2" label-class-name="label-bold">
          <div class="issue-description">{{ order.descriptionOfIssue }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h3>使用材料</h3>
          <el-button @click="materialDialogVisible = true" type="primary" size="small" icon="Plus">添加材料</el-button>
        </div>
      </template>
      
      <el-table 
        :data="order.orderMaterialsUsed" 
        stripe 
        border 
        class="custom-table"
        :empty-text="'暂无使用材料记录'"
      >
        <el-table-column prop="material.materialName" label="材料名称" min-width="150" align="center" />
        <el-table-column prop="quantityUsed" label="使用数量" width="120" align="center" />
        <el-table-column prop="pricePerUnitAtTimeOfUse" label="使用时单价" width="150" align="center">
          <template #default="scope">
            ¥{{ scope.row.pricePerUnitAtTimeOfUse.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="小计" width="150" align="center">
          <template #default="scope">
            <span class="price-total">¥{{ (scope.row.quantityUsed * scope.row.pricePerUnitAtTimeOfUse).toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h3>工时记录</h3>
          <el-button @click="hoursDialogVisible = true" type="primary" size="small" icon="Timer">记录工时</el-button>
        </div>
      </template>
      
      <el-table 
        :data="workHoursData" 
        stripe 
        border 
        class="custom-table"
        :empty-text="'暂无工时记录'"
      >
        <el-table-column prop="date" label="记录日期" min-width="180" align="center" />
        <el-table-column prop="hours" label="工时 (小时)" width="150" align="center" />
        <el-table-column prop="rate" label="工时费率" width="150" align="center">
          <template #default="scope">
            ¥{{ scope.row.rate.toFixed(2) }}/小时
          </template>
        </el-table-column>
        <el-table-column label="费用" width="150" align="center">
          <template #default="scope">
            <span class="price-total">¥{{ (scope.row.hours * scope.row.rate).toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="detail-card summary-card">
      <div class="order-summary">
        <h3>费用汇总</h3>
        <div class="summary-content">
          <div class="summary-item">
            <span>材料费用:</span>
            <span class="price">¥{{ calculateMaterialCost().toFixed(2) }}</span>
          </div>
          <div class="summary-item">
            <span>工时费用:</span>
            <span class="price">¥{{ calculateLaborCost().toFixed(2) }}</span>
          </div>
          <div class="summary-item total">
            <span>总计:</span>
            <span class="price">¥{{ (calculateMaterialCost() + calculateLaborCost()).toFixed(2) }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 添加材料对话框 -->
    <el-dialog v-model="materialDialogVisible" title="添加使用材料" width="30%" class="custom-dialog">
      <el-form :model="materialForm" label-position="top">
        <el-form-item label="材料">
           <!-- 假设有一个获取所有材料的API -->
          <el-select v-model="materialForm.materialId" placeholder="请选择材料" style="width: 100%">
            <el-option label="火花塞" :value="7"></el-option>
            <el-option label="机油" :value="8"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="materialForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="materialDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddMaterial">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 记录工时对话框 -->
    <el-dialog v-model="hoursDialogVisible" title="记录工时" width="30%" class="custom-dialog">
      <el-form :model="hoursForm" label-position="top">
        <el-form-item label="工时 (小时)">
          <el-input-number v-model="hoursForm.hours" :precision="1" :step="0.5" :min="0.5" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="hoursDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleLogHours">确认</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
  <div v-else class="loading-container">
    <el-skeleton :rows="6" animated />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getOrderDetails, addMaterialToOrder } from '@/api/repairOrder.js';
import { logHoursToAssignment } from '@/api/repairPersonnel.js';

const route = useRoute();
const router = useRouter();

const order = ref(null);
const orderId = ref(null);
const assignmentId = ref(null); // 需要从某个地方获取，例如路由参数或工单详情中

const materialDialogVisible = ref(false);
const materialForm = reactive({ materialId: null, quantity: 1 });

const hoursDialogVisible = ref(false);
const hoursForm = reactive({ hours: 0.5 });

// 模拟的工时数据，实际应该从API获取
const workHoursData = ref([
  {
    date: '2023-06-01 10:30:00',
    hours: 2.5,
    rate: 100
  },
  {
    date: '2023-06-02 14:15:00',
    hours: 1.5,
    rate: 100
  }
]);

onMounted(async () => {
  orderId.value = parseInt(route.params.orderId, 10);
  // 临时硬编码assignmentId，实际应从工单详情API获取或通过路由传递
  // 理想情况下，工单详情中应该包含当前维修人员的assignmentId
  const tempAssignmentId = route.query.assignmentId;
  if(tempAssignmentId) {
    assignmentId.value = parseInt(tempAssignmentId, 10);
  }

  try {
    const data = await getOrderDetails(orderId.value);
    order.value = data;
  } catch (error) {
    ElMessage.error('加载工单详情失败');
  }
});

const goBack = () => {
  router.back();
};

const handleAddMaterial = async () => {
  try {
    await addMaterialToOrder(orderId.value, materialForm.materialId, materialForm.quantity);
    ElMessage.success('材料添加成功');
    materialDialogVisible.value = false;
    // 重新获取详情以刷新列表
    order.value = await getOrderDetails(orderId.value);
  } catch (error) {
    ElMessage.error(error.message);
  }
};

const handleLogHours = async () => {
  if (!assignmentId.value) {
      ElMessage.error('无法确定工单分配ID，无法记录工时');
      return;
  }
  try {
    await logHoursToAssignment(assignmentId.value, hoursForm.hours);
    ElMessage.success('工时记录成功');
    hoursDialogVisible.value = false;
    // 这里也可以刷新整个工单详情，如果工时会影响总价的话
    
    // 模拟添加新工时记录
    workHoursData.value.push({
      date: new Date().toLocaleString(),
      hours: hoursForm.hours,
      rate: 100
    });
  } catch (error) {
    ElMessage.error(error.message);
  }
};

// 根据状态返回不同的标签类型
const getStatusType = (status) => {
  const statusMap = {
    'PENDING_ASSIGNMENT': 'info',
    'ASSIGNED': 'warning',
    'IN_PROGRESS': 'primary',
    'AWAITING_PARTS': 'danger',
    'COMPLETED': 'success',
    'CANCELLED_BY_USER': 'info',
    'CANNOT_REPAIR': 'danger',
    'PENDING_USER_CONFIRMATION': 'warning'
  };
  return statusMap[status] || 'info';
};

// 计算材料总成本
const calculateMaterialCost = () => {
  if (!order.value || !order.value.orderMaterialsUsed) return 0;
  return order.value.orderMaterialsUsed.reduce((total, item) => {
    return total + (item.quantityUsed * item.pricePerUnitAtTimeOfUse);
  }, 0);
};

// 计算工时总成本
const calculateLaborCost = () => {
  return workHoursData.value.reduce((total, item) => {
    return total + (item.hours * item.rate);
  }, 0);
};

</script>

<style scoped>
.order-detail-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
  padding: 10px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.detail-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #303133;
  font-weight: 600;
}

.issue-description {
  white-space: pre-line;
  line-height: 1.6;
  padding: 10px 0;
}

.custom-table {
  width: 100%;
}

:deep(.label-bold) {
  font-weight: 600;
  background-color: #f5f7fa;
}

:deep(.el-descriptions__label) {
  width: 120px;
}

.description-content {
  padding: 8px 0;
}

.price-total {
  color: #f56c6c;
  font-weight: 600;
}

.summary-card {
  background-color: #f0f9eb;
}

.order-summary {
  padding: 10px;
}

.order-summary h3 {
  margin-top: 0;
  color: #67c23a;
  border-bottom: 1px solid #e1f3d8;
  padding-bottom: 10px;
}

.summary-content {
  padding: 10px 0;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 16px;
}

.summary-item.total {
  border-top: 1px solid #e1f3d8;
  margin-top: 10px;
  padding-top: 15px;
  font-size: 18px;
  font-weight: bold;
}

.price {
  color: #f56c6c;
}

.loading-container {
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

:deep(.custom-dialog .el-dialog__body) {
  padding: 20px;
}
</style> 