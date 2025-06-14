<template>
  <div v-if="loading" class="loading-container">
    <el-skeleton :rows="6" animated />
    <div style="text-align: center; margin-top: 20px; color: #666;">
      正在加载工单详情...
    </div>
  </div>
  
  <div v-else-if="error" class="error-container">
    <el-result icon="error" title="加载失败" :sub-title="error">
      <template #extra>
        <el-button type="primary" @click="$router.back()">返回</el-button>
        <el-button @click="retryLoad">重试</el-button>
      </template>
    </el-result>
  </div>
  
  <div v-else-if="order" class="order-detail-container">
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
          <div class="description-content">{{ order.user?.fullName || '未知用户' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="车牌号" label-class-name="label-bold">
          <div class="description-content">{{ order.vehicle?.licensePlate || '未知车牌' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="车辆型号" label-class-name="label-bold">
          <div class="description-content">{{ (order.vehicle?.make || '') + ' ' + (order.vehicle?.model || '') }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="报修日期" label-class-name="label-bold">
          <div class="description-content">{{ order.reportDate ? new Date(order.reportDate).toLocaleString() : '未知日期' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2" label-class-name="label-bold">
          <div class="issue-description">{{ order.descriptionOfIssue || '无描述' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h3>使用材料</h3>
          <div>
            <span style="font-size: 12px; color: #999; margin-right: 10px;">
              Debug: isAssignmentCompleted = {{ isAssignmentCompleted }}
            </span>
            <el-button @click="openMaterialDialog" type="primary" size="small" icon="Plus" :disabled="isAssignmentCompleted">添加材料</el-button>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="order.orderMaterialsUsed || []" 
        stripe 
        border 
        class="custom-table"
        :empty-text="'暂无使用材料记录'"
      >
        <el-table-column prop="material.materialName" label="材料名称" min-width="150" align="center">
          <template #default="scope">
            {{ scope.row.material?.materialName || '未知材料' }}
          </template>
        </el-table-column>
        <el-table-column prop="quantityUsed" label="使用数量" width="120" align="center" />
        <el-table-column prop="pricePerUnitAtTimeOfUse" label="使用时单价" width="150" align="center">
          <template #default="scope">
            ¥{{ (scope.row.pricePerUnitAtTimeOfUse || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="小计" width="150" align="center">
          <template #default="scope">
            <span class="price-total">¥{{ ((scope.row.quantityUsed || 0) * (scope.row.pricePerUnitAtTimeOfUse || 0)).toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h3>我的工时</h3>
          <el-button @click="hoursDialogVisible = true" type="primary" size="small" icon="Timer" :disabled="isAssignmentCompleted">记录工时</el-button>
        </div>
      </template>
      
      <el-descriptions :column="1" border>
        <el-descriptions-item label="已记录工时" label-class-name="label-bold">
            <div class="description-content">{{ currentAssignment?.hoursWorked || 0 }} 小时</div>
        </el-descriptions-item>
        <el-descriptions-item label="我的工时费" label-class-name="label-bold">
            <div class="description-content price-total">¥{{ currentAssignment?.laborCostForPersonnel?.toFixed(2) || '0.00' }}</div>
        </el-descriptions-item>
      </el-descriptions>
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

    <div class="action-footer">
        <el-button 
            type="success" 
            size="large" 
            @click="handleCompleteAssignment"
            :disabled="isAssignmentCompleted"
        >
            完成我的维修任务
        </el-button>
    </div>

    <!-- 添加材料对话框 -->
    <el-dialog v-model="materialDialogVisible" title="添加使用材料" width="30%" class="custom-dialog">
      <el-form :model="materialForm" label-position="top">
        <el-form-item label="材料">
          <el-select v-model="materialForm.materialId" placeholder="请选择材料" style="width: 100%" filterable>
            <el-option 
              v-for="material in materials" 
              :key="material.materialId"
              :label="`${material.materialName} (库存: ${material.stockQuantity})`" 
              :value="material.materialId"
            ></el-option>
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
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getOrderDetails, addMaterialToOrder, getMaterials } from '@/api/repairOrder.js';
import { logHoursToAssignment, updateAssignmentStatus } from '@/api/repairPersonnel.js';

const route = useRoute();
const router = useRouter();

const order = ref(null);
const materials = ref([]);
const orderId = ref(null);
const assignmentId = ref(null); 
const loading = ref(true);
const error = ref(null);

const materialDialogVisible = ref(false);
const materialForm = reactive({ materialId: null, quantity: 1 });

const hoursDialogVisible = ref(false);
const hoursForm = reactive({ hours: 0.5 });

const currentAssignment = computed(() => {
    if (!order.value || !assignmentId.value || !order.value.repairAssignments || !Array.isArray(order.value.repairAssignments)) {
        return null;
    }
    return order.value.repairAssignments.find(a => a.id === assignmentId.value);
});

const isAssignmentCompleted = computed(() => {
    return currentAssignment.value?.status === 'Work_Completed';
});

onMounted(async () => {
  console.log('OrderDetailView mounted');
  console.log('Route params:', route.params);
  console.log('Route query:', route.query);
  
  orderId.value = parseInt(route.params.orderId, 10);
  console.log('Parsed orderId:', orderId.value);
  
  const tempAssignmentId = route.query.assignmentId;
  if(tempAssignmentId) {
    assignmentId.value = parseInt(tempAssignmentId, 10);
    console.log('Parsed assignmentId:', assignmentId.value);
  } else {
    console.error('No assignmentId in query params');
    ElMessage.error('无法获取任务ID，请从"我的工单"页面进入');
    router.back();
    return;
  }

  await Promise.all([fetchOrderDetails(), fetchMaterials()]);
  loading.value = false;
});

const fetchOrderDetails = async () => {
    try {
        console.log('Fetching order details for orderId:', orderId.value);
        const data = await getOrderDetails(orderId.value);
        console.log('Order details fetched successfully:', data);
        console.log('Order materials:', data.orderMaterialsUsed);
        
        // 详细检查每个材料记录
        if (data.orderMaterialsUsed && data.orderMaterialsUsed.length > 0) {
            data.orderMaterialsUsed.forEach((item, index) => {
                console.log(`Material ${index}:`, {
                    materialName: item.material?.materialName,
                    quantityUsed: item.quantityUsed,
                    pricePerUnitAtTimeOfUse: item.pricePerUnitAtTimeOfUse,
                    fullItem: item
                });
            });
        }
        
        console.log('Order assignments:', data.repairAssignments);
        order.value = data;
        error.value = null;
    } catch (err) {
        console.error('Failed to fetch order details:', err);
        console.error('Error response:', err.response);
        const errorMessage = err.response?.data?.message || err.message || '加载工单详情失败';
        error.value = errorMessage;
        ElMessage.error(`加载工单详情失败: ${errorMessage}`);
    }
}

const fetchMaterials = async () => {
    try {
        const data = await getMaterials();
        console.log('Fetched materials:', data);
        materials.value = data;
    } catch(error) {
        console.error('加载材料列表失败:', error);
        ElMessage.error('加载材料列表失败');
    }
}

const goBack = () => {
  router.back();
};

const retryLoad = async () => {
  loading.value = true;
  error.value = null;
  await Promise.all([fetchOrderDetails(), fetchMaterials()]);
  loading.value = false;
};

const openMaterialDialog = () => {
  // 重置表单
  materialForm.materialId = null;
  materialForm.quantity = 1;
  materialDialogVisible.value = true;
};

const handleAddMaterial = async () => {
  console.log('handleAddMaterial called');
  console.log('materialForm:', materialForm);
  console.log('materials:', materials.value);
  
  if (!materialForm.materialId) {
      ElMessage.warning('请选择材料');
      return;
  }
  
  console.log('Sending request with:', {
    orderId: orderId.value,
    materialId: materialForm.materialId,
    quantity: materialForm.quantity
  });
  
  try {
    await addMaterialToOrder(orderId.value, materialForm.materialId, materialForm.quantity);
    ElMessage.success('材料添加成功');
    materialDialogVisible.value = false;
    // 重置表单
    materialForm.materialId = null;
    materialForm.quantity = 1;
    fetchOrderDetails();
  } catch (error) {
    console.error('添加材料失败:', error);
    ElMessage.error(error.response?.data?.message || '添加材料失败');
  }
};

const handleLogHours = async () => {
  try {
    await logHoursToAssignment(assignmentId.value, hoursForm.hours);
    ElMessage.success('工时记录成功');
    hoursDialogVisible.value = false;
    fetchOrderDetails();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '记录工时失败');
  }
};

const handleCompleteAssignment = async () => {
    await ElMessageBox.confirm('您确定已完成此项维修任务吗?', '确认完成', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
    });
    try {
        await updateAssignmentStatus(assignmentId.value, 'Work_Completed');
        ElMessage.success('任务已完成！正在返回列表...');
        router.push({ name: 'PersonnelAssignments', query: { tab: 'Work_Completed' } });
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(error.response?.data?.message || '操作失败');
        }
    }
}

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
  console.log('Calculating material cost...');
  if (!order.value || !order.value.orderMaterialsUsed || !Array.isArray(order.value.orderMaterialsUsed)) {
    console.log('No materials data available');
    return 0;
  }
  console.log('Materials data:', order.value.orderMaterialsUsed);
  const total = order.value.orderMaterialsUsed.reduce((total, item) => {
    const quantity = item.quantityUsed || 0;
    const price = item.pricePerUnitAtTimeOfUse || 0;
    const itemCost = quantity * price;
    console.log(`Material: ${item.material?.materialName}, Quantity: ${quantity}, Price: ${price}, Cost: ${itemCost}`);
    return total + itemCost;
  }, 0);
  console.log('Total material cost:', total);
  return total;
};

// 计算工时总成本
const calculateLaborCost = () => {
  console.log('Calculating labor cost...');
  if (!order.value || !order.value.repairAssignments || !Array.isArray(order.value.repairAssignments)) {
    console.log('No assignments data available');
    return 0;
  }
  console.log('Assignments data:', order.value.repairAssignments);
  const total = order.value.repairAssignments.reduce((total, item) => {
    // 使用后端已计算好的单个任务的工时费
    const cost = item.laborCostForPersonnel || 0;
    console.log(`Assignment ID: ${item.id}, Labor Cost: ${cost}`);
    return total + cost;
  }, 0);
  console.log('Total labor cost:', total);
  return total;
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
  font-weight: bold;
  color: #e6a23c;
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

.error-container {
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

.action-footer {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style> 