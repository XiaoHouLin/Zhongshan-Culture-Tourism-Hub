<script>
import {apiRear} from "@/axios";
import {jwtDecode} from "jwt-decode";

export default {
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
      dangerColor: '#ff0000',
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
        this.$message.error('无法获取用户数据！');
      }
    },
    async getShoppingCart() {
      try {
        await this.getUserInfo(); //确保用户信息已加载
        if (this.userInfo.uid !== undefined) {
          apiRear.get(`/order/getOrderInfoBy=${this.userInfo.uid}`).then(info => {
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
                apiRear.get(`/order/getOrderByOrderId=${this.orderId}`).then(res => {
                  this.shoppingCart = res.data.data;
                })
              } else {
                this.$message.error('没有找到未支付的订单！');
                this.$router.push('/userPanel/culturalProducts');
              }
            }
          });
        }
      } catch (err) {
        this.$message.error('无法获取购物车数据！');
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
        await new Promise(resolve => setTimeout(resolve, 2000));//延迟2秒
        const res = await apiRear.post(`/order/payment`, this.orderInfo);
        if (res.data.message === "余额不足") {
          this.$message.error('余额不足，请充值后再试！');
          this.$notify.error({
            title: '错误',
            message: '💰您当前的余额为CNY￥' + this.userInfo.balance + '。',
            duration: 6000
          });
        } else if (res.data.message === "Cannot invoke \"java.lang.Double.doubleValue()\" because \"totalPrice\" is null") {
          this.$message.error('没有找到未支付的订单！');
        } else if (res.data.code === 0) {
          this.$message.error('结账失败！请稍后再试！');
        } else if (res.data.message === "success") {
          this.$message.success('支付成功！');
          await this.$router.push('/CompleteBill');
        }
      } catch (err) {
        this.$message.error('结账失败，请稍后再试！');
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
  <div style="height: 100%;">
    <el-page-header @back="goBack">
      <template #content>
        <span>选购文创产品</span>
      </template>
    </el-page-header>
    <div style="display: flex;width: 80%; align-items: flex-start;height: calc(100% - 24px);padding:0 100px 0 100px">
      <div class="both-ends-div-container">
        <p class="bold-content" style="margin:20px 0 5px 0">🛍️以下是你购物袋内的商品CNY￥{{ cartTotal.toFixed(2) }}。</p>
        <el-card shadow="always" class="border" style="width: 500px;height: calc(100% - 62px);">
          <el-table :data="shoppingCart" height="100%">
            <el-table-column label="文创产品编号" prop="productCode" width="100%"></el-table-column>
            <el-table-column label="名称" prop="productName" width="120%"></el-table-column>
            <el-table-column label="数量" prop="purchaseNum" width="120%"></el-table-column>
            <el-table-column label="单价" prop="price" width="120%"></el-table-column>
          </el-table>
        </el-card>
      </div>
      <div class="both-ends-div-container">
        <p class="bold-content" style="font-size: 25px; margin:20px 0 5px 0">🚚好心意即将送达。</p>
        <p style="font-weight: bold">联系信息</p>
        <el-input v-model="userInfo.email" placeholder="请输入邮箱" class="info-container">
          <template slot="prepend">邮箱</template>
        </el-input>
        <el-input v-model="userInfo.cphone" placeholder="请输入电话" class="info-container">
          <template slot="prepend">电话</template>
        </el-input>
        <el-input v-model="userInfo.cname" placeholder="请输入姓名" class="info-container">
          <template slot="prepend">姓名</template>
        </el-input>
        <p style="font-weight: bold">账单地址</p>
        <el-input v-model="userInfo.caddress" placeholder="请输入详细地址" class="info-container">
          <template slot="prepend">地址</template>
        </el-input>
        <p style="font-weight: bold">备注</p>
        <el-input
            type="textarea"
            :autosize="{ minRows: 6, maxRows: 6}"
            placeholder="请输入备注"
            v-model="userInfo.cremark"
            class="info-container"
            maxlength="150"
            show-word-limit
        >
        </el-input>
        <el-divider class="info-container"></el-divider>
        <div class="flex-container info-container info-font-container">
          <p>小计</p>
          <span>CNY￥{{ cartTotal.toFixed(2) }}</span>
        </div>
        <div class="flex-container info-container info-font-container">
          <p>运费
            <el-popover
                placement="top-start"
                width="200"
                trigger="hover"
                :content="`购物袋总金额大于\nCNY￥${exemptCarriage}\n可享受免运费服务。`">
              <el-button slot="reference" icon="el-icon-question" style="padding: 0;border: 0"></el-button>
            </el-popover>
          </p>
          <span>{{ carriage }}</span>
        </div>
        <el-divider class="info-container"></el-divider>
        <div class="flex-container info-container" style="height: 20px;font-weight: bold;font-size: 20px">
          <p>你的总金额</p>
          <span>CNY￥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <el-button
            type="primary"
            style="margin-top: 20px;border-radius: 10px;"
            class="custom-button info-container"
            :disabled="isBillInvalid || this.isPaying"
            @click="payment">
          <span v-if="isPaying">
                    <i class="el-icon-loading"></i> 结账中...
                 </span>
          <span v-else>结账</span>
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.border {
  border-radius: 10px;
}

.bold-content {
  font-weight: bold;
  font-size: 20px;
}

:deep(.el-card__body) {
  height: 95%;
}

.flex-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-container {
  width: 400px;
  margin-bottom: 10px
}

.info-font-container {
  height: 20px;
  font-size: 13px;
}

.both-ends-div-container {
  height: 100%;
  padding: 0 60px 0 60px;
  width: 50%;
  max-width: 790px
}

.custom-button {
  background-color: #016fe1;
  color: white;
  border: none;
  border-radius: 10px;
  transition: background-color 0.3s ease;
}

.custom-button:disabled {
  background-color: #cccccc; /* 浅色背景 */
  color: #888888; /* 浅色文字 */
}
</style>
