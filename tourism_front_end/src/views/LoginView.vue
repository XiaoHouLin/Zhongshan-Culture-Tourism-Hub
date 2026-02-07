<script>
import {apiRear} from "@/axios";
import {jwtDecode} from "jwt-decode";
import SliderCaptcha from "@/components/SliderCaptcha.vue";

export default {
  name: 'LoginView',
  components: { SliderCaptcha },
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
      captchaToken: '',
      captchaPassed: false,
      needCaptcha: false,
      captchaKey: 0,
      captchaLoading: false,
      url: '/zqxy.jpg',
      isLoading: false
    };
  },
  watch: {
    'login.username'() {
      this.needCaptcha = false;
      this.resetCaptchaState();
    }
  },
  computed: {
    isLoginInvalid() {
      return !this.login.username || !this.login.password;
    }
  },
  async created() {
    if (sessionStorage.getItem('loginExpired')) {
      this.$message.warning('登录信息过期，请重新登录');
      sessionStorage.removeItem('loginExpired');
    }
    await this.isTokenValid();
  },
  methods: {
    async submitForm() {
      if (this.isLoginInvalid) {
        return;
      }
      if (this.needCaptcha && !this.captchaToken) {
        await this.fetchCaptcha();
        return;
      }
      if (this.needCaptcha && !this.captchaPassed) {
        this.$message.warning('请先完成滑块验证');
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
        const payload = {
          ...this.login,
          captchaToken: this.needCaptcha ? this.captchaToken : '',
          captchaInput: this.needCaptcha ? 'pass' : ''
        };
        const response = await apiRear.post('login', payload);
        if (response.data.code === 0) {
          const needCaptcha = response.data && response.data.data && response.data.data.needCaptcha;
          const message = response.data.message || '用户名或密码错误';
          this.resetCaptchaState();
          if (message.includes('人机验证')) {
            this.needCaptcha = true;
            await this.fetchCaptcha();
            return;
          }
          if (needCaptcha) {
            this.needCaptcha = true;
            await this.fetchCaptcha();
          }
          this.$message.error(message);
        } else {
          this.$message.success('登录成功');
          this.needCaptcha = false;
          this.resetCaptchaState();
          await this.redirectAfterLogin();
        }
      } catch (error) {
        this.$message.error('未知错误');
      } finally {
        this.isLoading = false;
      }
    },
    async fetchCaptcha() {
      if (!this.login.username) {
        this.$message.warning('请输入用户名');
        return;
      }
      this.captchaLoading = true;
      try {
        const res = await apiRear.post('/login/captcha', { username: this.login.username });
        if (res.data.code !== 1) {
          this.$message.error(res.data.message || '暂时无法获取人机验证');
          return;
        }
        const data = res.data.data || {};
        this.captchaToken = data.token || '';
        this.captchaPassed = false;
        this.captchaKey += 1;
      } catch (error) {
        this.$message.error('获取人机验证失败，请稍后重试');
      } finally {
        this.captchaLoading = false;
      }
    },
    resetCaptchaState() {
      this.captchaToken = '';
      this.captchaPassed = false;
      this.captchaLoading = false;
      this.captchaKey += 1;
    },
    async handleCaptchaSuccess() {
      this.captchaPassed = true;
      this.$message.success('验证通过，请点击登录');
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
    <button class="back-button" @click="$router.push('/indexView')">
      <i class="el-icon-arrow-left"></i>
    </button>
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
        <div v-if="needCaptcha" class="inline-captcha">
          <slider-captcha :key="captchaKey" :disabled="captchaLoading" @success="handleCaptchaSuccess" />
          <p class="captcha-tip">多次登录失败后需要完成滑块验证。</p>
        </div>
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
            @click="$router.push('/forgot-password')"
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
  width: 1000px;
  max-width: 100%;
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

.back-button {
  position: fixed;
  top: 18px;
  left: 18px;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: none;
  background: #1f7a6d;
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(31, 56, 94, 0.2);
  z-index: 2;
}

.back-button:hover {
  background: #17675d;
}

.inline-captcha {
  margin: 12px 0 6px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.captcha-tip {
  margin: 0;
  color: #5f6b66;
  font-size: 12px;
  text-align: center;
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
