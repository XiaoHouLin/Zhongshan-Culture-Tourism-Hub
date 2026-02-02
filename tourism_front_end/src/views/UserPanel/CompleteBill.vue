<script>
import QRCode from 'qrcode';

export default {
  name: 'CompleteBill',
  data() {
    return {
      pickupCode: '',
      pickupToken: '',
      pickupQrUrl: '',
      orderId: ''
    };
  },
  created() {
    this.pickupCode = sessionStorage.getItem('pickupCode') || '';
    this.pickupToken = sessionStorage.getItem('pickupToken') || '';
    this.orderId = sessionStorage.getItem('recentOrderId') || '';
    this.generateQr();
  },
  methods: {
    async generateQr() {
      if (!this.pickupToken) {
        return;
      }
      try {
        const baseUrl = window.location.origin;
        const verifyUrl = `${baseUrl}/pickup-verify?token=${encodeURIComponent(this.pickupToken)}`;
        this.pickupQrUrl = await QRCode.toDataURL(verifyUrl, {width: 220, margin: 1});
      } catch (error) {
        this.pickupQrUrl = '';
      }
    },
    viewOrderDetail() {
      if (this.orderId) {
        sessionStorage.setItem('openOrderDetailId', this.orderId);
      } else {
        sessionStorage.setItem('openOrders', '1');
      }
      this.$router.push('/userInfo');
    }
  }
}
</script>

<template>
  <div class="complete-page">
    <el-card shadow="hover" class="complete-card">
      <div class="complete-icon">✅</div>
      <h2>支付成功</h2>
      <p class="complete-subtitle">请携带取件码到线下取货点领取文创商品。</p>
      <div class="pickup-info">
        <div class="pickup-code">
          <span>取件码</span>
          <strong>{{ pickupCode || '已生成' }}</strong>
        </div>
        <div v-if="pickupQrUrl" class="pickup-qr">
          <img :src="pickupQrUrl" alt="取件二维码" />
        </div>
      </div>
      <div class="complete-actions">
        <el-button type="primary" icon="el-icon-document" @click="viewOrderDetail" round>查看订单</el-button>
        <el-button type="primary" icon="el-icon-goods" @click="$router.push('/userPanel/culturalProducts')" round>继续购物</el-button>
        <el-button plain icon="el-icon-back" @click="$router.push('/indexView')" round>返回首页</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.complete-page {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.complete-card {
  width: min(720px, 92vw);
  text-align: center;
  border-radius: 24px;
}

.complete-icon {
  font-size: 72px;
}

.complete-subtitle {
  margin: 6px 0 20px;
  color: #5f6b66;
}

.pickup-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.pickup-code {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 18px;
}

.pickup-code strong {
  font-size: 28px;
  color: #1f7a6d;
}

.pickup-qr img {
  width: 200px;
  height: 200px;
  border-radius: 12px;
  border: 1px solid rgba(31, 122, 109, 0.2);
}

.complete-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}
</style>
