import Vue from 'vue'
import VueRouter from 'vue-router'
import loginView from "@/views/LoginView.vue";
import registerView from "@/views/RegisterView.vue";
import UserPanel from "@/views/UserPanel/Main.vue";
import UserInfo from "@/views/UserPanel/UserInfo.vue";
import Home from "@/views/UserPanel/UserHome.vue";
import UserManagement from "@/views/UserPanel/UserManagement.vue";
import CulturalProductShop from "@/views/UserPanel/CulturalProductShop.vue";
import CulturalProductManagement from "@/views/UserPanel/CulturalProductManagement.vue";
import BillPayment from "@/views/UserPanel/billPayment.vue";
import CompleteBill from "@/views/UserPanel/CompleteBill.vue";
import TravelPromotion from "@/views/TravelPromotion.vue";
import {jwtDecode} from "jwt-decode";

Vue.use(VueRouter)

//权限类型：1.管理员 3.顾客; 管理员包含顾客权限。
const router = new VueRouter({
    mode:'history',
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
            path: '/travel-experiences',
            component: TravelPromotion,
            meta: {
                showRouterView: true,
                requiredRole: 3
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
                    },
                },
                {
                    path: '/home',
                    component: Home,
                    meta: {showRouterView: true,
                           requiresAuth: true,
                        requiredRole: 3
                    },
                },
                {
                    path: '/userManagement',
                    component: UserManagement,
                    meta: {
                        showRouterView: true,
                        requiresAuth: true,
                        requiredRole: 1
                    },
                },
                {
                    path: 'culturalProducts',
                    component: CulturalProductShop,
                    meta: {
                        showRouterView: true,
                        requiresAuth: true,
                        requiredRole: 3
                    },
                },
                {
                    path: 'culturalProducts/manage',
                    component: CulturalProductManagement,
                    meta: {
                        showRouterView: true,
                        requiresAuth: true,
                        requiredRole: 1
                    },
                },
                {
                    path: '/billPayment',
                    component:  BillPayment,
                    meta: {
                        showRouterView: true,
                        requiresAuth: true,
                        requiredRole: 3
                    },
                },
                {
                    path: '/completeBill',
                    component:  CompleteBill,
                    meta: {
                        showRouterView: true,
                        requiresAuth: true,
                        requiredRole: 3
                    },
                },
            ]
        }
    ]
})

router.beforeEach((to, from, next) => {
    // 检查目标路由是否需要认证
    if (to.matched.length > 0 && to.matched.some(record => record.meta.requiresAuth)) {
        // 获取 token
        const token = sessionStorage.getItem('token');

        // 检查 token 是否存在且有效
        if (!token) {
            // 如果没有 token，重定向到登录页面
            console.log("token不存在");
            next('/login');
        } else {
            // 解析 token 获取权限级别
            const userRole = jwtDecode(sessionStorage.getItem('token') || '').authority;

            // 检查权限级别
            if (userRole && userRole <= to.meta.requiredRole) {
                // 权限足够，继续导航
                next();
            } else {
                // 权限不足，拒绝访问
                console.log("权限不足");
                const vm = new Vue();
                vm.$message.error('拒绝访问！');
                next('/');
            }
        }
    } else {
        // 如果目标路由不需要认证，直接放行
        next();
    }
});

export default router
