<script setup>

</script>

<template>
  <div style="text-align: center;margin-top: 10%">
    <p style="font-size: 200px; margin: 0;">🎁</p>
    <p class="strong" style="font-size: 50px; margin: 0;">
      您的文创好物正在打包发往旅程。
    </p>
    <p class="strong" style="color: #6a6a6e; font-size: 30px; margin-top: 20px;">
      请稍候，精彩即将送达。
    </p>
    <el-button
        type="primary"
        icon="el-icon-goods"
        @click="()=>{this.$router.push('/userPanel/culturalProducts');}"
        round>继续逛逛
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
