
<script>
import {jwtDecode} from "jwt-decode";
import {apiRear} from "@/axios";
import _ from 'lodash';
import QRCode from 'qrcode';
import MessageCenter from '@/components/MessageCenter.vue';
import SliderCaptcha from '@/components/SliderCaptcha.vue';
import echarts from 'echarts';

const productImageContext = require.context('@/assets/culturalProducts', false, /\.png$/);
const productImages = productImageContext.keys().reduce((acc, key) => {
  const fileName = key.replace('./', '').replace(/\.png$/, '');
  acc[fileName] = productImageContext(key);
  return acc;
}, {});
const fallbackImage = productImages['1'];

const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'));
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
const validateOptionalAge = (rule, value, callback) => {
  if (value === '' || value === null || value === undefined) {
    callback();
    return;
  }
  if (!Number.isInteger(value) || value < 0) {
    callback(new Error('年龄必须为非负整数'));
  } else {
    callback();
  }
};

export default {
  name: 'UserInfo',
  components: {
    MessageCenter,
    SliderCaptcha
  },
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
        callback(new Error('请输入新密码'));
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
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };
    return {
      token: sessionStorage.getItem('token') || '',
      loading: true,
      chartsLoading: false,
      dialogEditInfo: false,
      dialogUpdatePassword: false,
      dialog_zfb: false,
      dialog_wechat: false,
      dialogBalanceHistory: false,
      dialogWithdrawRecords: false,
      dialog_bill: false,
      dialog_orderDetail: false,
      balanceHistoryLoading: false,
      balanceHistory: [],
      dialogWithdrawApply: false,
      withdrawAmount: 0,
      withdrawPhone: '',
      withdrawTodayApplied: false,
      withdrawTodayPending: false,
      withdrawPendingAmount: null,
      withdrawPendingPhone: '',
      withdrawSubmitting: false,
      withdrawRecordsLoading: false,
      withdrawRecords: [],
      withdrawAuditDialogVisible: false,
      withdrawAuditLoading: false,
      withdrawAuditRequestId: null,
      withdrawAuditDetail: null,
      withdrawAuditHistory: [],
      withdrawAuditRejectReason: '',
      withdrawAuditSubmitting: false,
      withdrawPayoutConfirmVisible: false,
      statusDialogVisible: false,
      statusDialogOrder: null,
      searchLabelWidth: "120px",
      dangerColor: '#e83a3a',
      circleUrl: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png",
      uid: '',
      zfb_url: require('@/assets/pay/zfb_qr.jpg'),
      weChat_url: require('@/assets/pay/wechat_qr.jpg'),
      chooseOrderId: 0,
      userInfo: {},
      orders: [],
      orderDetail: [],
      orderSummary: {
        totalPrice: 0,
        orderId: []
      },
      stats: {
        totalOrders: 0,
        completedOrders: 0,
        withdrawRequestCount: 0,
        pendingOrders: 0,
        pendingAmount: 0,
        lastOrderDate: ''
      },
      categoryPieData: {
        columns: ['category', 'totalQuantity'],
        rows: []
      },
      categoryPieEmpty: false,
      pieChartInstance: null,
      productCategoryData: {
        columns: ['category', 'totalQuantity'],
        rows: []
      },
      productCategoryEmpty: false,
      productCategoryChartInstance: null,
      lowStockProducts: [],
      lowStockLoading: false,
      orderTrendData: {
        columns: ['month', 'orders'],
        rows: []
      },
      trendEmpty: false,
      trendChartInstance: null,
      revenueTrendData: {
        columns: ['month', 'amount'],
        rows: []
      },
      revenueEmpty: false,
      revenueChartInstance: null,
      sixMonthRevenueTotal: 0,
      orderFilter: 'all',
      orderSearch: {
        keyword: '',
        status: 'all',
        dateRange: []
      },
      orderAutoRefreshTimer: null,
      orderAutoRefreshInterval: 12000,
      orderAutoRefreshing: false,
      realtimeUpdateHandler: null,
      realtimeGlobalHandler: null,
      realtimeOpenOrdersHandler: null,
      realtimeOpenWithdrawHandler: null,
      refreshStatsDebounced: null,
      selectedOrder: null,
      pickupQrUrl: '',
      selectedOrderIds: [],
      selfDeleteDialogVisible: false,
      selfDeleteConfirmVisible: false,
      selfDeleteInput: '',
      refundDialogVisible: false,
      refundSubmitting: false,
      refundReason: '',
      refundOtherReason: '',
      selfDeleting: false,
      refundOptions: [
        '不想要了',
        '重复购买',
        '订错文创',
        '质量问题',
        '其他'
      ],
      statusOptions: [
        { value: 1, label: '待取货' },
        { value: 0, label: '待支付' },
        { value: 3, label: '已完成' },
        { value: 2, label: '已关闭' },
        { value: 4, label: '已退款' }
      ],
      statusFilterOptions: [
        { value: 0, label: '待支付' },
        { value: 1, label: '待取货' },
        { value: 3, label: '已完成' },
        { value: 2, label: '已关闭' },
        { value: 4, label: '已退款' },
        { value: 5, label: '退款申请中' },
        { value: 6, label: '部分退款' }
      ],
      edit: {
        username: '',
        email: '',
        oldEmailCode: '',
        newEmailCode: '',
        cname: '',
        csex: '',
        cage: 0,
        caddress: '',
        cphone: '',
        cremark: '',
        newPassword: '',
        oldPassword: '',
        checkPassword: '',
      },
      emailVerify: {
        old: {
          captchaRequired: false,
          captchaPassed: false,
          sending: false,
          countdown: 0
        },
        next: {
          captchaRequired: false,
          captchaPassed: false,
          sending: false,
          countdown: 0
        }
      },
      emailTimerOld: null,
      emailTimerNew: null,
      size: 'medium',
      rules: {
        username: [{required: true, message: '请输入用户名', validator: validateUsername, trigger: 'blur'}],
        email: [{message: '请输入邮箱地址', trigger: 'blur'}, {
          type: 'email',
          message: '请输入正确的邮箱地址',
          trigger: ['blur', 'change']
        }],
        cage: [{validator: validateOptionalAge, trigger: 'blur'}],
        cphone: [{required: true, message: '请输入电话', validator: validateCphone, trigger: 'blur'}],
        oldPassword: [{required: true, validator: validateOldPass, trigger: 'blur'}],
        newPassword: [{required: true, validator: validatePass, trigger: 'blur'}],
        checkPassword: [{required: true, validator: validatePass2, trigger: 'blur'}],
      },
    };
  },
  computed: {
    authority() {
      if (!this.token) {
        return null;
      }
      try {
        return jwtDecode(this.token).authority;
      } catch (error) {
        return null;
      }
    },
    isCustomer() {
      return this.authority === 3;
    },
    isAdmin() {
      return this.authority === 1;
    },
    formattedSex() {
      if (this.userInfo.csex === 'F') {
        return '男';
      } else if (this.userInfo.csex === 'M') {
        return '女';
      } else if (this.userInfo.csex === 'U') {
        return '不愿透露';
      } else {
        return '未知';
      }
    },
    formattedAge() {
      const age = this.userInfo.cage;
      if (age === null || age === undefined || age === '' || age === 0 || age === '0') {
        return '空';
      }
      return age;
    },
    emailChanged() {
      const current = this.userInfo.email || '';
      const target = this.edit.email || '';
      return Boolean(target) && target !== current;
    },
    needsOldEmailVerify() {
      return this.emailChanged && Boolean(this.userInfo.email);
    },
    needsNewEmailVerify() {
      return this.emailChanged && Boolean(this.edit.email);
    },
    isEditInvalid() {
      const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      const emailValid = !this.edit.email || emailPattern.test(this.edit.email);
      const ageValid = this.edit.cage === '' || this.edit.cage === null || Number.isInteger(this.edit.cage);
      const codeValid = value => /^\d{6}$/.test(value || '');
      const emailVerifyValid = !this.emailChanged
        || ((this.needsOldEmailVerify ? codeValid(this.edit.oldEmailCode) : true)
          && (this.needsNewEmailVerify ? codeValid(this.edit.newEmailCode) : true));
      return !this.edit.username || !this.edit.cphone || !emailValid || !ageValid || !emailVerifyValid;
    },
    isPasswordInvalid() {
      return !this.edit.oldPassword || !this.edit.newPassword || !this.edit.checkPassword ||
        this.edit.newPassword !== this.edit.checkPassword;
    },
    selfDeletePhrase() {
      return '我已确认此操作带来的风险，确认注销';
    },
    selfDeleteAllowed() {
      const balance = Number(this.userInfo.balance || 0);
      return this.isCustomer && balance === 0;
    },
    selfDeletePhraseOk() {
      return (this.selfDeleteInput || '').trim() === this.selfDeletePhrase;
    },
    recentOrders() {
      const sorted = [...this.orders].sort((a, b) => {
        return this.parseDate(this.getOrderCreateDate(b)) - this.parseDate(this.getOrderCreateDate(a));
      });
      return sorted.slice(0, 4);
    },
    filteredOrders() {
      let data = [...this.orders];
      if (this.isAdmin) {
        const keyword = (this.orderSearch.keyword || '').trim().toLowerCase();
        if (keyword) {
          data = data.filter(order => {
            const fields = [
              order.orderId,
              order.username,
              order.phone,
              order.cphone,
              order.pickupCode
            ];
            return fields.some(field => field !== undefined && field !== null
              && String(field).toLowerCase().includes(keyword));
          });
        }
        if (this.orderSearch.status !== 'all' && this.orderSearch.status !== '' && this.orderSearch.status !== null) {
          const statusValue = Number(this.orderSearch.status);
          data = data.filter(order => this.getOrderStatus(order) === statusValue);
        }
        if (Array.isArray(this.orderSearch.dateRange) && this.orderSearch.dateRange.length === 2) {
          const [start, end] = this.orderSearch.dateRange;
          const startTime = start ? new Date(start).getTime() : null;
          const endTime = end ? new Date(end).getTime() : null;
          data = data.filter(order => {
            const date = this.parseDate(this.getOrderCreateDate(order));
            if (!date) return false;
            const time = date.getTime();
            if (startTime && time < startTime) return false;
            if (endTime && time > endTime) return false;
            return true;
          });
        }
      } else {
        if (this.orderFilter === 'completed') {
          data = data.filter(order => this.getOrderStatus(order) === 3 || order.completeDate);
        } else if (this.orderFilter === 'pending') {
          const status = order => this.getOrderStatus(order);
          data = data.filter(order => [0, 1].includes(status(order)));
        }
      }
      return data.sort((a, b) => {
        const aTime = this.parseDate(this.getOrderCreateDate(a))?.getTime?.() || 0;
        const bTime = this.parseDate(this.getOrderCreateDate(b))?.getTime?.() || 0;
        return bTime - aTime;
      });
    },
    detailContact() {
      if (this.isAdmin && this.selectedOrder) {
        return {
          name: this.selectedOrder.name,
          phone: this.selectedOrder.phone || this.selectedOrder.cphone,
          address: this.selectedOrder.address,
          remark: this.selectedOrder.remark
        };
      }
      return {
        name: this.userInfo.cname,
        phone: this.userInfo.cphone,
        address: this.userInfo.caddress,
        remark: this.userInfo.cremark
      };
    },
    withdrawPhoneValid() {
      return /^\d{11}$/.test(String(this.withdrawPhone || ''));
    },
    withdrawSubmitDisabled() {
      const amount = Number(this.withdrawAmount || 0);
      const balance = Number(this.userInfo.balance || 0);
      return this.withdrawTodayApplied
        || !this.withdrawPhoneValid
        || !Number.isFinite(amount)
        || amount <= 0
        || amount > balance;
    },
    withdrawAuditRequest() {
      return this.withdrawAuditDetail && this.withdrawAuditDetail.request
        ? this.withdrawAuditDetail.request
        : null;
    },
    withdrawAuditUserInfo() {
      return this.withdrawAuditDetail && this.withdrawAuditDetail.userInfo
        ? this.withdrawAuditDetail.userInfo
        : null;
    }
  },
  watch: {
    dialogEditInfo(val) {
      if (val) {
        this.resetEmailVerification('all');
      }
    },
    'edit.email'(val, oldVal) {
      if (val !== oldVal) {
        this.resetEmailVerification('all');
        this.edit.newEmailCode = '';
      }
    }
  },
  created() {
    this.initEditUid();
    this.fetchData();
    this.getUserBill();
    this.getOrderSummary();
    this.initRealtimeHandlers();
    this.startOrderAutoRefresh();
  },
  mounted() {
    this.getProductCategoryStats();
    this.getMallCategoryStats();
    if (this.isAdmin) {
      this.getLowStockProducts();
      this.loadWithdrawRecords(true);
    }
    this.$nextTick(() => {
      this.initCharts();
      window.addEventListener('resize', this.resizeCharts);
    });
  },
  beforeDestroy() {
    this.stopOrderAutoRefresh();
    if (this.realtimeUpdateHandler && this.$eventBus) {
      this.$eventBus.$off('realtime:order-updated', this.realtimeUpdateHandler);
    }
    if (this.realtimeGlobalHandler && this.$eventBus) {
      this.$eventBus.$off('realtime:global-refresh', this.realtimeGlobalHandler);
    }
    if (this.realtimeOpenOrdersHandler && this.$eventBus) {
      this.$eventBus.$off('realtime:open-orders', this.realtimeOpenOrdersHandler);
    }
    if (this.realtimeOpenWithdrawHandler && this.$eventBus) {
      this.$eventBus.$off('realtime:open-withdraw-review', this.realtimeOpenWithdrawHandler);
    }
    if (this.emailTimerOld) {
      clearInterval(this.emailTimerOld);
      this.emailTimerOld = null;
    }
    if (this.emailTimerNew) {
      clearInterval(this.emailTimerNew);
      this.emailTimerNew = null;
    }
    window.removeEventListener('resize', this.resizeCharts);
    if (this.trendChartInstance) {
      this.trendChartInstance.dispose();
      this.trendChartInstance = null;
    }
    if (this.revenueChartInstance) {
      this.revenueChartInstance.dispose();
      this.revenueChartInstance = null;
    }
    if (this.pieChartInstance) {
      this.pieChartInstance.dispose();
      this.pieChartInstance = null;
    }
    if (this.productCategoryChartInstance) {
      this.productCategoryChartInstance.dispose();
      this.productCategoryChartInstance = null;
    }
  },
  methods: {
    initRealtimeHandlers() {
      if (!this.$eventBus) {
        return;
      }
      if (!this.refreshStatsDebounced) {
        this.refreshStatsDebounced = _.debounce(() => {
          this.refreshAllStats();
        }, 600);
      }
      this.realtimeUpdateHandler = (payload = {}) => {
        const eventUid = payload.uid;
        if (this.isAdmin) {
          this.refreshStatsDebounced();
          return;
        }
        if (eventUid !== null && eventUid !== undefined && String(eventUid) === String(this.uid)) {
          this.refreshStatsDebounced();
        }
      };
      this.$eventBus.$on('realtime:order-updated', this.realtimeUpdateHandler);
      this.realtimeGlobalHandler = () => {
        this.refreshStatsDebounced();
      };
      this.$eventBus.$on('realtime:global-refresh', this.realtimeGlobalHandler);
      this.realtimeOpenOrdersHandler = (payload = {}) => {
        if (payload.filter) {
          sessionStorage.setItem('openOrderFilter', payload.filter);
        }
        if (payload.status !== undefined && payload.status !== null) {
          sessionStorage.setItem('openOrderStatus', String(payload.status));
        }
        if (payload.orderId) {
          sessionStorage.setItem('openOrderDetailId', String(payload.orderId));
        }
        sessionStorage.setItem('openOrders', '1');
        this.checkAutoOpenDialogs();
      };
      this.$eventBus.$on('realtime:open-orders', this.realtimeOpenOrdersHandler);
      this.realtimeOpenWithdrawHandler = (payload = {}) => {
        if (!payload || payload.requestId === undefined || payload.requestId === null) {
          return;
        }
        sessionStorage.setItem('openWithdrawRequestId', String(payload.requestId));
        this.checkAutoOpenDialogs();
      };
      this.$eventBus.$on('realtime:open-withdraw-review', this.realtimeOpenWithdrawHandler);
    },
    refreshAllStats() {
      if (!this.dialogEditInfo) {
        this.fetchData();
      }
      if (this.dialogBalanceHistory) {
        this.loadBalanceHistory(true);
      }
      if (this.dialogWithdrawApply) {
        this.loadWithdrawTodayStatus(true);
      }
      if (this.dialogWithdrawRecords) {
        this.loadWithdrawRecords(true);
      }
      if (this.withdrawAuditDialogVisible && this.withdrawAuditRequestId) {
        this.loadWithdrawAuditDetail(this.withdrawAuditRequestId, true);
      }
      this.getUserBill(true);
      this.getOrderSummary();
      this.getProductCategoryStats(true);
      if (this.isAdmin) {
        this.getMallCategoryStats(true);
        this.getLowStockProducts(true);
        this.loadWithdrawRecords(true);
      }
    },
    startOrderAutoRefresh() {
      this.stopOrderAutoRefresh();
      const interval = this.isAdmin ? 8000 : 12000;
      this.orderAutoRefreshInterval = interval;
      this.orderAutoRefreshTimer = setInterval(() => {
        if (document.hidden) {
          return;
        }
        this.refreshOrdersSilently();
      }, interval);
    },
    stopOrderAutoRefresh() {
      if (this.orderAutoRefreshTimer) {
        clearInterval(this.orderAutoRefreshTimer);
        this.orderAutoRefreshTimer = null;
      }
    },
    async refreshOrdersSilently() {
      if (this.orderAutoRefreshing) {
        return;
      }
      this.orderAutoRefreshing = true;
      try {
        await this.getUserBill(true);
      } finally {
        this.orderAutoRefreshing = false;
      }
    },
    initEditUid() {
      if (!this.token) {
        return;
      }
      this.uid = jwtDecode(this.token).uid;
    },
    goHome() {
      this.$router.push('/indexView');
    },
    openOrderDialog(filter) {
      this.orderFilter = filter || 'all';
      this.dialog_bill = true;
    },
    async openWithdrawRecordsDialog() {
      if (!this.isAdmin) {
        return;
      }
      this.dialogWithdrawRecords = true;
      await this.loadWithdrawRecords(true);
    },
    async loadWithdrawRecords(silent = false) {
      if (!this.isAdmin) {
        return;
      }
      if (!silent) {
        this.withdrawRecordsLoading = true;
      }
      try {
        const response = await apiRear.get('/userInfo/balance/withdraw/records');
        const rows = Array.isArray(response.data.data) ? response.data.data : [];
        this.withdrawRecords = rows;
        this.stats.withdrawRequestCount = rows.length;
      } catch (error) {
        this.withdrawRecords = [];
        this.stats.withdrawRequestCount = 0;
        if (!silent) {
          this.$message.error('获取余额提现记录失败');
        }
      } finally {
        this.withdrawRecordsLoading = false;
      }
    },
    getWithdrawStatusLabel(status) {
      const value = Number(status);
      if (value === 0) return '待批准';
      if (value === 1) return '已批准';
      if (value === 2) return '已驳回';
      return '未知';
    },
    getWithdrawStatusTagType(status) {
      const value = Number(status);
      if (value === 0) return 'warning';
      if (value === 1) return 'success';
      if (value === 2) return 'danger';
      return 'info';
    },
    handleTotalOrdersClick() {
      if (this.isAdmin) {
        this.$router.push('/orderManagement');
        return;
      }
      this.openOrderDialog('all');
    },
    checkAutoOpenDialogs() {
      const openOrders = sessionStorage.getItem('openOrders');
      const openOrderFilter = sessionStorage.getItem('openOrderFilter');
      const openOrderStatus = sessionStorage.getItem('openOrderStatus');
      const openWithdrawRequestId = sessionStorage.getItem('openWithdrawRequestId');
      if (openOrders) {
        sessionStorage.removeItem('openOrders');
        if (openOrderFilter) {
          sessionStorage.removeItem('openOrderFilter');
        }
        this.orderSearch.keyword = '';
        this.orderSearch.dateRange = [];
        this.openOrderDialog(openOrderFilter || 'all');
      }
      if (openOrderStatus) {
        sessionStorage.removeItem('openOrderStatus');
        this.orderSearch.status = openOrderStatus;
      }
      const detailId = sessionStorage.getItem('openOrderDetailId');
      if (detailId) {
        sessionStorage.removeItem('openOrderDetailId');
        this.orderDetail_handle(Number(detailId));
      }
      if (this.isAdmin && openWithdrawRequestId) {
        sessionStorage.removeItem('openWithdrawRequestId');
        this.openWithdrawAuditDialog(Number(openWithdrawRequestId));
      }
    },
    async openEditInfo() {
      this.dialogBalanceHistory = true;
      await this.loadBalanceHistory(true);
    },
    async loadBalanceHistory(silent = false) {
      if (!silent) {
        this.balanceHistoryLoading = true;
      }
      try {
        const response = await apiRear.get('/userInfo/balance/history');
        this.balanceHistory = Array.isArray(response.data.data) ? response.data.data : [];
      } catch (error) {
        if (!silent) {
          this.$message.error('获取余额变动记录失败');
        }
      } finally {
        this.balanceHistoryLoading = false;
      }
    },
    getBalanceChangeTypeLabel(type) {
      const map = {
        RECHARGE: '账户充值',
        PURCHASE: '购买商品',
        REFUND: '订单退款',
        PARTIAL_REFUND: '部分退款',
        ADMIN_ADJUST: '管理员调整',
        WITHDRAW_REQUEST: '提现申请',
        WITHDRAW: '余额提现'
      };
      return map[type] || '余额变动';
    },
    formatDateTime(value) {
      if (!value) {
        return '--';
      }
      const date = this.parseDate(value);
      if (!date) {
        return value;
      }
      const yyyy = date.getFullYear();
      const mm = String(date.getMonth() + 1).padStart(2, '0');
      const dd = String(date.getDate()).padStart(2, '0');
      const hh = String(date.getHours()).padStart(2, '0');
      const mi = String(date.getMinutes()).padStart(2, '0');
      const ss = String(date.getSeconds()).padStart(2, '0');
      return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`;
    },
    formatBalanceAmount(value) {
      const num = Number(value || 0);
      if (Number.isNaN(num)) {
        return '0.00';
      }
      if (num > 0) {
        return `+${num.toFixed(2)}`;
      }
      return num.toFixed(2);
    },
    async submitWithdrawRequest() {
      if (!this.isCustomer) {
        this.$message.warning('仅顾客账号支持提现申请');
        return;
      }
      if (this.withdrawTodayApplied) {
        this.$message.warning('你今天已申请过提现，请明天再试');
        return;
      }
      const amount = Number(this.withdrawAmount || 0);
      const balance = Number(this.userInfo.balance || 0);
      if (!Number.isFinite(amount) || amount <= 0) {
        this.$message.warning('请输入大于0的提现金额');
        return;
      }
      if (amount > balance) {
        this.$message.warning('提现金额不能超过账户余额');
        return;
      }
      if (!this.withdrawPhoneValid) {
        this.$message.warning('请输入11位手机号');
        return;
      }
      this.withdrawSubmitting = true;
      try {
        const response = await apiRear.post('/userInfo/balance/withdraw/request', {
          amount: Number(amount.toFixed(2)),
          phone: this.withdrawPhone
        });
        if (response.data && response.data.code === 1) {
          this.$message.success('提现申请已提交，等待管理员处理');
          this.withdrawAmount = 0;
          this.withdrawPhone = '';
          this.dialogWithdrawApply = false;
          await this.loadWithdrawTodayStatus(true);
          return;
        }
        this.$message.error((response.data && response.data.message) || '提现申请提交失败');
      } catch (error) {
        this.$message.error('提现申请提交失败，请稍后重试');
      } finally {
        this.withdrawSubmitting = false;
      }
    },
    async openWithdrawApplyDialog() {
      if (!this.isCustomer) {
        return;
      }
      this.withdrawAmount = 0;
      this.withdrawPhone = '';
      this.dialogWithdrawApply = true;
      await this.loadWithdrawTodayStatus(true);
    },
    async loadWithdrawTodayStatus(silent = false) {
      try {
        const response = await apiRear.get('/userInfo/balance/withdraw/today');
        const data = response && response.data ? response.data.data : null;
        this.withdrawTodayApplied = Boolean(data && data.appliedToday);
        this.withdrawTodayPending = Boolean(data && data.pending);
        this.withdrawPendingAmount = data && data.pendingAmount !== undefined ? data.pendingAmount : null;
        this.withdrawPendingPhone = data && data.pendingPhone ? String(data.pendingPhone) : '';
      } catch (error) {
        if (!silent) {
          this.$message.error('获取提现状态失败');
        }
      }
    },
    sanitizeWithdrawPhone(value) {
      this.withdrawPhone = String(value || '').replace(/\D/g, '').slice(0, 11);
    },
    async openWithdrawAuditDialog(requestId) {
      if (!this.isAdmin || !requestId) {
        return;
      }
      this.dialogWithdrawRecords = false;
      this.withdrawAuditDialogVisible = true;
      this.withdrawAuditRequestId = Number(requestId);
      this.withdrawAuditRejectReason = '';
      this.withdrawPayoutConfirmVisible = false;
      await this.loadWithdrawAuditDetail(this.withdrawAuditRequestId, false);
    },
    async loadWithdrawAuditDetail(requestId, silent = false) {
      if (!requestId) {
        return;
      }
      if (!silent) {
        this.withdrawAuditLoading = true;
      }
      try {
        const response = await apiRear.get(`/userInfo/balance/withdraw/request/${requestId}`);
        const data = response && response.data ? response.data.data : null;
        this.withdrawAuditDetail = data || null;
        this.withdrawAuditHistory = Array.isArray(data && data.balanceHistory) ? data.balanceHistory : [];
      } catch (error) {
        if (!silent) {
          this.$message.error('获取提现申请详情失败');
        }
      } finally {
        this.withdrawAuditLoading = false;
      }
    },
    openPayoutConfirmDialog() {
      if (!this.withdrawAuditRequest || Number(this.withdrawAuditRequest.status) !== 0) {
        this.$message.warning('该提现申请已处理');
        return;
      }
      this.withdrawPayoutConfirmVisible = true;
    },
    async approveWithdrawRequest() {
      if (!this.withdrawAuditRequest) {
        return;
      }
      this.withdrawAuditSubmitting = true;
      try {
        const response = await apiRear.post(
          `/userInfo/balance/withdraw/request/${this.withdrawAuditRequest.id}/review`,
          { approved: true }
        );
        if (response.data && response.data.code === 1) {
          this.$message.success('提现申请已批准');
          this.withdrawPayoutConfirmVisible = false;
          await this.loadWithdrawAuditDetail(this.withdrawAuditRequest.id, true);
          this.refreshAllStats();
          return;
        }
        this.$message.error((response.data && response.data.message) || '批准失败');
      } catch (error) {
        this.$message.error('批准失败，请稍后重试');
      } finally {
        this.withdrawAuditSubmitting = false;
      }
    },
    async rejectWithdrawRequest() {
      if (!this.withdrawAuditRequest || Number(this.withdrawAuditRequest.status) !== 0) {
        this.$message.warning('该提现申请已处理');
        return;
      }
      let reason = String(this.withdrawAuditRejectReason || '').trim();
      if (!reason) {
        reason = '管理员审核未通过';
      }
      this.withdrawAuditSubmitting = true;
      try {
        const response = await apiRear.post(
          `/userInfo/balance/withdraw/request/${this.withdrawAuditRequest.id}/review`,
          { approved: false, rejectReason: reason }
        );
        if (response.data && response.data.code === 1) {
          this.$message.success('已驳回提现申请');
          await this.loadWithdrawAuditDetail(this.withdrawAuditRequest.id, true);
          this.refreshAllStats();
          return;
        }
        this.$message.error((response.data && response.data.message) || '驳回失败');
      } catch (error) {
        this.$message.error('驳回失败，请稍后重试');
      } finally {
        this.withdrawAuditSubmitting = false;
      }
    },
    handleBalanceDialogClose() {
      this.withdrawAmount = 0;
      this.withdrawPhone = '';
      this.dialogWithdrawApply = false;
    },
    handleOrderSelectionChange(selection) {
      this.selectedOrderIds = selection.map(item => item.orderId);
    },
    async confirmDeleteOrders(orderIds) {
      if (!orderIds || !orderIds.length) {
        this.$message.info('请选择需要删除的订单');
        return;
      }
      try {
        await this.$confirm('删除订单后将无法恢复，是否继续？', '确认删除', {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning'
        });
        await apiRear.post('/order/delete', orderIds);
        this.$message.success('订单已删除');
        this.selectedOrderIds = [];
        await this.getUserBill();
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
          this.$message.error('删除失败，请稍后再试');
        }
      }
    },
    deleteSingleOrder(orderId) {
      this.confirmDeleteOrders([orderId]);
    },
    resetOrderSearch() {
      this.orderSearch.keyword = '';
      this.orderSearch.status = 'all';
      this.orderSearch.dateRange = [];
    },
    exportOrders() {
      const rows = this.filteredOrders;
      if (!rows.length) {
        this.$message.info('没有可导出的订单');
        return;
      }
      const headers = ['订单号', '用户名', '电话', '创建时间', '完成时间', '取件码', '订单状态', '取货状态'];
      const toCsvValue = value => {
        const text = value === undefined || value === null ? '' : String(value);
        if (text.includes(',') || text.includes('"') || text.includes('\n')) {
          return '"' + text.replace(/"/g, '""') + '"';
        }
        return text;
      };
      const lines = rows.map(order => {
        return [
          order.orderId,
          order.username || '',
          order.phone || order.cphone || '',
          order.createDate || '',
          order.completeDate || '',
          order.pickupCode || '',
          this.getOrderStatusLabel(this.getOrderStatus(order)),
          this.getPickupStatusLabel(order)
        ].map(toCsvValue).join(',');
      });
      const csv = ['\ufeff' + headers.join(',')].concat(lines).join('\n');
      const blob = new Blob([csv], {type: 'text/csv;charset=utf-8;'});
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `orders_${new Date().toISOString().slice(0, 10)}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
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
    getPickupStatusLabel(order) {
      const status = this.getOrderStatus(order);
      if (status === 1) return '待取货';
      if (status === 3) return '已取货';
      return '暂无';
    },
    getPickupStatusTagType(order) {
      const status = this.getOrderStatus(order);
      if (status === 1) return 'warning';
      if (status === 3) return 'success';
      return 'info';
    },
    getPickupVerifyUrl(token) {
      if (!token) {
        return '';
      }
      const baseUrl = window.location.origin;
      return `${baseUrl}/pickup-verify?token=${encodeURIComponent(token)}`;
    },
    async generatePickupQr(token) {
      if (!token) {
        this.pickupQrUrl = '';
        return;
      }
      try {
        const verifyUrl = this.getPickupVerifyUrl(token);
        this.pickupQrUrl = await QRCode.toDataURL(verifyUrl, {width: 220, margin: 1});
      } catch (error) {
        this.pickupQrUrl = '';
      }
    },
    onDialogOpen() {
      document.addEventListener('keydown', this.handleKeydown);
    },
    onDialogClose() {
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
      this.$refs.userRef.resetFields();
      this.edit = _.cloneDeep(this.userInfo);
      this.edit.password = '';
      this.edit.oldEmailCode = '';
      this.edit.newEmailCode = '';
      this.resetEmailVerification('all');
      this.dialogEditInfo = false;
    },
    cancelUpdatePassword() {
      this.edit.newPassword = '';
      this.edit.oldPassword = '';
      this.edit.checkPassword = '';
      this.$refs.passwordRef.resetFields();
      this.dialogUpdatePassword = false;
    },
    openSelfDeleteDialog() {
      this.selfDeleteDialogVisible = true;
      this.selfDeleteConfirmVisible = false;
      this.selfDeleteInput = '';
    },
    confirmSelfDeleteStep() {
      if (!this.selfDeleteAllowed) {
        this.$message.error('账户余额不为0，无法注销，请联系管理员');
        return;
      }
      this.selfDeleteDialogVisible = false;
      this.selfDeleteConfirmVisible = true;
    },
    async submitSelfDelete() {
      if (!this.selfDeletePhraseOk) {
        this.$message.error('请输入完整的确认语句');
        return;
      }
      try {
        const response = await apiRear.post('/userInfo/self-delete');
        if (response.data && response.data.code === 1) {
          this.$message.success('账号已注销');
          this.selfDeleteConfirmVisible = false;
          this.selfDeleteDialogVisible = false;
          this.selfDeleting = true;
          this.stopOrderAutoRefresh();
          if (this.refreshStatsDebounced && this.refreshStatsDebounced.cancel) {
            this.refreshStatsDebounced.cancel();
          }
          if (this.realtimeUpdateHandler && this.$eventBus) {
            this.$eventBus.$off('realtime:order-updated', this.realtimeUpdateHandler);
          }
          if (this.realtimeGlobalHandler && this.$eventBus) {
            this.$eventBus.$off('realtime:global-refresh', this.realtimeGlobalHandler);
          }
          if (this.realtimeOpenOrdersHandler && this.$eventBus) {
            this.$eventBus.$off('realtime:open-orders', this.realtimeOpenOrdersHandler);
          }
          if (this.$store && this.$store.dispatch) {
            this.$store.dispatch('logout');
          }
          sessionStorage.removeItem('token');
          localStorage.removeItem('shoppingCart');
          await this.$router.push('/indexView');
        } else {
          this.$message.error(response.data.message || '注销失败');
        }
      } catch (err) {
        this.$message.error('注销失败，请稍后重试');
      }
    },
    async updatePassword() {
      const response = await apiRear.post('/userInfo/updatePassword', {
        uid: this.uid,
        oldPassword: this.edit.oldPassword,
        newPassword: this.edit.newPassword
      });
      if (response.data.code === 1) {
        this.$message({
          type: 'success',
          message: '密码修改成功，正在退出账号...'
        });
        this.cancelUpdatePassword();
        sessionStorage.removeItem('token');
        localStorage.removeItem('shoppingCart');
        localStorage.setItem('activeIndex', '1');
        await this.$router.push('/indexView');
      } else {
        this.$message({
          type: 'error',
          message: '密码修改失败，请检查旧密码是否正确'
        });
      }
    },
    async updateUserInfo() {
      if (!this.edit.csex) {
        this.edit.csex = 'U';
      }
      if (!Number.isInteger(this.edit.cage)) {
        this.edit.cage = 0;
      }
      if (this.emailChanged) {
        if (this.needsOldEmailVerify && !/^\d{6}$/.test(this.edit.oldEmailCode || '')) {
          this.$message.error('请填写旧邮箱验证码');
          return;
        }
        if (this.needsNewEmailVerify && !/^\d{6}$/.test(this.edit.newEmailCode || '')) {
          this.$message.error('请填写新邮箱验证码');
          return;
        }
      }
      try {
        const response = await apiRear.post('/userInfo/update', this.edit);
        if (response.data && response.data.code === 1) {
          await this.fetchData();
          this.$message({
            type: 'success',
            message: '资料更新成功'
          });
          this.loading = true;
          this.dialogEditInfo = false;
          this.resetEmailVerification('all');
          this.edit.oldEmailCode = '';
          this.edit.newEmailCode = '';
          return;
        }
        const message = this.mapUserInfoError(response.data && response.data.message);
        this.$message({
          type: 'error',
          message: message || '修改资料失败，请检查输入格式'
        });
      } catch (err) {
        console.log(err);
        this.$message({
          type: 'error',
          message: '修改资料失败，请检查输入格式'
        });
      }
    },
    mapUserInfoError(code) {
      const mapping = {
        USER_INFO_MISSING: '用户信息缺失',
        USER_NOT_FOUND: '用户不存在',
        EMAIL_FORMAT_INVALID: '邮箱格式不正确',
        EMAIL_IN_USE: '邮箱已被占用',
        OLD_EMAIL_REQUIRED: '请先验证旧邮箱',
        NEW_EMAIL_REQUIRED: '请先验证新邮箱',
        OLD_EMAIL_MISSING: '当前账号未绑定邮箱',
        EMAIL_NO_CHANGE: '新邮箱不能与旧邮箱相同',
        UNAUTHORIZED: '登录信息已失效，请重新登录',
        FORBIDDEN: '无权限操作'
      };
      return mapping[code] || '';
    },
    resetEmailVerification(scope = 'all') {
      const resetOne = target => {
        target.captchaRequired = false;
        target.captchaPassed = false;
        target.sending = false;
        target.countdown = 0;
      };
      if (scope === 'old' || scope === 'all') {
        resetOne(this.emailVerify.old);
      }
      if (scope === 'new' || scope === 'all') {
        resetOne(this.emailVerify.next);
      }
      if (scope === 'all') {
        if (this.emailTimerOld) {
          clearInterval(this.emailTimerOld);
          this.emailTimerOld = null;
        }
        if (this.emailTimerNew) {
          clearInterval(this.emailTimerNew);
          this.emailTimerNew = null;
        }
      }
    },
    startEmailCountdown(type) {
      const state = type === 'old' ? this.emailVerify.old : this.emailVerify.next;
      state.countdown = 60;
      const timerKey = type === 'old' ? 'emailTimerOld' : 'emailTimerNew';
      if (this[timerKey]) {
        clearInterval(this[timerKey]);
      }
      this[timerKey] = setInterval(() => {
        state.countdown -= 1;
        if (state.countdown <= 0) {
          clearInterval(this[timerKey]);
          this[timerKey] = null;
          state.countdown = 0;
        }
      }, 1000);
    },
    initCharts() {
      const trendEl = this.$refs.trendChart;
      if (trendEl && !this.trendChartInstance) {
        this.trendChartInstance = echarts.init(trendEl);
      }
      const revenueEl = this.$refs.revenueChart;
      if (revenueEl && !this.revenueChartInstance) {
        this.revenueChartInstance = echarts.init(revenueEl);
      }
      const pieEl = this.$refs.categoryPie;
      if (pieEl && !this.pieChartInstance) {
        this.pieChartInstance = echarts.init(pieEl);
      }
      const productPieEl = this.$refs.productCategoryPie;
      if (productPieEl && !this.productCategoryChartInstance) {
        this.productCategoryChartInstance = echarts.init(productPieEl);
      }
      this.renderTrendChart();
      this.renderRevenueChart();
      this.renderPieChart();
      this.renderProductCategoryChart();
    },
    renderTrendChart() {
      if (!this.trendChartInstance) {
        return;
      }
      const xData = this.orderTrendData.rows.map(item => item.month);
      const yData = this.orderTrendData.rows.map(item => Number(item.orders || 0));
      const option = {
        tooltip: { trigger: 'axis' },
        grid: { left: 20, right: 32, top: 20, bottom: 30, containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: xData,
          axisLine: { lineStyle: { color: '#cfd8d5' } },
          axisLabel: { color: '#40534c', margin: 12 }
        },
        yAxis: {
          type: 'value',
          minInterval: 1,
          axisLine: { lineStyle: { color: '#cfd8d5' } },
          axisLabel: { color: '#40534c' },
          splitLine: { lineStyle: { color: 'rgba(31, 122, 109, 0.12)' } }
        },
        series: [{
          type: 'line',
          smooth: true,
          data: yData,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { width: 3, color: '#1f7a6d' },
          itemStyle: { color: '#1f7a6d' },
          areaStyle: { color: 'rgba(31, 122, 109, 0.12)' }
        }]
      };
      this.trendChartInstance.setOption(option, true);
    },
    renderRevenueChart() {
      if (!this.revenueChartInstance) {
        return;
      }
      const xData = this.revenueTrendData.rows.map(item => item.month);
      const yData = this.revenueTrendData.rows.map(item => Number(item.amount || 0));
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter(params) {
            if (!params || !params.length) return '';
            const item = params[0];
            return `${item.axisValue}<br/>成交额：￥${Number(item.data || 0).toFixed(2)}`;
          }
        },
        grid: { left: 20, right: 32, top: 20, bottom: 30, containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: xData,
          axisLine: { lineStyle: { color: '#cfd8d5' } },
          axisLabel: { color: '#40534c', margin: 12 }
        },
        yAxis: {
          type: 'value',
          axisLine: { lineStyle: { color: '#cfd8d5' } },
          axisLabel: {
            color: '#40534c',
            formatter: value => `￥${Number(value || 0).toFixed(0)}`
          },
          splitLine: { lineStyle: { color: 'rgba(31, 122, 109, 0.12)' } }
        },
        series: [{
          type: 'line',
          smooth: true,
          data: yData,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { width: 3, color: '#c75a31' },
          itemStyle: { color: '#c75a31' },
          areaStyle: { color: 'rgba(199, 90, 49, 0.16)' }
        }]
      };
      this.revenueChartInstance.setOption(option, true);
    },
    renderPieChart() {
      if (!this.pieChartInstance) {
        return;
      }
      const data = (this.categoryPieData.rows || [])
        .map(item => ({
          name: item.category || '未分类',
          value: Number(item.totalQuantity || 0)
        }))
        .filter(item => item.value > 0);
      const option = {
        tooltip: { trigger: 'item' },
        legend: {
          orient: 'vertical',
          right: 0,
          top: 'center',
          textStyle: { color: '#40534c' }
        },
        series: [{
          type: 'pie',
          radius: '70%',
          center: ['40%', '50%'],
          data,
          label: { color: '#40534c' },
          labelLine: { lineStyle: { color: '#cfd8d5' } }
        }]
      };
      this.pieChartInstance.setOption(option, true);
    },
    renderProductCategoryChart() {
      if (!this.productCategoryChartInstance) {
        return;
      }
      const rows = (this.productCategoryData.rows || [])
        .map(item => ({
          name: item.category || '未分类',
          value: Number(item.totalQuantity || 0)
        }))
        .filter(item => item.value > 0);
      rows.sort((a, b) => b.value - a.value);
      const colorPalette = [
        '#1f7a6d',
        '#c75a31',
        '#4c87a0',
        '#d4a14a',
        '#7a9f35',
        '#b36b86',
        '#5b7aa6',
        '#8d6f5b',
        '#2f7f9d',
        '#c06c4b',
        '#6a8f6b',
        '#b85c5c',
        '#6e7fb0',
        '#a27c43',
        '#3f8a6b',
        '#9b6a8b'
      ];
      const series = rows.map((item, index) => ({
        name: item.name,
        type: 'bar',
        stack: 'total',
        barWidth: 22,
        data: [item.value],
        itemStyle: {
          color: colorPalette[index % colorPalette.length]
        }
      }));
      const option = {
        tooltip: { trigger: 'item' },
        legend: {
          type: 'scroll',
          bottom: 0,
          textStyle: { color: '#40534c' }
        },
        grid: { left: 20, right: 24, top: 20, bottom: 36, containLabel: true },
        xAxis: {
          type: 'value',
          axisLine: { lineStyle: { color: '#cfd8d5' } },
          axisLabel: { color: '#40534c' },
          splitLine: { lineStyle: { color: 'rgba(31, 122, 109, 0.12)' } }
        },
        yAxis: {
          type: 'category',
          data: ['品类分布'],
          axisLine: { lineStyle: { color: '#cfd8d5' } },
          axisLabel: { color: '#40534c' },
          axisTick: { show: false }
        },
        series
      };
      this.productCategoryChartInstance.setOption(option, true);
    },
    resizeCharts() {
      this.$nextTick(() => {
        if (this.trendChartInstance) {
          this.trendChartInstance.resize();
        }
        if (this.revenueChartInstance) {
          this.revenueChartInstance.resize();
        }
        if (this.pieChartInstance) {
          this.pieChartInstance.resize();
        }
        if (this.productCategoryChartInstance) {
          this.productCategoryChartInstance.resize();
        }
      });
    },
    openRefundDialog() {
      if (!this.selectedOrder) {
        return;
      }
      this.refundReason = '';
      this.refundOtherReason = '';
      this.refundDialogVisible = true;
    },
    resolveProductImage(productCode) {
      if (productCode && productImages[productCode]) {
        return productImages[productCode];
      }
      return fallbackImage || '';
    },
    async submitRefundRequest() {
      if (!this.selectedOrder || !this.selectedOrder.orderId) {
        return;
      }
      if (!this.refundReason) {
        this.$message.error('请选择退款原因');
        return;
      }
      let reason = this.refundReason;
      if (this.refundReason === '其他') {
        if (!this.refundOtherReason.trim()) {
          this.$message.error('请输入退款原因');
          return;
        }
        reason = this.refundOtherReason.trim();
      }
      this.refundSubmitting = true;
      try {
        const response = await apiRear.post('/order/refund/request', {
          orderId: this.selectedOrder.orderId,
          reason
        });
        if (response.data && response.data.code === 1) {
          this.$message.success('退款申请已提交');
          this.refundDialogVisible = false;
          this.selectedOrder.orderStatus = 5;
          await this.getUserBill();
        } else {
          this.$message.error(response.data.message || '退款申请失败');
        }
      } catch (err) {
        this.$message.error('退款申请失败，请稍后重试');
      } finally {
        this.refundSubmitting = false;
      }
    },
    async approveRefund(order) {
      if (!order || !order.orderId) {
        return;
      }
      try {
        await this.$confirm('确认同意该退款申请？系统将把订单金额退回给用户。', '退款确认', {
          confirmButtonText: '确认退款',
          cancelButtonText: '取消',
          type: 'warning'
        });
      } catch (err) {
        return;
      }
      try {
        const response = await apiRear.post('/order/refund/approve', { orderId: order.orderId });
        if (response.data && response.data.code === 1) {
          this.$message.success('已同意退款');
          await this.getUserBill();
          this.dialog_orderDetail = false;
        } else {
          this.$message.error(response.data.message || '操作失败');
        }
      } catch (err) {
        this.$message.error('操作失败，请稍后重试');
      }
    },
    async rejectRefund(order) {
      if (!order || !order.orderId) {
        return;
      }
      try {
        await this.$confirm('确认驳回该退款申请？', '驳回确认', {
          confirmButtonText: '确认驳回',
          cancelButtonText: '取消',
          type: 'warning'
        });
      } catch (err) {
        return;
      }
      try {
        const response = await apiRear.post('/order/refund/reject', { orderId: order.orderId });
        if (response.data && response.data.code === 1) {
          this.$message.success('已驳回退款');
          await this.getUserBill();
          this.dialog_orderDetail = false;
        } else {
          this.$message.error(response.data.message || '操作失败');
        }
      } catch (err) {
        this.$message.error('操作失败，请稍后重试');
      }
    },
    handleOldCaptchaSuccess() {
      this.emailVerify.old.captchaPassed = true;
    },
    handleNewCaptchaSuccess() {
      this.emailVerify.next.captchaPassed = true;
    },
    sanitizeEmailCode(field) {
      this.edit[field] = (this.edit[field] || '').replace(/\D/g, '').slice(0, 6);
    },
    async sendOldEmailCode() {
      if (!this.needsOldEmailVerify) {
        return;
      }
      const state = this.emailVerify.old;
      if (state.sending || state.countdown > 0) {
        return;
      }
      if (!state.captchaPassed) {
        state.captchaRequired = true;
        this.$message.warning('请先完成人机验证');
        return;
      }
      state.sending = true;
      try {
        const response = await apiRear.post('/userInfo/email/old-code', { uid: this.uid });
        if (response.data && response.data.code === 1) {
          this.$message.success('旧邮箱验证码已发送');
          this.startEmailCountdown('old');
        } else {
          const message = this.mapUserInfoError(response.data && response.data.message);
          this.$message.error(message || '发送验证码失败');
        }
      } catch (err) {
        this.$message.error('发送验证码失败，请稍后重试');
      } finally {
        state.sending = false;
      }
    },
    async sendNewEmailCode() {
      if (!this.needsNewEmailVerify) {
        return;
      }
      const state = this.emailVerify.next;
      if (state.sending || state.countdown > 0) {
        return;
      }
      if (!state.captchaPassed) {
        state.captchaRequired = true;
        this.$message.warning('请先完成人机验证');
        return;
      }
      if (!this.edit.email) {
        this.$message.error('请先填写新邮箱');
        return;
      }
      state.sending = true;
      try {
        const response = await apiRear.post('/userInfo/email/new-code', {
          uid: this.uid,
          email: this.edit.email
        });
        if (response.data && response.data.code === 1) {
          this.$message.success('新邮箱验证码已发送');
          this.startEmailCountdown('new');
        } else {
          const message = this.mapUserInfoError(response.data && response.data.message);
          this.$message.error(message || '发送验证码失败');
        }
      } catch (err) {
        this.$message.error('发送验证码失败，请稍后重试');
      } finally {
        state.sending = false;
      }
    },
    fetchData() {
      if (this.selfDeleting || !sessionStorage.getItem('token')) {
        return;
      }
      apiRear.get(`/userInfo/personal/${this.uid}`).then(res => {
        this.userInfo = res.data.data;
        this.edit = _.cloneDeep(res.data.data);
        if (!this.edit.csex) {
          this.edit.csex = 'U';
        }
        this.edit.password = '';
        this.edit.oldEmailCode = '';
        this.edit.newEmailCode = '';
        this.loading = false;
      }).catch(err => {
        console.log(err);
        if (!this.selfDeleting && sessionStorage.getItem('token')) {
          this.$message.error('获取用户信息失败，请重新登录后重试');
        }
      });
    },
    async getProductCategoryStats(silent = false) {
      if (!silent) {
        this.chartsLoading = true;
      }
      try {
        const url = this.isAdmin
          ? '/cultural-products/category-statistics'
          : '/cultural-products/category-statistics/personal';
        const info = await apiRear.get(url);
        const rows = Array.isArray(info.data.data) ? info.data.data : [];
        const normalized = rows.map(item => ({
          category: item.category,
          totalQuantity: Number(item.totalQuantity || 0)
        }));
        this.categoryPieData.rows = normalized;
        this.categoryPieEmpty = !normalized.length
          || normalized.every(item => Number(item.totalQuantity || 0) === 0);
        this.renderPieChart();
        this.resizeCharts();
      } catch (err) {
        if (!silent) {
          this.$message.error('无法获取文创分类数据');
        }
        this.categoryPieData.rows = [];
        this.categoryPieEmpty = true;
        this.renderPieChart();
        this.resizeCharts();
      } finally {
        if (!silent) {
          this.chartsLoading = false;
        }
      }
    },
    async getMallCategoryStats(silent = false) {
      try {
        const info = await apiRear.get('/cultural-products/category-product-statistics');
        const rows = Array.isArray(info.data.data) ? info.data.data : [];
        const normalized = rows.map(item => ({
          category: item.category,
          totalQuantity: Number(item.totalQuantity || 0)
        }));
        this.productCategoryData.rows = normalized;
        this.productCategoryEmpty = !normalized.length
          || normalized.every(item => Number(item.totalQuantity || 0) === 0);
        this.renderProductCategoryChart();
        this.resizeCharts();
      } catch (err) {
        this.productCategoryData.rows = [];
        this.productCategoryEmpty = true;
        this.renderProductCategoryChart();
        this.resizeCharts();
        if (!silent) {
          this.$message.error('无法获取商城文创品类数据');
        }
      }
    },
    async getLowStockProducts(silent = false) {
      if (!this.isAdmin) {
        return;
      }
      if (!silent) {
        this.lowStockLoading = true;
      }
      try {
        const params = { page: 1, pageSize: 2000 };
        const info = await apiRear.get('/cultural-products', { params });
        const rows = info.data && info.data.data ? info.data.data.rows || [] : [];
        const normalized = rows
          .map(item => ({
            productCode: item.productCode,
            productName: item.productName,
            category: item.category,
            stock: Number(item.stock)
          }))
          .filter(item => Number.isFinite(item.stock) && item.stock < 50)
          .sort((a, b) => a.stock - b.stock);
        this.lowStockProducts = normalized;
      } catch (err) {
        this.lowStockProducts = [];
        if (!silent) {
          this.$message.error('无法获取库存告警数据');
        }
      } finally {
        if (!silent) {
          this.lowStockLoading = false;
        }
      }
    },
    async getUserBill(silent = false) {
      try {
        if (this.isAdmin) {
          await this.getAllOrders(silent);
          return;
        }
        const info = await apiRear.get(`/order/getOrderByUID=${this.uid}`);
        const rows = info.data.data || [];
        this.orders = rows.map(item => ({
          ...item,
          orderStatus: this.getOrderStatus(item)
        }));
        this.computeOrderStats();
        this.buildOrderTrendData();
        this.checkAutoOpenDialogs();
      } catch (err) {
        if (!silent) {
          this.$message.error('无法获取用户订单');
        }
      }
    },
    async getAllOrders(silent = false) {
      try {
        const info = await apiRear.get(`/order/admin/list`);
        const rows = info.data.data || [];
        this.orders = rows.map(item => ({
          ...item,
          orderStatus: this.getOrderStatus(item)
        }));
        this.computeOrderStats();
        this.buildOrderTrendData();
        this.buildRevenueTrendData();
        this.checkAutoOpenDialogs();
      } catch (err) {
        if (!silent) {
          this.$message.error('无法获取全部订单信息');
        }
      }
    },
    openStatusDialog(order) {
      if (Number(order.orderStatus) === 5) {
        this.orderDetail_handle(order);
        return;
      }
      this.statusDialogOrder = order;
      this.statusDialogVisible = true;
    },
    statusButtonClass(value) {
      if (value === 3) return 'status-success';
      if (value === 4) return 'status-danger';
      if (value === 2) return 'status-muted';
      if (value === 1 || value === 0) return 'status-pending';
      return '';
    },
    async applyOrderStatus(nextStatus) {
      const order = this.statusDialogOrder;
      if (!order) {
        return;
      }
      const currentStatus = Number(order.orderStatus);
      const targetStatus = Number(nextStatus);
      if (currentStatus === 1 && targetStatus === 0) {
        this.$message.warning('待取货状态不可修改为待支付');
        return;
      }
      if (targetStatus === currentStatus) {
        this.statusDialogVisible = false;
        return;
      }
      if (targetStatus === 4) {
        try {
          await this.$confirm('确认将订单状态改为已退款？', '退款确认', {
            confirmButtonText: '确认退款',
            cancelButtonText: '取消',
            type: 'warning'
          });
        } catch (err) {
          return;
        }
      }
      try {
        const response = await apiRear.post('/order/admin/status', {
          orderId: order.orderId,
          orderStatus: targetStatus
        });
        if (response.data.code === 1) {
          this.$message.success('订单状态已更新');
          order.orderStatus = targetStatus;
          this.statusDialogVisible = false;
          await this.getUserBill();
        } else {
          this.$message.error(response.data.message || '更新失败');
        }
      } catch (error) {
        this.$message.error('更新失败，请稍后重试');
      }
    },
    async updateOrderStatus(order) {
      const previous = order.orderStatus;
      try {
        const targetStatus = Number(order.orderStatus);
        if (targetStatus === 4 && Number(previous) !== 4) {
          await this.$confirm('确认将该订单设为已退款？系统将把订单金额退回给用户。', '二次确认', {
            confirmButtonText: '确认退款',
            cancelButtonText: '取消',
            type: 'warning'
          });
        }
        const response = await apiRear.post('/order/admin/status', {
          orderId: order.orderId,
          orderStatus: targetStatus
        });
        if (response.data.code === 1) {
          this.$message.success('订单状态已更新');
          await this.getUserBill();
        } else {
          this.$message.error(response.data.message || '更新失败');
          order.orderStatus = previous;
        }
      } catch (error) {
        order.orderStatus = previous;
        if (error !== 'cancel' && error !== 'close') {
          this.$message.error('更新失败，请稍后再试');
        }
      }
    },
    async getOrderSummary() {
      try {
        const info = await apiRear.get(`/order/getOrderInfoBy=${this.uid}`);
        const summary = info.data.data || {};
        this.orderSummary = summary;
        this.stats.pendingAmount = summary.totalPrice || 0;
      } catch (err) {
        this.stats.pendingAmount = 0;
      }
    },
    async orderDetail_handle(order) {
      const orderId = typeof order === 'object' ? order.orderId : order;
      this.chooseOrderId = orderId;
      this.selectedOrder = typeof order === 'object'
        ? order
        : this.orders.find(item => item.orderId === orderId) || null;
      this.pickupQrUrl = '';
      if (this.selectedOrder && this.selectedOrder.pickupToken) {
        await this.generatePickupQr(this.selectedOrder.pickupToken);
      }
      this.dialog_orderDetail = true;
      try {
        const info = await apiRear.get(`/order/getOrderDetailByOrderId=${orderId}`);
        this.orderDetail = info.data.data;
        this.$message.success('订单信息获取成功');
      } catch (err) {
        this.$message.error('无法获取订单详情');
      }
    },
    repayOrder(order) {
      if (!order || !order.orderId) {
        return;
      }
      this.dialog_orderDetail = false;
      this.$router.push({path: '/billPayment', query: {orderId: order.orderId}}).catch(() => {});
    },
    computeOrderStats() {
      const totalOrders = this.orders.length;
      const completedOrders = this.orders.filter(order => this.getOrderStatus(order) === 3).length;
      const pendingOrders = this.orders.filter(order => [0, 1, 5].includes(this.getOrderStatus(order))).length;
      const lastOrder = [...this.orders].sort((a, b) =>
        this.parseDate(this.getOrderCreateDate(b)) - this.parseDate(this.getOrderCreateDate(a)))[0];
      this.stats.totalOrders = totalOrders;
      this.stats.completedOrders = completedOrders;
      this.stats.pendingOrders = pendingOrders;
      this.stats.lastOrderDate = lastOrder ? this.formatDate(this.getOrderCreateDate(lastOrder)) : '';
    },
    buildOrderTrendData() {
      const bucket = new Map();
      this.orders.forEach(order => {
        const date = this.parseDate(this.getOrderCreateDate(order));
        if (!date) return;
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        bucket.set(monthKey, (bucket.get(monthKey) || 0) + 1);
      });
      const months = [];
      const now = new Date();
      for (let i = 5; i >= 0; i -= 1) {
        const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
        const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
        months.push(key);
      }
      const rows = months.map(month => ({
        month,
        orders: bucket.get(month) || 0
      }));
      this.orderTrendData.rows = rows;
      this.trendEmpty = rows.every(item => Number(item.orders || 0) === 0);
      this.renderTrendChart();
      this.resizeCharts();
    },
    buildRevenueTrendData() {
      if (!this.isAdmin) {
        return;
      }
      const bucket = new Map();
      const now = new Date();
      const months = [];
      for (let i = 5; i >= 0; i -= 1) {
        const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
        const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
        months.push(key);
      }
      const monthSet = new Set(months);
      this.orders.forEach(order => {
        const status = this.getOrderStatus(order);
        if (status !== 3) {
          return;
        }
        if (order && Number(order.authority) === 1) {
          return;
        }
        const date = this.parseDate(this.getOrderCompleteDate(order)) || this.parseDate(this.getOrderCreateDate(order));
        if (!date) {
          return;
        }
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        if (!monthSet.has(monthKey)) {
          return;
        }
        const amount = Number(order.totalPrice || 0);
        bucket.set(monthKey, (bucket.get(monthKey) || 0) + amount);
      });
      const rows = months.map(month => ({
        month,
        amount: bucket.get(month) || 0
      }));
      this.revenueTrendData.rows = rows;
      this.sixMonthRevenueTotal = rows.reduce((sum, item) => sum + Number(item.amount || 0), 0);
      this.revenueEmpty = rows.every(item => Number(item.amount || 0) === 0);
      this.renderRevenueChart();
      this.resizeCharts();
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
    getOrderCompleteDate(order) {
      if (!order) {
        return null;
      }
      return order.completeDate || order.CompleteDate || order.pickupTime || null;
    },
    getOrderCreateDate(order) {
      if (!order) {
        return null;
      }
      return order.createDate || order.CreateDate || order.create_time || order.createTime || null;
    },
    formatDate(value) {
      if (!value) {
        return '暂无';
      }
      const date = this.parseDate(value);
      if (!date) {
        return value;
      }
      const yyyy = date.getFullYear();
      const mm = String(date.getMonth() + 1).padStart(2, '0');
      const dd = String(date.getDate()).padStart(2, '0');
      return `${yyyy}-${mm}-${dd}`;
    },
    formatMoney(value) {
      if (value === undefined || value === null || value === '') {
        return '0.00';
      }
      const num = Number(value);
      if (Number.isNaN(num)) {
        return '0.00';
      }
      return num.toFixed(2);
    }
  }
}
</script>

<template>
  <div class="panel-theme user-info">
    <button v-if="isCustomer" class="back-button" @click="goHome">
      <i class="el-icon-arrow-left"></i>
    </button>
    <message-center
      ref="messageCenter"
      class="panel-message-center"
      :uid="uid"
    ></message-center>
    <el-dialog
      title="通过支付宝充值"
      :visible.sync="dialog_zfb"
      center
      width="30%">
      <el-image :src="zfb_url"></el-image>
    </el-dialog>

    <el-dialog
      title="通过微信支付充值"
      :visible.sync="dialog_wechat"
      center
      width="30%">
      <el-image :src="weChat_url"></el-image>
    </el-dialog>

    <el-dialog
      title="账户余额变动记录"
      :visible.sync="dialogBalanceHistory"
      width="900px"
      :close-on-click-modal="false"
      @open="loadBalanceHistory"
      @close="handleBalanceDialogClose"
    >
      <div class="balance-history-toolbar">
        <div class="balance-current">
          <span>当前可用余额</span>
          <strong>￥{{ formatMoney(userInfo.balance) }}</strong>
        </div>
        <div v-if="isCustomer" class="balance-withdraw">
          <el-button
            type="warning"
            @click="openWithdrawApplyDialog"
          >
            余额提现
          </el-button>
        </div>
      </div>
      <p class="balance-history-tip">变动信息包含时间、原因、金额。一天仅可申请一次余额提现。</p>
      <el-table
        :data="balanceHistory"
        v-loading="balanceHistoryLoading"
        border
        stripe
        height="380"
        empty-text="暂无余额变动记录"
      >
        <el-table-column label="时间" min-width="190">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="变动原因" min-width="250">
          <template slot-scope="scope">
            {{ scope.row.reason || getBalanceChangeTypeLabel(scope.row.changeType) }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template slot-scope="scope">
            {{ getBalanceChangeTypeLabel(scope.row.changeType) }}
          </template>
        </el-table-column>
        <el-table-column label="变动金额" width="140">
          <template slot-scope="scope">
            <span :class="Number(scope.row.changeAmount || 0) >= 0 ? 'amount-plus' : 'amount-minus'">
              {{ formatBalanceAmount(scope.row.changeAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动后余额" width="140">
          <template slot-scope="scope">
            {{ scope.row.afterBalance === null || scope.row.afterBalance === undefined ? '--' : formatMoney(scope.row.afterBalance) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog
      title="余额提现申请"
      :visible.sync="dialogWithdrawApply"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form label-width="110px">
        <el-form-item label="提现金额">
          <el-input-number
            v-model="withdrawAmount"
            :min="0"
            :max="Number(userInfo.balance || 0)"
            :step="10"
            :precision="2"
            controls-position="right"
            style="width: 100%;"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="支付宝手机号">
          <el-input
            :value="withdrawPhone"
            maxlength="11"
            placeholder="请输入11位手机号"
            @input="sanitizeWithdrawPhone"
          ></el-input>
          <p class="withdraw-note">审核通过后余额将打款至该手机号绑定的支付宝账户。</p>
          <p class="withdraw-note">一天仅能申请一次余额提现。</p>
          <p v-if="withdrawTodayPending" class="withdraw-pending">
            当前有待审核提现申请：￥{{ formatMoney(withdrawPendingAmount) }}，手机号 {{ withdrawPendingPhone || '--' }}
          </p>
          <p v-if="withdrawTodayApplied" class="withdraw-applied">
            您今天已申请过提现
          </p>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogWithdrawApply = false">取消</el-button>
        <el-button
          type="warning"
          :loading="withdrawSubmitting"
          :disabled="withdrawSubmitDisabled"
          @click="submitWithdrawRequest"
        >
          提交申请
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="余额提现记录"
      :visible.sync="dialogWithdrawRecords"
      width="1100px"
      :close-on-click-modal="false"
      @open="loadWithdrawRecords"
    >
      <el-table
        :data="withdrawRecords"
        v-loading="withdrawRecordsLoading"
        border
        stripe
        height="460"
        empty-text="暂无提现申请记录"
      >
        <el-table-column label="申请单号" width="110">
          <template slot-scope="scope">
            #{{ scope.row.id }}
          </template>
        </el-table-column>
        <el-table-column prop="uid" label="UID" width="110"></el-table-column>
        <el-table-column prop="username" label="用户名" width="140"></el-table-column>
        <el-table-column prop="cname" label="姓名" width="130"></el-table-column>
        <el-table-column prop="phone" label="手机号" width="130"></el-table-column>
        <el-table-column label="提现金额" width="120">
          <template slot-scope="scope">
            ￥{{ formatMoney(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="getWithdrawStatusTagType(scope.row.status)">
              {{ getWithdrawStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" min-width="170">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="处理时间" min-width="170">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="openWithdrawAuditDialog(scope.row.id)">查看审核</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog
      title="提现申请审核"
      :visible.sync="withdrawAuditDialogVisible"
      width="980px"
      :close-on-click-modal="false"
    >
      <div v-loading="withdrawAuditLoading">
        <div v-if="withdrawAuditRequest && withdrawAuditUserInfo" class="withdraw-audit-header">
          <div class="withdraw-audit-item">
            <span>申请单号</span>
            <strong>#{{ withdrawAuditRequest.id }}</strong>
          </div>
          <div class="withdraw-audit-item">
            <span>用户</span>
            <strong>{{ withdrawAuditUserInfo.username || withdrawAuditUserInfo.uid }}</strong>
          </div>
          <div class="withdraw-audit-item">
            <span>提现金额</span>
            <strong>￥{{ formatMoney(withdrawAuditRequest.amount) }}</strong>
          </div>
          <div class="withdraw-audit-item">
            <span>手机号</span>
            <strong>{{ withdrawAuditRequest.phone }}</strong>
          </div>
          <div class="withdraw-audit-item">
            <span>状态</span>
            <strong>
              {{ Number(withdrawAuditRequest.status) === 0 ? '待审核' : Number(withdrawAuditRequest.status) === 1 ? '已通过' : '已驳回' }}
            </strong>
          </div>
          <div class="withdraw-audit-item">
            <span>申请时间</span>
            <strong>{{ formatDateTime(withdrawAuditRequest.createTime) }}</strong>
          </div>
        </div>

        <el-descriptions v-if="withdrawAuditUserInfo" :column="3" border size="small" class="withdraw-user-info">
          <el-descriptions-item label="UID">{{ withdrawAuditUserInfo.uid }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ withdrawAuditUserInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="当前余额">￥{{ formatMoney(withdrawAuditUserInfo.balance) }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ withdrawAuditUserInfo.cname || '--' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ withdrawAuditUserInfo.cphone || '--' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ withdrawAuditUserInfo.email || '--' }}</el-descriptions-item>
        </el-descriptions>

        <p class="balance-history-tip">请先核对该用户余额变动记录，确认无异常后再执行审核。</p>
        <el-table
          :data="withdrawAuditHistory"
          border
          stripe
          height="260"
          empty-text="暂无余额变动记录"
        >
          <el-table-column label="时间" min-width="180">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="变动原因" min-width="260">
            <template slot-scope="scope">
              {{ scope.row.reason || getBalanceChangeTypeLabel(scope.row.changeType) }}
            </template>
          </el-table-column>
          <el-table-column label="类型" width="120">
            <template slot-scope="scope">
              {{ getBalanceChangeTypeLabel(scope.row.changeType) }}
            </template>
          </el-table-column>
          <el-table-column label="变动金额" width="120">
            <template slot-scope="scope">
              <span :class="Number(scope.row.changeAmount || 0) >= 0 ? 'amount-plus' : 'amount-minus'">
                {{ formatBalanceAmount(scope.row.changeAmount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="变动后余额" width="130">
            <template slot-scope="scope">
              {{ scope.row.afterBalance === null || scope.row.afterBalance === undefined ? '--' : formatMoney(scope.row.afterBalance) }}
            </template>
          </el-table-column>
        </el-table>

        <div v-if="withdrawAuditRequest && Number(withdrawAuditRequest.status) === 0" class="withdraw-review-actions">
          <el-input
            v-model="withdrawAuditRejectReason"
            placeholder="驳回原因（可选）"
            maxlength="80"
            clearable
            class="withdraw-reject-input"
          ></el-input>
          <el-button
            type="danger"
            :loading="withdrawAuditSubmitting"
            @click="rejectWithdrawRequest"
          >
            驳回
          </el-button>
          <el-button
            type="success"
            :loading="withdrawAuditSubmitting"
            @click="openPayoutConfirmDialog"
          >
            批准
          </el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      title="确认已打款"
      :visible.sync="withdrawPayoutConfirmVisible"
      width="420px"
      :close-on-click-modal="false"
    >
      <p v-if="withdrawAuditRequest" class="withdraw-pay-confirm">
        请确认已向手机号 <strong>{{ withdrawAuditRequest.phone }}</strong> 对应支付宝账户打款
        <strong>￥{{ formatMoney(withdrawAuditRequest.amount) }}</strong>。
      </p>
      <div slot="footer" class="dialog-footer">
        <el-button @click="withdrawPayoutConfirmVisible = false">取消</el-button>
        <el-button type="primary" :loading="withdrawAuditSubmitting" @click="approveWithdrawRequest">
          我已打款
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="查看订单"
      :visible.sync="dialog_bill"
      center
      width="60%">
      <div v-if="isAdmin" class="order-admin-toolbar">
        <el-input
          v-model="orderSearch.keyword"
          placeholder="订单号/用户名/电话/取件码"
          clearable
          class="order-search-input"
        ></el-input>
        <el-select v-model="orderSearch.status" placeholder="订单状态" class="order-search-select">
          <el-option label="全部状态" value="all"></el-option>
          <el-option
            v-for="option in statusFilterOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          ></el-option>
        </el-select>
        <el-date-picker
          v-model="orderSearch.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="order-search-date"
        ></el-date-picker>
        <el-button @click="resetOrderSearch">重置</el-button>
        <el-button type="primary" icon="el-icon-download" @click="exportOrders">导出</el-button>
      </div>
      <div v-if="isCustomer" class="order-dialog-actions">
        <el-button type="danger" icon="el-icon-delete" size="mini" @click="confirmDeleteOrders(selectedOrderIds)">
          删除所选
        </el-button>
      </div>
      <el-table
        :data="filteredOrders"
        stripe
        height="500"
        max-height="500"
        style="width: 100%"
        @selection-change="handleOrderSelectionChange">
        <el-table-column v-if="isCustomer" type="selection" width="55"></el-table-column>
        <el-table-column prop="orderId" label="订单号" sortable width="180"></el-table-column>
        <el-table-column v-if="isAdmin" prop="username" label="用户名" width="140"></el-table-column>
        <el-table-column v-if="isAdmin" prop="phone" label="电话" width="140">
          <template slot-scope="scope">
            {{ scope.row.phone || scope.row.cphone || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="createDate" label="创建时间" sortable></el-table-column>
        <el-table-column prop="completeDate" label="完成时间" sortable>
          <template slot-scope="scope">
            {{ scope.row.completeDate || '暂无' }}
          </template>
        </el-table-column>
        <el-table-column prop="pickupCode" label="取件码" width="120">
          <template slot-scope="scope">
            {{ scope.row.pickupCode || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" width="140">
          <template slot-scope="scope">
            <el-tag
              v-if="isAdmin"
              :type="getOrderStatusTagType(scope.row.orderStatus)"
              class="status-edit-trigger"
              @click="openStatusDialog(scope.row)"
            >
              {{ getOrderStatusLabel(scope.row.orderStatus) }}
            </el-tag>
            <el-tag v-else :type="getOrderStatusTagType(scope.row.orderStatus)">
              {{ getOrderStatusLabel(scope.row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pickupStatus" label="取货状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="getPickupStatusTagType(scope.row)">
              {{ getPickupStatusLabel(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="100">
          <template slot-scope="scope">
            <el-button @click="orderDetail_handle(scope.row)" type="text" size="small">查看详情</el-button>
            <el-button
              v-if="isCustomer"
              @click="deleteSingleOrder(scope.row.orderId)"
              type="text"
              size="small"
              :style="{color: dangerColor}"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog
      title="订单详情"
      :visible.sync="dialog_orderDetail"
      center
      width="55%">
      <el-table
        :data="orderDetail"
        border
        stripe
        height="500"
        max-height="500"
        style="width: 100%">
        <el-table-column label="图片" width="120">
          <template slot-scope="scope">
            <img
              :src="resolveProductImage(scope.row.productCode)"
              :alt="scope.row.productName || '文创产品'"
              class="order-thumb"
            />
          </template>
        </el-table-column>
        <el-table-column prop="productCode" label="文创产品编号" sortable width="180"></el-table-column>
        <el-table-column prop="productName" label="文创产品名称" sortable></el-table-column>
        <el-table-column prop="purchaseNum" label="购买数量" sortable></el-table-column>
        <el-table-column prop="price" label="单价" sortable></el-table-column>
      </el-table>
      <div class="pickup-section" v-if="selectedOrder">
        <div class="pickup-code">
          <h4>取件码</h4>
          <p>{{ selectedOrder.pickupCode || '待生成' }}</p>
          <el-tag :type="getPickupStatusTagType(selectedOrder)">
            {{ getPickupStatusLabel(selectedOrder) }}
          </el-tag>
        </div>
        <div class="pickup-qr">
          <h4>取货二维码</h4>
          <div v-if="pickupQrUrl" class="pickup-qr-image">
            <img :src="pickupQrUrl" alt="取货二维码" />
          </div>
          <p v-else class="pickup-qr-empty">暂无二维码</p>
          <small>二维码为加密内容，需管理员扫码确认取货。</small>
        </div>
      </div>
      <el-card shadow="hover" class="tips-card">
        <p style="font-weight: bold;font-size: 18px;">其他信息：</p>
        <p style="font-weight: bold;margin-left: 12px">姓名：{{ detailContact.name || '暂无' }}</p>
        <p style="font-weight: bold;margin-left: 12px">电话：{{ detailContact.phone || '暂无' }}</p>
        <p style="font-weight: bold;margin-left: 12px">地址：{{ detailContact.address || '暂无' }}</p>
        <p style="font-weight: bold;margin-left: 12px">备注：{{ detailContact.remark || '暂无' }}</p>
      </el-card>
      <el-card v-if="selectedOrder && isAdmin && getOrderStatus(selectedOrder) === 5" shadow="hover" class="tips-card refund-review">
        <p style="font-weight: bold;font-size: 18px;">退款申请</p>
        <p style="font-weight: bold;margin-left: 12px">原因：{{ selectedOrder.refundReason || '未填写' }}</p>
        <p style="font-weight: bold;margin-left: 12px">申请时间：{{ selectedOrder.refundRequestTime || '未知' }}</p>
        <div class="refund-review-actions">
          <el-button type="danger" round @click="approveRefund(selectedOrder)">同意退款</el-button>
          <el-button type="info" round @click="rejectRefund(selectedOrder)">驳回申请</el-button>
        </div>
      </el-card>
      <div class="order-detail-actions" v-if="selectedOrder && isCustomer">
        <el-button
          v-if="[1,3].includes(getOrderStatus(selectedOrder))"
          type="warning"
          round
          @click="openRefundDialog"
        >
          申请退款
        </el-button>
        <el-button
          v-else-if="getOrderStatus(selectedOrder) === 5"
          type="warning"
          round
          :disabled="true"
        >
          退款申请中
        </el-button>
        <el-button
          v-else-if="getOrderStatus(selectedOrder) === 4"
          type="primary"
          round
          @click="repayOrder(selectedOrder)"
        >
          重新支付
        </el-button>
      </div>
    </el-dialog>
    <el-dialog
      title="申请退款"
      :visible.sync="refundDialogVisible"
      center
      width="520px"
      :close-on-click-modal="false">
      <div class="refund-form">
        <p class="refund-tip">请选择退款原因：</p>
        <el-radio-group v-model="refundReason">
          <el-radio
            v-for="item in refundOptions"
            :key="item"
            :label="item"
          >{{ item }}</el-radio>
        </el-radio-group>
        <el-input
          v-if="refundReason === '其他'"
          v-model="refundOtherReason"
          type="textarea"
          :rows="4"
          maxlength="120"
          show-word-limit
          placeholder="请说明退款原因"
        ></el-input>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="refundSubmitting" @click="submitRefundRequest">确认申请</el-button>
      </div>
    </el-dialog>
    <el-dialog
      title="订单状态"
      :visible.sync="statusDialogVisible"
      center
      width="420px"
      custom-class="status-dialog"
      :close-on-click-modal="false"
      @close="statusDialogOrder = null">
      <div v-if="statusDialogOrder">
        <div class="status-dialog-title">订单 #{{ statusDialogOrder.orderId }}</div>
        <div class="status-dialog-subtitle">当前状态：{{ getOrderStatusLabel(statusDialogOrder.orderStatus) }}</div>
        <div class="status-dialog-meta">
          <span>用户：{{ statusDialogOrder.username || '--' }}</span>
          <span>电话：{{ statusDialogOrder.phone || statusDialogOrder.cphone || '--' }}</span>
        </div>
        <div class="status-button-grid">
          <el-button
            v-for="option in statusOptions"
            :key="option.value"
            class="status-btn"
            :class="statusButtonClass(option.value)"
            :disabled="Number(statusDialogOrder.orderStatus) === 1 && option.value === 0"
            @click="applyOrderStatus(option.value)"
          >
            <span class="status-btn-label">{{ option.label }}</span>
          </el-button>
        </div>
        <p class="status-dialog-hint">待取货状态不可修改为待支付</p>
      </div>
    </el-dialog>
    <el-dialog
      title="编辑资料"
      :close-on-click-modal="false"
      :visible.sync="dialogEditInfo"
      center
      width="1000px"
      @open="onDialogOpen"
      @close="onDialogClose">
      <div class="edit-grid">
        <el-form ref="userRef" :model="edit" :rules="rules" class="edit-form">
          <el-form-item label="用户名" prop="username" :label-width="searchLabelWidth" class="strong marginWidth">
            <el-input v-model="edit.username" autocomplete="off" suffix-icon="el-icon-notebook-2"
                      placeholder="请输入内容"></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="cname" :label-width="searchLabelWidth" class="strong marginWidth">
            <el-input v-model="edit.cname" autocomplete="off" suffix-icon="el-icon-user"
                      placeholder="请输入内容"></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email" :label-width="searchLabelWidth" class="strong marginWidth">
            <el-input v-model="edit.email" autocomplete="off" suffix-icon="el-icon-message"
                      placeholder="请输入内容"></el-input>
          </el-form-item>
          <el-form-item label="年龄" prop="cage" :label-width="searchLabelWidth" class="strong marginWidth">
            <el-input v-model.number="edit.cage" autocomplete="off" suffix-icon="el-icon-edit-outline"
                      placeholder="请输入内容" type="number" min="0"></el-input>
          </el-form-item>
          <el-form-item label="电话" prop="cphone" :label-width="searchLabelWidth" class="strong marginWidth">
            <el-input v-model="edit.cphone" autocomplete="off" suffix-icon="el-icon-mobile-phone"
                      placeholder="请输入内容"></el-input>
          </el-form-item>
        </el-form>
        <el-form :model="edit" :rules="rules" class="edit-form">
          <el-form-item label="性别" prop="csex" :label-width="searchLabelWidth" class="strong marginWidth">
            <el-radio-group v-model="edit.csex">
              <el-radio-button label="F" border>男</el-radio-button>
              <el-radio-button label="M" border>女</el-radio-button>
              <el-radio-button label="U" border>不愿透露</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="地址" prop="caddress" :label-width="searchLabelWidth" class="strong marginWidth">
            <el-input v-model="edit.caddress" autocomplete="off" suffix-icon="el-icon-house"
                      placeholder="请输入内容"></el-input>
          </el-form-item>
          <el-form-item label="备注" prop="cremark" :label-width="searchLabelWidth" class="strong marginWidth">
            <el-input v-model="edit.cremark" autocomplete="off" suffix-icon="el-icon-edit-outline"
                      placeholder="请输入内容（限制150字）" type="textarea" maxlength="150" show-word-limit
                      :autosize="{ minRows: 6, maxRows: 8}"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div v-if="emailChanged" class="email-verify">
        <div v-if="needsOldEmailVerify" class="email-verify-section">
          <div class="email-verify-title">验证旧邮箱</div>
          <div class="email-verify-tip">当前邮箱需通过人机验证与验证码确认</div>
          <slider-captcha
            v-model="emailVerify.old.captchaPassed"
            @success="handleOldCaptchaSuccess"
          ></slider-captcha>
          <div class="email-verify-form">
            <el-input
              v-model="edit.oldEmailCode"
              maxlength="6"
              placeholder="输入旧邮箱验证码"
              @input="sanitizeEmailCode('oldEmailCode')"
            ></el-input>
            <el-button
              type="primary"
              plain
              :loading="emailVerify.old.sending"
              :disabled="emailVerify.old.countdown > 0 || !emailVerify.old.captchaPassed"
              @click="sendOldEmailCode"
            >
              {{ emailVerify.old.countdown > 0 ? (emailVerify.old.countdown + 's') : '发送验证码' }}
            </el-button>
          </div>
        </div>
        <div v-if="needsNewEmailVerify" class="email-verify-section">
          <div class="email-verify-title">验证新邮箱</div>
          <div class="email-verify-tip">请完成人机验证后发送验证码</div>
          <slider-captcha
            v-model="emailVerify.next.captchaPassed"
            @success="handleNewCaptchaSuccess"
          ></slider-captcha>
          <div class="email-verify-form">
            <el-input
              v-model="edit.newEmailCode"
              maxlength="6"
              placeholder="输入新邮箱验证码"
              @input="sanitizeEmailCode('newEmailCode')"
            ></el-input>
            <el-button
              type="primary"
              plain
              :loading="emailVerify.next.sending"
              :disabled="emailVerify.next.countdown > 0 || !emailVerify.next.captchaPassed"
              @click="sendNewEmailCode"
            >
              {{ emailVerify.next.countdown > 0 ? (emailVerify.next.countdown + 's') : '发送验证码' }}
            </el-button>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="updateUserInfo" :disabled="isEditInvalid">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="修改密码"
      :close-on-click-modal="false"
      :visible.sync="dialogUpdatePassword"
      center
      width="500px">
      <el-form ref="passwordRef" :model="edit" :rules="rules" class="dialog-form">
        <el-form-item label="旧密码" prop="oldPassword" :label-width="searchLabelWidth" class="strong marginWidth">
          <el-input v-model="edit.oldPassword" autocomplete="off" suffix-icon="el-icon-lock"
                    placeholder="请输入旧密码" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword" :label-width="searchLabelWidth" class="strong marginWidth">
          <el-input v-model="edit.newPassword" autocomplete="off" suffix-icon="el-icon-lock"
                    placeholder="请输入新密码" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="checkPassword" :label-width="searchLabelWidth" class="strong marginWidth">
          <el-input v-model="edit.checkPassword" autocomplete="off" suffix-icon="el-icon-lock"
                    placeholder="请再次输入新密码" type="password" show-password></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelUpdatePassword">取消</el-button>
        <el-button type="primary" @click="updatePassword" :disabled="isPasswordInvalid">确认</el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="注销用户"
      :close-on-click-modal="false"
      :visible.sync="selfDeleteDialogVisible"
      center
      width="520px">
      <div class="self-delete-body">
        <p class="self-delete-title">注销账号后，所有数据不可找回。</p>
        <p class="self-delete-desc">请确认已备份必要信息。</p>
        <p v-if="!selfDeleteAllowed" class="self-delete-warning">
          当前账户余额不为 0，无法注销，请联系管理员处理。
        </p>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="selfDeleteDialogVisible = false">取消</el-button>
        <el-button type="danger" :disabled="!selfDeleteAllowed" @click="confirmSelfDeleteStep">
          确认注销
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="危险操作确认"
      :close-on-click-modal="false"
      :visible.sync="selfDeleteConfirmVisible"
      center
      width="560px"
      custom-class="self-delete-confirm-dialog">
      <div class="self-delete-confirm-body">
        <p>此操作不可逆，所有数据将被永久删除。</p>
        <p class="self-delete-confirm-tip">请输入以下文本以继续：</p>
        <p style="font-size: 30px;">我已确认此操作带来的风险，确认注销</p>
        <el-input
          v-model="selfDeleteInput"
          type="textarea"
          :rows="3"
          placeholder="我已确认此操作带来的风险，确认注销"
        ></el-input>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="selfDeleteConfirmVisible = false">取消</el-button>
        <el-button type="danger" :disabled="!selfDeletePhraseOk" @click="submitSelfDelete">
          注销账号
        </el-button>
      </div>
    </el-dialog>

    <section class="profile-card panel-card" v-loading="loading">
      <div class="profile-header">
        <el-avatar :size="120" :src="circleUrl"></el-avatar>
        <div class="profile-meta">
          <p class="profile-eyebrow">个人中心</p>
          <h2 class="profile-title">{{ userInfo.username || '用户' }}</h2>
          <p class="profile-subtitle">UID：{{ uid }} · CNO：{{ userInfo.cno || '-' }}</p>
          <div class="profile-tags">
            <el-tag v-if="authority === 1" type="danger" effect="dark">管理员</el-tag>
            <el-tag v-else effect="dark">顾客</el-tag>
            <el-tag type="success" effect="dark">余额 {{ formatMoney(userInfo.balance) }}</el-tag>
          </div>
        </div>
        <div class="profile-actions">
          <el-button type="primary" icon="el-icon-edit-outline" @click="dialogEditInfo = true" round>
            修改资料
          </el-button>
          <el-button type="primary" icon="el-icon-lock" @click="dialogUpdatePassword = true" round>
            修改密码
          </el-button>
          <el-button
            v-if="isCustomer"
            type="danger"
            icon="el-icon-warning"
            @click="openSelfDeleteDialog"
            round
          >
            注销用户
          </el-button>
        </div>
      </div>
      <el-divider></el-divider>
      <el-descriptions class="info-desc" :column="3" :size="size" border>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-user"></i>
            <span class="strong">UID</span>
          </template>
          <span class="strong">{{ uid }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-user"></i>
            <span class="strong">CNO</span>
          </template>
          <span class="strong">{{ userInfo.cno }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-user"></i>
            <span class="strong">用户名</span>
          </template>
          <span class="strong">{{ userInfo.username }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-tickets"></i>
            <span class="strong">身份</span>
          </template>
          <template>
            <el-tag v-if="authority === 1" type="danger" effect="dark">管理员</el-tag>
            <el-tag v-else effect="dark">顾客</el-tag>
          </template>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-user"></i>
            <span class="strong">姓名</span>
          </template>
          <span class="strong">{{ userInfo.cname }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-location-outline"></i>
            <span class="strong">地址</span>
          </template>
          <span class="strong">{{ userInfo.caddress }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-message"></i>
            <span class="strong">邮箱</span>
          </template>
          <span class="strong">{{ userInfo.email }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i v-if="userInfo.csex === 'F'" class="el-icon-male"></i>
            <i v-else-if="userInfo.csex === 'M'" class="el-icon-female"></i>
            <i v-else class="el-icon-user"></i>
            <span class="strong">性别</span>
          </template>
          <span class="strong">{{ formattedSex }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-time"></i>
            <span class="strong">年龄</span>
          </template>
          <span class="strong">{{ formattedAge }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-mobile-phone"></i>
            <span class="strong">电话</span>
          </template>
          <span class="strong">{{ userInfo.cphone }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-message"></i>
            <span class="strong">创建日期</span>
          </template>
          <span class="strong">{{ userInfo.cdate }}</span>
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-message"></i>
            <span class="strong">备注</span>
          </template>
          <span class="strong">{{ userInfo.cremark }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </section>
    <section class="dashboard-hero panel-card">
      <div class="hero-left">
        <p class="hero-eyebrow">ZHONGSHAN · DASHBOARD</p>
        <h2>欢迎回来，{{ userInfo.username || '用户' }}</h2>
        <p class="hero-desc">在这里查看订单、管理账户余额与个人资料。</p>
        <div class="hero-tags">
          <span>订单管理</span>
          <span>文创收藏</span>
          <span>个人信息</span>
        </div>
      </div>
      <div class="hero-actions">
        <el-button type="primary" icon="el-icon-tickets" @click="openOrderDialog('all')">查看订单</el-button>
        <el-button type="success" icon="el-icon-shopping-bag-2" @click="$router.push('/culturalProducts')">
          前往商城
        </el-button>
      </div>
    </section>

    <section class="stats-grid">
      <div class="stat-card panel-card clickable" @click="handleTotalOrdersClick">
        <div class="stat-label">累计订单</div>
        <div class="stat-value">{{ stats.totalOrders }}</div>
        <div class="stat-foot">最近下单 {{ stats.lastOrderDate || '暂无' }}</div>
      </div>
      <div v-if="isAdmin" class="stat-card panel-card clickable" @click="openWithdrawRecordsDialog">
        <div class="stat-label">余额提现记录</div>
        <div class="stat-value">{{ stats.withdrawRequestCount }}</div>
        <div class="stat-foot">查看全部申请（待批准/已批准/已驳回）</div>
      </div>
      <div v-if="isAdmin" class="stat-card panel-card clickable" @click="openOrderDialog('pending')">
        <div class="stat-label">待处理</div>
        <div class="stat-value">{{ stats.pendingOrders }}</div>
        <div class="stat-foot">待发货 / 待确认</div>
      </div>
      <div class="stat-card panel-card clickable" @click="openOrderDialog('pending')">
        <div class="stat-label">待支付金额</div>
        <div class="stat-value">￥{{ formatMoney(stats.pendingAmount) }}</div>
        <div class="stat-foot">订单汇总金额</div>
      </div>
      <div class="stat-card panel-card clickable" @click="openEditInfo">
        <div class="stat-label">账户余额</div>
        <div class="stat-value">￥{{ formatMoney(userInfo.balance) }}</div>
        <div class="stat-foot">可用于下单 / 充值</div>
      </div>
    </section>

    <section class="account-grid">
      <el-card shadow="hover" class="panel-card account-card">
        <div class="card-header">
          <h3>账户充值</h3>
          <span>快捷支付</span>
        </div>
        <div class="account-balance">
          <span>可用余额</span>
          <strong>￥{{ formatMoney(userInfo.balance) }}</strong>
        </div>
        <div class="account-actions">
          <el-button class="border-radius custom-zfb" @click="dialog_zfb = true">支付宝</el-button>
          <el-button class="border-radius custom-weChatPay" @click="dialog_wechat = true">微信支付</el-button>
        </div>
      </el-card>
      <el-card v-if="isAdmin" shadow="hover" class="panel-card inventory-card" v-loading="lowStockLoading">
        <div class="card-header">
          <h3>库存告警</h3>
          <span>库存少于 50</span>
        </div>
        <div class="inventory-list">
          <div v-for="item in lowStockProducts" :key="item.productCode" class="inventory-item">
            <div>
              <p class="inventory-name">{{ item.productName }}</p>
              <p class="inventory-meta">编号 {{ item.productCode }} · {{ item.category || '暂无分类' }}</p>
            </div>
            <div class="inventory-stock">{{ item.stock }}</div>
          </div>
          <p v-if="!lowStockProducts.length" class="empty-text">暂无库存告警</p>
        </div>
      </el-card>
      <el-card shadow="hover" class="panel-card order-list-card">
        <div class="card-header">
          <h3>最近订单</h3>
          <span>近 4 单</span>
        </div>
        <div class="order-list">
          <div
            v-for="item in recentOrders"
            :key="item.orderId"
            class="order-item clickable"
            @click="orderDetail_handle(item)"
          >
            <div>
              <p class="order-id">订单 #{{ item.orderId }}</p>
              <p class="order-date">{{ formatDate(getOrderCreateDate(item)) }}</p>
            </div>
            <el-tag :type="getOrderStatusTagType(item.orderStatus)">
              {{ getOrderStatusLabel(item.orderStatus) }}
            </el-tag>
          </div>
          <p v-if="!recentOrders.length" class="empty-text">暂无订单记录</p>
        </div>
      </el-card>
    </section>

    <section class="dashboard-grid">
      <el-card shadow="hover" class="panel-card chart-card">
        <div class="card-header">
          <h3>近 6 个月订单趋势</h3>
          <span>订单数量</span>
        </div>
        <div ref="trendChart" class="chart-canvas"></div>
        <p v-if="trendEmpty" class="chart-hint">暂无订单数据，完成下单后将展示趋势。</p>
      </el-card>
      <el-card v-if="isAdmin" shadow="hover" class="panel-card chart-card revenue-card">
        <div class="card-header">
          <h3>近 6 个月成交额</h3>
          <span>已完成订单金额</span>
        </div>
        <div ref="revenueChart" class="chart-canvas revenue-chart"></div>
        <p v-if="revenueEmpty" class="chart-hint">暂无成交额数据。</p>
      </el-card>
      <el-card shadow="hover" class="panel-card chart-card">
        <div class="card-header">
          <h3>文创品类偏好</h3>
          <span>按购买次数</span>
        </div>
        <div ref="categoryPie" class="chart-canvas"></div>
        <p v-if="categoryPieEmpty" class="chart-hint">暂无数据，完成购买后将展示偏好统计。</p>
      </el-card>
    </section>

    <section v-if="isAdmin" class="dashboard-grid">
      <el-card shadow="hover" class="panel-card chart-card full-width-card">
        <div class="card-header">
          <h3>商城文创品类</h3>
          <span>按商品数量</span>
        </div>
        <div ref="productCategoryPie" class="chart-canvas"></div>
        <p v-if="productCategoryEmpty" class="chart-hint">暂无商品分类数据。</p>
      </el-card>
    </section>

  </div>
</template>

<style scoped>
.strong {
  font-weight: bold;
}

.border-radius {
  border-radius: 10px;
}

.custom-zfb {
  background: #1e78ff;
  border: none;
  color: #ffffff;
  width: 180px;
  height: 64px;
  padding: 0;
  font-size: 25px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.custom-weChatPay {
  background: #2db55d;
  border: none;
  color: #ffffff;
  width: 200px;
  height: 64px;
  padding: 0;
  font-size: 25px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.edit-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
}

.edit-form {
  width: 100%;
}

.marginWidth {
  margin-bottom: 16px;
}

.dialog-form {
  width: 100%;
}

.profile-card {
  padding: 24px;
  margin: 10px 6px 24px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.profile-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-eyebrow {
  font-size: 12px;
  letter-spacing: 0.2em;
  color: var(--teal);
  margin: 0;
}

.profile-title {
  margin: 0;
  font-size: 24px;
}

.profile-subtitle {
  margin: 0;
  color: var(--ink-soft);
  font-size: 13px;
}

.profile-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.profile-actions {
  margin-left: auto;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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

.dashboard-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 28px;
  margin-bottom: 24px;
}

.hero-eyebrow {
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.2em;
  color: var(--teal);
  font-weight: 600;
  margin-bottom: 8px;
}

.hero-left h2 {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;
}

.hero-desc {
  margin: 0 0 16px;
  color: var(--ink-soft);
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-tags span {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(31, 122, 109, 0.12);
  color: var(--teal);
  font-size: 12px;
  font-weight: 600;
}

.hero-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  padding: 18px 20px;
}

.stat-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--ink-soft);
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--ink);
}

.stat-foot {
  margin-top: 8px;
  font-size: 12px;
  color: var(--ink-soft);
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.full-width-card {
  grid-column: 1 / -1;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
}

.card-header span {
  font-size: 12px;
  color: var(--ink-soft);
}

.order-dialog-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.order-admin-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}

.order-search-input {
  width: 220px;
}

.order-search-select {
  width: 140px;
}

.order-search-date {
  min-width: 240px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-thumb {
  width: 88px;
  height: 88px;
  border-radius: 8px;
  object-fit: cover;
  background: rgba(31, 122, 109, 0.08);
  border: 1px solid rgba(31, 122, 109, 0.12);
}

.inventory-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.inventory-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(199, 90, 49, 0.08);
}

.inventory-name {
  margin: 0;
  font-weight: 600;
}

.inventory-meta {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--ink-soft);
}

.inventory-stock {
  font-size: 20px;
  font-weight: 700;
  color: #c75a31;
}

.order-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background: rgba(31, 122, 109, 0.06);
  border-radius: 12px;
}

.status-edit-trigger {
  cursor: pointer;
}

.status-dialog-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 6px;
}

.status-dialog-subtitle {
  font-size: 14px;
  color: var(--ink-soft);
  margin-bottom: 10px;
}

.status-dialog-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: var(--ink-soft);
}

.status-button-grid {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: center;
  min-height: 360px;
  gap: 50px;
  margin-top: 16px;
}

.status-btn {
  width: 100%;
  height: 56px;
  font-size: 16px;
  border-radius: 16px;
  padding: 0;
  text-align: center;
}

.status-btn > span {
  width: 100%;
  text-align: center;
}

.status-btn-label {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  text-align: center;
}

.status-btn ::v-deep span {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  text-align: center;
}

.status-btn.status-pending {
  background: #f1a447;
  border-color: #f1a447;
  color: #ffffff;
}

.status-btn.status-success {
  background: #2f8f5b;
  border-color: #2f8f5b;
  color: #ffffff;
}

.status-btn.status-muted {
  background: #909399;
  border-color: #909399;
  color: #ffffff;
}

.status-btn.status-danger {
  background: #e45b5b;
  border-color: #e45b5b;
  color: #ffffff;
}

.status-btn[disabled] {
  opacity: 0.55;
  cursor: not-allowed;
}

.status-dialog-hint {
  margin-top: 12px;
  font-size: 12px;
  color: var(--ink-soft);
  text-align: center;
}

.email-verify {
  margin-top: 20px;
  padding: 16px;
  border-radius: 16px;
  background: rgba(31, 122, 109, 0.08);
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.email-verify-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.email-verify-title {
  font-size: 15px;
  font-weight: 600;
}

.email-verify-tip {
  font-size: 12px;
  color: var(--ink-soft);
}

.email-verify-form {
  display: grid;
  grid-template-columns: 1fr 140px;
  gap: 12px;
  align-items: center;
}

.chart-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--ink-soft);
  text-align: center;
}

.chart-canvas {
  width: 100%;
  height: 300px;
}

.revenue-chart {
  height: 300px;
}

.clickable {
  cursor: pointer;
}

.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(25, 45, 40, 0.12);
}

.pickup-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-top: 18px;
  padding: 12px;
  border-radius: 16px;
  background: rgba(31, 122, 109, 0.08);
}

.pickup-code h4,
.pickup-qr h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--ink);
}

.pickup-code p {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 700;
  color: var(--teal);
}

.pickup-qr-image img {
  width: 140px;
  height: 140px;
  border-radius: 8px;
  border: 1px solid rgba(31, 122, 109, 0.2);
  background: #fff;
}

.pickup-qr-empty {
  margin: 0;
  color: var(--ink-soft);
  font-size: 12px;
}

.order-detail-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.refund-review {
  margin-top: 16px;
}

.refund-review-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.refund-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.refund-tip {
  margin: 0;
  font-weight: 600;
}

.self-delete-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: var(--ink);
}

.self-delete-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.self-delete-desc {
  margin: 0;
  color: var(--ink-soft);
}

.self-delete-warning {
  margin: 0;
  color: #e83a3a;
  font-weight: 600;
}

.self-delete-confirm-dialog .el-dialog__body {
  background: rgba(232, 58, 58, 0.08);
}

.self-delete-confirm-dialog .el-dialog__header {
  background: rgba(232, 58, 58, 0.15);
}

.self-delete-confirm-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: #7a1f1f;
  font-weight: 600;
}

.self-delete-confirm-tip {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
}

.order-id {
  margin: 0;
  font-weight: 600;
}

.order-date {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--ink-soft);
}

.empty-text {
  margin: 0;
  color: var(--ink-soft);
  font-size: 12px;
}

.account-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.user-info.admin-view {
  min-width: 2000px;
}

.account-balance {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  margin-bottom: 16px;
  border-radius: 12px;
  background: rgba(31, 122, 109, 0.08);
}

.account-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.balance-history-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  margin-bottom: 10px;
}

.balance-current {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.balance-current span {
  color: var(--ink-soft);
  font-size: 12px;
}

.balance-current strong {
  font-size: 24px;
  color: var(--teal);
}

.balance-withdraw {
  display: flex;
  align-items: center;
  gap: 10px;
}

.balance-history-tip {
  margin: 0 0 12px;
  color: var(--ink-soft);
  font-size: 12px;
}

.withdraw-note {
  margin: 8px 0 0;
  color: var(--ink-soft);
  font-size: 12px;
}

.withdraw-applied {
  margin: 8px 0 0;
  color: #e45b5b;
  font-size: 13px;
  font-weight: 600;
}

.withdraw-pending {
  margin: 8px 0 0;
  color: #c75a31;
  font-size: 12px;
}

.withdraw-audit-header {
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.withdraw-audit-item {
  background: rgba(31, 122, 109, 0.08);
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.withdraw-audit-item span {
  font-size: 12px;
  color: var(--ink-soft);
}

.withdraw-audit-item strong {
  font-size: 14px;
  color: var(--ink);
}

.withdraw-user-info {
  margin: 8px 0 12px;
}

.withdraw-review-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-end;
}

.withdraw-reject-input {
  width: 320px;
}

.withdraw-pay-confirm {
  margin: 0;
  line-height: 1.7;
  color: var(--ink);
}

.amount-plus {
  color: #2f8f5b;
  font-weight: 700;
}

.amount-minus {
  color: #e45b5b;
  font-weight: 700;
}

.shipping-list p {
  margin: 6px 0;
  color: var(--ink-soft);
}

.panel-card {
  border-radius: 20px !important;
  border: 1px solid rgba(31, 122, 109, 0.12);
  box-shadow: var(--shadow-soft);
  background: var(--paper) !important;
  color: var(--ink);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
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
  z-index: 50;
}

.back-button:hover {
  background: #17675d;
}

.panel-message-center {
  position: fixed;
  top: 18px;
  right: 18px;
  z-index: 50;
}

.panel-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 26px 60px rgba(25, 45, 40, 0.16);
}

.tips-card {
  margin-top: 20px;
  border-radius: 20px !important;
  border: 1px solid rgba(31, 122, 109, 0.16);
  background: rgba(255, 255, 255, 0.94) !important;
  box-shadow: var(--shadow-soft);
}

.chart-card {
  border-radius: 24px !important;
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

.panel-theme ::v-deep .el-table {
  border-radius: 14px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.96);
}

.panel-theme ::v-deep .el-table th {
  background: rgba(31, 122, 109, 0.08);
  color: var(--ink);
}

@media (max-width: 960px) {
  .profile-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .dashboard-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions {
    flex-direction: row;
    flex-wrap: wrap;
  }
}
</style>
