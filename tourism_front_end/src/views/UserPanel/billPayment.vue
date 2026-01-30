<script>
import {apiRear} from "@/axios";
import {jwtDecode} from "jwt-decode";

export default {
  name: 'BillPayment',
  data() {
    return {
      shoppingCart: [],
      cartTotal: 0,
      totalAmount: 0,
      carriage: '免额外运费',
      carriagePrice: 10,
      exemptCarriage: 60,
      loading: false,
      isPaying: false,
      userInfo: {},
      orderId: [],
      orderInfo: {
        uid: '',
        orderId: null,
        email: '',
        cphone: '',
        cname: '',
        caddress: '',
        cremark: ''
      }
    }
  },
  created() {
    this.getShoppingCart();
  },
  computed: {
    isBillInvalid() {
      return !this.userInfo.email || !this.userInfo.cphone || !this.userInfo.cname ||
        !this.userInfo.caddress || !this.userInfo.cremark;
    }
  },
  methods: {
    async getUserInfo() {
      try {
        this.userInfo.uid = jwtDecode(sessionStorage.getItem('token') || '').uid;
        const info = await apiRear.get(`/userInfo/personal/${this.userInfo.uid}`);
        this.userInfo = info.data.data;
      } catch (err) {
        this.$message.error('无法获取用户数据');
      }
    },
    async getShoppingCart() {
      try {
        await this.getUserInfo();
        if (this.userInfo.uid !== undefined) {
          const info = await apiRear.get(`/order/getOrderInfoBy=${this.userInfo.uid}`);
          if (info.data.data.orderId !== null) {
            this.orderId = info.data.data.orderId;
            this.cartTotal = info.data.data.totalPrice;
            this.totalAmount = this.cartTotal;
            if (this.totalAmount > this.exemptCarriage) {
              this.carriage = '免额外运费';
            } else {
              this.carriage = 'CNY￥' + this.carriagePrice.toFixed(2);
              this.totalAmount += this.carriagePrice;
            }
            if (this.orderId.length !== 0) {
              const res = await apiRear.get(`/order/getOrderByOrderId=${this.orderId}`);
              this.shoppingCart = res.data.data;
            } else {
              this.$message.error('没有找到未支付的订单');
              this.$router.push('/userPanel/culturalProducts');
            }
          }
        }
      } catch (err) {
        this.$message.error('无法获取购物车数据');
      }
    },
    async payment() {
      try {
        this.isPaying = true;
        this.orderInfo.uid = this.userInfo.uid;
        this.orderInfo.orderId = this.orderId.at(0);
        this.orderInfo.email = this.userInfo.email;
        this.orderInfo.cphone = this.userInfo.cphone;
        this.orderInfo.cname = this.userInfo.cname;
        this.orderInfo.caddress = this.userInfo.caddress;
        this.orderInfo.cremark = this.userInfo.cremark;
        await new Promise(resolve => setTimeout(resolve, 1200));
        const res = await apiRear.post(`/order/payment`, this.orderInfo);
        if (res.data.message === "余额不足") {
          this.$message.error('余额不足，请充值后再试');
          this.$notify.error({
            title: '错误',
            message: '您当前的余额为 CNY￥' + this.userInfo.balance + '。',
            duration: 6000
          });
          return;
        }
        if (res.data.code === 0) {
          this.$message.error(res.data.message || '结账失败，请稍后再试');
          return;
        }
        const pickupInfo = res.data.data || {};
        if (pickupInfo.pickupCode) {
          sessionStorage.setItem('pickupCode', pickupInfo.pickupCode);
        }
        if (pickupInfo.pickupToken) {
          sessionStorage.setItem('pickupToken', pickupInfo.pickupToken);
        }
        this.$message.success(`支付成功，取件码：${pickupInfo.pickupCode || '已生成'}`);
        await this.$router.push('/CompleteBill');
      } catch (err) {
        this.$message.error('结账失败，请稍后再试');
      } finally {
        this.isPaying = false;
      }
    },
    goBack() {
      this.$router.push('/userPanel/culturalProducts');
    }
  }
}
</script>

<template>
  <div class="bill-page">
    <el-page-header @back="goBack">
      <template #content>
        <span>选购文创产品</span>
      </template>
    </el-page-header>
    <div class="bill-grid">
      <div class="bill-column">
        <p class="bill-tip">以下是你购物车内的商品（CNY￥{{ cartTotal.toFixed(2) }}）</p>
        <el-card shadow="always" class="bill-card">
          <el-table :data="shoppingCart" height="100%">
            <el-table-column label="文创产品编号" prop="productCode"></el-table-column>
            <el-table-column label="名称" prop="productName"></el-table-column>
            <el-table-column label="数量" prop="purchaseNum"></el-table-column>
            <el-table-column label="单价" prop="price"></el-table-column>
          </el-table>
        </el-card>
      </div>
      <div class="bill-column">
        <p class="bill-title">确认付款</p>
        <p class="bill-subtitle">填写信息后即可完成支付并生成取件码</p>
        <div class="bill-info">
          <p class="bill-label">联系信息</p>
          <el-input v-model="userInfo.email" placeholder="请输入邮箱" class="info-container"></el-input>
          <el-input v-model="userInfo.cphone" placeholder="请输入电话" class="info-container"></el-input>
          <el-input v-model="userInfo.cname" placeholder="请输入姓名" class="info-container"></el-input>
          <el-input v-model="userInfo.caddress" placeholder="请输入地址" class="info-container"></el-input>
          <el-input v-model="userInfo.cremark" placeholder="请输入备注" class="info-container"></el-input>
        </div>
        <div class="bill-summary">
          <div>
            <p>运费：{{ carriage }}</p>
            <p>合计：<strong>CNY￥{{ totalAmount.toFixed(2) }}</strong></p>
          </div>
          <el-button type="primary" :loading="isPaying" :disabled="isBillInvalid" @click="payment">
            {{ isPaying ? '支付中...' : '确认支付' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bill-page {
  min-height: 100%;
  padding: 16px 24px 32px;
}

.bill-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 32px;
  padding: 24px 0;
}

.bill-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.bill-tip {
  font-weight: 600;
  margin: 0;
}

.bill-card {
  height: 100%;
  border-radius: 18px;
}

.bill-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
}

.bill-subtitle {
  color: #5f6b66;
  margin: 0;
}

.bill-info {
  display: grid;
  gap: 12px;
}

.bill-label {
  font-weight: 600;
  margin: 0;
}

.info-container {
  width: 100%;
}

.bill-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

@media (max-width: 900px) {
  .bill-grid {
    grid-template-columns: 1fr;
  }
}
</style>
