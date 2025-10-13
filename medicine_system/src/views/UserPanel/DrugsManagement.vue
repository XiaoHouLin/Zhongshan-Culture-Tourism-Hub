<script>
import {apiRear} from "@/axios";

const validateMno = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入药物编号'));
  } else {
    callback();
  }
};
const validateMname = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入药物名'));
  } else {
    callback();
  }
};
const validateMtype = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入药物分类'));
  } else {
    callback();
  }
};
const validateMode = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入使用方式'));
  } else {
    callback();
  }
};
const validateMefficacy = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入药效'));
  } else {
    callback();
  }
};
const validateMprice = (rule, value, callback) => {
  if (value === '') {
    return callback(new Error('请输入药物价格'));
  }
      // else if (isNaN(value) || value <= 0) {
  //   callback(new Error('价格必须为正数'));
  else {
    callback();
  }
};
const validateMnum = (rule, value, callback) => {
  if (value === '') {
    return callback(new Error('请输入药物数量'));
  }
      // else if (!Number.isInteger(value)) {
  //   callback(new Error('库存必须为整数'));
  else {
    callback();
  }
};

export default {
  name: 'DrugsManagement',
  data() {
    return {
      tableData: [],
      total: 0,
      mno: [],
      value: '',
      dialogTitle: '',
      rangeMprice: [0, 1000],
      rangeMnum: [0, 50000],
      dialogFormVisible: false,
      dialogUpdateAddDrugsVisible: false,
      searchLabelWidth: '120px',
      tableHeight: '86vh',
      loading: true,
      defaultTagColor: '#6d1677',
      dangerColor: '#e83a3a',
      changeDrug: {},
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
        mno: '',
        mname: '',
        mmode: '',
        mefficacy: '',
        mtype: '',
        minMprice: null,
        maxMprice: null,
        minMnum: null,
        maxMnum: null
      },
      rules: {
        mno: [{required: true, message: '请输入药物编号', validator: validateMno, trigger: 'blur'}],
        mname: [{required: true, message: '请输入药物名', validator: validateMname, trigger: 'blur'}],
        mtype: [{required: true, message: '请输入药物分类', validator: validateMtype, trigger: 'blur'}],
        mmode: [{required: true, message: '请输入使用方式', validator: validateMode, trigger: 'blur'}],
        mefficacy: [{required: true, message: '请输入药物功效', validator: validateMefficacy, trigger: 'blur'}],
        mprice: [{required: true, message: '请输入药物价格', validator: validateMprice, trigger: 'blur'}],
        mnum: [{required: true, message: '请输入药物数量', validator: validateMnum, trigger: 'blur'}]
      },
    }
  },
  computed: {
    isEditInvalid() {
      return !this.changeDrug.mno || !this.changeDrug.mname || !this.changeDrug.mtype ||
          !this.changeDrug.mmode || !this.changeDrug.mefficacy || !this.changeDrug.mprice || !this.changeDrug.mnum;
    }
  },
  methods: {
    clearSelection() { //清空多选
      this.$refs.drugsTable.clearSelection();
    },
    confirmClickHandler() {
      if (this.dialogTitle === '编辑') {
        return this.updateDrugs();
      } else if (this.dialogTitle === '添加药物') {
        return this.insertDrugs();
      }
      return () => {
        console.log('未定义的value')
      };
    },
    open(mno) {
      this.mno.push(mno);
      this.deleteChangeDrugs();
    },
    openEdit(mno) {
      this.dialogTitle = '编辑';
      this.mno = [];
      this.clearSelection();
      this.mno.push(mno);
      this.selectDrug();
      this.dialogUpdateAddDrugsVisible = true;
    },
    openAdd() {
      this.dialogTitle = '添加药物';
      this.dialogUpdateAddDrugsVisible = true;
    },
    onDialogOpen() {
      // 对话框打开时添加键盘事件监听器
      document.addEventListener('keydown', this.handleKeydown);
    },
    onDialogClose() {
      // 对话框关闭时移除键盘事件监听器
      document.removeEventListener('keydown', this.handleKeydown);
      this.cancelEditOrAdd();
    },
    handleKeydown(event) {
      if (event.key === 'Escape' || event.key === 'Esc') {
        this.cancelEditOrAdd();
      }
    },
    cancelEditOrAdd() {
      this.mno = [];
      this.rules = {
        mno: [{required: true, message: '请输入药物编号', validator: validateMno, trigger: 'blur'}],
        mname: [{required: true, message: '请输入药物名', validator: validateMname, trigger: 'blur'}],
        mtype: [{required: true, message: '请输入药物分类', validator: validateMtype, trigger: 'blur'}],
        mmode: [{required: true, message: '请输入使用方式', validator: validateMode, trigger: 'blur'}],
        mefficacy: [{required: true, message: '请输入药物功效', validator: validateMefficacy, trigger: 'blur'}],
        mprice: [{required: true, message: '请输入药物价格', validator: validateMprice, trigger: 'blur'}],
        mnum: [{required: true, message: '请输入药物数量', validator: validateMnum, trigger: 'blur'}]
      };
      this.changeDrug = {};
      this.dialogUpdateAddDrugsVisible = false;
    },
    selectDrug() {
      apiRear.get(`/drugsManagement/${this.mno}`).then(edit => {
        this.changeDrug = edit.data.data;
      }).catch(err => {
        console.log(err);
      });
    },
    // TODO : 先查询药品编号是否重复，再更新信息
    async updateDrugs() {
      try {
        console.log("update");
        const response = await apiRear.post(`/drugsManagement/${this.changeDrug.mno}&${this.mno}`);
        if (response.data.code === 0) {
          this.$message({
            type: 'error',
            message: '药物编号与其他药物编号重复，请重新输入！'
          })
        } else {
          this.$set(this.changeDrug, 'oldMno', this.mno.at(0));
          apiRear.post(`/drugsManagement/update`, this.changeDrug)
              .then(() => {
                this.fetchData();
                this.$message({
                  type: 'success',
                  message: '更改成功!'
                });
                this.dialogUpdateAddDrugsVisible = false;
                this.changeDrug = {};
                this.loading = true;
              }).catch(err => {
            console.log(err);
            this.$message({
              type: 'error',
              message: '更改药物信息失败！请检查格式是否正确！'
            });
          });
        }
      } catch (error) {
        console.error('未知错误:', error);
        this.$message.error('发生未知错误，请稍后再试');
      }
    },
    async insertDrugs() {
      try {
        console.log("insert");
        const response = await apiRear.post(`/drugsManagement/${this.changeDrug.mno}`);
        if (response.data.code === 0) {
          this.$message({
            type: 'error',
            message: '药物编号与其他药物编号重复，请重新输入！'
          })
        } else {
          apiRear.post(`/drugsManagement/insert`, this.changeDrug)
              .then(() => {
                this.fetchData();
                this.$message({
                  type: 'success',
                  message: '添加药物成功!'
                });
                this.dialogUpdateAddDrugsVisible = false;
                this.changeDrug = {};
                this.loading = true;
              }).catch(err => {
            console.log(err);
            this.$message({
              type: 'error',
              message: '添加药物失败！请检查格式是否正确！'
            });
          });
        }
      } catch (error) {
        console.error('未知错误:', error);
        this.$message.error('发生未知错误，请稍后再试');
      }
    },
    deleteChangeDrugs() {
      this.$confirm('此操作将永久删除所选择药物, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.delete(this.mno);
      }).catch(() => {
        this.mno = [];
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      })
    },
    searchForm() {
      this.dialogFormVisible = true;
    },
    searchConfirm() {
      this.dialogFormVisible = false;
      this.search.page = 1;
      this.search.minMprice = this.rangeMprice[0];
      this.search.maxMprice = this.rangeMprice[1];
      this.search.minMnum = this.rangeMnum[0];
      this.search.maxMnum = this.rangeMnum[1];
      this.loading = true;
      this.fetchData();
    },
    refreshSearch() {
      this.search = {
        page: 1,
        pageSize: 20,
        mno: '',
        mname: '',
        mmode: '',
        mefficacy: '',
        mtype: '',
        minMprice: null,
        maxMprice: null,
        minMnum: null,
        maxMnum: null
      };
      this.rangeMprice = [0, 1000];
      this.rangeMnum = [0, 50000];
      this.loading = true;
      this.fetchData();
    },
    handleSelectionChange(selection) {
      this.mno = selection.map(item => item.mno);
    },
    fetchData() {
      apiRear.get(`/drugsManagement`, {params: this.search}).then(res => {
        this.tableData = res.data.data.rows;
        this.total = res.data.data.total;
        this.loading = false;
        this.$nextTick(() => {
          this.$refs.drugsTable.doLayout();
        });
      }).catch(err => {
        console.log(err);
        this.$message.error('未知错误,请重新登录后再试');
      });
    },
    delete(mno) {
        if (!mno) throw new Error('mno为空');
        apiRear.delete(`/drugsManagement/${mno}`).then(() => {
          this.fetchData();
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
        }).catch(err => {
          console.log(err);
          this.$message({
            type: 'error',
            message: '删除药物失败,请检查是否选中药物'
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
  },
  mounted() {
    this.fetchData();
  }
}
</script>

<template>
  <div style="height: 100vh;">
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
          <el-button type="danger" icon="el-icon-delete" @click="deleteChangeDrugs" style="margin: 10px">批量删除
          </el-button>
          <el-button type="success" icon="el-icon-plus" @click="openAdd" style="margin: 10px;">添加药物
          </el-button>
        </div>
      </div>
      <!--      查询按钮对话框-->
      <el-dialog title="查询" :visible.sync="dialogFormVisible" :close-on-click-modal="false" center width="1000px">
        <div style="display: flex">
          <div style="width: 50%">
            <el-form :model="search" style="width: 400px">
              <el-form-item label="药物编号" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.mno" autocomplete="off" suffix-icon="el-icon-s-order"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="药物名称" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.mname" autocomplete="off" suffix-icon="el-icon-notebook-2"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="分类" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.mtype" autocomplete="off" suffix-icon="el-icon-s-grid"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
            </el-form>
          </div>
          <div style="width: 50%">
            <el-form :model="search" style="width: 400px">
              <el-form-item label="使用方式" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.mmode" autocomplete="off" suffix-icon="el-icon-fork-spoon"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="药效" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.mefficacy" autocomplete="off" suffix-icon="el-icon-thumb"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="占位格" :label-width="searchLabelWidth" class="strong">
                <el-input autocomplete="off" suffix-icon="el-icon-circle-close"
                          placeholder="只是因为这空着不好看..."
                          :disabled="true"></el-input>
              </el-form-item>
            </el-form>
          </div>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-card shadow="hover">
            <span>价格区间(非必填):</span>
            <div class="block">
              <el-slider
                  v-model="rangeMprice"
                  range
                  :max="1000">
              </el-slider>
            </div>
          </el-card>
          <br><br>
          <el-card shadow="hover">
            <span>库存区间(非必填):</span>
            <div class="block">
              <el-slider
                  v-model="rangeMnum"
                  range
                  :max="50000">
              </el-slider>
            </div>
          </el-card>
          <br>
          <el-button @click="dialogFormVisible = false">取 消</el-button>
          <el-button type="primary" @click="searchConfirm()">确 定</el-button>
        </div>
      </el-dialog>

      <!--编辑药物对话框-->
      <el-dialog :title="dialogTitle" :visible.sync="dialogUpdateAddDrugsVisible" :close-on-click-modal="false" center
                 width="1000px" @open="onDialogOpen" @close="onDialogClose">
        <div style="display: flex">
          <div style="width: 50%">
            <el-form ref="drugRef" :model="changeDrug" :rules="rules" style="width: 400px">
              <el-form-item label="药物编号" prop="mno" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeDrug.mno" autocomplete="off" suffix-icon="el-icon-s-order"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="药物名称" prop="mname" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeDrug.mname" autocomplete="off" suffix-icon="el-icon-notebook-2"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="分类" prop="mtype" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeDrug.mtype" autocomplete="off" suffix-icon="el-icon-s-grid"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="占位格" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input autocomplete="off" suffix-icon="el-icon-circle-close"
                          placeholder="只是因为这空着不好看..."
                          :disabled="true"></el-input>
              </el-form-item>
            </el-form>
          </div>
          <div style="width: 50%">
            <el-form :model="changeDrug" :rules="rules" style="width: 400px">
              <el-form-item label="使用方式" prop="mmode" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeDrug.mmode" autocomplete="off" suffix-icon="el-icon-fork-spoon"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="药效" prop="mefficacy" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeDrug.mefficacy" autocomplete="off" suffix-icon="el-icon-thumb"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="价格" prop="mprice" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeDrug.mprice" autocomplete="off" suffix-icon="el-icon-s-finance"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="库存" prop="mnum" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="changeDrug.mnum" autocomplete="off" suffix-icon="el-icon-box"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
            </el-form>
          </div>
        </div>
        <div slot="footer" class="dialog-footer" style="padding-top: 0">
          <el-card shadow="hover" style="border-radius: 30px;background-color: #d7eafd">
            <div style="text-align: left">
              <h2>tips:</h2>
              <p class="strong">1.💊药物编号格式：命名格式为 D-分类-药品ID。编号需唯一，
                <span :style="{color: dangerColor}">不重复⚠️。</span>
                为了便于显示和查找，不建议使用其他格式。</p>
              <p class="strong">2. 📛药物名称和药效可以为任意文字🌈。药物名称允许重复，但不建议这么做💡。</p>
              <p class="strong">
                3.📂现有14种药物分类:
                解热镇痛药🧊
                抗感冒药🤧
                抗过敏药🌼
                止咳药🤒
                抗生素💉
                抗病毒药🦠
                抗真菌药🍄
                消化系统药物🍽️
                心血管系统药物❤️
                精神科药物🧠
                皮肤科药物🩹
                内分泌系统药物🔄
                免疫调节药物🛡️
                抗肿瘤药物🎗️。
                允许自定义药物分类，
                <span :style="{ color: defaultTagColor}">这是自定义药物分类tag标签的默认颜色🎨。</span>
              </p>
              <p class="strong">4.📝使用方式仅限
                <span :style="{color: dangerColor}">2个字符</span>
                ，如「外用」💧、「内服」💊等。</p>
              <p class="strong">5.
                <span :style="{color: dangerColor}">
                  💰价格上限为¥1000💸；库存上限为50000📦。
                </span>
              </p>
            </div>
          </el-card>
          <br>
          <el-button @click="cancelEditOrAdd">取 消</el-button>
          <el-button type="primary" @click="confirmClickHandler" :disabled="isEditInvalid">确 定</el-button>
        </div>
      </el-dialog>

      <el-table ref="drugsTable" :data="tableData" stripe style="width: 100%" :height="tableHeight"
                v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="mno" label="药物编号" width="170" fixed="left" sortable></el-table-column>
        <el-table-column prop="mname" label="药物名称" width="200" sortable></el-table-column>
        <el-table-column prop="mtype" label="分类" width="200" sortable>
          <template slot-scope="scope">
            <el-tag v-if="scope.row.mtype ==='解热镇痛药'"
                    type="danger"
                    effect="dark">解热镇痛
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '抗感冒药' || scope.row.mtype === '抗感冒'"
                    type="success"
                    effect="dark">抗感冒
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '抗过敏药' || scope.row.mtype === '抗过敏'"
                    type="warning"
                    effect="dark">抗过敏
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '止咳药' || scope.row.mtype === '止咳'"
                    style="border-color: #F97198FF"
                    color="#F97198FF"
                    effect="dark">止咳
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '抗生素' || scope.row.mtype === '抗生素'"
                    style="border-color: #9A61FBFF"
                    color="#9A61FBFF"
                    effect="dark">抗生素
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '抗病毒药' || scope.row.mtype === '抗病毒'"
                    style="border-color: #0075d0"
                    color="#0075d0"
                    effect="dark">抗病毒
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '抗真菌药' || scope.row.mtype === '抗真菌'"
                    style="border-color: #F9A03EFF"
                    color="#F9A03EFF"
                    effect="dark">抗真菌
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '消化系统药' || scope.row.mtype === '消化科'"
                    style="border-color: #8634ad"
                    color="#8634ad"
                    effect="dark">消化科
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '心血管系统药' || scope.row.mtype === '心血管'"
                    style="border-color: #dd4643"
                    color="#dd4643"
                    effect="dark">心血管
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '精神科药' || scope.row.mtype === '精神科'"
                    style="border-color: #41b782"
                    color="#41b782"
                    effect="dark">精神科
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '皮肤科药' || scope.row.mtype === '皮肤科'"
                    style="border-color: #c57cba"
                    color="#c57cba"
                    effect="dark">皮肤科
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '内分泌系统药' || scope.row.mtype === '内分泌'"
                    style="border-color: #347575"
                    color="#347575"
                    effect="dark">内分泌科
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '免疫调节药' || scope.row.mtype === '免疫调节科'"
                    style="border-color: #2b2d30"
                    color="#2b2d30"
                    effect="dark">免疫调节科
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '抗肿瘤药' || scope.row.mtype === '抗肿瘤'"
                    style="border-color: #cd158f"
                    color="#cd158f"
                    effect="dark">抗肿瘤
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '天上仙丹'"
                    style="border-color: #14388e"
                    color="#14388e"
                    effect="dark">天上仙丹
            </el-tag>
            <el-tag v-else-if="scope.row.mtype === '人间丹药'"
                    style="border-color: #604c33"
                    color="#604c33"
                    effect="dark">人间丹药
            </el-tag>
            <el-tag v-else
                    :style="{borderColor: defaultTagColor}"
                    color="#6d1677"
                    effect="dark">{{ scope.row.mtype }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mmode" label="使用方式" width="170" sortable></el-table-column>
        <el-table-column prop="mefficacy" label="药效" width="220" sortable></el-table-column>
        <el-table-column prop="mprice" label="￥价格(单位:人民币)" width="220" sortable></el-table-column>
        <el-table-column prop="mnum" label="库存" width="150" sortable></el-table-column>
        <el-table-column fixed="right" label="操作" width="120">
          <template slot-scope="scope">
            <el-button
                @click="openEdit(scope.row.mno)"
                type="text"
                size="small">
              编辑
            </el-button>
            <el-button
                @click="clearSelection();open(scope.row.mno)"
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
        <el-select v-model="this.search.pageSize"
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
  height: 95%;
  overflow: hidden;
}

.strong {
  font-weight: bold;
}

.marginWidth {
  margin-bottom: 30px;
}
</style>
