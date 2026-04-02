import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

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
        meta: { title: '首页', requiresAdmin: true }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'student',
        name: 'Student',
        component: () => import('@/views/student/StudentList.vue'),
        meta: { title: '学生管理', requiresAdmin: true }
      },
      {
        path: 'teacher',
        name: 'Teacher',
        component: () => import('@/views/teacher/TeacherList.vue'),
        meta: { title: '教师管理', requiresAdmin: true }
      },
      {
        path: 'merchant',
        name: 'Merchant',
        component: () => import('@/views/merchant/MerchantList.vue'),
        meta: { title: '商户管理', requiresAdmin: true }
      },
      {
        path: 'card',
        name: 'Card',
        component: () => import('@/views/card/CardList.vue'),
        meta: { title: '校园卡管理', requiresAdmin: true }
      },
      {
        path: 'account',
        name: 'Account',
        component: () => import('@/views/account/AccountList.vue'),
        meta: { title: '账户管理', requiresAdmin: true }
      },
      {
        path: 'recharge',
        name: 'Recharge',
        component: () => import('@/views/recharge/RechargeList.vue'),
        meta: { title: '充值管理', requiresAdmin: true }
      },
      {
        path: 'consume',
        name: 'Consume',
        component: () => import('@/views/consume/ConsumeList.vue'),
        meta: { title: '消费管理', requiresAdmin: true }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/Statistics.vue'),
        meta: { title: '数据统计', requiresAdmin: true }
      },
      {
        path: 'book',
        name: 'Book',
        component: () => import('@/views/book/BookList.vue'),
        meta: { title: '图书管理', requiresAdmin: true }
      },
      {
        path: 'book/application',
        name: 'BorrowApplication',
        component: () => import('@/views/book/BorrowApplicationList.vue'),
        meta: { title: '借阅申请管理', requiresAdmin: true }
      },
      {
        path: 'access',
        name: 'Access',
        component: () => import('@/views/access/AccessRecordList.vue'),
        meta: { title: '门禁记录管理', requiresAdmin: true }
      },
      {
        path: 'access/point',
        name: 'AccessPoint',
        component: () => import('@/views/access/AccessPointList.vue'),
        meta: { title: '门禁点管理', requiresAdmin: true }
      },
      {
        path: 'access/my',
        name: 'MyAccess',
        component: () => import('@/views/access/MyAccessRecord.vue'),
        meta: { title: '我的门禁记录' }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/views/attendance/AttendanceList.vue'),
        meta: { title: '考勤管理', requiresAdmin: true }
      },
      {
        path: 'attendance/location',
        name: 'AttendanceLocation',
        component: () => import('@/views/attendance/AttendanceLocation.vue'),
        meta: { title: '打卡位置管理', requiresAdmin: false }
      },
      {
        path: 'commute',
        name: 'Commute',
        component: () => import('@/views/commute/CommuteList.vue'),
        meta: { title: '通勤车管理', requiresAdmin: true }
      },
      {
        path: 'commute/student',
        name: 'StudentCommute',
        component: () => import('@/views/commute/StudentCommute.vue'),
        meta: { title: '通勤车查询' }
      },
      {
        path: 'log',
        name: 'Log',
        component: () => import('@/views/log/LogList.vue'),
        meta: { title: '系统日志', requiresAdmin: true }
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
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }
  
  if (to.path === '/login' && token) {
    // 根据用户角色跳转到对应页面
    if (user && user.role === 'admin') {
      next('/')
    } else if (user && user.role === 'teacher') {
      next('/attendance/location')
    } else {
      next('/profile')
    }
    return
  }
  
  if (to.meta.requiresAdmin && user && user.role !== 'admin') {
    // 非管理员尝试访问管理员页面，跳转到个人中心
    ElMessage.warning('无权限访问该页面')
    next('/profile')
    return
  }
  
  next()
})

export default router
