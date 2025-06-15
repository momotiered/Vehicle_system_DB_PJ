import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/Login.vue';
import Dashboard from '../views/Dashboard.vue';
import Register from '../views/Register.vue';

// 维修人员视图
import PersonnelLayout from '../layouts/RepairPersonnelLayout.vue';
import PersonnelDashboard from '../views/personnel/PersonnelDashboard.vue';
import AssignmentsView from '../views/personnel/AssignmentsView.vue';
import OrderDetailView from '../views/personnel/OrderDetailView.vue';
import ProfileView from '../views/personnel/ProfileView.vue';
import PayrollView from '../views/personnel/PayrollView.vue';

// 管理员视图
import AdminLayout from '../views/admin/AdminLayout.vue';
import MonitorDashboard from '../views/admin/MonitorDashboard.vue';
import OrderAssignmentView from '../views/admin/OrderAssignmentView.vue';
import UserManagement from '../views/admin/UserManagement.vue';
import PersonnelManagement from '../views/admin/PersonnelManagement.vue';

const routes = [
  {
    path: '/',
    redirect: '/login' // 将根路径重定向到登录页
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/personnel',
    component: PersonnelLayout,
    children: [
      {
        path: 'dashboard',
        name: 'PersonnelDashboard',
        component: PersonnelDashboard
  },
      {
        path: 'assignments',
        name: 'PersonnelAssignments',
        component: AssignmentsView
      },
  {
        path: 'order/:orderId',
        name: 'PersonnelOrderDetail',
        component: OrderDetailView,
        props: true
      },
      {
        path: 'profile',
        name: 'PersonnelProfile',
        component: ProfileView
      },
      {
        path: 'payroll',
        name: 'PersonnelPayroll',
        component: PayrollView
      }
    ]
  },
  // 管理员路由
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      {
        path: 'monitor',
        name: 'AdminMonitorDashboard',
        component: MonitorDashboard
      },
      {
        path: 'assignment',
        name: 'AdminOrderAssignment',
        component: OrderAssignmentView
      },
      {
        path: 'users',
        name: 'AdminUserManagement',
        component: UserManagement
      },
      {
        path: 'personnel',
        name: 'AdminPersonnelManagement',
        component: PersonnelManagement
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router; 