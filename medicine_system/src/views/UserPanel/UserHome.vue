<script>
import {simpleDataConverter} from 'v-charts-v2/lib/converter';
import {apiRear} from "@/axios";
import {jwtDecode} from "jwt-decode";

export default {
  name: 'SystemHome',
  data() {
    this.handler = {
      dataConverter: simpleDataConverter
    }
    return {
      dialog_zfb: false,
      dialog_wechat: false,
      dialog_bill: false,
      dialog_orderDetail: false,
      loading: false,
      chooseOrderId: 0,
      zfb_url: require('@/assets/pay/zfb_qr.jpg'),
      weChat_url: require('@/assets/pay/wechat_qr.jpg'),
      chartData: {
        columns: ['category', 'totalQuantity'],
        rows: []
      },
      userInfo: {},
      orders: [],
      orderDetail: []
    }
  },
  created() {
    this.getUserInfo();
    this.getUserBill();
  },
  mounted() {
    this.getProductCategoryStats();
  },
  methods: {
    async getUserInfo() {
      try {
        const info = await apiRear.get(`/userInfo/${jwtDecode(sessionStorage.getItem('token') || '').uid}`);
        this.userInfo = info.data.data;
      } catch (err) {
        this.$message.error('无法获取用户数据！');
      }
    },
    async getProductCategoryStats() {
      this.loading = true;
      try {
        const info = await apiRear.get(`/cultural-products/category-statistics`);
        this.chartData.rows = info.data.data;
      } catch (err) {
        this.$message.error('无法获取文创产品分类数据！');
      } finally {
        this.loading = false;
      }
    },
    async getUserBill() {
      try {
        const info = await apiRear.get(`/order/getOrderByUID=${jwtDecode(sessionStorage.getItem('token') || '').uid}`);
        this.orders = info.data.data;
      } catch (err) {
        this.$message.error('无法获取用户订单！');
      }
    },
    async orderDetail_handle(orderId) {
      this.chooseOrderId = orderId;

      this.dialog_orderDetail = true;
      try {
        const info = await apiRear.get(`/order/getOrderDetailByOrderId=${orderId}`);
        this.orderDetail = info.data.data;
        this.$message.success('订单信息获取成功！');
      } catch (err) {
        this.$message.error('无法获取订单详情！');
      }
    },
    agencyName() {
      const order = this.orders.find(order => order.orderId === this.chooseOrderId);
      if (order) {
        return order.agency;
      } else {
        return '暂无';
      }
    }
  }
}
</script>
<template>
  <div>
    <!--    支付宝对话框-->
    <el-dialog
        title="通过支付宝充值"
        :visible.sync="dialog_zfb"
        center
        width="30%">
      <el-image
          :src="zfb_url"
      ></el-image>
    </el-dialog>

    <!--    微信支付对话框-->
    <el-dialog
        title="通过微信支付充值"
        :visible.sync="dialog_wechat"
        center
        width="30%">
      <el-image
          :src="weChat_url"
      ></el-image>
    </el-dialog>

    <!--    查看订单对话框-->
    <el-dialog
        title="查看订单"
        :visible.sync="dialog_bill"
        center
        width="60%">
      <el-table
          :data="orders"
          stripe
          height="500"
          max-height="500"
          style="width: 100%">
        <el-table-column
            prop="orderId"
            label="订单号"
            sortable
            width="180">
        </el-table-column>
        <el-table-column
            prop="createDate"
            label="创建时间"
            sortable
        />
        <el-table-column
            prop="completeDate"
            label="完成时间"
            sortable
        >
          <template slot-scope="scope">
            {{ scope.row.completeDate || '暂无' }}
          </template>
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            width="100">
          <template slot-scope="scope">
            <el-button @click="orderDetail_handle(scope.row.orderId)" type="text"
                       size="small">查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!--    订单详情对话框-->
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
        <el-table-column
            prop="productCode"
            label="文创产品编号"
            sortable
            width="180">
        </el-table-column>
        <el-table-column
            prop="productName"
            label="文创产品名称"
            sortable
        />
        <el-table-column
            prop="purchaseNum"
            label="购买数量"
            sortable
        />
        <el-table-column
            prop="price"
            label="单价"
            sortable
        />
      </el-table>
      <el-card shadow="hover" style="margin-top: 20px; border-radius: 30px;background-color: #d7eafd">
        <p style="font-weight: bold;font-size: 20px;">💁其他信息：</p>
        <p style="font-weight: bold;margin-left: 20px">👤姓名：{{ userInfo.cname ? userInfo.cname : "暂无" }}</p>
        <p style="font-weight: bold;margin-left: 20px">
          🏷️旅行顾问编号：{{ agencyName() ? agencyName() : "暂无" }}</p>
        <p style="font-weight: bold;margin-left: 20px">📱电话：{{ userInfo.cphone ? userInfo.cphone : "暂无" }}</p>
        <p style="font-weight: bold;margin-left: 20px">📍地址：{{ userInfo.caddress ? userInfo.caddress : "暂无" }}</p>
        <p style="font-weight: bold;margin-left: 20px">🧾备注：{{ userInfo.cremark ? userInfo.cremark : "暂无" }}</p>
      </el-card>
    </el-dialog>

    <div style="display: flex; height: 400px;">
      <el-card shadow="hover" class="border-radius" style="margin: 0 20px 20px 0; min-width: 57%">
        <p style="font-size: 45px;font-weight: bold;text-align: center;">
          🎉 欢迎来到旅游文创管理中心！ 🏖️
        </p>
        <div style="display: flex; justify-content: center;">
          <el-card shadow="hover" class="border-radius" style="margin-right: 20px; background-color: #eaf3fd">
            <span style="font-weight: bold; font-size: 25px;">
              ✨ 简单。
            </span>
          </el-card>
          <el-card shadow="hover" class="border-radius" style="margin-right: 20px; background-color: #eaf3fd">
            <span style="font-weight: bold; font-size: 25px;">
              ✨ 快捷。
            </span>
          </el-card>
          <el-card shadow="hover" class="border-radius" style="margin-right: 20px; background-color: #eaf3fd">
            <span style="font-weight: bold; font-size: 25px;">
              ✨ 高效。
            </span>
          </el-card>
        </div>
        <p class="strong" style="font-size: 20px;margin: 30px;text-align: center;color: #6a6a6e">
          👋欢迎您，{{ userInfo.username ? userInfo.username : "旅人" }}
        </p>
      </el-card>
      <el-card shadow="hover" class="border-radius"
               style="margin: 0 20px 20px 0;width: 280px; background-color: #eaf3fd">
        <div style="display: block;">
          <el-card shadow="hover" class="border-radius"
                   style="margin-bottom: 20px; display: flex; align-items: center; justify-content: center;">
            <div style="text-align: center;">
              <span style="font-weight: bold; font-size: 100px;">🧳</span>
              <p style="font-weight: bold; font-size: 25px;">旅途愉快。</p>
            </div>
          </el-card>
          <el-card shadow="hover" class="border-radius" style="height: 60px">
            <el-button shadow="hover"
                       class="border-radius"
                       style="margin: 0; padding: 0; height: 100%; width: 100%; border: none;"
                       @click="dialog_bill = true"
            >
              <span style="font-weight: bold; font-size: 25px;">查看订单</span>
            </el-button>
          </el-card>
        </div>
      </el-card>
      <el-card shadow="hover" class="border-radius"
               style="margin-bottom: 20px;min-width: 300px; background-color: #eaf3fd">
        <div style="display: block;">
          <el-card shadow="hover" class="border-radius" style="margin-bottom: 20px;">
            <span style="font-weight: bold; font-size: 20px;">💰余额：￥{{
                userInfo.balance !== undefined && userInfo.balance !== null ? (userInfo.balance === 0 ? '0.00' : userInfo.balance.toFixed(2)) : "unknown"
              }}</span>
          </el-card>
          <el-card shadow="hover" class="border-radius" style="display: block;">
            <p style="font-weight: bold; font-size: 20px;margin: 0 0 25px 0">💴余额增值：</p>
            <el-button shadow="hover"
                       class="border-radius custom-zfb"
                       style="margin-bottom: 30px;padding:22px 61px 22px 61px; border: none"
                       @click="dialog_zfb = true"
            >
            </el-button>
            <el-button shadow="hover"
                       class="border-radius custom-weChatPay"
                       style="margin: 0; padding: 21px 95px 21px 95px; border: none"
                       @click="dialog_wechat = true"
            >
            </el-button>
          </el-card>
        </div>
      </el-card>
    </div>
    <div>
      <el-card shadow="hover" style="height: 520px;" v-loading="loading">
        <p class="strong" style="font-size: 20px;margin: 10px;">🔥热销文创</p>
        <ve-line :data="chartData" style="height: 370px;"></ve-line>
        <div style="display: block; text-align: center;">
          <p style="font-style: italic;">带上灵感与伴手礼，一起发现旅途的惊喜。</p>
          <p class="strong">© POWER BY @202224111240🧠...Maybe🐵</p>
        </div>
      </el-card>
    </div>
  </div>
</template>
<style scoped>
.border-radius {
  border-radius: 10px;
}

.custom-zfb {
  background-image: url("https://gw.alipayobjects.com/mdn/rms_7e962d/afts/img/A*yCL8TrkjIx8AAAAAAAAAAAAAARQnAQ");
  background-size: contain;
  background-position: center; /* 图片居中 */
  color: white;
}

.custom-weChatPay {
  background-image: url("https://td.cdn-go.cn/enterprise_payment/v0.1.2/logo.svg");
  background-size: cover;
  color: white;
}
</style>
