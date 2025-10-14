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
      activeTextColor: '#ffd04b',
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
    handleLogout(){
      this.$confirm('确定要退出账号吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success('您已退出登录!');
        // 清除 sessionStorage 中的 token
        sessionStorage.removeItem('token');
        // 清除 localStorage 中的 activeIndex
        localStorage.removeItem('shoppingCart');
        localStorage.setItem('activeIndex', '1');
        // 跳转到首页
        this.$router.push('/indexView');
      }).catch(() => {
        this.$message.info('已取消退出');
      });
    }
  },
  watch: {
    $route(to) {
      // 根据路由变化设置当前激活的菜单项
      const routeToMenu = {
        '/home': '1',
        '/culturalProducts': '2',
        '/userInfo': '3',
        '/userManagement': '4',
        '/culturalProducts/manage': '5',
        '/indexView': '6'
      };
      this.activeIndex = routeToMenu[to.path] || '1';
      localStorage.setItem('activeIndex', this.activeIndex);
    }
  },
  computed: {
    username() {
      return jwtDecode(this.token).username;
    },
    uid() {
      return jwtDecode(this.token).uid;
    },
    authority(){
      return jwtDecode(this.token).authority;
    }
  }
}
</script>

<template>
  <el-container style="height: 100vh;background-color: #f2f3f6">
    <el-aside style="width: 200px">
      <el-menu
          :default-active="activeIndex"
          class="el-menu-vertical-demo userPanel_container is-active"
          @open="handleOpen"
          @close="handleClose"
          background-color="#5389F5FF"
          text-color="#fff"
          :active-text-color="activeTextColor" style="border: 0;height: 100%"
      >
        <el-row class="demo-avatar demo-basic">
          <div class="align-container" style="padding: 30px">
            <div class="center-content">
              <el-avatar :size="100" :src="circleUrl"></el-avatar>
              <h1 style="color: white">{{ username }}</h1>
              <template>
                <el-tag v-if="authority === 1" type="danger" effect="dark">管理员</el-tag>
                <el-tag v-else-if="authority === 2" type="success" effect="dark">旅行顾问</el-tag>
                <el-tag v-else-if="authority === 3" effect="dark">顾客</el-tag>
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
        <router-link to="/culturalProducts" class="no-underline">
          <el-menu-item index="2" @click="setActiveIndex('2')">
            <i class="el-icon-shopping-cart-2 icon-color"></i>
            <span slot="title" class="strong">文创选购</span>
          </el-menu-item>
        </router-link>
        <router-link to="/userInfo" class="no-underline">
          <el-menu-item index="3" @click="setActiveIndex('3')">
            <i class="el-icon-s-custom icon-color"></i>
            <span slot="title" class="strong">个人信息</span>
          </el-menu-item>
        </router-link>
        <div v-if="authority === 1">
          <router-link to="/userManagement" class="no-underline">
            <el-menu-item index="4" @click="setActiveIndex('4')">
              <i class="el-icon-user-solid icon-color"></i>
              <span slot="title" class="strong">用户管理</span>
            </el-menu-item>
          </router-link>
        </div>
        <div v-if="authority === 1 || authority === 2">
          <router-link to="/culturalProducts/manage" class="no-underline">
            <el-menu-item index="5" @click="setActiveIndex('5')">
              <i class="el-icon-notebook-1 icon-color"></i>
              <span slot="title" class="strong">文创管理</span>
            </el-menu-item>
          </router-link>
        </div>
          <el-menu-item index="6" @click="handleLogout">
            <i class="el-icon-right icon-color"></i>
            <span slot="title" class="strong">退出</span>
          </el-menu-item>
        <div class="uid-container">
          <h5 style="color: white;margin: 0">UID: {{ uid }}</h5>
        </div>
      </el-menu>
    </el-aside>

    <el-main style="padding: 10px">
        <router-view/>
    </el-main>
  </el-container>
</template>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh; /* 设置容器高度为 100% */
  --active-color: v-bind(activeColor);
}

.userPanel_container {
  height: 100vh; /* 确保容器高度占满视窗 */
}

.no-underline {
  text-decoration: none; /* 移除下划线 */
  border-bottom: none; /* 移除底部横线 */
}

.align-container { /* 垂直居中 */
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.center-content { /* 水平居中 */
  display: flex;
  flex-direction: column;
  align-items: center;
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
  width: 200px;
}

.uid-container {
  position: absolute;
  bottom: 0;
  width: 100%;
  text-align: center;
  padding: 10px 0;
}
</style>