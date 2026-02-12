import Vue from 'vue'
import ElementUI from 'element-ui';
import { Notification } from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import VCharts from 'v-charts-v2'
import App from './App.vue'
import router from './router'
import store from "@/store";
import {apiRear} from "@/axios";
import {jwtDecode} from "jwt-decode";

Vue.config.productionTip = false

Vue.use(ElementUI);
Vue.use(VCharts)

const eventBus = new Vue();
Vue.prototype.$eventBus = eventBus;

apiRear.interceptors.request.use(config => {
  if (config && config.skipAuth) {
    return config;
  }
  const token = sessionStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => {
  return Promise.reject(error);
});

apiRear.interceptors.response.use(
  response => response,
  error => {
    const status = error && error.response ? error.response.status : null;
    if (status === 401 && !(error.config && error.config.skipAuthRedirect)) {
      const token = sessionStorage.getItem('token');
      if (token) {
        sessionStorage.removeItem('token');
        sessionStorage.setItem('loginExpired', '1');
        if (!router.currentRoute || router.currentRoute.path !== '/login') {
          router.replace('/login');
        }
      }
    }
    return Promise.reject(error);
  }
);

let realtimeSource = null;
let realtimeReconnectTimer = null;
let realtimeReconnectDelay = 2000;

const getToken = () => sessionStorage.getItem('token') || '';

const getUserPayload = () => {
  const token = getToken();
  if (!token) {
    return null;
  }
  try {
    return jwtDecode(token);
  } catch (e) {
    return null;
  }
};

const closeRealtime = () => {
  if (realtimeSource) {
    realtimeSource.close();
    realtimeSource = null;
  }
  if (realtimeReconnectTimer) {
    clearTimeout(realtimeReconnectTimer);
    realtimeReconnectTimer = null;
  }
};

const scheduleReconnect = () => {
  if (realtimeReconnectTimer) {
    return;
  }
  realtimeReconnectTimer = setTimeout(() => {
    realtimeReconnectTimer = null;
    realtimeReconnectDelay = Math.min(realtimeReconnectDelay * 1.5, 20000);
    connectRealtime();
  }, realtimeReconnectDelay);
};

const openAdminOrderFromNotification = (payload = {}, options = {}) => {
  sessionStorage.setItem('openOrders', '1');
  if (options.filter) {
    sessionStorage.setItem('openOrderFilter', options.filter);
  }
  if (options.status !== undefined && options.status !== null) {
    sessionStorage.setItem('openOrderStatus', String(options.status));
  }
  if (payload && payload.orderId) {
    sessionStorage.setItem('openOrderDetailId', String(payload.orderId));
  }
  const currentPath = router.currentRoute && router.currentRoute.path ? router.currentRoute.path : '';
  if (currentPath === '/userInfo') {
    eventBus.$emit('realtime:open-orders', {
      filter: options.filter,
      status: options.status,
      orderId: payload.orderId
    });
    return;
  }
  router.push('/userInfo');
};

const handleRealtimeEvent = (type, payload) => {
  if (type === 'ADMIN_PENDING_ORDER') {
    const user = getUserPayload();
    if (user && Number(user.authority) === 1) {
      const notify = Notification({
        title: '提示',
        message: '您收到一个待处理的订单',
        type: 'info',
        duration: 10000,
        position: 'bottom-right',
        onClick: () => {
          openAdminOrderFromNotification(payload, { filter: 'pending', status: 1 });
          if (notify) {
            notify.close();
          }
        }
      });
    }
  }
  if (type === 'ADMIN_REFUND_REQUEST') {
    const user = getUserPayload();
    if (user && Number(user.authority) === 1) {
      const notify = Notification({
        title: '提示',
        message: '您收到一个退款申请',
        type: 'info',
        duration: 10000,
        position: 'bottom-right',
        onClick: () => {
          openAdminOrderFromNotification(payload, { filter: 'all', status: 5 });
          if (notify) {
            notify.close();
          }
        }
      });
    }
  }
  if (type === 'ORDER_UPDATED') {
    eventBus.$emit('realtime:order-updated', payload || {});
  }
  if (type === 'GLOBAL_REFRESH') {
    eventBus.$emit('realtime:global-refresh', payload || {});
  }
};

const connectRealtime = () => {
  const token = getToken();
  if (!token) {
    closeRealtime();
    return;
  }
  const baseUrl = apiRear && apiRear.defaults ? apiRear.defaults.baseURL : '';
  if (!baseUrl) {
    return;
  }
  closeRealtime();
  realtimeSource = new EventSource(`${baseUrl}/events/stream?token=${encodeURIComponent(token)}`);
  realtimeSource.addEventListener('ORDER_UPDATED', event => {
    try {
      handleRealtimeEvent('ORDER_UPDATED', JSON.parse(event.data || '{}'));
    } catch (e) {
      handleRealtimeEvent('ORDER_UPDATED', {});
    }
  });
  realtimeSource.addEventListener('ADMIN_PENDING_ORDER', event => {
    try {
      handleRealtimeEvent('ADMIN_PENDING_ORDER', JSON.parse(event.data || '{}'));
    } catch (e) {
      handleRealtimeEvent('ADMIN_PENDING_ORDER', {});
    }
  });
  realtimeSource.addEventListener('ADMIN_REFUND_REQUEST', event => {
    try {
      handleRealtimeEvent('ADMIN_REFUND_REQUEST', JSON.parse(event.data || '{}'));
    } catch (e) {
      handleRealtimeEvent('ADMIN_REFUND_REQUEST', {});
    }
  });
  realtimeSource.addEventListener('GLOBAL_REFRESH', event => {
    try {
      handleRealtimeEvent('GLOBAL_REFRESH', JSON.parse(event.data || '{}'));
    } catch (e) {
      handleRealtimeEvent('GLOBAL_REFRESH', {});
    }
  });
  realtimeSource.onerror = () => {
    closeRealtime();
    scheduleReconnect();
  };
  realtimeReconnectDelay = 2000;
};

store.subscribe((mutation, state) => {
  if (mutation.type === 'setToken') {
    if (state.token) {
      connectRealtime();
    } else {
      closeRealtime();
    }
  }
});

connectRealtime();


new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
