<script>
import {regionData, codeToText} from 'element-china-area-data'
import {apiRear} from "@/axios";

export default {
  name: 'RegisterView',
  data() {
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'));
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
        callback(new Error('两次输入密码不一致!'));
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
    const validateTelephone = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入电话'));
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
        cage: '',
        caddress: '',
        cphone: ''
      },
      rules: {
        username: [{required: true, validator: validateUsername, trigger: 'blur'}],
        pass: [{required: true, validator: validatePass, trigger: 'blur'}],
        checkPass: [{required: true, validator: validatePass2, trigger: 'blur'}],
        telephone: [{required: true, message: '请输入电话', validator: validateTelephone, trigger: 'blur'}],
        email: [{message: '请输入邮箱地址', trigger: 'blur'}, {
          type: 'email',
          message: '请输入正确的邮箱地址',
          trigger: ['blur', 'change']
        }]
      },
      url: '/zqxy.jpg',
      sex: null,
    };
  },
  computed: {
    isLoginInvalid() {
      const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      const ageValid =
        this.register.age === '' ||
        this.register.age === null ||
        (Number.isInteger(this.register.age) && this.register.age >= 0);
      return !this.register.username || this.register.pass !== this.register.checkPass
          || !this.register.telephone
          || !ageValid
          || (this.register.email && !emailPattern.test(this.register.email));
    }
  },
  methods: {
    async submitForm() {
      if (this.isLoginInvalid) {
        return;
      }
      this.isLoading = true;
      this.info.username = this.register.username;
      this.info.password = this.register.pass;
      this.info.cname = this.register.personalName || '';
      this.info.csex = this.register.sex || 'U';
      this.info.email = this.register.email;
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
        const response = await apiRear.post(`/isUnique/${this.info.username}`);
        if (response.data.code === 0) {
          this.$message({
            type: 'error',
            message: '用户名已存在！'
          })
        }
        else{
          await apiRear.post('/register', this.info);
          this.$message.success('注册成功');
          await this.$router.push('/login');
        }
      } catch (error) {
        this.$message.error('未知错误');
      } finally {
        this.isLoading = false;
      }
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
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <button class="back-button" @click="$router.push('/indexView')">
        <i class="el-icon-arrow-left"></i>
      </button>
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
          <div class="form-columns">
            <div class="left-column">
              <el-form-item label="用户名" prop="username" class="strong">
                <el-input type="txt" v-model="register.username" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="pass" class="strong">
                <el-input type="password" v-model="register.pass" autocomplete="off" show-password></el-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="checkPass" class="strong">
                <el-input type="password" v-model="register.checkPass" autocomplete="off" show-password></el-input>
              </el-form-item>
              <el-form-item label="邮箱(选填)" prop="email" class="strong">
                <el-input v-model="register.email"></el-input>
              </el-form-item>
              <el-form-item label="性别" prop="sex" class="strong">
                <el-radio-group v-model="register.sex" @change="updateSex('ruleForm')">
                  <el-radio label="M" border>男</el-radio>
                  <el-radio label="F" border>女</el-radio>
                  <el-radio label="U" border>不愿透露</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
            <div class="right-column">
              <el-form-item label="姓名" prop="personalName" class="strong">
                <el-input type="txt" v-model="register.personalName" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="年龄" prop="age" class="strong">
                <el-input v-model.number="register.age" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="地址" prop="chinaArea" class="strong">
                <el-cascader size="large" :options="options" v-model="register.chinaAreaNum"
                             @change="handleChangeAndUpdateArea" autocomplete="off">
                </el-cascader>
              </el-form-item>
              <el-form-item label="详细地址" prop="address" class="strong">
                <el-input type="txt" v-model="register.address" autocomplete="off"></el-input>
              </el-form-item>
              <el-form-item label="电话" prop="telephone" class="strong">
                <el-input type="txt" v-model="register.telephone" autocomplete="off"></el-input>
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
  width: 980px;
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 0;
  background: #ffffff;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(31, 56, 94, 0.18);
  animation: fadeInUp 0.6s ease both;
  position: relative;
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

.form-columns {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  column-gap: 20px;
  row-gap: 6px;
  align-items: start;
}

.left-column,
.right-column {
  display: flex;
  flex-direction: column;
  gap: 4px;
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

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
}

.back-button {
  position: absolute;
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

  .form-columns {
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
