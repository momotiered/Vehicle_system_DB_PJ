import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: (to) => {
        // 根据用户角色重定向到不同的默认页面
        const userStore = useUserStore()
        const userRole = userStore.user?.role
        
        if (!userStore.token) {
          return '/login'
        }
        
        switch (userRole) {
          case 'ADMIN':
            return '/dashboard'
          case 'TECHNICIAN':
            return '/technician/dashboard'
          case 'USER':
          default:
            return '/user-center'
        }
      }
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
      meta: { requiresAuth: true, adminOnly: true }
    },
    {
      path: '/user-center',
      name: 'UserCenter',
      component: () => import('@/views/user/UserCenter.vue'),
      meta: { requiresAuth: true, userOnly: true }
    },
    {
      path: '/users',
      name: 'UserManagement',
      component: () => import('@/views/user/UserList.vue'),
      meta: { requiresAuth: true, adminOnly: true }
    },
    {
      path: '/users/create',
      name: 'CreateUser',
      component: () => import('@/views/user/UserForm.vue'),
      meta: { requiresAuth: true, adminOnly: true }
    },
    {
      path: '/users/:id/edit',
      name: 'EditUser',
      component: () => import('@/views/user/UserForm.vue'),
      meta: { requiresAuth: true, adminOnly: true }
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
      meta: { requiresAuth: true, userOnly: true }
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
  const userRole = userStore.user?.role

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
    return
  }

  if (to.meta.requiresGuest && isAuthenticated) {
    // 已登录用户访问登录/注册页面时，重定向到对应的首页
    switch (userRole) {
      case 'ADMIN':
        next('/dashboard')
        break
      case 'TECHNICIAN':
        next('/technician/dashboard')
        break
      case 'USER':
      default:
        next('/user-center')
        break
    }
    return
  }

  // 角色权限检查
  if (isAuthenticated) {
    const isAdmin = userRole === 'ADMIN'
    const isTechnician = userRole === 'TECHNICIAN'
    const isUser = userRole === 'USER'

    if (to.meta.adminOnly && !isAdmin) {
      next('/dashboard') // 非管理员用户重定向到对应首页
      return
    }

    if (to.meta.technicianOnly && !isTechnician) {
      switch (userRole) {
        case 'ADMIN':
          next('/dashboard')
          break
        case 'USER':
        default:
          next('/user-center')
          break
      }
      return
    }

    if (to.meta.userOnly && !isUser) {
      switch (userRole) {
        case 'ADMIN':
          next('/dashboard')
          break
        case 'TECHNICIAN':
          next('/technician/dashboard')
          break
        default:
          next('/login')
          break
      }
      return
    }
  }

  next()
})

export default router
