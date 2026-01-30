<script>
export default {
  name: 'UserHome',
  mounted() {
    this.$router.replace('/userInfo');
  }
}
</script>

<template>
  <div class="redirect-page">
    <el-empty description="用户中心已整合到个人资料页，正在跳转..."></el-empty>
  </div>
</template>

<style scoped>
.redirect-page {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
