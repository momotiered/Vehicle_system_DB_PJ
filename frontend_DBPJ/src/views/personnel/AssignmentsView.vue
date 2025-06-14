<template>
  <div class="assignments-container">
    <el-tabs v-model="activeTab" class="custom-tabs">
      <el-tab-pane label="新任务" name="Assigned"></el-tab-pane>
      <el-tab-pane label="进行中" name="Accepted"></el-tab-pane>
      <el-tab-pane label="已完成" name="Work_Completed"></el-tab-pane>
    </el-tabs>

    <el-table 
      :data="assignments" 
      v-loading="loading" 
      style="width: 100%"
      class="custom-table"
      :row-class-name="tableRowClassName"
      border
      stripe
    >
      <el-table-column prop="assignmentId" label="分配ID" width="100" align="center"></el-table-column>
      <el-table-column prop="orderId" label="工单ID" width="100" align="center">
        <template #default="scope">
          <router-link :to="{ name: 'PersonnelOrderDetail', params: { orderId: scope.row.orderId }, query: { assignmentId: scope.row.assignmentId } }" class="order-link">
            {{ scope.row.orderId }}
          </router-link>
        </template>
      </el-table-column>
      <el-table-column prop="issueDescription" label="问题描述" min-width="250" show-overflow-tooltip>
        <template #default="scope">
          <div class="issue-description">{{ scope.row.issueDescription }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="assignmentDate" label="分配日期" width="180" align="center">
        <template #default="scope">
          {{ new Date(scope.row.assignmentDate).toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column prop="assignmentStatus" label="状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.assignmentStatus)">
            {{ scope.row.assignmentStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" v-if="activeTab === 'Assigned'" align="center">
        <template #default="scope">
          <div class="action-buttons">
            <el-button size="small" type="primary" @click="handleAccept(scope.row.assignmentId)">接受</el-button>
            <el-button size="small" type="danger" @click="handleReject(scope.row.assignmentId)">拒绝</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getAssignments, updateAssignmentStatus } from '@/api/repairPersonnel.js';

const router = useRouter();
const loading = ref(false);
const assignments = ref([]);
const activeTab = ref('Assigned');
const userInfo = ref(null);

onMounted(() => {
  const storedUser = sessionStorage.getItem('user');
  if (storedUser) {
    userInfo.value = JSON.parse(storedUser);

    // 检查路由参数，以确定初始显示的tab
    const initialTab = router.currentRoute.value.query.tab;
    if (initialTab && ['Assigned', 'Accepted', 'Work_Completed'].includes(initialTab)) {
        activeTab.value = initialTab;
    }

    fetchAssignments();
  } else {
    ElMessage.error('请先登录');
    router.push('/login');
  }
});

// 监听activeTab变化，自动获取数据
watch(activeTab, () => {
  if (userInfo.value) {
    fetchAssignments();
  }
});

const fetchAssignments = async () => {
  if (!userInfo.value || !userInfo.value.personnelId) {
    ElMessage.error('无法获取维修人员ID，请重新登录。');
    router.push('/login');
    return;
  }
  loading.value = true;
  try {
    const data = await getAssignments(userInfo.value.personnelId, activeTab.value);
    assignments.value = data;
  } catch (error) {
    ElMessage.error(error.message || '获取工单失败');
  } finally {
    loading.value = false;
  }
};

const handleAccept = async (assignmentId) => {
  try {
    await updateAssignmentStatus(assignmentId, 'Accepted');
    ElMessage.success('已接受工段，已自动跳转到"进行中"');
    activeTab.value = 'Accepted';
    // watch会自动触发fetchAssignments()
  } catch (error) {
    ElMessage.error(error.message);
  }
};

const handleReject = async (assignmentId) => {
  await ElMessageBox.confirm('确定要拒绝这个工单吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  });
  try {
    await updateAssignmentStatus(assignmentId, 'Rejected');
    ElMessage.info('已拒绝工单');
    // 当前标签页没有变化，但需要刷新数据以移除已拒绝的工单
    fetchAssignments();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message);
    }
  }
};

// 根据状态返回不同的标签类型
const getStatusType = (status) => {
  const statusMap = {
    'Assigned': 'info',
    'Accepted': 'primary',
    'Work_Completed': 'success',
    'Rejected': 'danger'
  };
  return statusMap[status] || 'info';
};

// 为表格行添加类名
const tableRowClassName = () => {
  return 'custom-row';
};
</script>

<style scoped>
.assignments-container {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.custom-tabs {
  margin-bottom: 20px;
}

.custom-table {
  margin-top: 20px;
}

.custom-row {
  height: 60px;
}

.order-link {
  color: #409EFF;
  font-weight: bold;
  text-decoration: none;
}

.order-link:hover {
  text-decoration: underline;
}

.issue-description {
  padding: 8px 0;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  justify-content: space-around;
}

.action-buttons .el-button {
  margin: 0 5px;
}

:deep(.el-table th) {
  background-color: #f0f2f5;
  color: #606266;
  font-weight: bold;
  padding: 12px 0;
}

:deep(.el-table td) {
  padding: 12px 0;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fafafa;
}
</style> 