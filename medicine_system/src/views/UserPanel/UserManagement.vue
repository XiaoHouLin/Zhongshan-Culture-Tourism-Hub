<script>
import {apiRear} from "@/axios";
import {codeToText, regionData} from "element-china-area-data";

const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'));
  } else {
    callback();
  }
};
const validateEmail = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入邮箱'));
  } else {
    callback();
  }
};
const validateCno = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入顾客编号'));
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
const validateCage = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入年龄'));
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
const validateCdate = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入日期'));
  } else {
    callback();
  }
};
const validateBalance = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入余额'));
  } else {
    callback();
  }
};
export default {
  data() {
    return {
      tableData: [],
      total: 0,
      uid: [],
      dialogSearchVisible: false,
      dialogUpdateAddUsersVisible: false,
      searchLabelWidth: '120px',
      tableHeight: '88vh',
      dangerColor: '#e83a3a',
      value: '',
      date: '',
      dialogTitle: '',
      address: regionData,
      changeUser: {},
      addressNum: [],
      fuzzyAddress: '',
      detailAddress: '',
      loading: true,
      options: [{
        value: '20',
        label: '20'
      }, {
        value: '40',
        label: '40'
      }, {
        value: '60',
        label: '60'
      }, {
        value: '80',
        label: '80'
      }, {
        value: '100',
        label: '100'
      }],
      search: {
        page: 1,
        pageSize: 20,
        uid: '',
        username: '',
        email: '',
        authority: null,
        cno: '',
        cname: '',
        csex: '',
        cage: null,
        caddress: '',
        cphone: '',
        csymptom: '',
        startDate: '',
        endDate: '',
        cremark: '',
        balance: null
      },
      rules: {
        username: [{required: true, message: '请输入用户名', validator: validateUsername, trigger: 'blur'}],
        email: [{message: '请输入邮箱', validator: validateEmail, trigger: 'blur'}],
        cno: [{required: true, message: '请输入顾客编号', validator: validateCno, trigger: 'blur'}],
        cname: [{required: true, message: '请输入姓名', validator: validateCname, trigger: 'blur'}],
        cage: [{required: true, message: '请输入年龄', validator: validateCage, trigger: 'blur'}],
        cphone: [{required: true, message: '请输入电话', validator: validateCphone, trigger: 'blur'}],
        caddress: [{required: true, message: '请输入地址', validator: validateCaddress, trigger: 'blur'}],
        csymptom: [{message: '请输入症状', validator: validateCsymptom, trigger: 'blur'}],
        cremark: [{message: '请输入备注', validator: validateCremark, trigger: 'blur'}],
        cdate: [{required: true, message: '请输入日期', validator: validateCdate, trigger: 'blur'}],
        balance: [{required: true, message: '请输入余额', validator: validateBalance, trigger: 'blur'}],
      },
    }
  },
  computed: {
    isEditInvalid() {
      return !this.changeUser.username ||
          !this.changeUser.cname || !this.changeUser.csex ||
          !this.changeUser.cage || !this.changeUser.cphone || !this.changeUser.authority ||
          !this.changeUser.caddress || !this.changeUser.cdate ||
          (this.changeUser.balance === undefined || this.changeUser.balance === null);
    }
  },
  methods: {
    clearSelection() {
      this.$refs.userTable.clearSelection();
    },
    confirmClickHandler() {
      if (this.dialogTitle === '编辑') {
        return this.updateUsers();
      } else if (this.dialogTitle === '添加用户') {
        return this.insertUsers();
      }
      return () => {
        console.log('未定义的value')
      };
    },
    open(uid) {
      this.uid.push(uid);
      this.deleteChangeUser();
    },
    openEdit(uid) {
      this.dialogTitle = '编辑';
      this.uid = [];
      this.clearSelection();
      this.uid.push(uid);
      this.searchUser();
      this.dialogUpdateAddUsersVisible = true;
    },
    openAdd() {
      this.dialogTitle = '添加用户';
      this.dialogUpdateAddUsersVisible = true;
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
        this.cancelEditOrAdd();
      }
    },
    cancelEditOrAdd() {
      this.changeUser = {};
      this.uid = [];
      this.rules = {
        username: [{required: true, message: '请输入用户名', validator: validateUsername, trigger: 'blur'}],
        email: [{message: '请输入邮箱', validator: validateEmail, trigger: 'blur'}],
        cno: [{required: true, message: '请输入顾客编号', validator: validateCno, trigger: 'blur'}],
        cname: [{required: true, message: '请输入姓名', validator: validateCname, trigger: 'blur'}],
        cage: [{required: true, message: '请输入年龄', validator: validateCage, trigger: 'blur'}],
        cphone: [{required: true, message: '请输入电话', validator: validateCphone, trigger: 'blur'}],
        caddress: [{required: true, message: '请输入地址', validator: validateCaddress, trigger: 'blur'}],
        csymptom: [{message: '请输入症状', validator: validateCsymptom, trigger: 'blur'}],
        cremark: [{message: '请输入备注', validator: validateCremark, trigger: 'blur'}],
        cdate: [{required: true, message: '请输入日期', validator: validateCdate, trigger: 'blur'}],
        balance: [{required: true, message: '请输入余额', validator: validateBalance, trigger: 'blur'}],
      };
      this.dialogUpdateAddUsersVisible = false;
    },
    searchUser() {
      apiRear.get(`/userInfo/${this.uid}`).then(edit => {
        this.changeUser = edit.data.data;
      }).catch(err => {
        console.log(err);
      });
    },
    async updateUsers() {
      try {
        apiRear.post(`/userInfo/update`, this.changeUser)
            .then(() => {
              this.fetchData();
              this.$message({
                type: 'success',
                message: '更改成功!'
              });
              this.dialogUpdateAddUsersVisible = false;
              this.changeUser = {};
              this.loading = true;
            }).catch(err => {
          console.log(err);
          this.$message({
            type: 'error',
            message: '更改用户信息失败！请检查格式是否正确！'
          });
        });
      } catch (error) {
        console.error('未知错误:', error);
        this.$message.error('发生未知错误，请稍后再试');
      }
    },
    async insertUsers() {
      apiRear.post(`/userInfo/insert`, this.changeUser)
          .then(() => {
            this.fetchData();
            this.$message({
              type: 'success',
              message: '添加用户成功!'
            });
            this.dialogUpdateAddUsersVisible = false;
            this.changeUser = {};
            this.loading = true;
          }).catch(err => {
        console.log(err);
        this.$message({
          type: 'error',
          message: '添加用户失败！格式错误或是(用户名)信息重复！'
        });
      });
    },
    deleteChangeUser() {
      this.$confirm('此操作将永久删除该用户, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.delete(this.uid);
        this.uid = [];
      }).catch(() => {
        this.uid = [];
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      })
    },
    searchForm() {
      this.dialogSearchVisible = true;
    },
    searchConfirm() {
      this.search.page = 1;
      this.loading = true;
      this.dialogSearchVisible = false;
      this.fetchData();
    },
    resetAuthority() {
      this.search.authority = null;
    },
    resetCsex() {
      this.search.csex = '';
    },
    handleChange(value) {
      this.fuzzyAddress = (codeToText[value[0]] + codeToText[value[1]] + codeToText[value[2]]);
    },
    refreshSearch() {
      this.search = {
        page: 1,
        pageSize: 20,
        uid: '',
        username: '',
        email: '',
        authority: null,
        cno: '',
        cname: '',
        csex: '',
        cage: null,
        caddress: '',
        cphone: '',
        csymptom: '',
        startDate: '',
        endDate: '',
        cremark: ''
      }
      this.date = [];
      this.addressNum = [];
      this.fuzzyAddress = '';
      this.detailAddress = '';
      this.loading = true;
      this.fetchData();
    },
    handleSelectionChange(selection) {
      this.uid = selection.map(item => item.uid);
    },
    fetchData() {
      apiRear.get(`/userInfo`, {params: this.search}).then(res => {
        this.tableData = res.data.data.rows;
        this.total = res.data.data.total;
        this.loading = false;
        this.$nextTick(() => {
          this.$refs.userTable.doLayout();
        });
      }).catch(err => {
        console.log(err);
        this.$message.error('未知错误,请重新登录后再试');
      });
    },
    delete(uid) {
      if (!uid) throw new Error('uid为空');
      apiRear.delete(`/userInfo/${uid}`).then(() => {
        this.fetchData();
        this.$message({
          type: 'success',
          message: '删除成功!'
        });
      }).catch(err => {
        console.log(err);
        this.$message({
          type: 'error',
          message: '删除用户失败,请检查是否选中用户'
        });
      });
    },
    handlePageChange(newPage) {
      this.loading = true;
      this.search.page = newPage;
      this.fetchData();
    },
    handlePageSizeChange(newPageSize) {
      this.search.pageSize = parseInt(newPageSize);
      this.handlePageChange(1);
    },
    getGenderText(gender) {
      if (gender === 'F')
        return '男';
      else if (gender === 'M')
        return '女';
      return '/';
    }
  },

  mounted() {
    this.fetchData();
  }
}
</script>

<!--TODO:1.有些需要精确查询，比如UID 2.年龄用滑块
-->
<template>
  <div style="height: 100vh">
    <el-card shadow="always" class="fixed-height-card">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div style="display: flex;">
          <el-button type="primary" icon="el-icon-search" @click="searchForm" style="margin: 10px">查询</el-button>
          <el-popconfirm
              confirm-button-text='好的'
              cancel-button-text='不用了'
              @confirm="refreshSearch"
              icon="el-icon-info"
              icon-color="red"
              title="即将重置所有查询条件(包括页码和显示条数),是否继续?"
          >
            <el-button type="warning" icon="el-icon-refresh" slot="reference" style="margin: 10px">重置查询</el-button>
          </el-popconfirm>
        </div>
        <div style="display: flex; justify-content: flex-end;">
          <el-button type="danger" icon="el-icon-delete" @click="deleteChangeUser" style="margin: 10px">批量删除
          </el-button>
          <el-button type="success" icon="el-icon-plus" @click="openAdd" style="margin: 10px;">添加用户
          </el-button>
        </div>
      </div>
      <!--      查询按钮对话框-->
      <el-dialog title="查询" :visible.sync="dialogSearchVisible" :close-on-click-modal="false" center width="1000px">
        <div style="display: flex">
          <div style="width: 50%">
            <!--            TODO : 实现查询功能-->
            <el-form :model="search" style="width: 400px">
              <el-form-item label="用户编号(UID)" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.uid" autocomplete="off" suffix-icon="el-icon-edit"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="顾客编号(CNO)" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.cno" autocomplete="off" suffix-icon="el-icon-edit"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="用户名" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.username" autocomplete="off" suffix-icon="el-icon-notebook-2"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="邮箱" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.email" autocomplete="off" suffix-icon="el-icon-message"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="姓名" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.cname" autocomplete="off" suffix-icon="el-icon-user"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="年龄" :label-width="searchLabelWidth" class="strong">
                <el-input v-model.number="search.cage" autocomplete="off" suffix-icon="el-icon-edit-outline"
                          placeholder="请输入内容,可为空" type="number" min="0"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="电话" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.cphone" autocomplete="off" suffix-icon="el-icon-mobile-phone"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
            </el-form>
          </div>
          <div style="width: 50%">
            <el-form :model="search" style="width: 400px">
              <el-form-item label="性别" :label-width="searchLabelWidth" class="strong">
                <el-radio-group v-model="search.csex" style="padding-right: 71px">
                  <el-radio-button label="F" border>男</el-radio-button>
                  <el-radio-button label="M" border>女</el-radio-button>
                  <el-radio-button label="U" border disabled>其他</el-radio-button>
                </el-radio-group>
                <el-button size="mini" type="danger" icon="el-icon-delete" circle @click="resetCsex"></el-button>
              </el-form-item>
              <el-form-item label="权限" :label-width="searchLabelWidth" class="strong">
                <el-radio-group v-model="search.authority" style="padding-right: 15px">
                  <el-radio-button label="1" border>管理员</el-radio-button>
                  <el-radio-button label="2" border>经办人</el-radio-button>
                  <el-radio-button label="3" border>顾客</el-radio-button>
                </el-radio-group>
                <el-button size="mini" type="danger" icon="el-icon-delete" circle @click="resetAuthority"></el-button>
              </el-form-item>
              <el-form-item label="地址" :label-width="searchLabelWidth" class="strong">
                <el-cascader size="large" :options="address" v-model="addressNum"
                             @change="handleChange" autocomplete="off">
                </el-cascader>
              </el-form-item>
              <el-form-item label="详细地址" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="detailAddress" autocomplete="off" suffix-icon="el-icon-house"
                          placeholder="不是中国大陆只填此处;可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="备注" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.cremark" autocomplete="off" suffix-icon="el-icon-notebook-1"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="症状" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.csymptom" autocomplete="off" suffix-icon="el-icon-chat-dot-square"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="日期范围" :label-width="searchLabelWidth" class="strong">
                <el-date-picker
                    v-model="date"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期,可为空"
                    end-placeholder="结束日期,可为空"
                    value-format="yyyy-MM-dd"
                    format="yyyy-MM-dd"
                >
                </el-date-picker>
              </el-form-item>
            </el-form>
          </div>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogSearchVisible = false">取 消</el-button>
          <el-button type="primary" @click="searchConfirm">确 定</el-button>
        </div>
      </el-dialog>

      <!--      编辑用户对话框-->
      <el-dialog :title="dialogTitle" :visible.sync="dialogUpdateAddUsersVisible" :close-on-click-modal="false" center
                 width="1000px" @open="onDialogOpen" @close="onDialogClose">
        <div style="display: flex">
          <div style="width: 50%">
            <el-form ref="userRef" :model="changeUser" :rules="rules" style="width: 400px">
              <el-form-item label="顾客编号" prop="cno" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeUser.cno" autocomplete="off" suffix-icon="el-icon-tickets"
                          placeholder="请输入内容"
                          disabled
                ></el-input>
              </el-form-item>
              <el-form-item label="用户名" prop="username" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeUser.username" autocomplete="off" suffix-icon="el-icon-notebook-2"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="邮箱" prop="email" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeUser.email" autocomplete="off" suffix-icon="el-icon-message"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="姓名" prop="cname" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeUser.cname" autocomplete="off" suffix-icon="el-icon-user"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="年龄" prop="cage" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model.number="changeUser.cage" autocomplete="off" suffix-icon="el-icon-edit-outline"
                          placeholder="请输入内容" type="number" min="0"
                ></el-input>
              </el-form-item>
              <el-form-item label="电话" prop="cphone" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeUser.cphone" autocomplete="off" suffix-icon="el-icon-mobile-phone"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="占位格" :label-width="searchLabelWidth" class="strong">
                <el-input autocomplete="off" suffix-icon="el-icon-circle-close"
                          placeholder="只是因为这空着不好看..."
                          :disabled="true"></el-input>
              </el-form-item>
            </el-form>
          </div>
          <div style="width: 50%">
            <el-form :model="changeUser" :rules="rules" style="width: 400px">
              <el-form-item label="性别" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-radio-group v-model="changeUser.csex" style=";height: 40px;">
                  <el-radio-button label="F" border>男</el-radio-button>
                  <el-radio-button label="M" border>女</el-radio-button>
                  <el-radio-button label="U" border disabled>其他</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="权限" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-radio-group v-model="changeUser.authority" style="padding-right: 15px">
                  <el-radio-button label="1" border>管理员</el-radio-button>
                  <el-radio-button label="2" border>经办人</el-radio-button>
                  <el-radio-button label="3" border>顾客</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="地址" prop="caddress" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeUser.caddress" autocomplete="off" suffix-icon="el-icon-house"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="症状" prop="csymptom" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeUser.csymptom" autocomplete="off" suffix-icon="el-icon-chat-dot-square"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="备注" prop="cremark" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeUser.cremark" autocomplete="off" suffix-icon="el-icon-notebook-1"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="日期" prop="cdate" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeUser.cdate" autocomplete="off" suffix-icon="el-icon-date"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="余额" prop="balance" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeUser.balance" autocomplete="off" suffix-icon="el-icon-money"
                          placeholder="请输入内容" type="number"
                ></el-input>
              </el-form-item>
            </el-form>
          </div>
        </div>
        <div slot="footer" class="dialog-footer" style="padding-top: 0">
          <div style="padding-bottom: 10px">
            <el-button @click="cancelEditOrAdd">取 消</el-button>
            <el-button type="primary" @click="confirmClickHandler" :disabled="isEditInvalid">确 定</el-button>
          </div>
          <el-card shadow="hover" style="border-radius: 30px;background-color: #d7eafd">
            <div style="text-align: left">
              <h2>tips:</h2>
              <h3 class="strong" :style="{color: dangerColor}"
              >新建用户时，初始密码🔑是<strong>123456</strong>，务必记得修改！</h3>
              <p class="strong">1. 🆔用户编号
                <span class="strong" :style="{color: dangerColor}">UID</span>
                创建时
                <span class="strong" :style="{color: dangerColor}">自动生成</span>
                ，起始ID为100000000📈，不可修改🔒，每个账号都有独特的UID🆔，若账号被删除，新创建的账号将
                <span class="strong" :style="{color: dangerColor}">不会重复使用被删除的UID🔄</span>
                。</p>
              <p class="strong">2. 由于没有接入👛支付宝/微信支付API,所以
                <span class="strong" :style="{color: dangerColor}">所有👤用户的余额💰可以随意修改XD️，😏</span>
              </p>
              <p class="strong">3. 👤顾客编号CNO格式为C-UID后3位，如C123,
                <span class="strong" :style="{color: dangerColor}">👤顾客编号唯一⚠️，️</span>
                为方便查询和管理，不要使用其他格式🔎。</p>
              <p class="strong">4.
                <span class="strong" :style="{color: dangerColor}"> 📧用户名唯一，⚠️不可重复</span>
                。若添加或修改用户失败，请检查用户名是否已存在🔍。</p>
              <p class="strong">5. 📞电话可为字符串，如：+86-123-456-789📲。</p>
              <p class="strong">6. 📍地址需详细填写，如：广东省肇庆市端州区肇庆学院🏢。可以使用英文🌐。</p>
              <p class="strong">7. 📅
                日期不建议修改！这是用户注册时间⏲️。若必须修改，请按照yyyy-MM-dd格式，如：2021-01-01🗓️。</p>
              <p class="strong">8. 🔖除顾客编号，用户名，性别，权限必需以外，可填null💼，邮箱按CNO-NULL格式填写📧。</p>
              <p class="strong">9.
                <span class="strong" :style="{color: dangerColor}">🛠️管理员和经办人也属于顾客范畴👥</span>
                ，可使用管理员账号和经办人账号进行与顾客相同的操作，如药品购买💊。</p>
            </div>
          </el-card>
        </div>
      </el-dialog>

      <el-table ref="userTable" :data="tableData" stripe style="width: 100%" :height="tableHeight"
                v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="uid" label="用户编号" width="100" fixed="left"></el-table-column>
        <el-table-column prop="cno" label="顾客编号" width="100" fixed="left"></el-table-column>
        <el-table-column prop="username" label="用户名" width="100"></el-table-column>
        <el-table-column prop="authority" label="权限" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.authority === 1" type="danger" effect="dark">管理员</el-tag>
            <el-tag v-else-if="scope.row.authority === 2" type="success" effect="dark">经办人</el-tag>
            <el-tag v-else-if="scope.row.authority === 3" effect="dark">顾客</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
        <el-table-column prop="cname" label="姓名" width="100"></el-table-column>
        <el-table-column prop="csex" label="性别" width="100">
          <template slot-scope="scope">
            {{ getGenderText(scope.row.csex) }}
          </template>
        </el-table-column>
        <el-table-column prop="cage" label="年龄" width="100"></el-table-column>
        <el-table-column prop="caddress" label="地址" width="220"></el-table-column>
        <el-table-column prop="cphone" label="电话" width="130"></el-table-column>
        <el-table-column prop="csymptom" label="症状" width="130"></el-table-column>
        <el-table-column prop="cdate" label="创建日期" width="260"></el-table-column>
        <el-table-column prop="cremark" label="备注" width="200"></el-table-column>
        <el-table-column prop="balance" label="余额" width="100"></el-table-column>
        <el-table-column fixed="right" label="操作" width="120">
          <template slot-scope="scope">
            <el-button
                @click="openEdit(scope.row.uid)"
                type="text"
                size="small">
              编辑
            </el-button>
            <el-button
                @click="clearSelection();open(scope.row.uid)"
                type="text"
                size="small">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <br>
    <div class="pagination-container">
      <div>
        <span style="margin-right: 10px">每页显示条数</span>
        <el-select v-model="search.pageSize"
                   placeholder="请选择"
                   @change="handlePageSizeChange"
                   style="width: 70px">
          <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          >
          </el-option>
        </el-select>
      </div>
      <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="search.pageSize"
          :current-page="search.page"
          @current-change="handlePageChange"
      >
      </el-pagination>
    </div>
  </div>
</template>

<style>
.pagination-container {
  display: flex;
  justify-content: center;
}

.fixed-height-card {
  height: 80%;
  overflow: hidden;
}

.strong {
  font-weight: bold;
}

.marginWidth {
  margin-bottom: 30px;
}
</style>
