<script>
export default {
  data() {
    return {
      activeIndex: '1',
      activeIndex2: '1',
      // 插图图片
      items: [
        {jpg: '/1.jpg'},
        {jpg: '/2.jpg'},
        {jpg: '/3.jpg'},
        {jpg: '/4.jpg'}
      ],
      backgrounds: [
        {svg: require('../assets/1v1.svg')},
        {svg: require('../assets/user-oriented.svg')}
      ],
      rules: {
        url: '/zqxy.jpg',
      }
    };
  },
  methods: {
    handleSelect(key, keyPath) {
      console.log(key, keyPath);
    },
    /*主页标题logo跳转页面*/
    async handleLogoClick() {
      await this.$router.push('/login');
    }
  },
  mounted() {
    this.tip();
  }
}
</script>


<template>
  <el-container style="background-color: #e7f0fa">
    <el-header style="padding: 0">
      <div>
        <el-menu :default-active="activeIndex" class="el-menu-demo container" mode="horizontal">
          <div class="logo-container">
            <el-image style="width: 80px; height: 80px" :src="rules.url" lass="border-radius" class="logo" alt="Logo"
                      @click="handleLogoClick">
            </el-image>
            <h1 class="manu_logo">TravelHub 旅游信息管理系统</h1>
            <span style="margin-left: 20px;font-size: 15px;font-weight: bold; color:#dd4643">*学生作业，非官方平台</span>
          </div>
          <router-link to="/travel-experiences" class="no-underline">
            <el-menu-item index="1" id="travel_button">
              <router-link class="hypertext" to="/travel-experiences" active-class="active">目的地推荐</router-link>
            </el-menu-item>
          </router-link>
          <router-link to="/register" class="no-underline">
            <el-menu-item index="2" id="register_button">
              <router-link class="hypertext" to="/register" active-class="active">注册</router-link>
            </el-menu-item>
          </router-link>
          <el-menu-item index="3" class="login-container" @click="handleLogoClick" id="login_button">
            <a class="hypertext">登录</a>
          </el-menu-item>
        </el-menu>
        <div class="line"></div>
      </div>
    </el-header>
    <el-main>
      <!--  图片跑马灯-->
      <div>
        <el-carousel :interval="5000" arrow="always" height="700px" style="min-width: 1650px;margin-top: 10px"
                     class="border-radius">
          <el-carousel-item v-for="item in items" :key="item.jpg">
            <img :src="item.jpg" alt="Carousel Image" class="carousel-image"/>
          </el-carousel-item>
        </el-carousel>
      </div>
      <br><br>
      <div style="display: flex;justify-content: center;align-items: center;">
        <el-card shadow="hover" style="width: 1500px">
          <div class="demo-image__placeholder">
            <div class="block">
              <div class="image-with-caption">
                <div class="caption">
                  <h1 class="art-title">目的地灵感<br>随时掌握。</h1>
                  <span>
                <a class="art-text1">一站式掌握景区资讯、文创好物与行程亮点，帮助团队迅速搭建沉浸式旅游体验。</a>
              </span>
                </div>
                <el-image :src="backgrounds[0].svg">
                  <div slot="placeholder" class="image-slot">
                  </div>
                </el-image>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      <br><br><br>
      <div style="display: flex;justify-content: center;align-items: center;">
        <el-card shadow="hover" style="width: 1500px">
          <div class="demo-image__placeholder">
            <div class="block">
              <div class="image-with-caption">
                <div class="caption">
                  <h1 class="art-title">全角色协同<br>乐享旅程。</h1>
                  <span>
                <a class="art-text1">支持管理员、旅行顾问与游客多角色协作，快捷处理订单、补充库存，让每一次旅程都精彩有序。</a>
              </span>
                </div>
                <el-image :src="backgrounds[1].svg">
                  <div slot="placeholder" class="image-slot">
                  </div>
                </el-image>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      <div style="display: block; text-align: center;">
        <p class="strong" style="margin: 15px">© POWER BY @202224111240🧠...Maybe🐵</p>
      </div>
    </el-main>
  </el-container>
</template>

<!--主页导航logo-->
<style scoped>.container {
  display: flex;
  justify-content: flex-end; /* 对齐至右侧 */
}
</style>
<style scoped>.logo-container {
  display: flex;
  align-items: center;
  margin-right: auto; /* 将 logo 容器推到左侧 */
}

.logo {
  cursor: pointer; /* 鼠标悬停时显示手型 */
}

.container {
  display: flex;
  align-items: center; /* 垂直居中对齐 */
}
</style>

<style scoped>.manu_logo {
  padding-left: 1em;
  font-size: 26px;
  font-family: 'PingFang SC', sans-serif;
  color: #007BFF;
}
</style>

<style scoped>.login-container {
  display: inline-block;
  background-color: #007bff;
  border-radius: 10px;
  color: white;
}
</style>

<style scoped>.hypertext {
  color: #007BFF;
  text-decoration: none;
  font-weight: bold;

  .hypertext:hover {
    color: #0056b3;
  }
}
</style>

<!--图片跑马灯-->
<style>
.el-carousel__item img {
  color: #475669;
  font-size: 18px;
  opacity: 0.75;
  line-height: 300px;
  margin: 0;
  border-radius: 10px;
}

.carousel-image {
  max-width: 100%;
  height: auto;
}

.el-carousel__item:nth-child(2n) {
  background-color: #99a9bf;
}

.el-carousel__item:nth-child(2n+1) {
  background-color: #d3dce6;
}
</style>

<!--圆角-->
<style>.border-radius {
  border-radius: 10px;
}
</style>

<!--设置导航栏登录及注册按钮激活状态逻辑-->
<style>
#login_button {
  border-bottom: none !important;
  color: white !important;
}

#login_button:hover {
  color: black !important;
}

#register_button {
  border-bottom: none !important;
  color: black !important;
}

#register_button:hover {
  color: #5f6165 !important;
}
</style>

<style>
.art-title {
  background-image: linear-gradient(to right, #42b983, #0056b3);
  -webkit-background-clip: text;
  -moz-background-clip: border;
  background-clip: text;
  color: transparent;
  font-size: 100px;
}

.no-underline {
  text-decoration: none; /* 移除下划线 */
  border-bottom: none; /* 移除底部横线 */
}

.art-text1 {
  background-image: linear-gradient(to right, #07759f, #00723c);
  -webkit-background-clip: text;
  -moz-background-clip: border;
  background-clip: text;
  color: transparent;
  font-size: 20px;
  font-weight: bold;
}
</style>

<!--背景图片-->
<style scoped>
.demo-image__placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.block {
  display: flex;
  align-items: center;
}

.image-with-caption {
  display: flex;
  align-items: center;
}

.caption {
  margin-right: 1rem; /* 调整标题与图片之间的间距 */
}

/* 图片的样式 */
.el-image {
  width: 1000px;
}

/* 占位符的样式 */
.image-slot {
  background-color: #eee;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 标题样式 */
h1 {
  font-size: 80px; /* 字体大小 */
}
</style>
