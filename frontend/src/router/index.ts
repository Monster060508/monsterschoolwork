import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/overview',
    children: [
      {
        path: 'overview',
        name: 'Overview',
        component: () => import('@/views/overview/index.vue'),
        meta: { title: '总览', icon: 'DataBoard' }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/products/index.vue'),
        meta: { title: '商品管理', icon: 'ShoppingCart', roles: ['ADMIN'] }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/orders/index.vue'),
        meta: { title: '订单管理', icon: 'Document' }
      },
      {
        path: 'employees',
        name: 'Employees',
        component: () => import('@/views/employees/index.vue'),
        meta: { title: '员工管理', icon: 'User', roles: ['ADMIN'] }
      },
      {
        path: 'ai-chat',
        name: 'AIChat',
        component: () => import('@/views/ai-chat/index.vue'),
        meta: { title: '智能问答', icon: 'ChatDotRound' }
      },
      {
        path: 'markdown-docs',
        name: 'MarkdownDocs',
        component: () => import('@/views/markdown-docs/index.vue'),
        meta: { title: '文档管理', icon: 'Document', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '企业销售管理系统'} - 企业销售管理系统`
  
  // 检查是否需要登录
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
    return
  }
  
  // 检查权限
  if (to.meta.roles) {
    const userRole = localStorage.getItem('userRole')
    if (!userRole || !to.meta.roles.includes(userRole)) {
      next('/overview')
      return
    }
  }
  
  next()
})

export default router