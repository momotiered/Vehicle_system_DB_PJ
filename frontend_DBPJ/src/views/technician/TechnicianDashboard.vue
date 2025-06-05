<template>
  <div class="technician-dashboard">
    <h1>技术人员工作台</h1>

    <div class="technician-info" v-if="technician">
      <h2>我的信息</h2>
      <div class="info-card">
        <div class="info-item">
          <span class="label">姓名:</span>
          <span>{{ technician.user ? technician.user.fullName : '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">技能类型:</span>
          <span>{{ getSkillTypeName(technician.skillType) }}</span>
        </div>
        <div class="info-item">
          <span class="label">时薪:</span>
          <span>¥{{ technician.hourlyRate }} / 小时</span>
        </div>
        <div class="info-item">
          <span class="label">工作状态:</span>
          <span :class="{'status-available': technician.isAvailable, 'status-unavailable': !technician.isAvailable}">
            {{ technician.isAvailable ? '可接单' : '不接单' }}
          </span>
          <el-button type="primary" size="small" @click="toggleAvailability">
            {{ technician.isAvailable ? '设为不接单' : '设为可接单' }}
          </el-button>
        </div>
        <div class="info-item">
          <span class="label">当前工单数:</span>
          <span>{{ technician.currentWorkOrders }} / {{ technician.maxWorkOrdersPerDay }}</span>
        </div>
      </div>
    </div>

    <div class="assignments-section">
      <h2>工单管理</h2>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待处理工单" name="pending">
          <el-table :data="pendingAssignments" stripe style="width: 100%">
            <el-table-column prop="repairOrder.orderId" label="工单号" width="120" />
            <el-table-column label="车辆" width="180">
              <template #default="scope">
                {{ scope.row.repairOrder.vehicle ? scope.row.repairOrder.vehicle.make + ' ' + scope.row.repairOrder.vehicle.model : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="车牌号" width="150">
              <template #default="scope">
                {{ scope.row.repairOrder.vehicle ? scope.row.repairOrder.vehicle.licensePlate : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="repairOrder.problemDescription" label="问题描述" />
            <el-table-column prop="repairOrder.urgencyLevel" label="紧急程度" width="120">
              <template #default="scope">
                <el-tag :type="getUrgencyTagType(scope.row.repairOrder.urgencyLevel)">
                  {{ getUrgencyLevelName(scope.row.repairOrder.urgencyLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="success" size="small" @click="acceptOrder(scope.row)">接受</el-button>
                <el-button type="danger" size="small" @click="showRejectDialog(scope.row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="进行中工单" name="in_progress">
          <el-table :data="inProgressAssignments" stripe style="width: 100%">
            <el-table-column prop="repairOrder.orderId" label="工单号" width="120" />
            <el-table-column label="车辆" width="180">
              <template #default="scope">
                {{ scope.row.repairOrder.vehicle ? scope.row.repairOrder.vehicle.make + ' ' + scope.row.repairOrder.vehicle.model : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="车牌号" width="150">
              <template #default="scope">
                {{ scope.row.repairOrder.vehicle ? scope.row.repairOrder.vehicle.licensePlate : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="repairOrder.problemDescription" label="问题描述" />
            <el-table-column label="状态" width="120">
              <template #default="scope">
                <el-tag type="warning">进行中</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="primary" size="small" @click="manageOrder(scope.row)">管理</el-button>
                <el-button type="success" size="small" @click="showCompleteDialog(scope.row)">完成</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="已完成工单" name="completed">
          <el-table :data="completedAssignments" stripe style="width: 100%">
            <el-table-column prop="repairOrder.orderId" label="工单号" width="120" />
            <el-table-column label="车辆" width="180">
              <template #default="scope">
                {{ scope.row.repairOrder.vehicle ? scope.row.repairOrder.vehicle.make + ' ' + scope.row.repairOrder.vehicle.model : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="车牌号" width="150">
              <template #default="scope">
                {{ scope.row.repairOrder.vehicle ? scope.row.repairOrder.vehicle.licensePlate : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="repairOrder.problemDescription" label="问题描述" />
            <el-table-column label="完成时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.completedTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="laborHours" label="工时" width="100" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="info" size="small" @click="viewOrderDetails(scope.row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 拒绝工单对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝工单" width="30%">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝原因:" required>
          <el-input v-model="rejectForm.reason" type="textarea" rows="4" placeholder="请输入拒绝原因"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="rejectOrder">确认拒绝</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 完成工单对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成工单" width="30%">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="工时(小时):" required>
          <el-input-number v-model="completeForm.laborHours" :min="1" :max="100"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="completeDialogVisible = false">取消</el-button>
          <el-button type="success" @click="completeOrder">确认完成</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  getTechnicianByUserId, 
  updateTechnicianAvailability,
  getTechnicianAssignmentsByStatus,
  acceptAssignment,
  rejectAssignment,
  completeAssignment
} from '@/api/technician';

export default {
  name: 'TechnicianDashboard',
  
  setup() {
    // 获取当前登录用户信息
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = currentUser.userId;
    const router = useRouter();
    
    const technician = ref(null);
    const pendingAssignments = ref([]);
    const inProgressAssignments = ref([]);
    const completedAssignments = ref([]);
    const activeTab = ref('pending');
    
    // 拒绝工单相关数据
    const rejectDialogVisible = ref(false);
    const currentAssignment = ref(null);
    const rejectForm = ref({
      reason: ''
    });
    
    // 完成工单相关数据
    const completeDialogVisible = ref(false);
    const completeForm = ref({
      laborHours: 1
    });
    
    // 加载技术人员信息
    const loadTechnicianInfo = async () => {
      try {
        const response = await getTechnicianByUserId(userId);
        technician.value = response.data;
        await loadAssignments();
      } catch (error) {
        console.error('加载技术人员信息失败:', error);
        ElMessage.error('加载技术人员信息失败');
      }
    };
    
    // 加载工单分配信息
    const loadAssignments = async () => {
      if (!technician.value) return;
      
      try {
        const pendingResponse = await getTechnicianAssignmentsByStatus(technician.value.technicianId, 'PENDING');
        pendingAssignments.value = pendingResponse.data;
        
        const inProgressResponse = await getTechnicianAssignmentsByStatus(technician.value.technicianId, 'ACCEPTED');
        inProgressAssignments.value = inProgressResponse.data;
        
        const completedResponse = await getTechnicianAssignmentsByStatus(technician.value.technicianId, 'COMPLETED');
        completedAssignments.value = completedResponse.data;
      } catch (error) {
        console.error('加载工单信息失败:', error);
        ElMessage.error('加载工单信息失败');
      }
    };
    
    // 切换可用状态
    const toggleAvailability = async () => {
      try {
        await updateTechnicianAvailability(
          technician.value.technicianId, 
          !technician.value.isAvailable
        );
        technician.value.isAvailable = !technician.value.isAvailable;
        ElMessage.success(`已设置为${technician.value.isAvailable ? '可接单' : '不接单'}状态`);
      } catch (error) {
        console.error('更新工作状态失败:', error);
        ElMessage.error('更新工作状态失败');
      }
    };
    
    // 接受工单
    const acceptOrder = async (assignment) => {
      try {
        await acceptAssignment(assignment.assignmentId);
        ElMessage.success('已接受工单');
        await loadAssignments();
      } catch (error) {
        console.error('接受工单失败:', error);
        ElMessage.error('接受工单失败');
      }
    };
    
    // 显示拒绝工单对话框
    const showRejectDialog = (assignment) => {
      currentAssignment.value = assignment;
      rejectForm.value.reason = '';
      rejectDialogVisible.value = true;
    };
    
    // 拒绝工单
    const rejectOrder = async () => {
      if (!rejectForm.value.reason) {
        ElMessage.warning('请输入拒绝原因');
        return;
      }
      
      try {
        await rejectAssignment(
          currentAssignment.value.assignmentId, 
          rejectForm.value.reason
        );
        rejectDialogVisible.value = false;
        ElMessage.success('已拒绝工单');
        await loadAssignments();
      } catch (error) {
        console.error('拒绝工单失败:', error);
        ElMessage.error('拒绝工单失败');
      }
    };
    
    // 管理工单
    const manageOrder = (assignment) => {
      router.push(`/technician/order/${assignment.repairOrder.orderId}`);
    };
    
    // 显示完成工单对话框
    const showCompleteDialog = (assignment) => {
      currentAssignment.value = assignment;
      completeForm.value.laborHours = 1;
      completeDialogVisible.value = true;
    };
    
    // 完成工单
    const completeOrder = async () => {
      try {
        await completeAssignment(
          currentAssignment.value.assignmentId, 
          completeForm.value.laborHours
        );
        completeDialogVisible.value = false;
        ElMessage.success('工单已完成');
        await loadAssignments();
      } catch (error) {
        console.error('完成工单失败:', error);
        ElMessage.error('完成工单失败');
      }
    };
    
    // 查看工单详情
    const viewOrderDetails = (assignment) => {
      router.push(`/technician/order/${assignment.repairOrder.orderId}`);
    };
    
    // 标签页切换
    const handleTabClick = () => {
      // 这里可以添加额外的处理逻辑
    };
    
    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return '-';
      const date = new Date(dateTime);
      return date.toLocaleString();
    };
    
    // 获取技能类型名称
    const getSkillTypeName = (skillType) => {
      const skillMap = {
        'ENGINE_REPAIR': '发动机维修',
        'TRANSMISSION_REPAIR': '变速箱维修',
        'ELECTRICAL_REPAIR': '电气系统维修',
        'BRAKE_REPAIR': '制动系统维修',
        'SUSPENSION_REPAIR': '悬挂系统维修',
        'BODY_REPAIR': '车身维修',
        'PAINT': '喷漆',
        'TIRE_SERVICE': '轮胎服务',
        'AC_SERVICE': '空调系统维修',
        'GENERAL_MAINTENANCE': '常规保养'
      };
      return skillMap[skillType] || skillType;
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
    
    onMounted(() => {
      loadTechnicianInfo();
    });
    
    return {
      technician,
      pendingAssignments,
      inProgressAssignments,
      completedAssignments,
      activeTab,
      rejectDialogVisible,
      rejectForm,
      completeDialogVisible,
      completeForm,
      toggleAvailability,
      acceptOrder,
      showRejectDialog,
      rejectOrder,
      manageOrder,
      showCompleteDialog,
      completeOrder,
      viewOrderDetails,
      handleTabClick,
      formatDateTime,
      getSkillTypeName,
      getUrgencyLevelName,
      getUrgencyTagType
    };
  }
};
</script>

<style scoped>
.technician-dashboard {
  padding: 20px;
}

h1, h2 {
  margin-bottom: 20px;
}

.info-card {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.label {
  font-weight: bold;
  margin-right: 10px;
  min-width: 100px;
}

.status-available {
  color: #67c23a;
  font-weight: bold;
  margin-right: 15px;
}

.status-unavailable {
  color: #f56c6c;
  font-weight: bold;
  margin-right: 15px;
}

.assignments-section {
  margin-top: 30px;
}

.el-table {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 