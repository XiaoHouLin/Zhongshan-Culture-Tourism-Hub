<script>
import {apiRear} from "@/axios";

export default {
  name: 'PickupVerify',
  data() {
    return {
      token: '',
      loading: true,
      confirming: false,
      error: '',
      orderInfo: null,
      orderDetails: []
    };
  },
  computed: {
    isPickedUp() {
      return this.orderInfo && Number(this.orderInfo.pickupStatus) === 1;
    }
  },
  created() {
    this.token = this.$route.query.token || '';
    if (!this.token) {
      this.loading = false;
      this.error = '缺少取件二维码信息';
      return;
    }
    this.fetchPickupInfo();
  },
  methods: {
    async fetchPickupInfo() {
      this.loading = true;
      this.error = '';
      try {
        const response = await apiRear.get('/order/pickup/info', {params: {token: this.token}});
        if (response.data && response.data.code === 1) {
          const payload = response.data.data || {};
          this.orderInfo = payload.orderInfo || null;
          this.orderDetails = Array.isArray(payload.orderDetails) ? payload.orderDetails : [];
        } else {
          this.error = response.data && response.data.message ? response.data.message : '无法获取取货信息';
        }
      } catch (error) {
        this.error = '无权限或网络异常';
      } finally {
        this.loading = false;
      }
    },
    async confirmPickup() {
      if (!this.token) {
        return;
      }
      this.confirming = true;
      try {
        const response = await apiRear.post('/order/pickup/confirm', null, {params: {token: this.token}});
        if (response.data && response.data.code === 1) {
          this.$message.success(response.data.data || '取货已确认');
          await this.fetchPickupInfo();
        } else {
          this.$message.error(response.data && response.data.message ? response.data.message : '确认失败');
        }
      } catch (error) {
        this.$message.error('无权限或网络异常');
      } finally {
        this.confirming = false;
      }
    },
    formatTime(value) {
      if (!value) {
        return '-';
      }
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) {
        return value;
      }
      return date.toLocaleString();
    }
  }
}
</script>

<template>
  <div class="verify-page">
    <el-card shadow="hover" class="verify-card">
      <div class="verify-header">
        <div>
          <h2>取货核销</h2>
          <p>管理员登录后可核验取件二维码并确认取货。</p>
        </div>
        <el-tag :type="isPickedUp ? 'success' : 'warning'">
          {{ isPickedUp ? '已取货' : '待取货' }}
        </el-tag>
      </div>

      <el-alert
        v-if="error"
        :title="error"
        type="error"
        show-icon
        class="verify-alert"
      ></el-alert>

      <el-skeleton v-if="loading" :rows="6" animated></el-skeleton>

      <template v-else>
        <div v-if="orderInfo" class="verify-section">
          <h3>订单信息</h3>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="订单号">{{ orderInfo.orderId }}</el-descriptions-item>
            <el-descriptions-item label="用户编号">{{ orderInfo.uid }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ orderInfo.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ orderInfo.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ orderInfo.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="取件码">{{ orderInfo.pickupCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ orderInfo.address || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ orderInfo.remark || '-' }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">
              {{ formatTime(orderInfo.createDate || orderInfo.CreateDate) }}
            </el-descriptions-item>
            <el-descriptions-item label="取货时间">{{ formatTime(orderInfo.pickupTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="orderDetails.length" class="verify-section">
          <h3>订单明细</h3>
          <el-table :data="orderDetails" border>
            <el-table-column prop="productCode" label="产品编号" width="140"></el-table-column>
            <el-table-column prop="productName" label="名称"></el-table-column>
            <el-table-column prop="purchaseNum" label="数量" width="90"></el-table-column>
            <el-table-column prop="price" label="单价" width="120"></el-table-column>
          </el-table>
        </div>

        <div class="verify-actions">
          <el-button
            type="primary"
            icon="el-icon-check"
            :loading="confirming"
            :disabled="isPickedUp || !orderInfo"
            @click="confirmPickup"
          >
            {{ isPickedUp ? '已取货' : '确认取货' }}
          </el-button>
          <el-button v-if="error" @click="$router.push('/login')">去登录</el-button>
        </div>
      </template>
    </el-card>
  </div>
</template>

<style scoped>
.verify-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at 12% 10%, rgba(31, 122, 109, 0.12), transparent 45%),
    radial-gradient(circle at 85% 18%, rgba(199, 90, 49, 0.12), transparent 50%),
    linear-gradient(180deg, #f9f4ec 0%, #f5f9fb 55%, #ffffff 100%);
}

.verify-card {
  width: min(900px, 96vw);
  border-radius: 20px;
}

.verify-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.verify-header h2 {
  margin: 0 0 6px;
  color: #1c2c27;
}

.verify-header p {
  margin: 0;
  color: #5f6b66;
  font-size: 13px;
}

.verify-alert {
  margin: 16px 0;
}

.verify-section {
  margin-top: 18px;
}

.verify-section h3 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #1c2c27;
}

.verify-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
</style>
