<template>
  <div class="dashboard-container">
    <div class="header">
      <h2>欢迎, {{ user.fullName }}!</h2>
      <button @click="logout" class="logout-button">退出登录</button>
    </div>

    <div class="tabs">
      <button :class="{ active: activeTab === 'info' }" @click="activeTab = 'info'">个人信息</button>
      <button :class="{ active: activeTab === 'vehicles' }" @click="fetchVehicles">我的车辆</button>
      <button :class="{ active: activeTab === 'orders' }" @click="fetchRepairOrders">维修记录</button>
    </div>

    <div class="tab-content">
      <!-- 个人信息 -->
      <div v-if="activeTab === 'info'" class="user-info">
        <p><strong>用户名:</strong> {{ user.username }}</p>
        <p><strong>邮箱:</strong> {{ user.contactEmail }}</p>
        <p><strong>电话:</strong> {{ user.contactPhone }}</p>
        <p><strong>地址:</strong> {{ user.address }}</p>
        <p><strong>注册日期:</strong> {{ new Date(user.registrationDate).toLocaleDateString() }}</p>
        <p><strong>角色:</strong> {{ user.role }}</p>
      </div>

      <!-- 我的车辆 -->
      <div v-if="activeTab === 'vehicles'">
        <div v-if="vehicles.length === 0">暂无车辆信息。</div>
        <ul v-else class="item-list">
          <li v-for="vehicle in vehicles" :key="vehicle.vehicleId">
            <div class="item-info">
              <p><strong>车牌号:</strong> {{ vehicle.licensePlate }}</p>
              <p><strong>品牌:</strong> {{ vehicle.make }}</p>
              <p><strong>型号:</strong> {{ vehicle.model }}</p>
              <p><strong>年份:</strong> {{ vehicle.yearOfManufacture }}</p>
            </div>
            <div class="item-actions">
              <button @click="showRepairModal(vehicle.vehicleId)">报修</button>
            </div>
          </li>
        </ul>
      </div>

      <!-- 维修记录 -->
      <div v-if="activeTab === 'orders'">
        <div v-if="repairOrders.length === 0">暂无维修记录。</div>
        <ul v-else class="item-list">
          <li v-for="order in repairOrders" :key="order.orderId">
            <div class="item-info">
              <p><strong>问题描述:</strong> {{ order.descriptionOfIssue }}</p>
              <p><strong>状态:</strong> {{ formatStatus(order.status) }}</p>
              <p><strong>创建日期:</strong> {{ new Date(order.reportDate).toLocaleDateString() }}</p>
            </div>
            <div class="item-actions">
              <button v-if="['PENDING_ASSIGNMENT', 'ASSIGNED', 'IN_PROGRESS'].includes(order.status)" @click="urgeOrder(order.orderId)" class="urge-button">催单</button>
              <button v-if="order.status === 'COMPLETED'" @click="showFeedbackModal(order.orderId)">评价</button>
            </div>
          </li>
        </ul>
      </div>
    </div>

    <!-- 报修模态框 -->
    <div v-if="isRepairModalVisible" class="modal">
      <div class="modal-content">
        <span class="close" @click="isRepairModalVisible = false">&times;</span>
        <h3>提交报修</h3>
        <textarea v-model="repairDescription" placeholder="请描述您车辆遇到的问题..."></textarea>
        <button @click="submitRepair">提交</button>
      </div>
    </div>

    <!-- 反馈模态框 -->
    <div v-if="isFeedbackModalVisible" class="modal">
      <div class="modal-content">
        <span class="close" @click="isFeedbackModalVisible = false">&times;</span>
        <h3>服务评价</h3>
        <input type="number" v-model.number="feedbackRating" min="1" max="5" placeholder="评分 (1-5)">
        <textarea v-model="feedbackComment" placeholder="请留下您的宝贵意见..."></textarea>
        <button @click="submitFeedback">提交</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: null,
      activeTab: 'info', // 默认显示个人信息
      vehicles: [],
      repairOrders: [],
      isRepairModalVisible: false,
      isFeedbackModalVisible: false,
      selectedVehicleId: null,
      selectedOrderId: null,
      repairDescription: '',
      feedbackRating: 5,
      feedbackComment: ''
    };
  },
  created() {
    const userData = sessionStorage.getItem('user');
    if (userData) {
      this.user = JSON.parse(userData);
    } else {
      this.$router.push('/login');
    }
  },
  methods: {
    logout() {
      sessionStorage.removeItem('user');
      alert('您已成功退出！');
      this.$router.push('/login');
    },
    formatStatus(status) {
      const statusMap = {
        'PENDING_ASSIGNMENT': '待处理',
        'ASSIGNED': '已分配',
        'IN_PROGRESS': '处理中',
        'AWAITING_PARTS': '等待配件',
        'COMPLETED': '已完成',
        'CANCELLED_BY_USER': '用户已取消',
        'CANNOT_REPAIR': '无法维修',
        'PENDING_USER_CONFIRMATION': '等待用户确认'
      };
      return statusMap[status] || status;
    },
    async fetchVehicles() {
      this.activeTab = 'vehicles';
      if (!this.user) return;
      try {
        const response = await fetch(`/api/users/${this.user.userId}/vehicles`);
        if (response.ok) {
          this.vehicles = await response.json();
        } else {
          console.error('获取车辆信息失败');
        }
      } catch (error) {
        console.error('请求车辆信息出错:', error);
      }
    },
    async fetchRepairOrders() {
      this.activeTab = 'orders';
      if (!this.user) return;
      try {
        const response = await fetch(`/api/users/${this.user.userId}/repair-orders`);
        if (response.ok) {
          this.repairOrders = await response.json();
        } else {
          console.error('获取维修记录失败');
        }
      } catch (error) {
        console.error('请求维修记录出错:', error);
      }
    },
    showRepairModal(vehicleId) {
      this.selectedVehicleId = vehicleId;
      this.isRepairModalVisible = true;
    },
    async submitRepair() {
      if (!this.repairDescription) {
        alert('请填写问题描述！');
        return;
      }
      try {
        const response = await fetch('/api/repair-orders', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            userId: this.user.userId,
            vehicleId: this.selectedVehicleId,
            descriptionOfIssue: this.repairDescription,
          }),
        });
        if (response.status === 201) {
          alert('报修提交成功！');
          this.isRepairModalVisible = false;
          this.repairDescription = '';
          this.fetchRepairOrders(); // 刷新维修列表
        } else {
          alert('提交失败：' + await response.text());
        }
      } catch (error) {
        alert('请求失败：' + error);
      }
    },
    async urgeOrder(orderId) {
      try {
        const response = await fetch(`/api/repair-orders/${orderId}/urge`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
        });
        if (response.ok) {
          alert('已成功催单！我们将尽快为您处理。');
        } else {
          alert('催单失败：' + await response.text());
        }
      } catch (error) {
        alert('请求失败：' + error);
      }
    },
    showFeedbackModal(orderId) {
      this.selectedOrderId = orderId;
      this.isFeedbackModalVisible = true;
    },
    async submitFeedback() {
        if (!this.feedbackRating) {
            alert('请提供评分！');
            return;
        }
        try {
            const response = await fetch('/api/feedback', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    userId: this.user.userId,
                    orderId: this.selectedOrderId,
                    ratingScore: this.feedbackRating,
                    comments: this.feedbackComment,
                }),
            });
            if (response.status === 201) {
                alert('感谢您的反馈！');
                this.isFeedbackModalVisible = false;
                this.feedbackRating = 5;
                this.feedbackComment = '';
            } else {
                alert('提交失败：' + await response.text());
            }
        } catch (error) {
            alert('请求失败：' + error);
        }
    }
  }
};
</script>

<style scoped>
.dashboard-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}
.header h2 {
  color: #333;
  font-weight: 600;
}
.logout-button {
  padding: 8px 15px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}
.logout-button:hover {
  background-color: #c82333;
}
.tabs {
  margin-top: 20px;
}
.tabs button {
  padding: 10px 15px;
  cursor: pointer;
  border: 1px solid transparent;
  background-color: #f0f0f0;
  margin-right: 5px;
  border-radius: 5px 5px 0 0;
  transition: background-color 0.2s ease, color 0.2s ease;
}
.tabs button:not(.active):hover {
  background-color: #e2e6ea;
}
.tabs button.active {
  background-color: #007bff;
  color: white;
  border-color: #007bff;
}
.tab-content {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 0 5px 5px 5px;
}
.user-info p {
  margin: 15px 0;
  font-size: 16px;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}
.user-info p:last-child {
  border-bottom: none;
}
.user-info strong {
  color: #007bff;
  margin-right: 10px;
}
.item-list {
  list-style: none;
  padding: 0;
}
.item-list li {
  padding: 15px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.item-list li:last-child {
  border-bottom: none;
}
.item-info p {
  margin: 4px 0;
  font-size: 15px;
  color: #333;
}
.item-info p strong {
  font-weight: 600;
  color: #007bff;
  margin-right: 8px;
  display: inline-block;
  width: 90px;
}
.item-actions button {
  background-color: #28a745;
  color: white;
  padding: 8px 15px;
  border-radius: 5px;
  border: none;
  cursor: pointer;
  margin-left: 10px;
  transition: background-color 0.2s ease;
}
.item-actions button:hover {
  background-color: #218838;
}
.item-actions button:first-child {
  margin-left: 0;
}
.item-actions .urge-button {
    background-color: #ffc107;
}
.item-actions .urge-button:hover {
    background-color: #e0a800;
}
.modal {
  position: fixed;
  z-index: 1;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-content {
  background-color: #fefefe;
  padding: 20px;
  border: 1px solid #888;
  width: 80%;
  max-width: 500px;
  border-radius: 5px;
  position: relative;
}
.close {
  color: #aaa;
  position: absolute;
  right: 15px;
  top: 10px;
  font-size: 28px;
  font-weight: bold;
  cursor: pointer;
}
.modal-content textarea, .modal-content input {
    width: 100%;
    padding: 10px;
    margin: 10px 0;
    box-sizing: border-box;
    border: 1px solid #ccc;
    border-radius: 4px;
}
.modal-content button {
    width: 100%;
    padding: 10px;
    margin-top: 10px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 16px;
    transition: background-color 0.2s ease;
}
.modal-content button:hover {
    background-color: #0056b3;
}
</style> 