import Vue from 'vue'
import VueRouter from 'vue-router'
import loginView from '@/views/LoginView.vue'
import registerView from '@/views/RegisterView.vue'
import ForgotPasswordView from '@/views/ForgotPasswordView.vue'
import UserPanel from '@/views/UserPanel/Main.vue'
import UserInfo from '@/views/UserPanel/UserInfo.vue'
import UserManagement from '@/views/UserPanel/UserManagement.vue'
import CulturalProductShop from '@/views/UserPanel/CulturalProductShop.vue'
import CulturalProductManagement from '@/views/UserPanel/CulturalProductManagement.vue'
import OrderManagement from '@/views/UserPanel/OrderManagement.vue'
import BillPayment from '@/views/UserPanel/billPayment.vue'
import CompleteBill from '@/views/UserPanel/CompleteBill.vue'
import TravelPromotion from '@/views/TravelPromotion.vue'
import PickupVerify from '@/views/PickupVerify.vue'
import { jwtDecode } from 'jwt-decode'

Vue.use(VueRouter)

const router = new VueRouter({
  mode: 'history',
  routes: [
    {
      path: '/culturalProducts',
      component: CulturalProductShop,
      meta: {
        showRouterView: true
      }
    },
    {
      path: '/login',
      component: loginView,
      meta: {
        showRouterView: true,
        requiredRole: 3
      }
    },
    {
      path: '/register',
      component: registerView,
      meta: {
        showRouterView: true,
        requiredRole: 3
      }
    },
    {
      path: '/forgot-password',
      component: ForgotPasswordView,
      meta: {
        showRouterView: true,
        requiredRole: 3
      }
    },
    {
      path: '/travel-experiences',
      component: TravelPromotion,
      meta: {
        showRouterView: true,
        requiredRole: 3
      }
    },
    {
      path: '/pickup-verify',
      component: PickupVerify,
      meta: {
        showRouterView: true,
        requiresAuth: true,
        requiredRole: 1
      }
    },
    {
      path: '/userPanel',
      component: UserPanel,
      meta: {
        showRouterView: true,
        requiresAuth: true,
        requiredRole: 3
      },
      children: [
        {
          path: '/userInfo',
          component: UserInfo,
          meta: {
            showRouterView: true,
            requiresAuth: true,
            requiredRole: 3
          }
        },
        {
          path: '/home',
          redirect: '/userInfo',
          meta: {
            showRouterView: true,
            requiresAuth: true,
            requiredRole: 3
          }
        },
        {
          path: '/userManagement',
          component: UserManagement,
          meta: {
            showRouterView: true,
            requiresAuth: true,
            requiredRole: 1
          }
        },
        {
          path: '/orderManagement',
          component: OrderManagement,
          meta: {
            showRouterView: true,
            requiresAuth: true,
            requiredRole: 1
          }
        },
        {
          path: 'culturalProducts',
          component: CulturalProductShop,
          meta: {
            showRouterView: true,
            requiresAuth: true,
            requiredRole: 3
          }
        },
        {
          path: 'culturalProducts/manage',
          component: CulturalProductManagement,
          meta: {
            showRouterView: true,
            requiresAuth: true,
            requiredRole: 1
          }
        },
        {
          path: '/billPayment',
          component: BillPayment,
          meta: {
            showRouterView: true,
            requiresAuth: true,
            requiredRole: 3
          }
        },
        {
          path: '/completeBill',
          component: CompleteBill,
          meta: {
            showRouterView: true,
            requiresAuth: true,
            requiredRole: 3
          }
        }
      ]
    }
  ]
})

function tokenExpired(token) {
  try {
    const payload = jwtDecode(token)
    if (!payload || !payload.exp) {
      return false
    }
    return Date.now() >= payload.exp * 1000
  } catch (e) {
    return true
  }
}

router.beforeEach((to, from, next) => {
  if (!(to.matched.length > 0 && to.matched.some(record => record.meta.requiresAuth))) {
    next()
    return
  }

  const token = sessionStorage.getItem('token')
  if (!token || tokenExpired(token)) {
    sessionStorage.removeItem('token')
    sessionStorage.setItem('loginExpired', '1')
    next('/login')
    return
  }

  let userRole
  try {
    userRole = jwtDecode(token).authority
  } catch (e) {
    sessionStorage.removeItem('token')
    sessionStorage.setItem('loginExpired', '1')
    next('/login')
    return
  }

  if (userRole && userRole <= to.meta.requiredRole) {
    next()
  } else {
    const vm = new Vue()
    vm.$message.error('拒绝访问')
    next('/')
  }
})

export default router
