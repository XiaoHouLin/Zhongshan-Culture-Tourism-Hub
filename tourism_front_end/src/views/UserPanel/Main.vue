<script>
import {jwtDecode} from "jwt-decode";

export default {
  name: 'UserPanel',
  data() {
    return {
      sessionStorage: sessionStorage,
      token: sessionStorage.getItem('token') || '',
      activeIndex: localStorage.getItem('activeIndex') || '1',
      circleUrl: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png",
      activeTextColor: '#f2d18f'
    }
  },
  methods: {
    handleOpen(key, keyPath) {
      console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      console.log(key, keyPath);
    },
    setActiveIndex(index) {
      this.activeIndex = index;
      localStorage.setItem('activeIndex', index);
    },
    handleLogout() {
      this.$confirm('确定要退出账号吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success('您已退出登录');
        sessionStorage.removeItem('token');
        localStorage.removeItem('shoppingCart');
        localStorage.setItem('activeIndex', '1');
        this.$router.push('/indexView');
      }).catch(() => {
        this.$message.info('已取消退出');
      });
    },
    decodeToken() {
      if (!this.token) {
        return null;
      }
      try {
        return jwtDecode(this.token);
      } catch (error) {
        return null;
      }
    }
  },
  watch: {
    $route(to) {
      const routeToMenu = {
        '/home': '1',
        '/userInfo': '1',
        '/userPanel/culturalProducts': '2',
        '/userManagement': '3',
        '/userPanel/culturalProducts/manage': '4',
        '/indexView': '5'
      };
      this.activeIndex = routeToMenu[to.path] || '1';
      localStorage.setItem('activeIndex', this.activeIndex);
    }
  },
  computed: {
    userPayload() {
      return this.decodeToken();
    },
    username() {
      return this.userPayload ? this.userPayload.username : '用户';
    },
    uid() {
      return this.userPayload ? this.userPayload.uid : '';
    },
    authority() {
      return this.userPayload ? this.userPayload.authority : null;
    },
    isCustomer() {
      return this.authority === 3;
    },
    showCustomerTopbar() {
      return this.isCustomer && !['/userInfo', '/billPayment', '/completeBill', '/CompleteBill'].includes(this.$route.path);
    }
  }
}
</script>

<template>
  <el-container class="panel-layout">
    <el-aside v-if="!isCustomer" class="panel-aside">
      <el-menu
        :default-active="activeIndex"
        class="el-menu-vertical-demo userPanel_container is-active"
        @open="handleOpen"
        @close="handleClose"
        background-color="#1f7a6d"
        text-color="#fff"
        :active-text-color="activeTextColor"
        style="border: 0;height: 100%"
      >
        <el-row class="demo-avatar demo-basic">
          <div class="align-container">
            <div class="center-content">
              <el-avatar :size="86" :src="circleUrl"></el-avatar>
              <h2>{{ username }}</h2>
              <template>
                <el-tag v-if="authority === 1" type="danger" effect="dark">管理员</el-tag>
                <el-tag v-else effect="dark">顾客</el-tag>
              </template>
            </div>
          </div>
        </el-row>
        <router-link to="/home" class="no-underline">
          <el-menu-item index="1" @click="setActiveIndex('1')">
            <i class="el-icon-menu icon-color"></i>
            <span slot="title" class="strong">首页</span>
          </el-menu-item>
        </router-link>
        <router-link to="/userPanel/culturalProducts" class="no-underline">
          <el-menu-item index="2" @click="setActiveIndex('2')">
            <i class="el-icon-shopping-cart-2 icon-color"></i>
            <span slot="title" class="strong">文创选购</span>
          </el-menu-item>
        </router-link>
        <div v-if="authority === 1">
          <router-link to="/userManagement" class="no-underline">
            <el-menu-item index="3" @click="setActiveIndex('3')">
              <i class="el-icon-user-solid icon-color"></i>
              <span slot="title" class="strong">用户管理</span>
            </el-menu-item>
          </router-link>
        </div>
        <div v-if="authority === 1">
          <router-link to="/userPanel/culturalProducts/manage" class="no-underline">
            <el-menu-item index="4" @click="setActiveIndex('4')">
              <i class="el-icon-notebook-1 icon-color"></i>
              <span slot="title" class="strong">文创管理</span>
            </el-menu-item>
          </router-link>
        </div>
        <el-menu-item index="5" @click="handleLogout">
          <i class="el-icon-right icon-color"></i>
          <span slot="title" class="strong">退出登录</span>
        </el-menu-item>
        <div class="uid-container">
          <h5>UID: {{ uid }}</h5>
        </div>
      </el-menu>
    </el-aside>

    <el-main class="panel-main">
      <div v-if="showCustomerTopbar" class="customer-topbar">
        <div>
          <h3 class="customer-title">欢迎回来，{{ username }}</h3>
          <p class="customer-subtitle">你可以在此查看订单与个人信息，或返回首页继续浏览。</p>
        </div>
        <el-button type="primary" icon="el-icon-back" @click="$router.push('/indexView')">返回首页</el-button>
      </div>
      <router-view/>
    </el-main>
  </el-container>
</template>

<style scoped>
.panel-layout {
  height: 100vh;
  --active-color: #f2d18f;
  background:
    radial-gradient(circle at 12% 10%, rgba(31, 122, 109, 0.12), transparent 45%),
    radial-gradient(circle at 85% 18%, rgba(199, 90, 49, 0.12), transparent 50%),
    linear-gradient(180deg, #f9f4ec 0%, #f5f9fb 55%, #ffffff 100%);
}

.panel-aside {
  width: 220px;
}

.panel-main {
  padding: 16px 20px;
  overflow: auto;
}

.customer-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 22px;
  margin-bottom: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(31, 122, 109, 0.12);
  box-shadow: 0 14px 30px rgba(25, 45, 40, 0.12);
}

.customer-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1c2c27;
}

.customer-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #5a6b65;
}

.userPanel_container {
  height: 100vh;
}

.no-underline {
  text-decoration: none;
  border-bottom: none;
}

.align-container {
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 24px 16px;
}

.center-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.center-content h2 {
  margin: 8px 0 4px;
  color: #fff;
  font-size: 16px;
}

.strong {
  font-weight: bold;
}

.icon-color {
  color: #fff;
}

.is-active .icon-color {
  color: var(--active-color);
}

.el-menu-vertical-demo {
  width: 220px;
}

.uid-container {
  position: absolute;
  bottom: 0;
  width: 100%;
  text-align: center;
  padding: 10px 0;
  color: rgba(255, 255, 255, 0.8);
}
</style>
