<script>
import {apiRear} from "@/axios";
import {jwtDecode} from "jwt-decode";

const AMAP_SCRIPT_ID = 'amap-js-sdk';

export default {
  name: 'BillPayment',
  data() {
    return {
      shoppingCart: [],
      cartTotal: 0,
      totalAmount: 0,
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
      },
      pickupAddress: '广东省中山市孙文西路156之1号',
      pickupDialogVisible: false,
      pickupMapReady: false,
      pickupMapError: '',
      pickupMapInstance: null,
      pickupMarker: null,
      pickupLabel: null,
      pickupLocation: null,
      rechargeDialogVisible: false,
      rechargeTitle: '',
      rechargeImage: '',
      amapKey:
        (typeof window !== 'undefined' && window.AMAP_KEY) ||
        process.env.VUE_APP_AMAP_KEY ||
        '9827c6fd18aa7e77238f230af42469cf',
      amapSecurityCode:
        (typeof window !== 'undefined' && window.AMAP_SECURITY_CODE) ||
        process.env.VUE_APP_AMAP_SECURITY_CODE ||
        'c21177cb56c8467218eecf96a7bbcca2'
    }
  },
  created() {
    this.getShoppingCart();
  },
  computed: {
    isBillInvalid() {
      return !this.userInfo.cphone;
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
        this.orderInfo.email = this.userInfo.email || '';
        this.orderInfo.cphone = this.userInfo.cphone;
        this.orderInfo.cname = this.userInfo.cname || '';
        this.orderInfo.caddress = this.userInfo.caddress || '';
        this.orderInfo.cremark = this.userInfo.cremark || '';
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
    },
    openPickupMap() {
      this.pickupMapError = '';
      this.pickupDialogVisible = true;
    },
    handlePickupDialogOpen() {
      this.pickupMapError = '';
      this.$nextTick(() => {
        this.initPickupMap();
      });
    },
    handlePickupDialogClose() {
      if (this.pickupMapInstance) {
        this.pickupMapInstance.destroy();
        this.pickupMapInstance = null;
      }
      this.pickupMapReady = false;
      this.pickupMapError = '';
      this.pickupMarker = null;
      this.pickupLabel = null;
      this.pickupLocation = null;
    },
    async initPickupMap() {
      if (this.pickupMapReady) {
        return;
      }
      this.pickupMapError = '';
      try {
        const AMap = await this.loadAmap();
        this.createPickupMap(AMap);
      } catch (error) {
        this.pickupMapError = '地图加载失败，请检查高德 Key 或网络连接。';
      }
    },
    loadAmap() {
      if (window.AMap) {
        return Promise.resolve(window.AMap);
      }
      if (this.amapSecurityCode) {
        window._AMapSecurityConfig = {
          securityJsCode: this.amapSecurityCode
        };
      }
      return new Promise((resolve, reject) => {
        const existing = document.getElementById(AMAP_SCRIPT_ID);
        if (existing) {
          const timer = setInterval(() => {
            if (window.AMap) {
              clearInterval(timer);
              resolve(window.AMap);
            }
          }, 60);
          setTimeout(() => {
            clearInterval(timer);
            reject(new Error('AMap load timeout'));
          }, 8000);
          return;
        }
        const script = document.createElement('script');
        script.id = AMAP_SCRIPT_ID;
        script.src = `https://webapi.amap.com/maps?v=2.0&key=${this.amapKey}&plugin=AMap.Geocoder,AMap.Scale,AMap.ToolBar`;
        script.crossOrigin = 'anonymous';
        script.async = true;
        script.onload = () => {
          if (window.AMap) {
            resolve(window.AMap);
          } else {
            reject(new Error('AMap not available'));
          }
        };
        script.onerror = () => reject(new Error('AMap load failed'));
        document.head.appendChild(script);
      });
    },
    createPickupMap(AMap) {
      const container = document.getElementById('pickup-amap');
      if (!container) {
        return;
      }
      const map = new AMap.Map('pickup-amap', {
        zoom: 15,
        center: [113.391, 22.521],
        viewMode: '2D',
        mapStyle: 'amap://styles/whitesmoke'
      });
      this.pickupMapInstance = map;
      this.pickupMapReady = true;
      this.pickupMapError = '';
      const geocoder = new AMap.Geocoder({ city: '中山市' });
      geocoder.getLocation(this.pickupAddress, (status, result) => {
        if (status === 'complete' && result.geocodes && result.geocodes.length) {
          const location = result.geocodes[0].location;
          map.setCenter(location);
          map.setZoom(17);
          const marker = new AMap.Marker({
            position: location,
            title: '文创自取点',
            icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
            offset: new AMap.Pixel(-10, -34)
          });
          map.add(marker);
          this.pickupMarker = marker;
          const label = new AMap.Text({
            text: '文创自取门店',
            position: location,
            offset: new AMap.Pixel(0, -45),
            style: {
              background: 'rgba(31, 122, 109, 0.85)',
              color: '#ffffff',
              padding: '4px 10px',
              borderRadius: '12px',
              fontSize: '12px',
              border: 'none'
            }
          });
          map.add(label);
          this.pickupLabel = label;
          this.pickupLocation = {
            lng: location.lng,
            lat: location.lat
          };
          return;
        }
        this.pickupMapError = '暂时无法定位该地址，请稍后再试。';
      });
      AMap.plugin(['AMap.Scale', 'AMap.ToolBar'], () => {
        map.addControl(new AMap.Scale());
        map.addControl(new AMap.ToolBar({ position: 'LT' }));
      });
    },
    openRecharge(type) {
      if (type === 'zfb') {
        this.rechargeTitle = '支付宝充值';
        this.rechargeImage = '/zfb_qr.jpg';
      } else {
        this.rechargeTitle = '微信支付充值';
        this.rechargeImage = '/wechat_qr.jpg';
      }
      this.rechargeDialogVisible = true;
    },
    openPickupNavigation() {
      if (!this.pickupLocation) {
        this.$message.warning('正在定位门店位置，请稍后再试');
        return;
      }
      const {lng, lat} = this.pickupLocation;
      const url = `https://uri.amap.com/navigation?to=${lng},${lat},文创自取门店&mode=car&policy=1&src=webapp`;
      window.open(url, '_blank');
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
  <div class="bill-page">
    <div class="bill-container">
      <el-page-header @back="goBack">
        <template #content>
          <span>选购文创产品</span>
        </template>
      </el-page-header>
      <div class="bill-grid">
        <div class="bill-column">
          <p class="bill-tip">以下是你购物车内的商品（CNY￥{{ cartTotal.toFixed(2) }}）</p>
          <el-card shadow="always" class="bill-card">
            <el-table :data="shoppingCart">
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
            <div class="required-label">
              <span class="required-star">*</span>
              <span>联系电话</span>
            </div>
            <el-input v-model="userInfo.cphone" placeholder="请输入联系电话" class="info-container"></el-input>
            <el-input
              v-model="userInfo.cremark"
              placeholder="备注（可选）"
              class="info-container"
            ></el-input>
          </div>
          <div class="pickup-alert" @click="openPickupMap">
            <i class="el-icon-location-outline"></i>
            <span>仅支持线下自取 · 点击查看门店地图</span>
          </div>
          <div class="recharge-section">
            <p class="bill-label">余额增值</p>
            <div class="recharge-buttons">
              <el-button type="primary" @click="openRecharge('zfb')">支付宝</el-button>
              <el-button class="wechat-btn" @click="openRecharge('wechat')">微信支付</el-button>
            </div>
          </div>
          <div class="bill-summary">
            <div>
              <p class="balance-line">账户余额：CNY￥{{ formatMoney(userInfo.balance) }}</p>
              <p>合计：<strong>CNY￥{{ totalAmount.toFixed(2) }}</strong></p>
            </div>
            <el-button type="primary" :loading="isPaying" :disabled="isBillInvalid" @click="payment">
              {{ isPaying ? '支付中...' : '确认支付' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
    <el-dialog
      title="中山文创自取门店"
      :visible.sync="pickupDialogVisible"
      width="70%"
      @open="handlePickupDialogOpen"
      @close="handlePickupDialogClose"
    >
      <div class="pickup-dialog-body">
        <div class="pickup-address">
          <strong>地址：</strong>{{ pickupAddress }}
        </div>
        <div class="pickup-map-wrapper">
          <div id="pickup-amap" class="pickup-map"></div>
          <div v-if="pickupMapError && !pickupMapReady" class="pickup-map-fallback">
            {{ pickupMapError }}
          </div>
        </div>
        <div class="pickup-meta">
          <div>
            <strong>门店：</strong>中山文创自取门店
          </div>
          <el-button
            type="primary"
            icon="el-icon-location"
            :disabled="!pickupLocation"
            @click="openPickupNavigation"
          >
            导航到门店
          </el-button>
        </div>
        <p v-if="pickupMapError && pickupMapReady" class="pickup-map-note">{{ pickupMapError }}</p>
      </div>
    </el-dialog>
    <el-dialog
      :title="rechargeTitle"
      :visible.sync="rechargeDialogVisible"
      width="420px"
    >
      <div class="recharge-dialog">
        <img :src="rechargeImage" alt="充值二维码" />
        <p>扫码后完成充值，再返回本页继续支付。</p>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.bill-page {
  min-height: 100%;
  padding: 16px 0 32px;
}

.bill-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
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

.required-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
  color: #2c3e35;
}

.info-container {
  width: 100%;
}

.bill-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.balance-line {
  margin: 0 0 6px;
  color: #5f6b66;
  font-size: 13px;
}

.required-star {
  color: #e83a3a;
  font-weight: 700;
  margin-right: 4px;
}

.recharge-section {
  display: grid;
  gap: 8px;
}

.recharge-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.wechat-btn {
  background: #17b356;
  border-color: #17b356;
  color: #ffffff;
}

.wechat-btn:hover,
.wechat-btn:focus {
  background: #12984a;
  border-color: #12984a;
  color: #ffffff;
}

.pickup-alert {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(31, 122, 109, 0.08);
  color: #1f7a6d;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.pickup-alert:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(31, 56, 94, 0.12);
}

.pickup-dialog-body {
  display: grid;
  gap: 12px;
}

.pickup-address {
  font-size: 14px;
  color: #3a4b45;
}

.pickup-map-wrapper {
  position: relative;
  height: 380px;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(31, 122, 109, 0.05);
}

.pickup-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
  color: #2f3d38;
}

.pickup-map {
  width: 100%;
  height: 100%;
}

.pickup-map-fallback {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.92);
  color: #a2472b;
  font-weight: 600;
  padding: 16px;
  text-align: center;
}

.pickup-map-note {
  margin: 0;
  font-size: 12px;
  color: #a2472b;
}

.recharge-dialog {
  display: grid;
  gap: 12px;
  text-align: center;
}

.recharge-dialog img {
  width: 260px;
  height: 260px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid rgba(31, 122, 109, 0.15);
  margin: 0 auto;
}

.recharge-dialog p {
  margin: 0;
  color: #5f6b66;
  font-size: 13px;
}

@media (max-width: 900px) {
  .bill-grid {
    grid-template-columns: 1fr;
  }
}
</style>
