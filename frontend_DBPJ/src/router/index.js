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
      meta: { requiresAuth: true }
    },
    {
      path: '/repair-orders/:id/edit',
      name: 'EditRepairOrder',
      component: () => import('@/views/repairOrder/RepairOrderForm.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/repair-orders/:id',
      name: 'RepairOrderDetail',
      component: () => import('@/views/repairOrder/RepairOrderDetail.vue'),
      meta: { requiresAuth: true }
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

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresGuest && isAuthenticated) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
