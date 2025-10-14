<script>
import {jwtDecode} from "jwt-decode";
import {apiRear} from "@/axios";
import _ from 'lodash';

const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'));
  } else {
    callback();
  }
};
const validateCname = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入姓名'));
  } else {
    callback();
  }
};
const validateCaddress = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入地址'));
  } else {
    callback();
  }
};
const validateCphone = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入电话'));
  } else {
    callback();
  }
};
const validateCsymptom = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入症状'));
  } else {
    callback();
  }
};
const validateCremark = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入备注'));
  } else {
    callback();
  }
};

export default {
  data() {
    const validateOldPass = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入旧密码'));
      } else {
        callback();
      }
    };
    const validatePass = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入密码'));
      } else {
        if (!this.edit.newPassword) {
          this.$refs.passwordRef.validateField('checkPassword');
        }
        callback();
      }
    };
    const validatePass2 = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请再次输入新密码'));
      } else if (value !== this.edit.newPassword) {
        callback(new Error('两次输入密码不一致!'));
      } else {
        callback();
      }
    };
    return {
      token: sessionStorage.getItem('token') || '',
      loading: true,
      dialogEditInfo: false,
      dialogUpdatePassword: false,
      searchLabelWidth: "120px",
      dangerColor: '#e83a3a',
      userinfo: {},
      circleUrl: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png",
      uid: '',
      edit: {
        username: '',
        email: '',
        cname: '',
        csex: '',
        cage: 0,
        caddress: '',
        cphone: '',
        csymptom: '',
        cremark: '',
        newPassword: '',
        oldPassword: '',
        checkPassword: '',
      },
      size: 'medium',
      rules: {
        username: [{required: true, message: '请输入用户名', validator: validateUsername, trigger: 'blur'}],
        email: [{message: '请输入邮箱地址', trigger: 'blur'}, {
          type: 'email',
          message: '请输入正确的邮箱地址',
          trigger: ['blur', 'change']
        }],
        cname: [{required: true, message: '请输入姓名', validator: validateCname, trigger: 'blur'}],
        cage: [{required: true, message: '年龄不能为空', trigger: 'blur'}, {
          type: 'number',
          message: '年龄必须为数字值'
        }],
        cphone: [{required: true, message: '请输入电话', validator: validateCphone, trigger: 'blur'}],
        csex: [{required: true, trigger: 'blur'}],
        caddress: [{required: true, message: '请输入地址', validator: validateCaddress, trigger: 'blur'}],
        csymptom: [{required: true, message: '请输入症状', validator: validateCsymptom, trigger: 'blur'}],
        cremark: [{required: true, message: '请输入备注', validator: validateCremark, trigger: 'blur'}],
        oldPassword: [{required: true, validator: validateOldPass, trigger: 'blur'}],
        newPassword: [{required: true, validator: validatePass, trigger: 'blur'}],
        checkPassword: [{required: true, validator: validatePass2, trigger: 'blur'}],
      },
    };
  },
  created() {
    this.initEditUid();
  },
  computed: {
    getUid() {
      return jwtDecode(this.token).uid;
    },
    formattedSex() {
      if (this.userinfo.csex === 'F') {
        return '男';
      } else if (this.userinfo.csex === 'M') {
        return '女';
      } else {
        return '未知';
      }
    },
    isEditInvalid() {
      const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      return !this.edit.username || !this.edit.cname || !this.edit.csex ||
          !Number.isInteger(this.edit.cage) || !this.edit.cphone || !this.edit.caddress ||
          !this.edit.csymptom || !this.edit.cremark ||
          (emailPattern.test(this.edit.email));
    },
    isPasswordInvalid() {
      return this.edit.pass !== this.edit.checkPass;
    },
  },
  methods: {
    initEditUid() {
      this.uid = jwtDecode(this.token).uid;
    },
    onDialogOpen() {
      // 对话框打开时添加键盘事件监听器
      document.addEventListener('keydown', this.handleKeydown);
    },
    onDialogClose() {
      // 对话框关闭时移除键盘事件监听器
      document.removeEventListener('keydown', this.handleKeydown);
    },
    handleKeydown(event) {
      if (event.key === 'Escape' || event.key === 'Esc') {
        if (this.dialogEditInfo) {
          this.cancelEdit();
        }
      }
    },
    cancelEdit() {
      this.$refs["userRef"].resetFields();
      this.edit = _.cloneDeep(this.userinfo);
      this.edit.password = '';
      this.dialogEditInfo = false;
    },
    cancelUpdatePassword() {
      this.edit.newPassword = '';
      this.edit.oldPassword = '';
      this.edit.checkPassword = '';
      this.$refs["passwordRef"].resetFields();
      this.dialogUpdatePassword = false;
    },
    async updatePassword() {
      const response = await apiRear.post(`/userInfo/updatePassword/${this.uid}&${this.edit.oldPassword}&${this.edit.newPassword}`);
      if (response.data.code === 1) {
        this.$message({
          type: 'success',
          message: '密码修改成功!正在退出账号...'
        });
        this.cancelUpdatePassword();
        // 清除 sessionStorage 中的 token
        sessionStorage.removeItem('token');
        // 清除 localStorage 中的 activeIndex
        localStorage.removeItem('shoppingCart');
        localStorage.setItem('activeIndex', '1');
        // 跳转到首页
        await this.$router.push('/indexView');
      } else {
        console.log(response.data.code)
        this.$message({
          type: 'error',
          message: '密码修改失败！请检查原密码是否正确！'
        });
      }
    },
    updateUserInfo() {
      apiRear.post(`/userInfo/update`, this.edit)
          .then(() => {
            this.fetchData();
            this.$message({
              type: 'success',
              message: '更改成功!'
            });
            this.loading = true;
            this.dialogEditInfo = false;
          }).catch(err => {
        console.log(err);
        this.$message({
          type: 'error',
          message: '更改资料失败！用户名已存在或格式错误！'
        });
      });
    },
    fetchData() {
      apiRear.get(`/userInfo/personal/${this.uid}`,).then(res => {
        this.userinfo = res.data.data;
        this.edit = _.cloneDeep(res.data.data);
        this.edit.password = '';
        this.loading = false;
      }).catch(err => {
        console.log(err);
        this.$message.error('未知错误,请重新登录后再试');
      });
    },
  },
  mounted() {
    this.fetchData();
  }
}
</script>
<template>
  <div class="center-container">

    <el-dialog title="编辑资料" :close-on-click-modal="false" :visible.sync="dialogEditInfo" center
               width="1000px" @open="onDialogOpen" @close="onDialogClose">
      <div style="display: flex">
        <div style="width: 50%">
          <el-form ref="userRef" :model="edit" :rules="rules" style="width: 400px">
            <el-form-item label="用户名" prop="username" :label-width="searchLabelWidth" class="strong marginWidth">
              <el-input v-model="edit.username" autocomplete="off" suffix-icon="el-icon-notebook-2"
                        placeholder="请输入内容"
              ></el-input>
            </el-form-item>
            <el-form-item label="姓名" prop="cname" :label-width="searchLabelWidth" class="strong marginWidth">
              <el-input v-model="edit.cname" autocomplete="off" suffix-icon="el-icon-user"
                        placeholder="请输入内容"
              ></el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="email" :label-width="searchLabelWidth" class="strong marginWidth">
              <el-input v-model="edit.email" autocomplete="off" suffix-icon="el-icon-message"
                        placeholder="请输入内容"
              ></el-input>
            </el-form-item>
            <el-form-item label="年龄" prop="cage" :label-width="searchLabelWidth" class="strong marginWidth">
              <el-input v-model.number="edit.cage" autocomplete="off" suffix-icon="el-icon-edit-outline"
                        placeholder="请输入内容" type="number" min="0"
              ></el-input>
            </el-form-item>
            <el-form-item label="电话" prop="cphone" :label-width="searchLabelWidth" class="strong marginWidth">
              <el-input v-model="edit.cphone" autocomplete="off" suffix-icon="el-icon-mobile-phone"
                        placeholder="请输入内容"
              ></el-input>
            </el-form-item>
          </el-form>
          <el-card shadow="hover"
                   style="width: 300px;height: 220px;margin-left: 110px;
                   display: flex; align-items: center; justify-content: center;">
            <p style="font-size: 130px">🎯</p>
          </el-card>
        </div>
        <div style="width: 50%">
          <el-form :model="edit" :rules="rules" style="width: 400px">
            <el-form-item label="性别" prop="csex" :label-width="searchLabelWidth" class="strong marginWidth">
              <el-radio-group v-model="edit.csex">
                <el-radio-button label="F" border>男</el-radio-button>
                <el-radio-button label="M" border>女</el-radio-button>
                <el-radio-button label="U" border disabled>其他</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="地址" prop="caddress" :label-width="searchLabelWidth" class="strong marginWidth">
              <el-input v-model="edit.caddress" autocomplete="off" suffix-icon="el-icon-house"
                        placeholder="请输入内容"
              ></el-input>
            </el-form-item>
            <el-form-item label="症状" prop="csymptom" :label-width="searchLabelWidth" class="strong marginWidth">
              <el-input v-model="edit.csymptom" autocomplete="off" suffix-icon="el-icon-edit-outline"
                        placeholder="请输入内容,限制100字" type="textarea" maxlength="100" show-word-limit
                        :autosize="{ minRows: 8, maxRows: 9}"
              ></el-input>
            </el-form-item>
            <el-form-item label="备注" prop="cremark" :label-width="searchLabelWidth" class="strong marginWidth">
              <el-input v-model="edit.cremark" autocomplete="off" suffix-icon="el-icon-edit-outline"
                        placeholder="请输入内容,限制150字" type="textarea" maxlength="150" show-word-limit
                        :autosize="{ minRows: 10, maxRows: 10}"
              ></el-input>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <div slot="footer" class="dialog-footer" style="padding-top: 0">
        <div style="padding-bottom: 10px">
          <el-button @click="cancelEdit">取 消</el-button>
          <el-button type="primary" @click="updateUserInfo" :disabled="isEditInvalid">确 定</el-button>
        </div>
        <el-card shadow="hover" style="border-radius: 30px;background-color: #d7eafd">
          <div style="text-align: left">
            <h2>tips:</h2>
            <p class="strong">1. 🆔用户编号、顾客编号唯一⚠️，不可修改🔒。</p>
            <p class="strong">2.
              <span class="strong" :style="{color: dangerColor}"> 📧用户名唯一，⚠️不可重复</span>
              。若修改资料失败，请检查用户名是否已存在🔍。</p>
            <p class="strong">3. 📞电话可为字符串，如：+86-123-456-789📲。</p>
            <p class="strong">4. 📍地址需详细填写，如：广东省肇庆市端州区肇庆学院🏢。可以使用英文🌐。</p>
          </div>
        </el-card>
      </div>
    </el-dialog>

    <el-dialog title="修改密码" :close-on-click-modal="false"
               :visible.sync="dialogUpdatePassword" center width="500px">
      <el-form ref="passwordRef" :model="edit" :rules="rules" style="width: 400px">
        <el-form-item label="原密码" prop="oldPassword" :label-width="searchLabelWidth" class="strong marginWidth">
          <el-input v-model="edit.oldPassword" autocomplete="off" suffix-icon="el-icon-lock"
                    placeholder="请输入原密码" type="password" show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword" :label-width="searchLabelWidth" class="strong marginWidth">
          <el-input v-model="edit.newPassword" autocomplete="off" suffix-icon="el-icon-lock"
                    placeholder="请输入新密码" type="password" show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="checkPassword" :label-width="searchLabelWidth" class="strong marginWidth">
          <el-input v-model="edit.checkPassword" autocomplete="off" suffix-icon="el-icon-lock"
                    placeholder="请再次输入新密码" type="password" show-password
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <div style="padding-bottom: 10px">
          <el-button @click="cancelUpdatePassword">取 消</el-button>
          <el-button type="primary" @click="updatePassword" :disabled="isPasswordInvalid">确 定</el-button>
        </div>
      </div>
    </el-dialog>

    <el-card class="interior-container" v-loading="loading">
      <div class="center-container">
        <el-avatar :size="300" :src="circleUrl"></el-avatar>
        <br><br>
        <h1 class="strong">{{ this.userinfo.username }}</h1>
      </div>
      <el-descriptions ref="infoTable" class="margin-top" :column="3" :size="size" border>
        <template slot="extra">
          <el-button
              type="primary"
              icon="el-icon-edit-outline"
              @click="dialogUpdatePassword = true"
              round>修改密码
          </el-button>
        </template>
        <template slot="extra">
          <el-button
              type="primary"
              icon="el-icon-edit-outline"
              @click="dialogEditInfo = true"
              round>修改资料
          </el-button>
        </template>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-user"></i>
            <span class="strong"> UID</span>
          </template>
          <span class="strong">{{ this.getUid }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-user"></i>
            <span class="strong"> CNO</span>
          </template>
          <span class="strong">{{ this.userinfo.cno }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-user"></i>
            <span class="strong"> 用户名</span>
          </template>
          <span class="strong">{{ this.userinfo.username }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-tickets"></i>
            <span class="strong"> 身份</span>
          </template>
          <template>
            <el-tag v-if="userinfo.authority === 1" type="danger" effect="dark">管理员</el-tag>
            <el-tag v-else-if="userinfo.authority === 2" type="success" effect="dark">旅行顾问</el-tag>
            <el-tag v-else-if="userinfo.authority === 3" effect="dark">顾客</el-tag>
          </template>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-user"></i>
            <span class="strong"> 姓名</span>
          </template>
          <span class="strong">{{ this.userinfo.cname }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-location-outline"></i>
            <span class="strong"> 地址</span>
          </template>
          <span class="strong">{{ this.userinfo.caddress }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-message"></i>
            <span class="strong"> 邮箱</span>
          </template>
          <span class="strong">{{ this.userinfo.email }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i v-if="userinfo.csex === 'F'" class="el-icon-male"></i>
            <i v-else class="el-icon-female"></i>
            <span class="strong"> 性别</span>
          </template>
          <span class="strong">{{ formattedSex }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-time"></i>
            <span class="strong"> 年龄</span>
          </template>
          <span class="strong">{{ this.userinfo.cage }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-mobile-phone"></i>
            <span class="strong"> 电话</span>
          </template>
          <span class="strong">{{ this.userinfo.cphone }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-message"></i>
            <span class="strong"> 创建日期</span>
          </template>
          <span class="strong">{{ this.userinfo.cdate }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-message"></i>
            <span class="strong"> 症状</span>
          </template>
          <span class="strong">{{ this.userinfo.csymptom }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-message"></i>
            <span class="strong"> 备注</span>
          </template>
          <span class="strong">{{ this.userinfo.cremark }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<style scoped>
.strong {
  font-weight: bold;
}

.center-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.interior-container {
  width: 80%;
  height: 80%;
  margin-top: 90px;
}
</style>