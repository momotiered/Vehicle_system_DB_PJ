import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/Login.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/auth/Register.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/user-center',
      name: 'UserCenter',
      component: () => import('@/views/user/UserCenter.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/users',
      name: 'UserManagement',
      component: () => import('@/views/user/UserList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/users/create',
      name: 'CreateUser',
      component: () => import('@/views/user/UserForm.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/users/:id/edit',
      name: 'EditUser',
      component: () => import('@/views/user/UserForm.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/vehicles',
      name: 'VehicleManagement',
      component: () => import('@/views/vehicle/VehicleList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/vehicles/create',
      name: 'CreateVehicle',
      component: () => import('@/views/vehicle/VehicleForm.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/vehicles/:id/edit',
      name: 'EditVehicle',
      component: () => import('@/views/vehicle/VehicleForm.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/repair-orders',
      name: 'RepairOrderManagement',
      component: () => import('@/views/repairOrder/RepairOrderList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/repair-orders/create',
      name: 'CreateRepairOrder',
      component: () => import('@/views/repairOrder/RepairOrderForm.vue'),
      meta: { requiresAuth: true, adminOnly: true }
    },
    {
      path: '/repair-orders/user/create',
      name: 'UserCreateRepairOrder',
      component: () => import('@/views/repairOrder/UserRepairForm.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/repair-orders/:id/edit',
      name: 'EditRepairOrder',
      component: () => import('@/views/repairOrder/RepairOrderForm.vue'),
      meta: { requiresAuth: true, adminOnly: true }
    },
    {
      path: '/repair-orders/:id',
      name: 'RepairOrderDetail',
      component: () => import('@/views/repairOrder/RepairOrderDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/technician/dashboard',
      name: 'TechnicianDashboard',
      component: () => import('../views/technician/TechnicianDashboard.vue'),
      meta: { requiresAuth: true, technicianOnly: true }
    },
    {
      path: '/technician/order/:id',
      name: 'TechnicianOrderDetail',
      component: () => import('../views/technician/OrderDetail.vue'),
      meta: { requiresAuth: true, technicianOnly: true }
    },
    {
      path: '/materials',
      name: 'MaterialManagement',
      component: () => import('../views/material/MaterialManagement.vue'),
      meta: { requiresAuth: true, adminOnly: true }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue')
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthenticated = !!userStore.token
  const isAdmin = userStore.user?.role === 'ADMIN'
  const isTechnician = userStore.user?.role === 'TECHNICIAN'

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresGuest && isAuthenticated) {
    next('/dashboard')
  } else if (to.meta.adminOnly && !isAdmin) {
    next('/dashboard') // 非管理员用户重定向到仪表板
  } else if (to.meta.technicianOnly && !isTechnician) {
    next('/dashboard') // 非技术人员用户重定向到仪表板
  } else {
    next()
  }
})

export default router
