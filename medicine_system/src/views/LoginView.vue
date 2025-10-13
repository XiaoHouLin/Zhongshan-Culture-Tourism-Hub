<script>
import {apiRear} from "@/axios";

export default {
  name: 'LoginView',
  data() {
    var validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'));
      } else {
        callback();
      }
    };
    const validateUsername = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入用户名'));
      } else {
        callback();
      }
    };
    return {
      // backgroundImage: require('../../public/BackGround.png'),
      login: {
        password: '',
        username: '',
      },
      rules: {
        username: [{validator: validateUsername, trigger: 'blur'}],
        password: [
          {validator: validatePass, trigger: 'blur'}
        ]
      },
      url: '/zqxy.jpg',
      isLoading: false
    };
  },
  computed: {
    isLoginInvalid() {
      return !this.login.username || !this.login.password;
    }
  },
  async created() {
    await this.isTokenValid();
  },
  methods: {
    async submitForm() {
      if (this.isLoginInvalid) {
        return;
      }
      this.isLoading = true;
      try {
        // 响应拦截器
        apiRear.interceptors.response.use(
            response => {
              const authorizationHeader = response.headers['authorization'];
              if (authorizationHeader) {
                const token = authorizationHeader.replace('Bearer ', '');
                sessionStorage.setItem('token', token);
              }
              return response;
            },
            error => {
              return Promise.reject(error);
            }
        );
        const response = await apiRear.post('login', this.login);
        if (response.data.code === 0) {
          this.$message.error('用户名或密码错误');
        } else {
          this.$message.success('登录成功');
          // await this.$store.dispatch('setUser', {user: this.login.username});
          await this.$router.push('/home');
        }
      } catch (error) {
        this.$message.error('未知错误');
      } finally {
        this.isLoading = false;
      }
    },
    async isTokenValid() {
      const token = sessionStorage.getItem('token');
      if (token) {
        try {
          const response = await apiRear.post('/api/validate-token');
          if (response.data.code === 1) {
            this.$message.success('您已登录！即将为您跳转...');
            await this.delay(2000); // 延时 2 秒
            await this.$router.push('/home');
          } else {
            // Token 无效，处理相应逻辑
            console.log('Token is invalid');
          }
        } catch (error) {
          console.error('Error validating token:', error);
        }
      }
    },
    delay(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    }
  }
}
</script>

<template>
  <!--使用背景图片  <div :style="{'background-image': 'url(' + backgroundImage + ')'}">-->
  <div :style="{'background-color': '#c4dbfd'}">
    <div class="login_container">
      <el-card class="box-card">
        <div class="demo-image center">
          <div class="block" style="display: flex; justify-content: center; align-items: center;">
            <el-image style="width: 100px; height: 100px"
                      :src="url"
                      class="border-radius"
            ></el-image>
          </div>
        </div>
        <h1 style="display: flex; justify-content: center; align-content: center" class="manu_logo">
          药物信息管理系统</h1>
        <div class="text item" style="padding: 0" @keyup.enter="submitForm">
          <el-form :model="login" status-icon :rules="rules" ref="ruleForm"
                   class="demo-ruleForm">
            <el-form-item label="用户名" prop="username" class="strong">
              <el-input type="txt" v-model="login.username" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password" class="strong">
              <el-input type="password" v-model="login.password" autocomplete="off" show-password></el-input>
            </el-form-item>
            <br>
            <el-form-item>
              <el-button type="primary" @click="submitForm" :disabled="isLoginInvalid || isLoading" style="width: 100%;"
                         class="strong">
                 <span v-if="isLoading">
                    <i class="el-icon-loading"></i> 登录中...
                 </span>
                 <span v-else>登录</span>
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        <br>
        <div class="align-container">
          <router-link class="hypertext" to="/register" active-class="active">注册</router-link>
          <el-button class="hypertext"
                     style="padding: 0;background: none; border: none; color: #5f6165; font-size: 16px"
                     @click="()=>{this.$message.warning('请联系管理员！')}">忘记密码?</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style>
.login_container {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  height: 100vh; /* 确保容器高度占满视窗 */
}

.align-container {
  display: flex;
  justify-content: space-between; /* 让两端对齐 */
}

.text {
  font-size: 14px;
}

.item {
  padding: 18px 0;
}

.box-card {
  width: 480px;
}

.border-radius {
  border-radius: 10px;
}

.manu_logo {
  font-size: 26px;
  font-family: 'PingFang SC', sans-serif;
  color: #007BFF;
}

.strong {
  font-weight: bold;
}

.hypertext {
  color: #5f6165;
  text-decoration: none;
  font-weight: bold;
}
</style>