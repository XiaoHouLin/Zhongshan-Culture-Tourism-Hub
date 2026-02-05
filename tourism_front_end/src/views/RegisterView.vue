<script>
import {regionData, codeToText} from 'element-china-area-data'
import {apiRear} from "@/axios";

export default {
  name: 'RegisterView',
  data() {
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'));
      } else if (value.length < 8 || value.length > 20) {
        callback(new Error('密码长度需为8-20位'));
      } else if (!/^(?=.*[A-Za-z])(?=.*\d).{8,20}$/.test(value)) {
        callback(new Error('密码需包含字母和数字'));
      } else {
        if (this.register.checkPass !== '') {
          this.$refs.ruleForm.validateField('checkPass');
        }
        callback();
      }
    };
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.register.pass) {
        callback(new Error('两次输入的密码不一致！'));
      } else {
        callback();
      }
    };
    const validateUsername = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入用户名'));
      } else if (value.length < 5 || value.length > 20) {
        callback(new Error('用户名长度需为5-20位'));
      } else {
        callback();
      }
    };
    const validateUsernameUnique = async (rule, value, callback) => {
      if (!value || value.length < 5 || value.length > 20) {
        this.usernameUnique = false;
        callback();
        return;
      }
      try {
        const response = await apiRear.post(`/isUnique/${encodeURIComponent(value)}`);
        this.usernameUnique = response.data.code === 1;
        if (this.usernameUnique) {
          callback();
        } else {
          callback(new Error('用户名已存在'));
        }
      } catch (error) {
        this.usernameUnique = false;
        callback(new Error('无法验证用户名，请稍后再试'));
      }
    };
    const validateTelephone = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入电话'));
      } else if (!/^\d{11}$/.test(value)) {
        callback(new Error('请输入11位手机号码'));
      } else {
        callback();
      }
    };
    return {
      // backgroundImage: require('../../public/BackGround.png'),
      options: regionData,
      isLoading: false,
      register: {
        username: '',
        pass: '',
        checkPass: '',
        personalName: '',
        sex: 'U',
        email: '',
        emailCode: '',
        age: '',
        chinaArea: [],
        chinaAreaNum: [],
        address: '',
        telephone: ''
      },
      info: {
        username: '',
        password: '',
        cname: '',
        csex: '',
        email: '',
        emailCode: '',
        cage: '',
        caddress: '',
        cphone: ''
      },
      codeSending: false,
      codeCountdown: 0,
      codeTimer: null,
      usernameUnique: true,
      captchaDialogVisible: false,
      captchaQuestion: '',
      captchaAnswer: 0,
      captchaInput: '',
      captchaError: '',
      rules: {
        username: [
          {required: true, validator: validateUsername, trigger: 'blur'},
          {validator: validateUsernameUnique, trigger: 'blur'}
        ],
        pass: [{required: true, validator: validatePass, trigger: 'blur'}],
        checkPass: [{required: true, validator: validatePass2, trigger: 'blur'}],
        telephone: [{required: true, message: '请输入电话', validator: validateTelephone, trigger: 'blur'}],
        email: [
          {required: true, message: '请输入邮箱地址', trigger: 'blur'},
          {
            type: 'email',
            message: '请输入正确的邮箱地址',
            trigger: ['blur', 'change']
          }
        ],
        emailCode: [
          {required: true, message: '请输入邮箱验证码', trigger: ['blur', 'change']},
          {pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: ['blur', 'change']}
        ]
      },
      url: '/zqxy.jpg',
      sex: null,
    };
  },
  computed: {
    isLoginInvalid() {
      const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      const usernameValid = this.register.username.length >= 5 && this.register.username.length <= 20;
      const passwordValid = /^(?=.*[A-Za-z])(?=.*\d).{8,20}$/.test(this.register.pass);
      const phoneValid = /^\d{11}$/.test(this.register.telephone);
      const ageValid =
        this.register.age === '' ||
        this.register.age === null ||
        (Number.isInteger(this.register.age) && this.register.age >= 0);
      const emailValid = emailPattern.test(this.register.email);
      const codeValid = /^\d{6}$/.test(this.register.emailCode);
      return !this.register.username || !usernameValid || !passwordValid || !this.usernameUnique
          || this.register.pass !== this.register.checkPass
          || !this.register.telephone
          || !phoneValid
          || !ageValid
          || !emailValid
          || !codeValid;
    }
  },
  methods: {
    handlePhoneInput() {
      if (!this.register.telephone) {
        return;
      }
      const digits = this.register.telephone.toString().replace(/\D/g, '').slice(0, 11);
      this.register.telephone = digits;
    },
    handleEmailCodeInput() {
      if (!this.register.emailCode) {
        return;
      }
      const digits = this.register.emailCode.toString().replace(/\D/g, '').slice(0, 6);
      this.register.emailCode = digits;
    },
    async submitForm() {
      const valid = await new Promise(resolve => {
        this.$refs.ruleForm.validate(resolve);
      });
      if (!valid || this.isLoginInvalid) {
        return;
      }
      this.isLoading = true;
      this.info.username = this.register.username;
      this.info.password = this.register.pass;
      this.info.cname = this.register.personalName || '';
      this.info.csex = this.register.sex || 'U';
      this.info.email = this.register.email;
      this.info.emailCode = this.register.emailCode;
      this.info.cage = Number.isInteger(this.register.age) ? this.register.age : 0;
      const addressParts = [];
      if (this.register.chinaArea) {
        addressParts.push(this.register.chinaArea);
      }
      if (this.register.address) {
        addressParts.push(this.register.address);
      }
      this.info.caddress = addressParts.join('');
      this.info.cphone = this.register.telephone;
      try {
        const registerRes = await apiRear.post('/register', this.info);
        if (registerRes.data.code === 1) {
          this.$message.success('注册成功');
          /*
          addMessage({username: this.register.username}, {
            id: `register-${this.register.username}`,
            title: '注册成功',
            content: `欢迎加入中山文旅系统，${this.register.username}。`,
            time: formatMessageTime(),
            read: false,
            action: '/indexView'
          });
          */
          await this.$router.push('/login');
        } else {
          this.$message.error(registerRes.data.message || '注册失败');
        }
      } catch (error) {
        this.$message.error('未知错误');
      } finally {
        this.isLoading = false;
      }
    },
    async requestEmailCode() {
      const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (!this.register.email || !emailPattern.test(this.register.email)) {
        this.$message.error('请输入正确的邮箱地址');
        return;
      }
      if (!this.register.username) {
        this.$message.warning('请先填写用户名');
        return;
      }
      if (this.register.username.length < 5 || this.register.username.length > 20) {
        this.$message.warning('用户名长度需为5-20位');
        return;
      }
      if (this.codeCountdown > 0 || this.codeSending) {
        return;
      }
      this.openCaptchaDialog();
    },
    async sendEmailCode() {
      this.codeSending = true;
      try {
        const res = await apiRear.post('/register/email-code', {
          email: this.register.email,
          username: this.register.username
        });
        if (res.data.code === 1) {
          this.$message.success('验证码已发送，请查收邮箱');
          this.startCodeCountdown();
        } else {
          this.$message.error(res.data.message || '验证码发送失败');
        }
      } catch (error) {
        this.$message.error('验证码发送失败，请稍后再试');
      } finally {
        this.codeSending = false;
      }
    },
    openCaptchaDialog() {
      this.captchaInput = '';
      this.captchaError = '';
      this.generateCaptcha();
      this.captchaDialogVisible = true;
    },
    generateCaptcha() {
      const left = Math.floor(Math.random() * 9) + 1;
      const right = Math.floor(Math.random() * 9) + 1;
      this.captchaQuestion = `${left} + ${right} = ?`;
      this.captchaAnswer = left + right;
    },
    confirmCaptcha() {
      if (Number(this.captchaInput) !== this.captchaAnswer) {
        this.captchaError = '验证失败，请重试';
        this.generateCaptcha();
        return;
      }
      this.captchaDialogVisible = false;
      this.sendEmailCode();
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
    handleChangeAndUpdateArea(value) {
      this.handleChange(value);
      this.updateArea('ruleForm');
    },
    handleChange(value) {
      this.register.chinaArea = (codeToText[value[0]] + codeToText[value[1]] + codeToText[value[2]]);
    },
    updateArea(formName) {
      this.$refs[formName].validateField("chinaArea", () => {
      });
    },
    updateSex(formName) {
      this.$refs[formName].validateField("sex", () => {});
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
    <button class="back-button" @click="$router.push('/indexView')">
      <i class="el-icon-arrow-left"></i>
    </button>
    <div class="auth-card">
      <div class="auth-aside">
        <el-image :src="url" class="auth-logo" fit="cover"></el-image>
        <h1>中山市城市文化旅游特色展示系统</h1>
        <p>注册后可收藏景点、管理订单与文创清单。</p>
        <div class="auth-tags">
          <span>岭南水乡</span>
          <span>文创商城</span>
          <span>慢游路线</span>
        </div>
      </div>
      <div class="auth-form">
        <h2>账号注册</h2>
        <p class="form-subtitle">填写完整信息，开启你的中山城市文化旅程。</p>
        <el-form
          :model="register"
          status-icon
          :rules="rules"
          ref="ruleForm"
          label-position="top"
          class="demo-ruleForm"
        >
          <div class="form-section">
            <div class="form-section-title">必填信息</div>
            <div class="form-grid">
              <el-form-item label="用户名" prop="username" class="strong">
                <el-input type="txt" v-model="register.username" autocomplete="off" maxlength="20" show-word-limit
                          @input="usernameUnique = true"></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="pass" class="strong">
                <el-input type="password" v-model="register.pass" autocomplete="off" show-password maxlength="20"></el-input>
              </el-form-item>
              <el-form-item label="电话" prop="telephone" class="strong">
                <el-input type="txt" v-model="register.telephone" autocomplete="off" maxlength="11" show-word-limit
                          @input="handlePhoneInput">
                  <template slot="prepend">+86</template>
                </el-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="checkPass" class="strong">
                <el-input type="password" v-model="register.checkPass" autocomplete="off" show-password maxlength="20"></el-input>
              </el-form-item>
              <el-form-item label="邮箱" prop="email" class="strong full-span email-item">
                <el-input v-model="register.email" placeholder="请输入邮箱">
                  <el-button
                    slot="append"
                    :class="{'is-cooldown': codeCountdown > 0}"
                    :disabled="codeCountdown > 0 || codeSending"
                    :loading="codeSending"
                    @click="requestEmailCode"
                  >
                    {{ codeCountdown > 0 ? `重新发送(${codeCountdown}s)` : '获取验证码' }}
                  </el-button>
                </el-input>
              </el-form-item>
              <el-form-item label="邮箱验证码" prop="emailCode" class="strong full-span">
                <el-input v-model="register.emailCode" placeholder="请输入6位验证码" maxlength="6"
                          @input="handleEmailCodeInput"></el-input>
              </el-form-item>
            </div>
          </div>
          <div class="form-section">
            <div class="form-section-title">选填信息</div>
            <div class="form-grid">
              <el-form-item label="姓名" prop="personalName" class="strong">
                <el-input type="txt" v-model="register.personalName" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="年龄" prop="age" class="strong">
                <el-input v-model.number="register.age" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="性别" prop="sex" class="strong full-span">
                <el-radio-group v-model="register.sex" class="sex-group" @change="updateSex('ruleForm')">
                  <el-radio-button label="M">男</el-radio-button>
                  <el-radio-button label="F">女</el-radio-button>
                  <el-radio-button label="U">不愿透露</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="地址" prop="chinaArea" class="strong">
                <el-cascader size="large" :options="options" v-model="register.chinaAreaNum"
                             @change="handleChangeAndUpdateArea" autocomplete="off">
                </el-cascader>
              </el-form-item>
              <el-form-item label="详细地址" prop="address" class="strong full-span">
                <el-input type="txt" v-model="register.address" autocomplete="off"></el-input>
              </el-form-item>
            </div>
          </div>
          <el-form-item>
            <el-button type="primary" @click="submitForm" style="width: 100%;"
                       :disabled="isLoginInvalid || isLoading" class="strong">
              <span v-if="isLoading">
                  <i class="el-icon-loading"></i> 注册中...
               </span>
              <span v-else>注册</span>
            </el-button>
          </el-form-item>
        </el-form>
        <div class="form-footer">
          <router-link class="hypertext" to="/login" active-class="active">登录</router-link>
        </div>
      </div>
    </div>
    <el-dialog
      title="人机验证"
      :visible.sync="captchaDialogVisible"
      width="380px"
      :close-on-click-modal="false"
      class="captcha-dialog"
    >
      <div class="captcha-body">
        <div class="captcha-question">{{ captchaQuestion }}</div>
        <el-input v-model="captchaInput" placeholder="请输入结果" @input="captchaError = ''"></el-input>
        <p v-if="captchaError" class="captcha-error">{{ captchaError }}</p>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="captchaDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCaptcha">验证并发送</el-button>
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
  grid-template-columns: 1fr 1.4fr;
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
  padding: 40px 36px;
}

.auth-form h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #1c2c27;
}

.form-subtitle {
  margin: 0 0 20px;
  color: #5f6b66;
  font-size: 14px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 20px;
  row-gap: 10px;
  align-items: start;
}

.form-grid .full-span {
  grid-column: 1 / -1;
}

.form-section {
  margin-bottom: 16px;
}

.form-section-title {
  font-size: 14px;
  font-weight: 700;
  color: #1f2b2a;
  margin: 8px 0 12px;
  padding-left: 4px;
  border-left: 3px solid #1f7a6d;
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

.demo-ruleForm .el-form-item__content,
.demo-ruleForm .el-input,
.demo-ruleForm .el-cascader,
.demo-ruleForm .el-radio-group {
  width: 100%;
}

.demo-ruleForm .el-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.note {
  font-size: 12px;
  color: #999;
  margin-left: 5px;
}

.email-item .el-input-group,
.email-item .el-input {
  width: 100%;
}

.email-item .el-input-group__append {
  background: #1f7a6d;
  border-color: #1f7a6d;
  color: #ffffff;
}

.email-item .el-input-group__append .el-button {
  color: #ffffff;
}

.email-item .el-button.is-cooldown {
  background: rgba(31, 122, 109, 0.7);
  border-color: rgba(31, 122, 109, 0.7);
  color: #ffffff;
}

.email-item .el-button.is-loading {
  transition: opacity 0.2s ease;
}

.captcha-dialog .el-dialog__header {
  border-bottom: 1px solid #eef2f0;
}

.captcha-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.captcha-question {
  font-size: 18px;
  font-weight: 700;
  color: #1f2b2a;
  background: #f0f6f4;
  padding: 12px;
  border-radius: 12px;
  text-align: center;
  letter-spacing: 1px;
}

.captcha-error {
  color: #e83a3a;
  margin: 0;
  font-size: 12px;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
}

.sex-group {
  display: flex;
  gap: 10px;
}

.sex-group :deep(.el-radio-button__inner) {
  border-radius: 12px;
  padding: 10px 16px;
  border: 1px solid rgba(31, 122, 109, 0.35);
  color: #1f7a6d;
}

.sex-group :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background: #1f7a6d;
  border-color: #1f7a6d;
  color: #ffffff;
  box-shadow: 0 8px 16px rgba(31, 122, 109, 0.22);
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

@media (max-width: 1000px) {
  .auth-card {
    grid-template-columns: 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 680px) {
  .auth-form {
    padding: 32px 24px;
  }

  .auth-aside {
    padding: 32px 24px;
  }
}
</style>
