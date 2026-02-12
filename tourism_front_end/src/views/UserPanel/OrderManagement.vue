<script>
import { apiRear } from "@/axios";

const productImageContext = require.context('@/assets/culturalProducts', false, /\.png$/);
const productImages = productImageContext.keys().reduce((acc, key) => {
  const fileName = key.replace('./', '').replace(/\.png$/, '');
  acc[fileName] = productImageContext(key);
  return acc;
}, {});
const fallbackImage = productImages['1'];

export default {
  name: 'OrderManagement',
  data() {
    return {
      tableData: [],
      loading: true,
      dialogSearchVisible: false,
      detailDialogVisible: false,
      detailLoading: false,
      searchLabelWidth: '120px',
      tableHeight: '88vh',
      selectedOrderIds: [],
      page: 1,
      pageSize: 20,
      options: [
        { value: '20', label: '20' },
        { value: '40', label: '40' },
        { value: '60', label: '60' },
        { value: '80', label: '80' },
        { value: '100', label: '100' }
      ],
      search: {
        orderId: '',
        username: '',
        phone: '',
        pickupCode: '',
        dateRange: [],
        orderStatus: '',
        productName: '',
        productCode: '',
        exactMatch: false
      },
      statusOptions: [
        { value: 0, label: '待支付' },
        { value: 1, label: '待取货' },
        { value: 3, label: '已完成' },
        { value: 2, label: '已关闭' },
        { value: 4, label: '已退款' },
        { value: 5, label: '退款申请中' },
        { value: 6, label: '部分退款' }
      ],
      orderDetailsCache: {},
      selectedOrder: null,
      orderDetail: [],
      lastProductNameQuery: '',
      realtimeRefreshTimer: null,
      realtimeHandler: null
    };
  },
  computed: {
    filteredOrders() {
      return (this.tableData || []).filter(order => {
        if (!this.matchesBaseFilters(order)) {
          return false;
        }
        return this.matchesProductFilters(order);
      });
    },
    filteredTotal() {
      return this.filteredOrders.length;
    },
    pagedOrders() {
      const start = (this.page - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredOrders.slice(start, end);
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
        }, 600);
      };
      this.$eventBus.$on('realtime:global-refresh', this.realtimeHandler);
      this.$eventBus.$on('realtime:order-updated', this.realtimeHandler);
    },
    async fetchData() {
      this.loading = true;
      try {
        const res = await apiRear.get('/order/admin/list');
        this.tableData = Array.isArray(res.data.data) ? res.data.data : [];
        await this.preloadOrderDetailsForSearch();
      } catch (error) {
        this.$message.error('无法获取订单列表');
      } finally {
        this.loading = false;
        this.$nextTick(() => {
          if (this.$refs.ordersTable) {
            this.$refs.ordersTable.doLayout();
          }
        });
      }
    },
    searchForm() {
      this.dialogSearchVisible = true;
    },
    async searchConfirm() {
      this.page = 1;
      await this.preloadOrderDetailsForSearch();
      this.dialogSearchVisible = false;
    },
    refreshSearch() {
      this.search = {
        orderId: '',
        username: '',
        phone: '',
        pickupCode: '',
        dateRange: [],
        orderStatus: '',
        productName: '',
        productCode: '',
        exactMatch: false
      };
      this.page = 1;
      this.dialogSearchVisible = false;
    },
    async fetchProductNameSuggestions(queryString, cb) {
      const keyword = (queryString || '').trim();
      this.lastProductNameQuery = queryString || '';
      if (!keyword) {
        cb([]);
        return;
      }
      try {
        const res = await apiRear.get('/cultural-products', {
          params: {
            page: 1,
            pageSize: 20,
            productName: keyword
          }
        });
        const rows = (res && res.data && res.data.data && res.data.data.rows) ? res.data.data.rows : [];
        const seen = new Set();
        const suggestions = [];
        rows.forEach(item => {
          const name = item && item.productName ? String(item.productName) : '';
          if (name && !seen.has(name)) {
            seen.add(name);
            suggestions.push({ value: name });
          }
        });
        if (!suggestions.length) {
          cb([{ value: '暂无文创产品', isEmpty: true }]);
          return;
        }
        cb(suggestions);
      } catch (error) {
        cb([{ value: '暂无文创产品', isEmpty: true }]);
      }
    },
    handleProductNameSelect(item) {
      if (item && item.isEmpty) {
        this.search.productName = (this.lastProductNameQuery || '').trim();
      }
    },
    handleSelectionChange(selection) {
      this.selectedOrderIds = selection.map(item => item.orderId);
    },
    async confirmDeleteSelected() {
      if (!this.selectedOrderIds.length) {
        this.$message.info('请选择需要删除的订单');
        return;
      }
      try {
        await this.$confirm('删除订单后将无法恢复，是否继续？', '确认删除', {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning'
        });
        await apiRear.post('/order/delete', this.selectedOrderIds);
        this.$message.success('订单已删除');
        this.selectedOrderIds = [];
        await this.fetchData();
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
          this.$message.error('删除失败，请稍后再试');
        }
      }
    },
    handlePageChange(newPage) {
      this.page = newPage;
    },
    handlePageSizeChange(newPageSize) {
      this.pageSize = parseInt(newPageSize, 10);
      this.page = 1;
    },
    async openDetail(order) {
      this.selectedOrder = order;
      this.detailDialogVisible = true;
      this.detailLoading = true;
      try {
        const details = await this.getOrderDetails(order.orderId);
        this.orderDetail = details;
      } catch (error) {
        this.orderDetail = [];
        this.$message.error('无法获取订单详情');
      } finally {
        this.detailLoading = false;
      }
    },
    async getOrderDetails(orderId) {
      if (this.orderDetailsCache[orderId]) {
        return this.orderDetailsCache[orderId];
      }
      const res = await apiRear.get(`/order/getOrderDetailByOrderId=${orderId}`);
      const details = Array.isArray(res.data.data) ? res.data.data : [];
      this.$set(this.orderDetailsCache, orderId, details);
      return details;
    },
    async preloadOrderDetailsForSearch() {
      if (!this.search.productName && !this.search.productCode) {
        return;
      }
      const candidates = (this.tableData || []).filter(order => this.matchesBaseFilters(order));
      const orderIds = candidates.map(order => order.orderId).filter(Boolean);
      await this.loadOrderDetailsBatch(orderIds);
    },
    async loadOrderDetailsBatch(orderIds) {
      const missing = orderIds.filter(id => !this.orderDetailsCache[id]);
      if (!missing.length) {
        return;
      }
      const batchSize = 8;
      for (let i = 0; i < missing.length; i += batchSize) {
        const batch = missing.slice(i, i + batchSize);
        await Promise.all(batch.map(async id => {
          try {
            const res = await apiRear.get(`/order/getOrderDetailByOrderId=${id}`);
            const details = Array.isArray(res.data.data) ? res.data.data : [];
            this.$set(this.orderDetailsCache, id, details);
          } catch (error) {
            this.$set(this.orderDetailsCache, id, []);
          }
        }));
      }
    },
    matchesBaseFilters(order) {
      const orderId = String(order.orderId || '');
      if (!this.matchText(orderId, this.search.orderId)) {
        return false;
      }
      if (!this.matchText(order.username, this.search.username)) {
        return false;
      }
      const phone = order.phone || order.cphone || '';
      if (!this.matchText(phone, this.search.phone)) {
        return false;
      }
      if (!this.matchText(order.pickupCode, this.search.pickupCode)) {
        return false;
      }
      if (this.search.orderStatus !== '' && this.search.orderStatus !== null) {
        const statusValue = Number(this.search.orderStatus);
        if (this.getOrderStatus(order) !== statusValue) {
          return false;
        }
      }
      if (Array.isArray(this.search.dateRange) && this.search.dateRange.length === 2) {
        const [start, end] = this.search.dateRange;
        const startTime = start ? new Date(start).getTime() : null;
        const endTime = end ? new Date(end).getTime() : null;
        const date = this.parseDate(this.getOrderCreateDate(order));
        if (!date) {
          return false;
        }
        const time = date.getTime();
        if (startTime && time < startTime) return false;
        if (endTime) {
          const endOfDay = endTime + 24 * 60 * 60 * 1000 - 1;
          if (time > endOfDay) return false;
        }
      }
      return true;
    },
    matchesProductFilters(order) {
      if (!this.search.productName && !this.search.productCode) {
        return true;
      }
      const details = this.orderDetailsCache[order.orderId];
      if (!details || !details.length) {
        return false;
      }
      const exact = this.search.exactMatch === true;
      const nameKeywordRaw = (this.search.productName || '').trim();
      const codeKeywordRaw = (this.search.productCode || '').trim();
      const nameKeyword = exact ? nameKeywordRaw : nameKeywordRaw.toLowerCase();
      const codeKeyword = exact ? codeKeywordRaw : codeKeywordRaw.toLowerCase();
      return details.some(item => {
        const productName = String(item.productName || '');
        const productCode = String(item.productCode || '');
        const nameSource = exact ? productName : productName.toLowerCase();
        const codeSource = exact ? productCode : productCode.toLowerCase();
        const nameMatch = !nameKeyword || (exact ? nameSource === nameKeyword : nameSource.includes(nameKeyword));
        const codeMatch = !codeKeyword || (exact ? codeSource === codeKeyword : codeSource.includes(codeKeyword));
        return nameMatch && codeMatch;
      });
    },
    matchText(source, keyword) {
      const raw = (keyword || '').toString().trim();
      if (!raw) {
        return true;
      }
      const sourceStr = String(source || '');
      if (this.search.exactMatch === true) {
        return sourceStr === raw;
      }
      return sourceStr.toLowerCase().includes(raw.toLowerCase());
    },
    parseDate(value) {
      if (!value) {
        return null;
      }
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) {
        return null;
      }
      return date;
    },
    getOrderCreateDate(order) {
      if (!order) {
        return null;
      }
      return order.createDate || order.CreateDate || order.create_time || order.createTime || null;
    },
    getOrderStatus(order) {
      if (order && order.orderStatus !== undefined && order.orderStatus !== null) {
        return Number(order.orderStatus);
      }
      if (order && order.pickupStatus === 1) {
        return 3;
      }
      if (order && order.pickupStatus === 0) {
        return 1;
      }
      if (order && order.completeDate) {
        return 3;
      }
      return 0;
    },
    getOrderStatusLabel(status) {
      const value = Number(status);
      if (value === 6) {
        return '部分退款';
      }
      if (value === 5) {
        return '退款申请中';
      }
      const found = this.statusOptions.find(item => item.value === value);
      return found ? found.label : '待支付';
    },
    getOrderStatusTagType(status) {
      const value = Number(status);
      if (value === 3) return 'success';
      if (value === 4) return 'danger';
      if (value === 6) return 'warning';
      if (value === 5) return 'warning';
      if (value === 2) return 'info';
      if (value === 1) return 'warning';
      return 'danger';
    },
    resolveProductImage(productCode) {
      if (productCode && productImages[productCode]) {
        return productImages[productCode];
      }
      return fallbackImage || '';
    }
  },
  mounted() {
    this.fetchData();
    this.initRealtimeRefresh();
  },
  beforeDestroy() {
    if (this.realtimeHandler && this.$eventBus) {
      this.$eventBus.$off('realtime:global-refresh', this.realtimeHandler);
      this.$eventBus.$off('realtime:order-updated', this.realtimeHandler);
    }
    if (this.realtimeRefreshTimer) {
      clearTimeout(this.realtimeRefreshTimer);
      this.realtimeRefreshTimer = null;
    }
  }
}
</script>

<template>
  <div class="panel-theme order-management">
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
              title="即将重置所有查询条件,是否继续?"
          >
            <el-button type="warning" icon="el-icon-refresh" slot="reference" style="margin: 10px">重置查询</el-button>
          </el-popconfirm>
        </div>
        <div style="display: flex; justify-content: flex-end;">
        </div>
      </div>

      <el-dialog title="查询" :visible.sync="dialogSearchVisible" :close-on-click-modal="false" center width="1000px">
        <div class="exact-match-toggle" :style="{ paddingLeft: searchLabelWidth }">
          <span class="strong">精确查询</span>
          <el-tooltip content="区分大小写，完全匹配" placement="bottom">
            <el-button size="mini" :type="search.exactMatch ? 'primary' : 'info'"
                       @click="search.exactMatch = !search.exactMatch">
              {{ search.exactMatch ? '已开启' : '已关闭' }}
            </el-button>
          </el-tooltip>
        </div>
        <div style="display: flex">
          <div style="width: 50%">
            <el-form :model="search" style="width: 400px">
              <el-form-item label="订单号" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.orderId" placeholder="请输入订单号" clearable></el-input>
              </el-form-item>
              <el-form-item label="用户名" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.username" placeholder="请输入用户名" clearable></el-input>
              </el-form-item>
              <el-form-item label="电话" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.phone" placeholder="请输入电话" clearable></el-input>
              </el-form-item>
              <el-form-item label="取件码" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.pickupCode" placeholder="请输入取件码" clearable></el-input>
              </el-form-item>
            </el-form>
          </div>
          <div style="width: 50%">
            <el-form :model="search" style="width: 400px">
              <el-form-item label="订单状态" :label-width="searchLabelWidth" class="strong">
                <el-select v-model="search.orderStatus" placeholder="请选择订单状态" clearable>
                  <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="日期范围" :label-width="searchLabelWidth" class="strong">
                <el-date-picker
                  v-model="search.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 100%"
                ></el-date-picker>
              </el-form-item>
              <el-form-item label="文创产品" :label-width="searchLabelWidth" class="strong">
                <el-autocomplete
                  v-model="search.productName"
                  placeholder="请输入文创产品名称"
                  clearable
                  :fetch-suggestions="fetchProductNameSuggestions"
                  :trigger-on-focus="false"
                  @select="handleProductNameSelect"
                  class="product-name-autocomplete"
                >
                  <template slot-scope="{ item }">
                    <span :class="['product-suggestion', item && item.isEmpty ? 'is-empty' : '']">
                      {{ item.value }}
                    </span>
                  </template>
                </el-autocomplete>
              </el-form-item>
              <el-form-item label="文创产品编号" :label-width="searchLabelWidth" class="strong">
                <el-input v-model="search.productCode" placeholder="请输入文创产品编号" clearable></el-input>
              </el-form-item>
            </el-form>
          </div>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogSearchVisible = false">取消</el-button>
          <el-button type="primary" @click="searchConfirm">确定</el-button>
        </div>
      </el-dialog>

      <el-table
        ref="ordersTable"
        v-loading="loading"
        :data="pagedOrders"
        class="modern-table"
        stripe
        style="width: 100%"
        :height="tableHeight"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="orderId" label="订单号" min-width="160" sortable></el-table-column>
        <el-table-column prop="username" label="用户名" min-width="140" sortable></el-table-column>
        <el-table-column prop="phone" label="电话" min-width="140" sortable>
          <template slot-scope="scope">
            {{ scope.row.phone || scope.row.cphone || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="pickupCode" label="取件码" min-width="120" sortable></el-table-column>
        <el-table-column prop="totalPrice" label="金额" min-width="120" sortable>
          <template slot-scope="scope">
            ￥{{ Number(scope.row.totalPrice || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="createDate" label="创建时间" min-width="170" sortable></el-table-column>
        <el-table-column prop="completeDate" label="完成时间" min-width="170" sortable>
          <template slot-scope="scope">
            {{ scope.row.completeDate || '暂无' }}
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" min-width="130" sortable>
          <template slot-scope="scope">
            <el-tag :type="getOrderStatusTagType(scope.row.orderStatus)">
              {{ getOrderStatusLabel(scope.row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="openDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <br>
    <div class="pagination-container">
      <div>
        <span style="margin-right: 10px">每页显示条数</span>
        <el-select v-model="pageSize" placeholder="请选择" @change="handlePageSizeChange" style="width: 70px">
          <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"></el-option>
        </el-select>
      </div>
      <el-pagination
        background
        layout="prev, pager, next"
        :total="filteredTotal"
        :page-size="pageSize"
        :current-page="page"
        @current-change="handlePageChange"
      ></el-pagination>
    </div>

    <el-dialog title="订单详情" :visible.sync="detailDialogVisible" center width="900px">
      <div v-loading="detailLoading">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="订单号">{{ selectedOrder ? selectedOrder.orderId : '--' }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ selectedOrder ? selectedOrder.username : '--' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ selectedOrder ? (selectedOrder.phone || selectedOrder.cphone || '--') : '--' }}</el-descriptions-item>
          <el-descriptions-item label="取件码">{{ selectedOrder ? selectedOrder.pickupCode : '--' }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag v-if="selectedOrder" :type="getOrderStatusTagType(selectedOrder.orderStatus)">
              {{ getOrderStatusLabel(selectedOrder.orderStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">
            ￥{{ selectedOrder ? Number(selectedOrder.totalPrice || 0).toFixed(2) : '0.00' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedOrder ? (selectedOrder.createDate || selectedOrder.CreateDate || '--') : '--' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ selectedOrder ? (selectedOrder.completeDate || selectedOrder.CompleteDate || '暂无') : '--' }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="orderDetail" border style="width: 100%; margin-top: 16px;">
          <el-table-column label="图片" width="120">
            <template slot-scope="scope">
              <img
                :src="resolveProductImage(scope.row.productCode)"
                :alt="scope.row.productName || '文创产品'"
                class="order-product-thumb"
              />
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="文创产品"></el-table-column>
          <el-table-column prop="productCode" label="产品编号" width="160"></el-table-column>
          <el-table-column prop="price" label="单价" width="120">
            <template slot-scope="scope">
              ￥{{ Number(scope.row.price || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="purchaseNum" label="数量" width="100"></el-table-column>
          <el-table-column label="小计" width="120">
            <template slot-scope="scope">
              ￥{{ (Number(scope.row.price || 0) * Number(scope.row.purchaseNum || 0)).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
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

.product-name-autocomplete {
  width: 100%;
}

.product-suggestion.is-empty {
  color: #9aa3a0;
}

.order-product-thumb {
  width: 88px;
  height: 88px;
  border-radius: 8px;
  object-fit: cover;
  background: rgba(31, 122, 109, 0.08);
  border: 1px solid rgba(31, 122, 109, 0.12);
}
</style>
