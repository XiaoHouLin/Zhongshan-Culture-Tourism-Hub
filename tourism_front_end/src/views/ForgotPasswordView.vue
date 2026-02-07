<script>
import {apiRear} from "@/axios";
import SliderCaptcha from "@/components/SliderCaptcha.vue";

export default {
  name: 'ForgotPasswordView',
  components: { SliderCaptcha },
  data() {
    return {
      step: 'identify',
      identifier: '',
      lookupMode: '',
      maskedEmail: '',
      email: '',
      emailForm: {
        email: ''
      },
      emailRules: {},
      emailMismatch: false,
      emailChecking: false,
      resetForm: {
        code: '',
        newPassword: '',
        confirmPassword: ''
      },
      resetRules: {},
      isLoading: false,
      codeSending: false,
      codeCountdown: 0,
      codeTimer: null,
      verifyLoading: false,
      captchaRequired: false,
      captchaPassed: false,
      captchaKey: 0,
      emailDialogVisible: false,
      url: '/zqxy.jpg'
    };
  },
  computed: {
    passwordValid() {
      return /^(?=.*[A-Za-z])(?=.*\d).{8,20}$/.test(this.resetForm.newPassword);
    },
    canReset() {
      return this.passwordValid
        && this.resetForm.newPassword === this.resetForm.confirmPassword;
    }
  },
  created() {
    this.emailRules = {
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        {
          type: 'email',
          message: '请输入正确的邮箱地址',
          trigger: ['blur', 'change']
        },
        { validator: this.validateEmailMatch, trigger: ['blur', 'change'] }
      ]
    };
    this.resetRules = {
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { validator: this.validateNewPassword, trigger: ['blur', 'change'] }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        { validator: this.validateConfirmPassword, trigger: ['blur', 'change'] }
      ]
    };
  },
  methods: {
    validateNewPassword(rule, value, callback) {
      if (!value) {
        callback(new Error('请输入新密码'));
        return;
      }
      if (value.length < 8 || value.length > 20) {
        callback(new Error('密码长度需为8-20位'));
        return;
      }
      if (!/^(?=.*[A-Za-z])(?=.*\d).{8,20}$/.test(value)) {
        callback(new Error('密码需包含字母和数字'));
        return;
      }
      callback();
    },
    validateConfirmPassword(rule, value, callback) {
      if (!value) {
        callback(new Error('请再次输入新密码'));
        return;
      }
      if (value !== this.resetForm.newPassword) {
        callback(new Error('两次输入的密码不一致！'));
        return;
      }
      callback();
    },
    validateEmailMatch(rule, value, callback) {
      if (this.emailMismatch) {
        callback(new Error('邮箱信息不匹配'));
      } else {
        callback();
      }
    },
    async lookupAccount() {
      if (!this.identifier) {
        this.$message.warning('请输入用户名或邮箱');
        return;
      }
      this.isLoading = true;
      try {
        const res = await apiRear.post('/password-reset/lookup', {identifier: this.identifier}, {
          skipAuth: true,
          skipAuthRedirect: true
        });
        if (res.data.code !== 1) {
          this.$message.error(res.data.message || '用户不存在');
          return;
        }
        const data = res.data.data || {};
        this.lookupMode = data.mode;
        this.maskedEmail = data.maskedEmail || '';
        if (this.lookupMode === 'email') {
          this.email = this.identifier;
          this.step = 'verify';
          this.resetForm.code = '';
          this.resetForm.newPassword = '';
          this.resetForm.confirmPassword = '';
        } else {
          this.emailForm.email = '';
          this.emailMismatch = false;
          this.emailDialogVisible = true;
        }
      } catch (error) {
        this.$message.error('查询失败，请稍后再试');
      } finally {
        this.isLoading = false;
      }
    },
    async confirmEmail() {
      this.emailMismatch = false;
      const valid = await new Promise(resolve => {
        this.$refs.emailForm.validate(resolve);
      });
      if (!valid) {
        return;
      }
      this.emailChecking = true;
      try {
        const res = await apiRear.post('/password-reset/verify-email', {
          username: this.identifier,
          email: this.emailForm.email
        }, {
          skipAuth: true,
          skipAuthRedirect: true
        });
        if (res.data.code === 1) {
          this.email = this.emailForm.email;
          this.emailDialogVisible = false;
          this.step = 'verify';
        } else {
          this.emailMismatch = true;
          this.$refs.emailForm.validateField('email');
          this.$message.error(res.data.message || '用户信息错误');
        }
      } catch (error) {
        this.$message.error('用户信息错误');
      } finally {
        this.emailChecking = false;
      }
    },
    requestSendCode() {
      if (this.codeCountdown > 0 || this.codeSending) {
        return;
      }
      if (!this.captchaRequired || !this.captchaPassed) {
        this.captchaRequired = true;
        this.captchaPassed = false;
        this.captchaKey += 1;
        return;
      }
      this.sendResetCode();
    },
    async sendResetCode() {
      this.codeSending = true;
      try {
        const payload = this.lookupMode === 'username'
          ? {username: this.identifier, email: this.email}
          : {email: this.email};
        const res = await apiRear.post('/password-reset/send-code', payload, {
          skipAuth: true,
          skipAuthRedirect: true
        });
        if (res.data.code === 1) {
          this.$message.success('验证码已发送，请查收邮箱');
          this.startCodeCountdown();
          this.resetForm.code = '';
        } else {
          if (res.data.message && res.data.message.includes('邮箱信息不匹配')) {
            this.emailMismatch = true;
            this.emailForm.email = this.email;
            this.emailDialogVisible = true;
            this.$nextTick(() => this.$refs.emailForm && this.$refs.emailForm.validateField('email'));
          }
          this.$message.error(res.data.message || '验证码发送失败');
        }
      } catch (error) {
        this.$message.error('验证码发送失败，请稍后再试');
      } finally {
        this.codeSending = false;
        this.captchaRequired = false;
        this.captchaPassed = false;
        this.captchaKey += 1;
      }
    },
    async verifyCode() {
      if (!/^\d{6}$/.test(this.resetForm.code)) {
        this.$alert('请输入6位数字验证码', '提示', { type: 'error' });
        return;
      }
      this.verifyLoading = true;
      try {
        const res = await apiRear.post('/password-reset/verify-code', {
          email: this.email,
          code: this.resetForm.code
        }, {
          skipAuth: true,
          skipAuthRedirect: true
        });
        if (res.data.code === 1) {
          this.step = 'reset';
        } else {
          this.$alert(res.data.message || '验证码校验失败', '提示', { type: 'error' });
        }
      } catch (error) {
        this.$alert('验证码校验失败', '提示', { type: 'error' });
      } finally {
        this.verifyLoading = false;
      }
    },
    async resetPassword() {
      const valid = await new Promise(resolve => {
        this.$refs.resetForm.validate(resolve);
      });
      if (!valid) {
        return;
      }
      this.isLoading = true;
      try {
        const res = await apiRear.post('/password-reset/confirm', {
          email: this.email,
          code: this.resetForm.code,
          newPassword: this.resetForm.newPassword
        }, {
          skipAuth: true,
          skipAuthRedirect: true
        });
        if (res.data.code === 1) {
          this.$message.success('密码重置成功，请重新登录');
          await this.$router.push('/login');
        } else {
          this.$alert(res.data.message || '重置失败', '提示', { type: 'error' });
        }
      } catch (error) {
        this.$alert('重置失败，请稍后再试', '提示', { type: 'error' });
      } finally {
        this.isLoading = false;
      }
    },
    handleCaptchaSuccess() {
      this.captchaPassed = true;
      this.$message.success('验证通过，请点击发送验证码');
    },
    startCodeCountdown() {
      this.codeCountdown = 60;
      if (this.codeTimer) {
        clearInterval(this.codeTimer);
      }
      this.codeTimer = setInterval(() => {
        if (this.codeCountdown <= 1) {
          this.codeCountdown = 0;
          clearInterval(this.codeTimer);
          this.codeTimer = null;
        } else {
          this.codeCountdown -= 1;
        }
      }, 1000);
    },
    handleCodeInput() {
      if (!this.resetForm.code) {
        return;
      }
      this.resetForm.code = this.resetForm.code.toString().replace(/\D/g, '').slice(0, 6);
    }
  },
  beforeDestroy() {
    if (this.codeTimer) {
      clearInterval(this.codeTimer);
      this.codeTimer = null;
    }
  }
}
</script>

<template>
  <div class="auth-page">
    <button class="back-button" @click="$router.push('/login')">
      <i class="el-icon-arrow-left"></i>
    </button>
    <div class="auth-card">
      <div class="auth-aside">
        <el-image :src="url" class="auth-logo" fit="cover"></el-image>
        <h1>中山市城市文化旅游特色展示系统</h1>
        <p>通过邮箱验证快速找回账号密码。</p>
        <div class="auth-tags">
          <span>安全验证</span>
          <span>密码重置</span>
          <span>账号保护</span>
        </div>
      </div>
      <div class="auth-form">
        <h2>找回密码</h2>
        <p class="form-subtitle">请输入用户名或邮箱，验证后重置密码。</p>

        <div v-if="step === 'identify'">
          <el-form label-position="top" class="demo-ruleForm">
            <el-form-item label="用户名或邮箱" class="strong">
              <el-input v-model="identifier" placeholder="请输入用户名或邮箱"></el-input>
            </el-form-item>
            <el-button type="primary" style="width: 100%;" class="strong"
                       :loading="isLoading" @click="lookupAccount">
              下一步
            </el-button>
          </el-form>
        </div>

        <div v-else-if="step === 'verify'">
          <div class="info-card">
            <div class="info-title">已绑定邮箱</div>
            <div class="info-value">{{ maskedEmail || email }}</div>
          </div>
          <el-form label-position="top" class="demo-ruleForm">
            <el-form-item label="邮箱验证码" class="strong">
              <el-input v-model="resetForm.code" placeholder="请输入6位验证码" maxlength="6"
                        @input="handleCodeInput"></el-input>
            </el-form-item>
            <div v-if="captchaRequired" class="inline-captcha">
              <slider-captcha :key="captchaKey" @success="handleCaptchaSuccess" />
              <p class="captcha-tip">拖动滑块完成验证后将发送验证码。</p>
            </div>
            <div class="verify-actions">
              <el-button class="strong"
                         :disabled="codeCountdown > 0 || codeSending"
                         :loading="codeSending"
                         @click="requestSendCode">
                {{ codeCountdown > 0 ? `重新发送(${codeCountdown}s)` : '发送验证码' }}
              </el-button>
              <el-button type="primary" class="strong"
                         :loading="verifyLoading"
                         @click="verifyCode">
                验证验证码
              </el-button>
            </div>
          </el-form>
        </div>

        <div v-else-if="step === 'reset'">
          <el-form ref="resetForm" :model="resetForm" :rules="resetRules" label-position="top" class="demo-ruleForm">
            <el-form-item label="新密码" prop="newPassword" class="strong">
              <el-input type="password" v-model="resetForm.newPassword" show-password placeholder="8-20位，字母+数字"></el-input>
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword" class="strong">
              <el-input type="password" v-model="resetForm.confirmPassword" show-password></el-input>
            </el-form-item>
            <el-button type="primary" style="width: 100%;" class="strong"
                       :disabled="!canReset || isLoading"
                       :loading="isLoading"
                       @click="resetPassword">
              重置密码
            </el-button>
          </el-form>
        </div>
      </div>
    </div>

    <el-dialog
      title="邮箱校验"
      :visible.sync="emailDialogVisible"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form ref="emailForm" :model="emailForm" :rules="emailRules" label-position="top">
        <div class="captcha-body">
          <div class="info-title">该账号绑定邮箱</div>
          <div class="info-value">{{ maskedEmail }}</div>
          <el-form-item label="请补全邮箱" prop="email">
            <el-input v-model="emailForm.email" placeholder="请输入完整邮箱"
                      @input="emailMismatch = false"></el-input>
          </el-form-item>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="emailDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="emailChecking" @click="confirmEmail">继续</el-button>
      </div>
    </el-dialog>

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

.info-card {
  background: #f0f6f4;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
}

.info-title {
  font-size: 12px;
  color: #6a756f;
}

.info-value {
  font-size: 16px;
  font-weight: 700;
  color: #1f2b2a;
  margin-top: 6px;
}

.verify-actions {
  display: flex;
  gap: 12px;
}

.verify-actions .el-button {
  flex: 1;
}

.demo-ruleForm .el-form-item {
  margin-bottom: 14px;
}

.demo-ruleForm .el-form-item__label {
  padding: 0 0 6px;
  line-height: 1.2;
  color: #3b4a45;
  font-weight: 600;
}

.strong {
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

.captcha-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.inline-captcha {
  margin: 10px 0 6px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.captcha-tip {
  color: #5f6b66;
  margin: 0;
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
