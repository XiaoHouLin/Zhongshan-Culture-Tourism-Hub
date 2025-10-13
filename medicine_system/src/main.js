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
  const token = sessionStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => {
  return Promise.reject(error);
});


new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
