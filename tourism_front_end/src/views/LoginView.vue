<script>
import {apiRear} from "@/axios";
import {jwtDecode} from "jwt-decode";

export default {
  name: 'LoginView',
  data() {
    const validatePass = (rule, value, callback) => {
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
      login: {
        password: '',
        username: '',
      },
      rules: {
        username: [{validator: validateUsername, trigger: 'blur'}],
        password: [{validator: validatePass, trigger: 'blur'}]
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
    if (sessionStorage.getItem('loginExpired')) {
      this.$message.warning('登录信息已过期，请重新登录');
      sessionStorage.removeItem('loginExpired');
    }
    await this.isTokenValid();
  },
  methods: {
    async submitForm() {
      if (this.isLoginInvalid) {
        return;
      }
      this.isLoading = true;
      try {
        apiRear.interceptors.response.use(
          response => {
            const authorizationHeader = response.headers['authorization'];
            if (authorizationHeader) {
              const token = authorizationHeader.replace('Bearer ', '');
              sessionStorage.setItem('token', token);
            }
            return response;
          },
          error => Promise.reject(error)
        );
        const response = await apiRear.post('login', this.login);
        if (response.data.code === 0) {
          this.$message.error('用户名或密码错误');
        } else {
          this.$message.success('登录成功');
          await this.redirectAfterLogin();
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
            this.$message.success('您已登录，即将为您跳转...');
            await this.delay(1200);
            await this.redirectAfterLogin();
          } else {
            console.log('Token is invalid');
          }
        } catch (error) {
          console.error('Error validating token:', error);
        }
      }
    },
    redirectAfterLogin() {
      const token = sessionStorage.getItem('token');
      if (!token) {
        return this.$router.push('/indexView');
      }
      try {
        const user = jwtDecode(token);
        if (user && user.authority === 3) {
          return this.$router.push('/indexView');
        }
      } catch (error) {
        // ignore decode failures and return to homepage
      }
      return this.$router.push('/home');
    },
    delay(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    }
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-aside">
        <el-image :src="url" class="auth-logo" fit="cover"></el-image>
        <h1>中山市城市文化旅游特色展示系统</h1>
        <p>欢迎回到中山文旅平台，登录后管理行程与文创订单。</p>
        <div class="auth-tags">
          <span>孙中山故里</span>
          <span>岐江夜景</span>
          <span>古镇灯饰</span>
        </div>
      </div>
      <div class="auth-form" @keyup.enter="submitForm">
        <h2>账号登录</h2>
        <p class="form-subtitle">游客可浏览商城，登录后解锁收藏、下单等功能。</p>
        <el-form :model="login" status-icon :rules="rules" ref="ruleForm" class="demo-ruleForm">
          <el-form-item label="用户名" prop="username" class="strong">
            <el-input type="text" v-model="login.username" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password" class="strong">
            <el-input type="password" v-model="login.password" autocomplete="off" show-password></el-input>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="submitForm"
              :disabled="isLoginInvalid || isLoading"
              style="width: 100%;"
              class="strong"
            >
              <span v-if="isLoading">
                <i class="el-icon-loading"></i> 登录中...
              </span>
              <span v-else>登录</span>
            </el-button>
          </el-form-item>
        </el-form>
        <div class="form-footer">
          <router-link class="hypertext" to="/register" active-class="active">注册</router-link>
          <el-button
            class="hypertext"
            style="padding: 0;background: none; border: none; font-size: 14px"
            @click="() => { this.$message.warning('请联系管理员') }"
          >忘记密码?</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 12% 10%, rgba(31, 122, 109, 0.15), transparent 45%),
    radial-gradient(circle at 80% 15%, rgba(199, 90, 49, 0.15), transparent 50%),
    linear-gradient(135deg, #f5f7f4 0%, #eef5f7 55%, #ffffff 100%);
  padding: 24px;
}

.auth-card {
  width: 920px;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 0;
  background: #ffffff;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(31, 56, 94, 0.18);
  animation: fadeInUp 0.6s ease both;
}

.auth-aside {
  background: linear-gradient(135deg, rgba(31, 122, 109, 0.95), rgba(199, 90, 49, 0.88));
  color: #ffffff;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  justify-content: center;
}

.auth-logo {
  width: 72px;
  height: 72px;
  border-radius: 16px;
}

.auth-aside h1 {
  font-size: 24px;
  margin: 0;
  line-height: 1.4;
}

.auth-aside p {
  margin: 0;
  opacity: 0.9;
  line-height: 1.6;
}

.auth-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.auth-tags span {
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
}

.auth-form {
  padding: 48px 40px;
}

.auth-form h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #1c2c27;
}

.form-subtitle {
  margin: 0 0 24px;
  color: #5f6b66;
  font-size: 14px;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.strong {
  font-weight: bold;
}

.hypertext {
  color: #5f6165;
  text-decoration: none;
  font-weight: bold;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 900px) {
  .auth-card {
    grid-template-columns: 1fr;
  }

  .auth-aside {
    padding: 36px 32px;
  }

  .auth-form {
    padding: 36px 32px;
  }
}
</style>
