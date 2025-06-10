<template>
  <div class="order-detail">
    <div class="header">
      <h1>工单详情 #{{ orderId }}</h1>
      <el-button type="primary" @click="goBack">返回</el-button>
    </div>

    <el-card v-if="orderInfo" class="order-info">
      <template #header>
        <div class="card-header">
          <h3>工单信息</h3>
          <el-tag :type="getStatusTagType(orderInfo.status)">{{ getStatusName(orderInfo.status) }}</el-tag>
        </div>
      </template>
      <div class="info-grid">
        <div class="info-item">
          <span class="label">提交时间:</span>
          <span>{{ formatDateTime(orderInfo.creationDate) }}</span>
        </div>
        <div class="info-item">
          <span class="label">期望完成时间:</span>
          <span>{{ formatDateTime(orderInfo.expectedCompletionDate) }}</span>
        </div>
        <div class="info-item">
          <span class="label">紧急程度:</span>
          <el-tag :type="getUrgencyTagType(orderInfo.urgencyLevel)">
            {{ getUrgencyLevelName(orderInfo.urgencyLevel) }}
          </el-tag>
        </div>
        <div class="info-item">
          <span class="label">车主:</span>
          <span>{{ orderInfo.user ? orderInfo.user.fullName : '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">联系电话:</span>
          <span>{{ orderInfo.user ? orderInfo.user.contactPhone : '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">联系邮箱:</span>
          <span>{{ orderInfo.user ? orderInfo.user.contactEmail : '-' }}</span>
        </div>
      </div>
      <div class="description-block">
        <h4>问题描述:</h4>
        <p>{{ orderInfo.problemDescription }}</p>
      </div>
    </el-card>

    <el-card v-if="orderInfo && orderInfo.vehicle" class="vehicle-info">
      <template #header>
        <div class="card-header">
          <h3>车辆信息</h3>
        </div>
      </template>
      <div class="info-grid">
        <div class="info-item">
          <span class="label">品牌型号:</span>
          <span>{{ orderInfo.vehicle.make }} {{ orderInfo.vehicle.model }}</span>
        </div>
        <div class="info-item">
          <span class="label">车牌号:</span>
          <span>{{ orderInfo.vehicle.licensePlate }}</span>
        </div>
        <div class="info-item">
          <span class="label">年份:</span>
          <span>{{ orderInfo.vehicle.year }}</span>
        </div>
        <div class="info-item">
          <span class="label">VIN码:</span>
          <span>{{ orderInfo.vehicle.vin }}</span>
        </div>
        <div class="info-item">
          <span class="label">颜色:</span>
          <span>{{ orderInfo.vehicle.color }}</span>
        </div>
        <div class="info-item">
          <span class="label">里程数:</span>
          <span>{{ orderInfo.vehicle.mileage }} km</span>
        </div>
      </div>
    </el-card>

    <el-card class="material-usage-section">
      <template #header>
        <div class="card-header">
          <h3>材料使用记录</h3>
          <el-button type="primary" @click="showAddMaterialDialog">添加材料</el-button>
        </div>
      </template>
      
      <el-table v-loading="loadingMaterials" :data="materialUsages" stripe style="width: 100%">
        <el-table-column label="材料名称" min-width="180">
          <template #default="scope">
            {{ scope.row.material ? scope.row.material.name : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="数量" width="120">
          <template #default="scope">
            {{ scope.row.quantity }} {{ scope.row.material ? scope.row.material.unit : '' }}
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="scope">
            ¥{{ scope.row.unitPrice }}
          </template>
        </el-table-column>
        <el-table-column label="总价" width="120">
          <template #default="scope">
            ¥{{ scope.row.totalPrice }}
          </template>
        </el-table-column>
        <el-table-column label="使用时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.usageTime) }}
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="180">
          <template #default="scope">
            {{ scope.row.notes || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" v-if="orderInfo && orderInfo.status !== 'COMPLETED'">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editMaterialUsage(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteMaterialUsage(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="total-cost" v-if="materialUsages.length > 0">
        <h4>材料总成本: ¥{{ totalMaterialCost }}</h4>
      </div>
      <div v-else class="empty-data">
        <p>暂无材料使用记录</p>
      </div>
    </el-card>

    <el-card class="repair-progress-section">
      <template #header>
        <div class="card-header">
          <h3>维修进度</h3>
          <el-button type="primary" @click="showUpdateStatusDialog" v-if="orderInfo && orderInfo.status !== 'COMPLETED'">
            更新状态
          </el-button>
        </div>
      </template>
      
      <el-timeline>
        <el-timeline-item 
          v-for="log in statusLogs" 
          :key="log.logId" 
          :timestamp="formatDateTime(log.changeTime)" 
          :type="getStatusTimelineType(log.newStatus)">
          <h4>{{ getStatusName(log.newStatus) }}</h4>
          <p v-if="log.note">备注: {{ log.note }}</p>
          <p>操作人: {{ log.operatorName || '-' }}</p>
        </el-timeline-item>
      </el-timeline>
      
      <div v-if="statusLogs.length === 0" class="empty-data">
        <p>暂无状态变更记录</p>
      </div>
    </el-card>

    <!-- 添加/编辑材料使用对话框 -->
    <el-dialog 
      v-model="materialDialogVisible" 
      :title="isEditingMaterial ? '编辑材料使用' : '添加材料使用'" 
      width="50%">
      <el-form :model="materialForm" label-width="100px" :rules="materialRules" ref="materialFormRef">
        <el-form-item label="材料:" prop="materialId">
          <el-select v-model="materialForm.materialId" placeholder="请选择材料" filterable>
            <el-option 
              v-for="item in availableMaterials" 
              :key="item.materialId" 
              :label="`${item.name} (库存: ${item.stockQuantity} ${item.unit})`" 
              :value="item.materialId">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="数量:" prop="quantity">
          <el-input-number 
            v-model="materialForm.quantity" 
            :min="0.01" 
            :max="selectedMaterial ? selectedMaterial.stockQuantity : 9999" 
            :precision="2" 
            :step="0.5">
          </el-input-number>
          <span v-if="selectedMaterial" class="unit-label">{{ selectedMaterial.unit }}</span>
        </el-form-item>
        <el-form-item label="备注:">
          <el-input v-model="materialForm.notes" type="textarea" rows="3"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="materialDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveMaterialUsage">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 更新状态对话框 -->
    <el-dialog v-model="statusDialogVisible" title="更新维修状态" width="40%">
      <el-form :model="statusForm" label-width="100px">
        <el-form-item label="新状态:" prop="newStatus">
          <el-select v-model="statusForm.newStatus" placeholder="请选择状态">
            <el-option v-for="status in availableStatuses" :key="status.value" :label="status.label" :value="status.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注:">
          <el-input v-model="statusForm.note" type="textarea" rows="3"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateOrderStatus">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import repairOrderApi from '@/api/repairOrder';
import { 
  getUsagesByOrderId, 
  getTotalMaterialCost,
  createMaterialUsage,
  updateMaterialUsage,
  deleteMaterialUsage
} from '@/api/material';
import { getAllMaterials } from '@/api/material';
import { getTechnicianByUserId } from '@/api/technician';

export default {
  name: 'OrderDetail',
  
  setup() {
    const route = useRoute();
    const router = useRouter();
    const orderId = ref(route.params.id);
    
    // 获取当前登录用户信息
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = currentUser.userId;
    
    const orderInfo = ref(null);
    const technician = ref(null);
    const materialUsages = ref([]);
    const statusLogs = ref([]);
    const availableMaterials = ref([]);
    const totalMaterialCost = ref(0);
    
    const loadingOrder = ref(false);
    const loadingMaterials = ref(false);
    
    // 材料使用对话框
    const materialDialogVisible = ref(false);
    const isEditingMaterial = ref(false);
    const materialFormRef = ref(null);
    const materialForm = reactive({
      materialId: null,
      quantity: 1,
      notes: ''
    });
    
    const materialRules = {
      materialId: [{ required: true, message: '请选择材料', trigger: 'change' }],
      quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
    };
    
    // 状态更新对话框
    const statusDialogVisible = ref(false);
    const statusForm = reactive({
      newStatus: '',
      note: ''
    });
    
    const availableStatuses = computed(() => {
      const currentStatus = orderInfo.value?.status;
      
      // 根据当前状态决定可以更新到哪些状态
      switch (currentStatus) {
        case 'ASSIGNED':
          return [
            { value: 'IN_PROGRESS', label: '维修中' }
          ];
        case 'IN_PROGRESS':
          return [
            { value: 'REPAIR_COMPLETED', label: '维修完成' },
            { value: 'ON_HOLD', label: '暂停维修' }
          ];
        case 'ON_HOLD':
          return [
            { value: 'IN_PROGRESS', label: '继续维修' }
          ];
        case 'REPAIR_COMPLETED':
          return [
            { value: 'QUALITY_CHECK', label: '质检中' }
          ];
        case 'QUALITY_CHECK':
          return [
            { value: 'COMPLETED', label: '工单完成' },
            { value: 'IN_PROGRESS', label: '返工' }
          ];
        default:
          return [];
      }
    });
    
    // 根据材料ID获取材料信息
    const selectedMaterial = computed(() => {
      if (!materialForm.materialId) return null;
      return availableMaterials.value.find(m => m.materialId === materialForm.materialId);
    });
    
    // 加载工单信息
    const loadOrderInfo = async () => {
      loadingOrder.value = true;
      try {
        const response = await repairOrderApi.getRepairOrderById(orderId.value);
        orderInfo.value = response.data;
        loadStatusLogs();
      } catch (error) {
        console.error('加载工单信息失败:', error);
        ElMessage.error('加载工单信息失败');
      } finally {
        loadingOrder.value = false;
      }
    };
    
    // 加载技术人员信息
    const loadTechnicianInfo = async () => {
      try {
        const response = await getTechnicianByUserId(userId);
        technician.value = response.data;
      } catch (error) {
        console.error('加载技术人员信息失败:', error);
      }
    };
    
    // 加载材料使用记录
    const loadMaterialUsages = async () => {
      loadingMaterials.value = true;
      try {
        const response = await getUsagesByOrderId(orderId.value);
        materialUsages.value = response.data;
        
        // 获取材料总成本
        const costResponse = await getTotalMaterialCost(orderId.value);
        totalMaterialCost.value = costResponse.data.totalCost;
      } catch (error) {
        console.error('加载材料使用记录失败:', error);
        ElMessage.error('加载材料使用记录失败');
      } finally {
        loadingMaterials.value = false;
      }
    };
    
    // 加载状态变更日志
    const loadStatusLogs = async () => {
      if (!orderInfo.value) return;
      
      try {
        // 假设有获取状态变更日志的API
        // const response = await getStatusLogsByOrderId(orderId.value);
        // statusLogs.value = response.data;
        
        // 临时模拟数据
        statusLogs.value = [
          {
            logId: 1,
            orderId: orderId.value,
            oldStatus: null,
            newStatus: 'PENDING_ASSIGNMENT',
            changeTime: orderInfo.value.creationDate,
            operatorName: orderInfo.value.user?.fullName,
            note: '工单创建'
          },
          {
            logId: 2,
            orderId: orderId.value,
            oldStatus: 'PENDING_ASSIGNMENT',
            newStatus: 'ASSIGNED',
            changeTime: new Date(new Date(orderInfo.value.creationDate).getTime() + 3600000),
            operatorName: 'System',
            note: '工单已分配'
          }
        ];
        
        if (orderInfo.value.status !== 'ASSIGNED') {
          statusLogs.value.push({
            logId: 3,
            orderId: orderId.value,
            oldStatus: 'ASSIGNED',
            newStatus: orderInfo.value.status,
            changeTime: new Date(),
            operatorName: technician.value?.user?.fullName,
            note: '维修处理中'
          });
        }
      } catch (error) {
        console.error('加载状态变更日志失败:', error);
      }
    };
    
    // 加载可用材料
    const loadAvailableMaterials = async () => {
      try {
        const response = await getAllMaterials();
        availableMaterials.value = response.data.filter(m => m.stockQuantity > 0);
      } catch (error) {
        console.error('加载可用材料失败:', error);
        ElMessage.error('加载可用材料失败');
      }
    };
    
    // 显示添加材料对话框
    const showAddMaterialDialog = () => {
      isEditingMaterial.value = false;
      materialForm.materialId = null;
      materialForm.quantity = 1;
      materialForm.notes = '';
      materialDialogVisible.value = true;
    };
    
    // 编辑材料使用记录
    const editMaterialUsage = (item) => {
      isEditingMaterial.value = true;
      materialForm.usageId = item.usageId;
      materialForm.materialId = item.material.materialId;
      materialForm.quantity = item.quantity;
      materialForm.notes = item.notes || '';
      materialDialogVisible.value = true;
    };
    
    // 保存材料使用记录
    const saveMaterialUsage = async () => {
      if (!materialFormRef.value) return;
      
      await materialFormRef.value.validate(async (valid) => {
        if (!valid) return;
        
        try {
          if (isEditingMaterial.value) {
            // 更新现有记录
            await updateMaterialUsage(materialForm.usageId, {
              quantity: materialForm.quantity,
              notes: materialForm.notes
            });
            ElMessage.success('材料使用记录已更新');
          } else {
            // 创建新记录
            await createMaterialUsage(
              {
                quantity: materialForm.quantity,
                notes: materialForm.notes
              },
              orderId.value,
              materialForm.materialId,
              technician.value.technicianId
            );
            ElMessage.success('材料使用记录已添加');
          }
          
          materialDialogVisible.value = false;
          loadMaterialUsages();
        } catch (error) {
          console.error('保存材料使用记录失败:', error);
          ElMessage.error('保存材料使用记录失败');
        }
      });
    };
    
    // 删除材料使用记录
    const deleteMaterialUsage = (item) => {
      ElMessageBox.confirm('确定要删除这条材料使用记录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteMaterialUsage(item.usageId);
          ElMessage.success('材料使用记录已删除');
          loadMaterialUsages();
        } catch (error) {
          console.error('删除材料使用记录失败:', error);
          ElMessage.error('删除材料使用记录失败');
        }
      }).catch(() => {});
    };
    
    // 显示更新状态对话框
    const showUpdateStatusDialog = () => {
      statusForm.newStatus = availableStatuses.value.length > 0 ? availableStatuses.value[0].value : '';
      statusForm.note = '';
      statusDialogVisible.value = true;
    };
    
    // 更新工单状态
    const updateOrderStatus = async () => {
      if (!statusForm.newStatus) {
        ElMessage.warning('请选择新状态');
        return;
      }
      
      try {
        await repairOrderApi.updateRepairOrderStatus(orderId.value, statusForm.newStatus, statusForm.note);
        statusDialogVisible.value = false;
        ElMessage.success('工单状态已更新');
        loadOrderInfo();
      } catch (error) {
        console.error('更新工单状态失败:', error);
        ElMessage.error('更新工单状态失败');
      }
    };
    
    // 返回上一页
    const goBack = () => {
      router.push('/technician/dashboard');
    };
    
    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return '-';
      const date = new Date(dateTime);
      return date.toLocaleString();
    };
    
    // 获取状态名称
    const getStatusName = (status) => {
      const statusMap = {
        'PENDING': '待处理',
        'PENDING_ASSIGNMENT': '待分配',
        'ASSIGNED': '已分配',
        'IN_PROGRESS': '维修中',
        'ON_HOLD': '维修暂停',
        'REPAIR_COMPLETED': '维修完成',
        'QUALITY_CHECK': '质检中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      };
      return statusMap[status] || status;
    };
    
    // 获取状态标签类型
    const getStatusTagType = (status) => {
      const typeMap = {
        'PENDING': 'info',
        'PENDING_ASSIGNMENT': 'info',
        'ASSIGNED': 'warning',
        'IN_PROGRESS': 'warning',
        'ON_HOLD': 'danger',
        'REPAIR_COMPLETED': 'success',
        'QUALITY_CHECK': 'warning',
        'COMPLETED': 'success',
        'CANCELLED': 'danger'
      };
      return typeMap[status] || 'info';
    };
    
    // 获取状态时间线类型
    const getStatusTimelineType = (status) => {
      const typeMap = {
        'PENDING': 'info',
        'PENDING_ASSIGNMENT': 'info',
        'ASSIGNED': 'warning',
        'IN_PROGRESS': 'primary',
        'ON_HOLD': 'danger',
        'REPAIR_COMPLETED': 'success',
        'QUALITY_CHECK': 'warning',
        'COMPLETED': 'success',
        'CANCELLED': 'danger'
      };
      return typeMap[status] || 'info';
    };
    
    // 获取紧急程度名称
    const getUrgencyLevelName = (level) => {
      const levelMap = {
        'LOW': '低',
        'MEDIUM': '中',
        'HIGH': '高',
        'CRITICAL': '紧急'
      };
      return levelMap[level] || level;
    };
    
    // 获取紧急程度标签类型
    const getUrgencyTagType = (level) => {
      const typeMap = {
        'LOW': 'info',
        'MEDIUM': 'warning',
        'HIGH': 'danger',
        'CRITICAL': 'danger'
      };
      return typeMap[level] || 'info';
    };
    
    onMounted(async () => {
      await Promise.all([
        loadTechnicianInfo(),
        loadOrderInfo(),
        loadAvailableMaterials()
      ]);
      loadMaterialUsages();
    });
    
    return {
      orderId,
      orderInfo,
      technician,
      materialUsages,
      statusLogs,
      availableMaterials,
      totalMaterialCost,
      loadingOrder,
      loadingMaterials,
      materialDialogVisible,
      isEditingMaterial,
      materialFormRef,
      materialForm,
      materialRules,
      statusDialogVisible,
      statusForm,
      availableStatuses,
      selectedMaterial,
      showAddMaterialDialog,
      editMaterialUsage,
      saveMaterialUsage,
      deleteMaterialUsage,
      showUpdateStatusDialog,
      updateOrderStatus,
      goBack,
      formatDateTime,
      getStatusName,
      getStatusTagType,
      getStatusTimelineType,
      getUrgencyLevelName,
      getUrgencyTagType
    };
  }
};
</script>

<style scoped>
.order-detail {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-card {
  margin-bottom: 30px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  font-weight: bold;
  margin-right: 10px;
  min-width: 100px;
}

.description-block {
  margin-top: 20px;
}

.material-usage-section, 
.repair-progress-section {
  margin-top: 30px;
}

.empty-data {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}

.total-cost {
  margin-top: 20px;
  text-align: right;
}

.unit-label {
  margin-left: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 