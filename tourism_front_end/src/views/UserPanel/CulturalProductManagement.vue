<script>
import {apiRear} from "@/axios";

const validateProductCode = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('文创产品编号未生成，请稍后重试'));
  } else {
    callback();
  }
};
const validateProductName = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入文创产品名'));
  } else {
    callback();
  }
};
const validateCategory = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请选择文创产品分类'));
  } else {
    callback();
  }
};
const validateDisplayMode = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请选择展示形式'));
  } else {
    callback();
  }
};
const validateHighlight = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入产品亮点'));
  } else {
    callback();
  }
};
const validatePrice = (rule, value, callback) => {
  if (value === '') {
    return callback(new Error('请输入文创产品价格'));
  }
  const numeric = Number(value);
  if (!Number.isFinite(numeric) || numeric < 0.01) {
    return callback(new Error('价格需为不小于0.01的数字'));
  }
  if (numeric > 99999) {
    return callback(new Error('价格上限为99999'));
  }
  callback();
};
const validateStock = (rule, value, callback) => {
  if (value === '') {
    return callback(new Error('请输入文创产品数量'));
  }
  const numeric = Number(value);
  if (!Number.isFinite(numeric) || numeric < 0) {
    return callback(new Error('库存需为不小于0的数字'));
  }
  callback();
};

export default {
  name: 'CulturalProductManagement',
  data() {
    return {
      tableData: [],
      total: 0,
      selectedCodes: [],
      value: '',
      dialogTitle: '',
      priceRange: [0, 99999],
      stockRange: [0, 999999],
      dialogFormVisible: false,
      productDialogVisible: false,
      searchLabelWidth: '120px',
      tableHeight: '86vh',
      realtimeRefreshTimer: null,
      realtimeHandler: null,
      loading: true,
      defaultTagColor: '#6d1677',
      dangerColor: '#e83a3a',
      categoryColorMap: {
        '国潮经典': '#2f6fed',
        '节庆限定': '#1bb6c7',
        '民俗非遗': '#e67e22',
        '山水生态': '#2e9d67',
        '艺术设计': '#8e63d3',
        '人气伴手礼': '#e2557a',
        '民族风情': '#c7792b',
        '户外主题': '#3aa2af',
        '铁路文创': '#5c6bc0',
        '邮轮精选': '#6d4c41',
        '海岛风光': '#2f8fd6',
        '夜游精品': '#8d6e63',
        '赛事主题': '#f06292'
      },
      editingProduct: {},
      categoryOptions: [],
      selectedCategories: [],
      categoryDeleteDialogVisible: false,
      categoryDeleteTarget: '',
      categoryDeleteUsage: [],
      uncategorizedDialogVisible: false,
      highlightDialogVisible: false,
      highlightTarget: 'edit',
      highlightDraft: '',
      newCategoryName: '',
      displayModeOptions: [
        {label: '线上发售', value: '线上'},
        {label: '线下预定', value: '线下'}
      ],
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
        productCode: '',
        productName: '',
        displayMode: '',
        highlight: '',
        category: '',
        categoryList: [],
        minPrice: null,
        maxPrice: null,
        minStock: null,
        maxStock: null,
        exactMatch: false
      },
      rules: {
        productCode: [{required: true, message: '文创产品编号未生成，请稍后重试', validator: validateProductCode, trigger: 'blur'}],
        productName: [{required: true, message: '请输入文创产品名', validator: validateProductName, trigger: 'blur'}],
        category: [{required: true, message: '请输入文创产品分类', validator: validateCategory, trigger: 'blur'}],
        displayMode: [{required: true, message: '请输入展示形式', validator: validateDisplayMode, trigger: 'blur'}],
        highlight: [{required: true, message: '请输入文创产品亮点', validator: validateHighlight, trigger: 'blur'}],
        price: [{required: true, message: '请输入文创产品价格', validator: validatePrice, trigger: 'blur'}],
        stock: [{required: true, message: '请输入文创产品数量', validator: validateStock, trigger: 'blur'}]
      },
    }
  },
  watch: {
    selectedCategories() {
      this.syncCategoryField();
      this.$nextTick(() => {
        if (this.$refs.drugRef) {
          this.$refs.drugRef.validateField('category');
        }
      });
    }
  },
  computed: {
    isEditInvalid() {
      const priceValue = Number(this.editingProduct.price);
      const hasPrice = Number.isFinite(priceValue) && priceValue >= 0.01;
      const hasStock = this.editingProduct.stock !== null && this.editingProduct.stock !== undefined && this.editingProduct.stock !== '';
      return !this.editingProduct.productCode || !this.editingProduct.productName || !this.editingProduct.category ||
          !this.editingProduct.displayMode || !this.editingProduct.highlight || !hasPrice || !hasStock;
    },
    uncategorizedProducts() {
      return (this.tableData || []).filter(item => !this.hasCategory(item.category));
    }
  },
  methods: {
    initRealtimeRefresh() {
      if (!this.$eventBus) {
        return;
      }
      this.realtimeHandler = () => {
        if (this.realtimeRefreshTimer) {
          return;
        }
        this.realtimeRefreshTimer = setTimeout(() => {
          this.realtimeRefreshTimer = null;
          this.fetchData();
          this.loadCategoryOptions();
        }, 600);
      };
      this.$eventBus.$on('realtime:global-refresh', this.realtimeHandler);
    },
    clearSelection() { //清空多选
      this.$refs.productsTable.clearSelection();
    },
    confirmClickHandler() {
      if (this.dialogTitle === '编辑') {
        return this.updateProduct();
      } else if (this.dialogTitle === '添加文创产品') {
        return this.createProduct();
      }
      return () => {
        console.log('未定义的value')
      };
    },
    open(productCode) {
      this.selectedCodes = [productCode];
      this.confirmDeleteSelected();
    },
    openEdit(productCode) {
      this.dialogTitle = '编辑';
      this.selectedCodes = [];
      this.clearSelection();
      this.selectedCodes.push(productCode);
      this.loadProductForEdit();
      this.productDialogVisible = true;
    },
    openAdd() {
      this.dialogTitle = '添加文创产品';
      this.editingProduct = {displayMode: '线上', price: 0.01, stock: 0};
      this.selectedCategories = [];
      this.selectedCodes = [];
      this.productDialogVisible = true;
      this.fetchNextProductCode();
      this.$nextTick(() => {
        if (this.$refs.drugRef) {
          this.$refs.drugRef.clearValidate();
        }
        if (this.$refs.drugRefSecondary) {
          this.$refs.drugRefSecondary.clearValidate();
        }
      });
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
      this.selectedCodes = [];
      this.rules = {
        productCode: [{required: true, message: '文创产品编号未生成，请稍后重试', validator: validateProductCode, trigger: 'blur'}],
        productName: [{required: true, message: '请输入文创产品名', validator: validateProductName, trigger: 'blur'}],
        category: [{required: true, message: '请输入文创产品分类', validator: validateCategory, trigger: 'blur'}],
        displayMode: [{required: true, message: '请输入展示形式', validator: validateDisplayMode, trigger: 'blur'}],
        highlight: [{required: true, message: '请输入文创产品亮点', validator: validateHighlight, trigger: 'blur'}],
        price: [{required: true, message: '请输入文创产品价格', validator: validatePrice, trigger: 'blur'}],
        stock: [{required: true, message: '请输入文创产品数量', validator: validateStock, trigger: 'blur'}]
      };
      this.editingProduct = {};
      this.selectedCategories = [];
      this.productDialogVisible = false;
    },
    loadProductForEdit() {
      const code = this.selectedCodes[0];
      if (!code) {
        return;
      }
      apiRear.get(`/cultural-products/${code}`).then(edit => {
        this.editingProduct = edit.data.data;
        const priceValue = Number(this.editingProduct.price);
        const stockValue = Number(this.editingProduct.stock);
        this.editingProduct.price = Number.isFinite(priceValue) ? Math.max(0.01, priceValue) : 0.01;
        this.editingProduct.stock = Number.isFinite(stockValue) ? stockValue : 0;
        this.syncSelectedCategories();
        this.$nextTick(() => {
          if (this.$refs.drugRef) {
            this.$refs.drugRef.clearValidate();
          }
          if (this.$refs.drugRefSecondary) {
            this.$refs.drugRefSecondary.clearValidate();
          }
        });
      }).catch(err => {
        console.log(err);
      });
    },
    async fetchNextProductCode() {
      try {
        const res = await apiRear.get('/cultural-products/next-code');
        if (res && res.data && res.data.data) {
          this.$set(this.editingProduct, 'productCode', res.data.data);
        }
      } catch (err) {
        console.log(err);
        this.$message.error('获取文创产品编号失败，请稍后重试');
      }
    },
    async loadCategoryOptions() {
      try {
        const res = await apiRear.get('/cultural-products/categories');
        const list = (res && res.data && Array.isArray(res.data.data)) ? res.data.data : [];
        this.mergeCategoryOptions(list);
      } catch (err) {
        console.log(err);
      }
    },
    mergeCategoryOptions(list) {
      const normalized = (list || [])
          .map(item => (item || '').trim())
          .filter(Boolean);
      const set = new Set(this.categoryOptions);
      normalized.forEach(item => {
        item.split(/[,，、]/).forEach(part => {
          const name = part.trim();
          if (name) {
            set.add(name);
          }
        });
      });
      this.categoryOptions = Array.from(set);
    },
    normalizeCategoryList(raw) {
      if (!raw) {
        return [];
      }
      return String(raw).split(/[,，、]/).map(item => item.trim()).filter(Boolean);
    },
    hasCategory(raw) {
      return this.normalizeCategoryList(raw).length > 0;
    },
    syncSelectedCategories() {
      const raw = this.editingProduct.category || '';
      const parts = this.normalizeCategoryList(raw);
      this.selectedCategories = parts;
      this.mergeCategoryOptions(parts);
    },
    syncCategoryField() {
      const categories = (this.selectedCategories || [])
          .map(item => item.trim())
          .filter(Boolean);
      this.editingProduct.category = categories.join('、');
    },
    getCategoryUsage(label) {
      const target = (label || '').trim();
      if (!target) {
        return [];
      }
      return (this.tableData || []).filter(item => {
        const categories = this.normalizeCategoryList(item.category);
        return categories.includes(target);
      });
    },
    requestDeleteCategory(label) {
      const target = (label || '').trim();
      if (!target) {
        return;
      }
      this.$confirm(`确认删除分类“${target}”？`, '删除分类', {
        confirmButtonText: '继续',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.categoryDeleteTarget = target;
        this.categoryDeleteUsage = this.getCategoryUsage(target);
        this.categoryDeleteDialogVisible = true;
      }).catch(() => {});
    },
    async confirmDeleteCategory() {
      const target = this.categoryDeleteTarget;
      if (!target) {
        return;
      }
      const affected = [...this.categoryDeleteUsage];
      const updates = affected.map(item => {
        const categories = this.normalizeCategoryList(item.category).filter(label => label !== target);
        const newCategory = categories.join('、');
        const payload = {
          ...item,
          category: newCategory,
          oldProductCode: item.productCode
        };
        return apiRear.post(`/cultural-products/update`, payload).then(() => {
          item.category = newCategory;
        });
      });
      const failures = [];
      await Promise.all(updates.map(p => p.catch(err => failures.push(err))));
      this.categoryOptions = this.categoryOptions.filter(item => item !== target);
      this.selectedCategories = (this.selectedCategories || []).filter(item => item !== target);
      this.categoryDeleteDialogVisible = false;
      this.categoryDeleteTarget = '';
      this.categoryDeleteUsage = [];
      if (failures.length) {
        this.$message.error('部分产品更新失败，请稍后重试');
      } else {
        this.$message.success('分类已删除并同步更新关联产品');
      }
    },
    openUncategorizedDialog() {
      this.uncategorizedDialogVisible = true;
    },
    getCategoryLabels(raw) {
      if (!raw) {
        return [];
      }
      return raw.split(/[,，、]/).map(item => item.trim()).filter(Boolean);
    },
    getCategoryColor(label) {
      if (!label) {
        return this.defaultTagColor;
      }
      if (this.categoryColorMap[label]) {
        return this.categoryColorMap[label];
      }
      return this.hashCategoryColor(label);
    },
    hashCategoryColor(label) {
      let hash = 0;
      for (let i = 0; i < label.length; i += 1) {
        hash = (hash * 31 + label.charCodeAt(i)) >>> 0;
      }
      const hue = hash % 360;
      return `hsl(${hue}, 55%, 45%)`;
    },
    getCategoryTagStyle(label) {
      const color = this.getCategoryColor(label);
      return {
        backgroundColor: color,
        borderColor: color,
        color: '#fff'
      };
    },
    addCategoryOption() {
      this.$prompt('请输入新分类名称', '新建分类', {
        confirmButtonText: '保存',
        cancelButtonText: '取消',
        inputPattern: /^.{1,20}$/,
        inputErrorMessage: '分类名称不能为空且不超过20字'
      }).then(({ value }) => {
        const name = (value || '').trim();
        if (!name) {
          return;
        }
        if (!this.categoryOptions.includes(name)) {
          this.categoryOptions.push(name);
        }
        if (!this.selectedCategories.includes(name)) {
          this.selectedCategories.push(name);
        }
      }).catch(() => {});
    },
    openHighlightDialog() {
      this.highlightTarget = 'edit';
      this.highlightDraft = this.editingProduct.highlight || '';
      this.highlightDialogVisible = true;
    },
    openSearchHighlightDialog() {
      this.highlightTarget = 'search';
      this.highlightDraft = this.search.highlight || '';
      this.highlightDialogVisible = true;
    },
    confirmHighlight() {
      const value = (this.highlightDraft || '').trim();
      if (this.highlightTarget === 'search') {
        this.search.highlight = value;
      } else {
        this.editingProduct.highlight = value;
      }
      this.highlightDialogVisible = false;
    },
    // TODO : 先查询文创品编号是否重复，再更新信息
    async updateProduct() {
      try {
        console.log("update");
        this.syncCategoryField();
        const originalCode = this.selectedCodes[0];
        const response = await apiRear.post(`/cultural-products/validate/${this.editingProduct.productCode}&${originalCode}`);
        if (response.data.code === 0) {
          this.$message({
            type: 'error',
            message: '文创产品编号与其他文创产品编号重复，请重新输入！'
          })
        } else {
          this.$set(this.editingProduct, 'oldProductCode', originalCode);
          apiRear.post(`/cultural-products/update`, this.editingProduct)
              .then(() => {
                this.fetchData();
                this.$message({
                  type: 'success',
                  message: '更改成功!'
                });
                this.productDialogVisible = false;
                this.editingProduct = {};
                this.selectedCodes = [];
                this.loading = true;
              }).catch(err => {
            console.log(err);
            this.$message({
              type: 'error',
              message: '更改文创产品信息失败！请检查格式是否正确！'
            });
          });
        }
      } catch (error) {
        console.error('未知错误:', error);
        this.$message.error('发生未知错误，请稍后再试');
      }
    },
    async createProduct() {
      try {
        console.log("insert");
        this.syncCategoryField();
        if (!this.editingProduct.productCode) {
          await this.fetchNextProductCode();
        }
        apiRear.post(`/cultural-products`, this.editingProduct)
            .then(() => {
              this.fetchData();
              this.$message({
                type: 'success',
                message: '添加文创产品成功!'
              });
              this.productDialogVisible = false;
              this.editingProduct = {};
              this.selectedCodes = [];
              this.loading = true;
            }).catch(err => {
          console.log(err);
          this.$message({
            type: 'error',
            message: '添加文创产品失败！请检查格式是否正确！'
          });
        });
      } catch (error) {
        console.error('未知错误:', error);
        this.$message.error('发生未知错误，请稍后再试');
      }
    },
    async confirmDeleteSelected() {
      if (this.selectedCodes.length === 0) {
        this.$message.warning('请先选择文创产品');
        return;
      }
      this.$confirm('此操作将永久删除所选择文创产品, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const payingUsers = await this.getPendingPayingUsersCount(this.selectedCodes);
        if (payingUsers > 0) {
          this.$confirm(`当前有${payingUsers}名用户正在支付中，确认删除吗？`, '二次确认', {
            confirmButtonText: '继续删除',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.deleteProducts(this.selectedCodes);
          }).catch(() => {
            this.$message({
              type: 'info',
              message: '已取消删除'
            });
          });
          return;
        }
        this.deleteProducts(this.selectedCodes);
      }).catch(() => {
        this.selectedCodes = [];
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      })
    },
    async getPendingPayingUsersCount(productCodes) {
      if (!productCodes || productCodes.length === 0) {
        return 0;
      }
      try {
        const res = await apiRear.get('/cultural-products/delete-check', {
          params: {productCodes: productCodes.join(',')}
        });
        return res && res.data && res.data.data ? Number(res.data.data.payingUsers) || 0 : 0;
      } catch (error) {
        return 0;
      }
    },
    searchForm() {
      this.dialogFormVisible = true;
    },
    searchConfirm() {
      this.dialogFormVisible = false;
      this.search.page = 1;
      this.search.minPrice = this.priceRange[0];
      this.search.maxPrice = this.priceRange[1];
      this.search.minStock = this.stockRange[0];
      this.search.maxStock = this.stockRange[1];
      this.loading = true;
      this.fetchData();
    },
    refreshSearch() {
      this.search = {
        page: 1,
        pageSize: 20,
        productCode: '',
        productName: '',
        displayMode: '',
        highlight: '',
        category: '',
        categoryList: [],
        minPrice: null,
        maxPrice: null,
        minStock: null,
        maxStock: null,
        exactMatch: false
      };
      this.priceRange = [0, 99999];
      this.stockRange = [0, 999999];
      this.loading = true;
      this.fetchData();
    },
    normalizeRange(type) {
      const range = type === 'price' ? this.priceRange : this.stockRange;
      let minValue = Number(range[0]);
      let maxValue = Number(range[1]);
      if (!Number.isFinite(minValue)) {
        minValue = 0;
      }
      if (!Number.isFinite(maxValue)) {
        maxValue = 0;
      }
      if (minValue > maxValue) {
        const temp = minValue;
        minValue = maxValue;
        maxValue = temp;
      }
      range[0] = minValue;
      range[1] = maxValue;
    },
    handleSelectionChange(selection) {
      this.selectedCodes = selection.map(item => item.productCode);
    },
    fetchData() {
      const params = {
        ...this.search,
        categoryList: (this.search.categoryList || []).join('、')
      };
      apiRear.get(`/cultural-products`, {params}).then(res => {
        this.tableData = res.data.data.rows;
        this.total = res.data.data.total;
        this.loading = false;
        this.$nextTick(() => {
          this.$refs.productsTable.doLayout();
        });
      }).catch(err => {
        console.log(err);
        this.$message.error('未知错误,请重新登录后再试');
      });
    },
    deleteProducts(productCodes) {
        if (!productCodes || productCodes.length === 0) throw new Error('productCodes为空');
        apiRear.delete(`/cultural-products/${productCodes}`).then(() => {
          this.fetchData();
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
          this.selectedCodes = [];
        }).catch(err => {
          console.log(err);
          this.$message({
            type: 'error',
            message: '删除文创产品失败,请检查是否选中文创产品'
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
    this.loadCategoryOptions();
    this.initRealtimeRefresh();
  },
  beforeDestroy() {
    if (this.realtimeHandler && this.$eventBus) {
      this.$eventBus.$off('realtime:global-refresh', this.realtimeHandler);
    }
    if (this.realtimeRefreshTimer) {
      clearTimeout(this.realtimeRefreshTimer);
      this.realtimeRefreshTimer = null;
    }
  }
}
</script>

<template>
  <div class="panel-theme cultural-management">
    <el-card shadow="always" class="fixed-height-card panel-card">
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
          <el-button type="danger" icon="el-icon-delete" @click="confirmDeleteSelected" style="margin: 10px">批量删除
          </el-button>
          <el-button type="success" icon="el-icon-plus" @click="openAdd" style="margin: 10px;">添加文创产品
          </el-button>
        </div>
      </div>
      <el-alert
          v-if="uncategorizedProducts.length"
          type="warning"
          show-icon
          class="uncategorized-alert"
      >
        <span slot="title">
          当前有 {{ uncategorizedProducts.length }} 个文创产品没有分类。
          <el-button type="text" @click="openUncategorizedDialog">查看详情</el-button>
        </span>
      </el-alert>
      <!--      查询按钮对话框-->
      <el-dialog title="查询" :visible.sync="dialogFormVisible" :close-on-click-modal="false" center width="1000px">
        <div class="exact-match-toggle" :style="{ paddingLeft: searchLabelWidth }">
          <span class="strong">精确查询</span>
          <el-tooltip content="区分大小写，完全匹配" placement="bottom">
            <el-button size="mini" :type="search.exactMatch ? 'primary' : 'info'"
                       @click="search.exactMatch = !search.exactMatch">
              {{ search.exactMatch ? '已开启' : '已关闭' }}
            </el-button>
          </el-tooltip>
        </div>
        <el-form :model="search" class="search-dialog-form" :label-width="searchLabelWidth">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="文创产品编号" class="strong">
                <el-input v-model="search.productCode" autocomplete="off" suffix-icon="el-icon-s-order"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
              <el-form-item label="文创产品名称" class="strong">
                <el-input v-model="search.productName" autocomplete="off" suffix-icon="el-icon-notebook-2"
                          placeholder="请输入内容,可为空"
                          clearable></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="展示形式" class="strong">
                <el-radio-group v-model="search.displayMode" class="display-mode-group">
                  <el-radio :label="'线上'">线上发售</el-radio>
                  <el-radio :label="'线下'">线下预定</el-radio>
                  <el-button type="text" @click="search.displayMode = ''">清除</el-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="产品亮点" class="strong">
                <el-input v-model="search.highlight" readonly placeholder="点击右侧按钮编辑">
                  <el-button slot="append" icon="el-icon-edit" @click="openSearchHighlightDialog">编辑</el-button>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="24">
              <el-form-item label="分类" class="strong">
                <el-checkbox-group v-model="search.categoryList" class="search-category-group">
                  <el-checkbox v-for="item in categoryOptions" :key="item" :label="item">
                    {{ item }}
                  </el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="价格区间" class="strong">
                <div class="range-panel">
                  <el-slider v-model="priceRange" range :max="99999"></el-slider>
                  <div class="range-inputs">
                    <el-input-number v-model="priceRange[0]" :min="0" :max="99999" @change="normalizeRange('price')"></el-input-number>
                    <span>至</span>
                    <el-input-number v-model="priceRange[1]" :min="0" :max="99999" @change="normalizeRange('price')"></el-input-number>
                  </div>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="库存区间" class="strong">
                <div class="range-panel">
                  <el-slider v-model="stockRange" range :max="999999"></el-slider>
                  <div class="range-inputs">
                    <el-input-number v-model="stockRange[0]" :min="0" :max="999999" @change="normalizeRange('stock')"></el-input-number>
                    <span>至</span>
                    <el-input-number v-model="stockRange[1]" :min="0" :max="999999" @change="normalizeRange('stock')"></el-input-number>
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取 消</el-button>
          <el-button type="primary" @click="searchConfirm()">确 定</el-button>
        </div>
      </el-dialog>

      <!--编辑文创产品对话框-->
      <el-dialog :title="dialogTitle" :visible.sync="productDialogVisible" :close-on-click-modal="false" center
                 width="1000px" @open="onDialogOpen" @close="onDialogClose">
        <div style="display: flex">
          <div style="width: 50%">
            <el-form ref="drugRef" :model="editingProduct" :rules="rules" style="width: 400px">
              <el-form-item label="文创产品编号" prop="productCode" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="editingProduct.productCode" autocomplete="off" suffix-icon="el-icon-s-order"
                          placeholder="系统自动生成" :disabled="true"></el-input>
              </el-form-item>
              <el-form-item label="文创产品名称" prop="productName" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="editingProduct.productName" autocomplete="off" suffix-icon="el-icon-notebook-2"
                          placeholder="请输入内容"
                ></el-input>
              </el-form-item>
              <el-form-item label="分类" prop="category" :label-width="searchLabelWidth" class="strong marginWidth">
                <div class="category-select">
                  <el-checkbox-group v-model="selectedCategories">
                    <el-checkbox v-for="item in categoryOptions" :key="item" :label="item">
                      <span>{{ item }}</span>
                      <i class="el-icon-close category-delete-icon" @click.stop="requestDeleteCategory(item)"></i>
                    </el-checkbox>
                  </el-checkbox-group>
                  <el-button type="text" class="category-add-btn" @click="addCategoryOption">+ 新建分类</el-button>
                </div>
              </el-form-item>
            </el-form>
          </div>
          <div style="width: 50%">
            <el-form ref="drugRefSecondary" :model="editingProduct" :rules="rules" style="width: 400px">
              <el-form-item label="展示形式" prop="displayMode" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-radio-group v-model="editingProduct.displayMode" class="display-mode-group">
                  <el-radio v-for="item in displayModeOptions" :key="item.value" :label="item.value">
                    {{ item.label }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="产品亮点" prop="highlight" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input v-model="editingProduct.highlight" readonly placeholder="点击右侧按钮编辑">
                  <el-button slot="append" icon="el-icon-edit" @click="openHighlightDialog">编辑</el-button>
                </el-input>
              </el-form-item>
              <el-form-item label="价格" prop="price" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input-number v-model="editingProduct.price" :min="0.01" :max="99999" :step="0.01" :precision="2"
                                 controls-position="right" class="full-width-number"></el-input-number>
              </el-form-item>
              <el-form-item label="库存" prop="stock" :label-width="searchLabelWidth" class="strong marginWidth">
                <el-input-number v-model="editingProduct.stock" :min="0"
                                 controls-position="right" class="full-width-number"></el-input-number>
              </el-form-item>
            </el-form>
          </div>
        </div>
        <div slot="footer" class="dialog-footer" style="padding-top: 0">
          <el-card shadow="hover" class="tips-card">
            <div style="text-align: left">
              <h2>tips:</h2>
              <p class="strong">1.🏷️ 编号格式建议使用 TP-分类-编号，务必
                <span :style="{color: dangerColor}">保持唯一</span>
                以便快速定位产品。</p>
              <p class="strong">2.🖋️ 文创产品名称与亮点可自由撰写，鼓励突出地域文化或旅游故事。</p>
              <p class="strong">
                3.🗂️ 推荐分类：城市地标🏯、民俗非遗🧧、山水生态🛶、艺术设计🎨、人气伴手礼🍵、民族风情🎎、户外主题⛰️、铁路文创🚂、邮轮精选🛳️、海岛风光🏖️、夜游精品🌌、赛事主题🏟️、国潮经典🏰、节庆限定🌸。
                也支持自定义分类，
                <span :style="{ color: defaultTagColor}">自定义标签默认采用此配色。</span>
              </p>
              <p class="strong">4.🎭 展示形式建议使用「线上发售 / 线下限定」，便于前端展示。</p>
              <p class="strong">
                5.<span :style="{color: dangerColor}">💰 价格区间为¥0.01-¥99999，库存不设上限。</span>
              </p>
            </div>
          </el-card>
          <br>
          <el-button @click="cancelEditOrAdd">取 消</el-button>
          <el-button type="primary" @click="confirmClickHandler" :disabled="isEditInvalid">确 定</el-button>
        </div>
      </el-dialog>

      <el-dialog title="产品亮点" :visible.sync="highlightDialogVisible" width="640px" center>
        <el-input type="textarea" v-model="highlightDraft" :rows="8" maxlength="500"
                  show-word-limit placeholder="请输入产品亮点内容"></el-input>
        <span slot="footer" class="dialog-footer">
          <el-button @click="highlightDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmHighlight">保存</el-button>
        </span>
      </el-dialog>

      <el-dialog title="删除分类确认" :visible.sync="categoryDeleteDialogVisible" width="680px" center>
        <div class="category-delete-body">
          <p>即将删除分类：<strong>{{ categoryDeleteTarget }}</strong></p>
          <p>当前有 <strong>{{ categoryDeleteUsage.length }}</strong> 个文创正在使用该分类。</p>
          <el-table v-if="categoryDeleteUsage.length" :data="categoryDeleteUsage" size="mini" style="width: 100%">
            <el-table-column prop="productCode" label="编号" width="140"></el-table-column>
            <el-table-column prop="productName" label="名称"></el-table-column>
            <el-table-column label="操作" width="120">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="openEdit(scope.row.productCode)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <p v-else>没有产品使用该分类。</p>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="categoryDeleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDeleteCategory">坚持删除</el-button>
        </span>
      </el-dialog>

      <el-dialog title="未分类文创" :visible.sync="uncategorizedDialogVisible" width="720px" center>
        <div v-if="!uncategorizedProducts.length">暂无未分类文创产品。</div>
        <el-table v-else :data="uncategorizedProducts" size="mini" style="width: 100%">
          <el-table-column prop="productCode" label="编号" width="140"></el-table-column>
          <el-table-column prop="productName" label="名称"></el-table-column>
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="openEdit(scope.row.productCode)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
        <span slot="footer" class="dialog-footer">
          <el-button @click="uncategorizedDialogVisible = false">关闭</el-button>
        </span>
      </el-dialog>

      <el-table ref="productsTable" :data="tableData" stripe style="width: 100%" :height="tableHeight" class="modern-table"
                v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="productCode" label="文创产品编号" width="160" fixed="left" sortable></el-table-column>
        <el-table-column prop="productName" label="文创产品名称" min-width="200" sortable></el-table-column>
        <el-table-column prop="category" label="分类" min-width="160" sortable>
          <template slot-scope="scope">
            <div class="category-tags">
              <el-tag
                  v-for="(label, index) in getCategoryLabels(scope.row.category)"
                  :key="`${label}-${index}`"
                  :style="getCategoryTagStyle(label)"
                  effect="dark">
                {{ label }}
              </el-tag>
              <el-tag v-if="getCategoryLabels(scope.row.category).length === 0"
                      :style="getCategoryTagStyle('未分类')" effect="dark">
                未分类
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="displayMode" label="展示形式" width="120" sortable></el-table-column>
        <el-table-column prop="highlight" label="产品亮点" min-width="240" sortable></el-table-column>
        <el-table-column prop="price" label="售价(元)" width="120" sortable></el-table-column>
        <el-table-column prop="stock" label="库存" width="100" sortable></el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button
                @click="openEdit(scope.row.productCode)"
                type="text"
                size="small">
              编辑
            </el-button>
            <el-button
                @click="clearSelection();open(scope.row.productCode)"
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

.exact-match-toggle {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  margin-bottom: 12px;
  color: var(--ink-soft);
}

.panel-theme {
  --font-display: 'STZhongsong', 'FangSong', 'Songti SC', 'STSong', serif;
  --font-body: 'HarmonyOS Sans SC', 'Source Han Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --ink: #1c2c27;
  --ink-soft: #40534c;
  --accent: #c75a31;
  --teal: #1f7a6d;
  --paper: #ffffff;
  --shadow-soft: 0 20px 45px rgba(25, 45, 40, 0.12);
  min-height: 100%;
  padding: 8px 6px 28px;
  color: var(--ink);
  font-family: var(--font-body);
  background:
    radial-gradient(circle at 12% 10%, rgba(31, 122, 109, 0.12), transparent 45%),
    radial-gradient(circle at 85% 18%, rgba(199, 90, 49, 0.12), transparent 50%),
    repeating-linear-gradient(90deg, rgba(31, 122, 109, 0.05) 0, rgba(31, 122, 109, 0.05) 1px, transparent 1px, transparent 120px),
    repeating-linear-gradient(0deg, rgba(31, 122, 109, 0.04) 0, rgba(31, 122, 109, 0.04) 1px, transparent 1px, transparent 120px),
    linear-gradient(180deg, #f9f4ec 0%, #f5f9fb 55%, #ffffff 100%);
}

.panel-card {
  border-radius: 24px !important;
  border: 1px solid rgba(31, 122, 109, 0.12);
  box-shadow: var(--shadow-soft);
  background: var(--paper) !important;
  color: var(--ink);
}

.tips-card {
  border-radius: 20px !important;
  border: 1px solid rgba(31, 122, 109, 0.16);
  background: rgba(255, 255, 255, 0.94) !important;
  box-shadow: var(--shadow-soft);
}

.panel-theme ::v-deep .el-card {
  border: 1px solid rgba(31, 122, 109, 0.1);
}

.panel-theme ::v-deep .el-card__body {
  color: var(--ink);
}

.panel-theme ::v-deep .el-dialog {
  border-radius: 18px;
}

.panel-theme ::v-deep .el-dialog__body {
  padding-top: 16px;
}

.panel-theme ::v-deep .modern-table {
  border-radius: 16px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid rgba(31, 122, 109, 0.18);
  box-shadow: 0 12px 28px rgba(31, 56, 94, 0.12);
}

.panel-theme ::v-deep .modern-table .el-table__header th {
  background: linear-gradient(120deg, rgba(31, 122, 109, 0.18), rgba(199, 90, 49, 0.12));
  color: var(--ink);
  font-weight: 600;
}

.panel-theme ::v-deep .modern-table .el-table__body tr:hover > td {
  background: rgba(31, 122, 109, 0.08);
}

.panel-theme ::v-deep .modern-table::before {
  height: 0;
}

.panel-theme ::v-deep .modern-table .el-table__fixed,
.panel-theme ::v-deep .modern-table .el-table__fixed-right {
  background: #ffffff;
  box-shadow: none;
}

.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.category-tags .el-tag {
  border-radius: 12px;
  padding: 0 10px;
  transition: none !important;
}

.category-select {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-select ::v-deep .el-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
}

.category-delete-icon {
  margin-left: 6px;
  font-size: 12px;
  color: #d9534f;
  cursor: pointer;
}

.uncategorized-alert {
  margin: 0 10px 12px;
}

.category-add-btn {
  align-self: flex-start;
  padding: 0;
  color: var(--teal);
}

.display-mode-group {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
  min-height: 36px;
}

.full-width-number {
  width: 100%;
}

.range-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.search-category-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 16px;
}

.search-category-group ::v-deep .el-checkbox {
  margin-right: 0;
}

.search-dialog-form {
  width: 100%;
}

.search-dialog-form .el-form-item {
  margin-bottom: 16px;
}

.range-panel {
  padding: 8px 12px 10px;
  border-radius: 14px;
  border: 1px solid rgba(31, 122, 109, 0.18);
  background: rgba(31, 122, 109, 0.04);
}
</style>
