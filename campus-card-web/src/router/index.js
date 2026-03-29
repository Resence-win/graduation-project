import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'student',
        name: 'Student',
        component: () => import('@/views/student/StudentList.vue'),
        meta: { title: '学生管理' }
      },
      {
        path: 'teacher',
        name: 'Teacher',
        component: () => import('@/views/teacher/TeacherList.vue'),
        meta: { title: '教师管理' }
      },
      {
        path: 'merchant',
        name: 'Merchant',
        component: () => import('@/views/merchant/MerchantList.vue'),
        meta: { title: '商户管理' }
      },
      {
        path: 'card',
        name: 'Card',
        component: () => import('@/views/card/CardList.vue'),
        meta: { title: '校园卡管理' }
      },
      {
        path: 'account',
        name: 'Account',
        component: () => import('@/views/account/AccountList.vue'),
        meta: { title: '账户管理' }
      },
      {
        path: 'recharge',
        name: 'Recharge',
        component: () => import('@/views/recharge/RechargeList.vue'),
        meta: { title: '充值管理' }
      },
      {
        path: 'consume',
        name: 'Consume',
        component: () => import('@/views/consume/ConsumeList.vue'),
        meta: { title: '消费管理' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/Statistics.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'book',
        name: 'Book',
        component: () => import('@/views/book/BookList.vue'),
        meta: { title: '图书管理' }
      },
      {
        path: 'access',
        name: 'Access',
        component: () => import('@/views/access/AccessList.vue'),
        meta: { title: '门禁管理' }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/views/attendance/AttendanceList.vue'),
        meta: { title: '考勤管理' }
      },
      {
        path: 'commute',
        name: 'Commute',
        component: () => import('@/views/commute/CommuteList.vue'),
        meta: { title: '通勤车管理' }
      },
      {
        path: 'log',
        name: 'Log',
        component: () => import('@/views/log/LogList.vue'),
        meta: { title: '系统日志' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
