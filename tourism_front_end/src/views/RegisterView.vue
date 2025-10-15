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
    const validatePersonalName = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入姓名'));
      } else {
        callback();
      }
    };
    const validateSex = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请选择性别'));
      } else {
        callback();
      }
    };
    const validateChinaArea = (rule, value, callback) => {
      if (value === null || value.length === 0) {
        callback(new Error('请选择中国大陆地区'));
      } else {
        callback();
      }
    };
    const validateAddress = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入详细住址'));
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
        sex: '',
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
        personalName: [{required: true, message: '请输入姓名', validator: validatePersonalName, trigger: 'blur'}],
        sex: [{required: true, message: '请选择性别', validator: validateSex, trigger: 'blur'}],
        chinaArea: [{required: true, message: '请选择中国大陆地区', validator: validateChinaArea, trigger: 'blur'}],
        address: [{required: true, message: '请输入详细住址', validator: validateAddress, trigger: 'blur'}],
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
      return !this.register.username ||  this.register.pass !== this.register.checkPass
          || !this.register.personalName || !this.register.sex
          || !Number.isInteger(this.register.age) || this.register.age < 0
          || !this.register.chinaArea || !this.register.address || !this.register.telephone|| !this.register.chinaArea || !this.register.address || !this.register.telephone
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
      this.info.cname = this.register.personalName;
      this.info.csex = this.register.sex;
      this.info.email = this.register.email;
      this.info.cage = this.register.age;
      this.info.caddress = this.register.chinaArea + this.register.address;
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
      this.$refs[formName].validateField("sex", () => {
      });
    }
  },
}
</script>

<template>
  <!--使用背景图片  <div :style="{'background-image': 'url(' + backgroundImage + ')'}">-->
  <div :style="{'background-color': '#c4dbfd'}">
    <div class="register_container">
      <el-card class="register_box-card">
        <div class="demo-image center">
          <div class="block" style="display: flex; justify-content: center; align-items: center;">
            <el-image style="width: 100px; height: 100px"
                      :src="url"
                      class="border-radius"
            ></el-image>
          </div>
        </div>
        <h1 style="display: flex; justify-content: center; align-content: center" class="manu_logo">
          旅游信息管理系统</h1>
        <div class="text item" style="padding: 0">
          <el-form :model="register" status-icon :rules="rules" ref="ruleForm" class="demo-ruleForm">
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
                  <br>
                  <el-radio-group v-model="register.sex" @change="updateSex('ruleForm')">
                    <el-radio label="F" border>女</el-radio>
                    <el-radio label="M" border>男</el-radio>
                    <el-radio label="U" border disabled>其他</el-radio>
                  </el-radio-group>
                </el-form-item>
              </div>
              <div class="right-column">
                <el-form-item label="姓名" prop="personalName" class="strong">
                  <el-input type="txt" v-model="register.personalName" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="年龄" prop="age" class="strong"
                              :rules="[
                          { required: true, message: '年龄不能为空', trigger: 'blur'},
                          { type: 'number', message: '年龄必须为数字值'}
                        ]"
                >
                  <el-input v-model.number="register.age" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="中国大陆地区" prop="chinaArea" class="strong">
                  <br>
                  <el-cascader size="large" :options="options" v-model="register.chinaAreaNum"
                               @change="handleChangeAndUpdateArea" autocomplete="off">
                  </el-cascader>
                  <span class="note">*暂不支持其他国家/地区</span>
                </el-form-item>
                <el-form-item label="详细住址" prop="address" class="strong">
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
        </div>
        <br>
        <div class="align-container">
          <router-link class="hypertext" to="/login" active-class="active">登录</router-link>
          <router-link class="hypertext" to="/" active-class="active">返回主页</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style>
.register_container {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  min-height: 100%;
  height: 100vh;
}

.text {
  font-size: 14px;
}

.item {
  padding: 18px 0;
}

.register_box-card {
  width: 800px;
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

.form-columns {
  display: flex;
  justify-content: space-between;
}

.left-column,
.right-column {
  flex: 1;
  padding: 10px;
}

.note {
  font-size: 12px;
  color: #999;
  margin-left: 5px;
}
</style>
