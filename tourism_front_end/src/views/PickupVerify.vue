<script>
import {apiRear} from "@/axios";

export default {
  name: 'PickupVerify',
  data() {
    return {
      status: 'loading',
      message: '',
    };
  },
  computed: {
    resultIcon() {
      if (this.status === 'success') return 'success';
      if (this.status === 'error') return 'error';
      return 'info';
    },
    titleText() {
      if (this.status === 'success') return '取货成功';
      if (this.status === 'error') return '无法核销';
      return '正在验证';
    }
  },
  created() {
    const token = this.$route.query.token;
    if (!token) {
      this.status = 'error';
      this.message = '缺少取件二维码信息';
      return;
    }
    this.verify(token);
  },
  methods: {
    async verify(token) {
      this.status = 'loading';
      this.message = '正在验证管理员权限...';
      try {
        const response = await apiRear.get('/order/pickup/verify', {params: {token}});
        if (response.data && response.data.code === 1) {
          this.status = 'success';
          this.message = response.data.data || '已取货';
        } else {
          this.status = 'error';
          this.message = response.data && response.data.msg ? response.data.msg : '无权限或取件码无效';
        }
      } catch (error) {
        this.status = 'error';
        this.message = '无权限或网络异常';
      }
    }
  }
}
</script>

<template>
  <div class="verify-page">
    <el-card shadow="hover" class="verify-card">
      <h2>取货核销</h2>
      <el-result :icon="resultIcon" :title="titleText" :sub-title="message"></el-result>
      <el-button v-if="status === 'error'" type="primary" @click="$router.push('/login')">去登录</el-button>
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
  width: min(520px, 92vw);
  border-radius: 20px;
}

.verify-card h2 {
  margin: 0 0 8px;
  color: #1c2c27;
}
</style>
