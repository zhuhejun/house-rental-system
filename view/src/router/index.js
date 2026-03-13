import Vue from "vue";
import VueRouter from "vue-router";
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import { clearToken, getToken, getRole } from "@/utils/storage.js";
import echarts from 'echarts';
Vue.prototype.$echarts = echarts;
Vue.use(ElementUI);
Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    redirect: '/view'
  },
  {
    path: "/view",
    name: 'view',
    component: () => import(`@/views/view/Index.vue`)
  },
  {
    path: "/notice-detail",
    name: 'noticeDetail',
    component: () => import(`@/views/user/NoticeDetail.vue`)
  },
  {
    path: "/community-detail",
    name: 'communityDetail',
    component: () => import(`@/views/user/CommunityDetail.vue`)
  },
  {
    path: "/search-house",
    name: 'searchHouse',
    component: () => import(`@/views/view/SearchPage.vue`)
  },
  {
    path: "/house-news-page",
    name: 'houseNewsPage',
    component: () => import(`@/views/view/HouseNewsPage.vue`)
  },
  {
    path: "/house-detail-page",
    name: 'houseDetailPage',
    component: () => import(`@/views/view/HouseDetailPage.vue`)
  },
  {
    path: "/house-detail",
    name: 'houseDetail',
    component: () => import(`@/views/user/HouseDetail.vue`)
  },
  {
    path: "/house-news-detail",
    name: 'houseNewsDetail',
    component: () => import(`@/views/user/HouseNewsDetail.vue`)
  },
  {
    path: "/service-center",
    name: 'serviceCenter',
    component: () => import(`@/views/service/Index.vue`),
    children: [
      {
        path: 'landlord',
        component: () => import(`@/views/service/Landlord.vue`),
      },
      {
        path: 'post-house',
        component: () => import(`@/views/service/PostHouse.vue`),
      },
      {
        path: 'update-house',
        component: () => import(`@/views/service/UpdateHouse.vue`),
      },
      {
        path: 'house-list',
        component: () => import(`@/views/service/HouseList.vue`),
      },
      {
        path: 'flow-index',
        component: () => import(`@/views/service/FlowIndex.vue`),
      },
      {
        path: 'house-order-info',
        component: () => import(`@/views/service/HouseOrderInfo.vue`),
      },
      {
        path: 'rental-contract',
        component: () => import(`@/views/service/RentalContract.vue`),
      },
      {
        path: 'rental-bill',
        component: () => import(`@/views/service/RentalBill.vue`),
      }
    ],
  },
  {
    path: "/reply-landlord",
    name: 'replyLandlord',
    component: () => import(`@/views/user/ReplyLandlord.vue`)
  },
  {
    path: "/login",
    name: 'login',
    component: () => import(`@/views/login/Login.vue`)
  },
  {
    path: "/register",
    component: () => import(`@/views/register/Register.vue`)
  },
  {
    path: "/admin",
    component: () => import(`@/views/admin/Home.vue`),
    meta: {
      requireAuth: true,
    },
    children: [
      {
        path: "/admin-layout",
        name: '仪表盘',
        icon: 'el-icon-pie-chart',
        show: true,
        component: () => import(`@/views/admin/Main.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/user-manage",
        name: '用户管理',
        show: true,
        icon: 'el-icon-user',
        component: () => import(`@/views/admin/UserManage.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/landlord-manage",
        name: '房东管理',
        show: true,
        icon: 'el-icon-user-solid',
        component: () => import(`@/views/admin/LandlordManage.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/community-manage",
        name: '小区管理',
        show: true,
        icon: 'el-icon-s-help',
        component: () => import(`@/views/admin/CommunityManage.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/house-manage",
        name: '房源管理',
        show: true,
        icon: 'el-icon-s-shop',
        component: () => import(`@/views/admin/HouseManage.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/house-news-manage",
        name: '房源资讯管理',
        show: true,
        icon: 'el-icon-tickets',
        component: () => import(`@/views/admin/HouseNewsManage.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/house-order-info-manage",
        name: '预约看房管理',
        show: true,
        icon: 'el-icon-s-grid',
        component: () => import(`@/views/admin/HouseOrderInfoManage.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/notice-manage",
        name: '公告管理',
        show: true,
        icon: 'el-icon-c-scale-to-original',
        component: () => import(`@/views/admin/NoticeManage.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/rental-contract-manage",
        name: '租赁合同管理',
        show: true,
        icon: 'el-icon-document',
        component: () => import(`@/views/admin/RentalContractManage.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/rental-bill-manage",
        name: '租赁账单管理',
        show: true,
        icon: 'el-icon-wallet',
        component: () => import(`@/views/admin/RentalBillManage.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/community-update",
        name: '修改小区信息',
        show: false,
        icon: 'el-icon-user',
        component: () => import(`@/views/admin/CommunityUpdate.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/evaluations-manage",
        name: '评论管理',
        show: true,
        icon: 'el-icon-chat-dot-round',
        component: () => import(`@/views/admin/EvaluationsManage.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/update-password",
        name: '修改个人密码',
        show: false, // 不在导航栏里面显示
        component: () => import(`@/views/admin/UpdatePassword.vue`),
        meta: { requireAuth: true },
      },
    ]
  },
  {
    path: "/user",
    component: () => import(`@/views/user/Main.vue`),
    meta: {
      requireAuth: true,
    },
    children: [
      {
        path: "/home",
        name: '首页',
        component: () => import(`@/views/user/Home.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/house-news",
        name: '房屋资讯',
        component: () => import(`@/views/user/HouseNews.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/my-house-order-info",
        name: '我的预约看房',
        component: () => import(`@/views/user/MyHouseOrderInfo.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/notice-list",
        name: '系统公告',
        component: () => import(`@/views/user/NoticeList.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/my-save",
        name: '我的收藏',
        component: () => import(`@/views/user/Save.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/my-rental-contract",
        name: '我的合同',
        component: () => import(`@/views/user/MyRentalContract.vue`),
        meta: { requireAuth: true },
      },
      {
        path: "/my-rental-bill",
        name: '我的账单',
        component: () => import(`@/views/user/MyRentalBill.vue`),
        meta: { requireAuth: true },
      },
    ]
  },
  {
    path: "/payment-result",
    name: 'paymentResult',
    component: () => import(`@/views/user/PaymentResult.vue`)
  }
];
const router = new VueRouter({
  routes,
  mode: 'history'
});
router.beforeEach((to, from, next) => {
  // 放行登录页和注册页
  if (to.path === '/login' || to.path === '/register') {
    return next();
  }

  // 检查需要认证的路由
  if (to.matched.some(record => record.meta.requireAuth)) {
    const token = getToken();

    // 未登录情况处理
    if (!token) {
      return next({
        path: '/login',
        query: { redirect: to.fullPath } // 记录目标路由
      });
    }

    // 已登录时的权限检查
    try {
      const role = parseInt(getRole());

      // 管理员路径检查
      if (to.matched[0].path === '/admin' && role !== 1) {
        clearToken();
        return next("/login"); //返回登录页
      }

      return next();
    } catch (error) {
      console.error('权限检查失败:', error);
      return next('/login');
    }
  }

  // 普通页面直接放行
  next();
});
export default router;
