import Vue from 'vue'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import VCharts from 'v-charts-v2'
import App from './App.vue'
import router from './router'
import store from "@/store";
import {apiRear} from "@/axios";

Vue.config.productionTip = false

Vue.use(ElementUI);
Vue.use(VCharts)

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
      if (sessionStorage.getItem('token')) {
        sessionStorage.removeItem('token');
      }
      sessionStorage.setItem('loginExpired', '1');
      if (!router.currentRoute || router.currentRoute.path !== '/login') {
        router.replace('/login');
      }
    }
    return Promise.reject(error);
  }
);


new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
