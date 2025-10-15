<template>
  <div>
    <router-view v-show="$route.meta.showRouterView"></router-view>
    <element-view v-show="!$route.meta.showRouterView"></element-view>
  </div>
</template>

<script>
import ElementView from './views/IndexView.vue'
export default {
  name: 'App',
  components: {
    ElementView
  }
}
</script>

<style>
</style>
