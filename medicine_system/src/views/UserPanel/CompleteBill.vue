<script setup>

</script>

<template>
  <div style="text-align: center;margin-top: 10%">
    <p style="font-size: 200px; margin: 0;">🎁</p>
    <p class="strong" style="font-size: 50px; margin: 0;">
      我们已经收到了您的订单。
    </p>
    <p class="strong" style="color: #6a6a6e; font-size: 30px; margin-top: 20px;">
      请坐和放宽。
    </p>
    <el-button
        type="primary"
        icon="el-icon-goods"
        @click="()=>{this.$router.push('/purchaseDrugs');}"
        round>返回购物
    </el-button>
    <el-button
        type="primary"
        icon="el-icon-menu"
        @click="()=>{this.$router.push('/home');}"
        round>返回首页
    </el-button>
  </div>
</template>

<style scoped>

</style>