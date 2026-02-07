
<script>
import {jwtDecode} from "jwt-decode";
import {apiRear} from "@/axios";
import _ from 'lodash';
import {simpleDataConverter} from 'v-charts-v2/lib/converter';
import QRCode from 'qrcode';
import MessageCenter from '@/components/MessageCenter.vue';

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
    MessageCenter
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
    this.handler = {
      dataConverter: simpleDataConverter
    };
    return {
      token: sessionStorage.getItem('token') || '',
      loading: true,
      chartsLoading: false,
      dialogEditInfo: false,
      dialogUpdatePassword: false,
      dialog_zfb: false,
      dialog_wechat: false,
      dialog_bill: false,
      dialog_orderDetail: false,
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
        pendingOrders: 0,
        pendingAmount: 0,
        lastOrderDate: ''
      },
      categoryPieData: {
        columns: ['category', 'totalQuantity'],
        rows: []
      },
      orderTrendData: {
        columns: ['month', 'orders'],
        rows: []
      },
      pieSettings: {
        radius: 90,
        offsetY: 0
      },
      pieExtend: {
        legend: {
          orient: 'vertical',
          right: 0,
          top: 'center'
        },
        animationDuration: 1200,
        animationEasing: 'cubicOut'
      },
      orderFilter: 'all',
      orderSearch: {
        keyword: '',
        status: 'all',
        dateRange: []
      },
      selectedOrder: null,
      pickupQrUrl: '',
      selectedOrderIds: [],
      statusOptions: [
        { value: 0, label: '待支付' },
        { value: 1, label: '待取货' },
        { value: 2, label: '已关闭' },
        { value: 3, label: '已完成' },
        { value: 4, label: '已退款' }
      ],
      edit: {
        username: '',
        email: '',
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
    isEditInvalid() {
      const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      const emailValid = !this.edit.email || emailPattern.test(this.edit.email);
      const ageValid = this.edit.cage === '' || this.edit.cage === null || Number.isInteger(this.edit.cage);
      return !this.edit.username || !this.edit.cphone || !emailValid || !ageValid;
    },
    isPasswordInvalid() {
      return !this.edit.oldPassword || !this.edit.newPassword || !this.edit.checkPassword ||
        this.edit.newPassword !== this.edit.checkPassword;
    },
    recentOrders() {
      const sorted = [...this.orders].sort((a, b) => {
        return this.parseDate(b.createDate) - this.parseDate(a.createDate);
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
            const date = this.parseDate(order.createDate);
            if (!date) return false;
            const time = date.getTime();
            if (startTime && time < startTime) return false;
            if (endTime && time > endTime) return false;
            return true;
          });
        }
        return data;
      }
      if (this.orderFilter === 'completed') {
        return data.filter(order => this.getOrderStatus(order) === 3 || order.completeDate);
      }
      if (this.orderFilter === 'pending') {
        const status = order => this.getOrderStatus(order);
        return data.filter(order => [0, 1].includes(status(order)));
      }
      return data;
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
    }
  },
  created() {
    this.initEditUid();
    this.fetchData();
    this.getUserBill();
    this.getOrderSummary();
  },
  mounted() {
    this.getProductCategoryStats();
  },
  methods: {
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
    checkAutoOpenDialogs() {
      const openOrders = sessionStorage.getItem('openOrders');
      if (openOrders) {
        sessionStorage.removeItem('openOrders');
        this.openOrderDialog('all');
      }
      const detailId = sessionStorage.getItem('openOrderDetailId');
      if (detailId) {
        sessionStorage.removeItem('openOrderDetailId');
        this.orderDetail_handle(Number(detailId));
      }
    },
    openEditInfo() {
      this.dialogEditInfo = true;
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
      const found = this.statusOptions.find(item => item.value === Number(status));
      return found ? found.label : '待支付';
    },
    getOrderStatusTagType(status) {
      const value = Number(status);
      if (value === 3) return 'success';
      if (value === 4) return 'info';
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
      this.dialogEditInfo = false;
    },
    cancelUpdatePassword() {
      this.edit.newPassword = '';
      this.edit.oldPassword = '';
      this.edit.checkPassword = '';
      this.$refs.passwordRef.resetFields();
      this.dialogUpdatePassword = false;
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
    updateUserInfo() {
      if (!this.edit.csex) {
        this.edit.csex = 'U';
      }
      if (!Number.isInteger(this.edit.cage)) {
        this.edit.cage = 0;
      }
      apiRear.post(`/userInfo/update`, this.edit)
        .then(() => {
          this.fetchData();
          this.$message({
            type: 'success',
            message: '资料更新成功'
          });
          this.loading = true;
          this.dialogEditInfo = false;
        }).catch(err => {
        console.log(err);
        this.$message({
          type: 'error',
          message: '修改资料失败，请检查输入格式'
        });
      });
    },
    fetchData() {
      apiRear.get(`/userInfo/personal/${this.uid}`).then(res => {
        this.userInfo = res.data.data;
        this.edit = _.cloneDeep(res.data.data);
        if (!this.edit.csex) {
          this.edit.csex = 'U';
        }
        this.edit.password = '';
        this.loading = false;
      }).catch(err => {
        console.log(err);
        this.$message.error('获取用户信息失败，请重新登录后重试');
      });
    },
    async getProductCategoryStats() {
      this.chartsLoading = true;
      try {
        const info = await apiRear.get(`/cultural-products/category-statistics`);
        this.categoryPieData.rows = info.data.data;
      } catch (err) {
        this.$message.error('无法获取文创分类数据');
      } finally {
        this.chartsLoading = false;
      }
    },
    async getUserBill() {
      try {
        if (this.isAdmin) {
          await this.getAllOrders();
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
        this.$message.error('无法获取用户订单');
      }
    },
    async getAllOrders() {
      try {
        const info = await apiRear.get(`/order/admin/list`);
        const rows = info.data.data || [];
        this.orders = rows.map(item => ({
          ...item,
          orderStatus: this.getOrderStatus(item)
        }));
        this.computeOrderStats();
        this.buildOrderTrendData();
        this.checkAutoOpenDialogs();
      } catch (err) {
        this.$message.error('无法获取全部订单信息');
      }
    },
    /*
    syncOrderNotifications() {
      if (!this.uid || !this.isCustomer) {
        return;
      }
      const context = {uid: this.uid, username: this.userInfo.username || ''};
      this.orders.forEach(order => {
        const status = this.getOrderStatus(order);
        if (status === 0) {
          addMessage(context, {
            id: `order-${order.orderId}-pending`,
            title: '订单待支付',
            content: `订单 ${order.orderId} 尚未支付，请在订单详情中完成付款。`,
            time: formatMessageTime(),
            read: false,
            action: '/userInfo'
          });
        }
        if (status === 1) {
          addMessage(context, {
            id: `order-${order.orderId}-pickup`,
            title: '订单待取货',
            content: `订单 ${order.orderId} 待取货，请留意取件码与取货时间。`,
            time: formatMessageTime(),
            read: false,
            action: '/userInfo'
          });
        }
        if (status === 4) {
          addMessage(context, {
            id: `order-${order.orderId}-refund`,
            title: '订单已退款',
            content: `订单 ${order.orderId} 已退款，可进入订单详情重新支付。`,
            time: formatMessageTime(),
            read: false,
            action: '/userInfo'
          });
        }
      });
      this.$nextTick(() => {
        if (this.$refs.messageCenter) {
          this.$refs.messageCenter.refresh();
        }
      });
    },
    */
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
      const pendingOrders = this.orders.filter(order => [0, 1].includes(this.getOrderStatus(order))).length;
      const lastOrder = [...this.orders].sort((a, b) => this.parseDate(b.createDate) - this.parseDate(a.createDate))[0];
      this.stats.totalOrders = totalOrders;
      this.stats.completedOrders = completedOrders;
      this.stats.pendingOrders = pendingOrders;
      this.stats.lastOrderDate = lastOrder ? this.formatDate(lastOrder.createDate) : '';
    },
    buildOrderTrendData() {
      const bucket = new Map();
      this.orders.forEach(order => {
        const date = this.parseDate(order.createDate);
        if (!date) return;
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        bucket.set(monthKey, (bucket.get(monthKey) || 0) + 1);
      });
      const rows = Array.from(bucket.entries())
        .sort((a, b) => a[0].localeCompare(b[0]))
        .map(([month, orders]) => ({month, orders}));
      this.orderTrendData.rows = rows.length ? rows.slice(-6) : [];
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
    />
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
            v-for="option in statusOptions"
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
            <el-select
              v-if="isAdmin"
              v-model="scope.row.orderStatus"
              size="mini"
              :disabled="Number(scope.row.orderStatus) === 4"
              @change="() => updateOrderStatus(scope.row)"
            >
              <el-option
                v-for="option in statusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              ></el-option>
            </el-select>
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
        </div>
      </div>
      <el-card shadow="hover" class="tips-card">
        <p style="font-weight: bold;font-size: 18px;">其他信息：</p>
        <p style="font-weight: bold;margin-left: 12px">姓名：{{ detailContact.name || '暂无' }}</p>
        <p style="font-weight: bold;margin-left: 12px">电话：{{ detailContact.phone || '暂无' }}</p>
        <p style="font-weight: bold;margin-left: 12px">地址：{{ detailContact.address || '暂无' }}</p>
        <p style="font-weight: bold;margin-left: 12px">备注：{{ detailContact.remark || '暂无' }}</p>
      </el-card>
      <div class="order-detail-actions" v-if="selectedOrder && isCustomer && getOrderStatus(selectedOrder) === 4">
        <el-button type="primary" round @click="repayOrder(selectedOrder)">重新支付</el-button>
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
      <div class="stat-card panel-card clickable" @click="openOrderDialog('all')">
        <div class="stat-label">累计订单</div>
        <div class="stat-value">{{ stats.totalOrders }}</div>
        <div class="stat-foot">最近下单 {{ stats.lastOrderDate || '暂无' }}</div>
      </div>
      <div v-if="isAdmin" class="stat-card panel-card clickable" @click="openOrderDialog('completed')">
        <div class="stat-label">已完成</div>
        <div class="stat-value">{{ stats.completedOrders }}</div>
        <div class="stat-foot">完成率 {{ stats.totalOrders ? Math.round(stats.completedOrders / stats.totalOrders * 100) : 0 }}%</div>
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

    <section class="dashboard-grid">
      <el-card v-if="isAdmin" shadow="hover" class="panel-card chart-card">
        <div class="card-header">
          <h3>近 6 个月订单趋势</h3>
          <span>订单数量</span>
        </div>
        <ve-histogram :data="orderTrendData" :height="'300px'"></ve-histogram>
      </el-card>
      <el-card shadow="hover" class="panel-card chart-card">
        <div class="card-header">
          <h3>文创品类偏好</h3>
          <span>按购买次数</span>
        </div>
        <ve-pie
          :data="categoryPieData"
          :height="'300px'"
          :settings="pieSettings"
          :extend="pieExtend"
          :legend-visible="true"
        ></ve-pie>
      </el-card>
      <el-card shadow="hover" class="panel-card chart-card order-list-card">
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
              <p class="order-date">{{ formatDate(item.createDate) }}</p>
            </div>
            <el-tag :type="getOrderStatusTagType(item.orderStatus)">
              {{ getOrderStatusLabel(item.orderStatus) }}
            </el-tag>
          </div>
          <p v-if="!recentOrders.length" class="empty-text">暂无订单记录</p>
        </div>
      </el-card>
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
          <el-button class="border-radius custom-zfb" @click="dialog_zfb = true"></el-button>
          <el-button class="border-radius custom-weChatPay" @click="dialog_wechat = true"></el-button>
        </div>
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
  background-image: url("https://gw.alipayobjects.com/mdn/rms_7e962d/afts/img/A*yCL8TrkjIx8AAAAAAAAAAAAAARQnAQ");
  background-size: contain;
  background-position: center;
  color: white;
  width: 170px;
  height: 64px;
}

.custom-weChatPay {
  background-image: url("https://td.cdn-go.cn/enterprise_payment/v0.1.2/logo.svg");
  background-size: cover;
  color: white;
  width: 170px;
  height: 64px;
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

.order-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background: rgba(31, 122, 109, 0.06);
  border-radius: 12px;
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
