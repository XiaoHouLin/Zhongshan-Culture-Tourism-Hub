<script>
export default {
  data() {
    return {
      activeIndex: 'home',
      navItems: [
        { index: 'home', label: '首页', route: '/' },
        { index: 'destinations', label: '目的地推荐', route: '/travel-experiences' },
        { index: 'register', label: '注册', route: '/register' }
      ],
      heroContent: {
        eyebrow: 'TravelHub 城市旅游宣传平台',
        title: '探索城市，开启本地旅程',
        description: '整合热门景区、文化活动与智能行程推荐，让游客与管理者快速掌握城市旅游亮点。',
        stats: [
          { value: '25+', label: '热门城市' },
          { value: '160+', label: '精品路线' },
          { value: '480+', label: '真实分享' }
        ]
      },
      heroIllustration: require('../assets/1v1.svg'),
      carouselItems: [
        { jpg: '/1.jpg', title: '滨海夜色' },
        { jpg: '/2.jpg', title: '人文老街' },
        { jpg: '/3.jpg', title: '山野徒步' },
        { jpg: '/4.jpg', title: '城市地标' }
      ],
      featureCards: [
        {
          icon: '📍',
          title: '目的地一键掌握',
          description: '聚合景区资讯、开放时间与高人气玩法，让旅客快速锁定心仪去处。'
        },
        {
          icon: '🧭',
          title: '智能行程管理',
          description: '自动生成推荐路线，并同步团队安排、库存与提醒。'
        },
        {
          icon: '📸',
          title: '旅客故事沉淀',
          description: '收集真实游记、短评和图集，打造可信赖的口碑库。'
        }
      ],
      highlightCards: [
        {
          title: '目的地灵感库',
          description: '通过城市主题、节庆活动与季节亮点，激发更具沉浸感的旅行灵感。',
          svg: require('../assets/1v1.svg')
        },
        {
          title: '角色协作中心',
          description: '管理员、顾问、游客同台协作，订单处理与库存补充都更高效。',
          svg: require('../assets/user-oriented.svg')
        }
      ],
      ctaCopy: {
        title: '还在为行程筹备发愁？',
        description: '立即登录 TravelHub，将复杂的旅行筹备交给系统，团队与游客都能省心放心。'
      },
      rules: {
        url: '/zqxy.jpg'
      }
    };
  },
  methods: {
    handleSelect(index) {
      const matched = this.navItems.find(item => item.index === index);
      if (matched) {
        this.goTo(matched.route);
      }
    },
    handleLogoClick() {
      this.goTo('/login');
    },
    goTo(route) {
      if (route) {
        this.$router.push(route);
        const matched = this.navItems.find(item => item.route === route);
        if (matched) {
          this.activeIndex = matched.index;
        }
      }
    }
  },
  mounted() {
    if (typeof this.tip === 'function') {
      this.tip();
    }
  }
};
</script>

<template>
  <el-container class="index-layout">
    <el-header class="index-header">
      <div class="header-content">
        <div class="logo-container" @click="goTo('/')">
          <el-image class="logo" :src="rules.url" fit="cover" alt="TravelHub 标志" />
          <div class="logo-text">
            <h1>TravelHub 城市旅游宣传平台</h1>
            <span>*学生作业，非官方平台</span>
          </div>
        </div>
        <div class="nav-wrapper">
          <el-menu
            class="nav-menu"
            mode="horizontal"
            :default-active="activeIndex"
            background-color="transparent"
            text-color="#2f3b52"
            active-text-color="#0056b3"
            :style="{ borderBottom: 'none' }"
            @select="handleSelect"
          >
            <el-menu-item
              v-for="item in navItems"
              :key="item.index"
              :index="item.index"
            >
              {{ item.label }}
            </el-menu-item>
          </el-menu>
          <el-button type="primary" class="login-button" @click="handleLogoClick">
            登录
          </el-button>
        </div>
      </div>
    </el-header>

    <el-main class="index-main">
      <section class="hero-section">
        <div class="hero-inner content-container">
          <div class="hero-copy">
            <p class="hero-eyebrow">{{ heroContent.eyebrow }}</p>
            <h2 class="hero-title">{{ heroContent.title }}</h2>
            <p class="hero-description">
              {{ heroContent.description }}
            </p>
            <div class="hero-actions">
              <el-button type="primary" size="medium" round @click="goTo('/travel-experiences')">
                开始探索
              </el-button>
              <el-button size="medium" round plain @click="handleLogoClick">
                管理登录
              </el-button>
            </div>
            <div class="hero-stats">
              <div v-for="stat in heroContent.stats" :key="stat.label" class="stat-item">
                <span class="stat-value">{{ stat.value }}</span>
                <span class="stat-label">{{ stat.label }}</span>
              </div>
            </div>
          </div>
          <div class="hero-visual">
            <img :src="heroIllustration" alt="城市旅游插画" />
          </div>
        </div>
      </section>

      <section class="feature-section content-container">
        <h3 class="section-title">一站式城市旅游服务</h3>
        <p class="section-subtitle">
          从灵感探索到行程管理，TravelHub 为每位旅客提供高品质的旅行体验。
        </p>
        <el-row :gutter="24">
          <el-col
            v-for="feature in featureCards"
            :key="feature.title"
            :md="8"
            :sm="12"
            :xs="24"
            class="feature-column"
          >
            <el-card shadow="hover" class="feature-card">
              <div class="feature-icon">{{ feature.icon }}</div>
              <h4 class="feature-title">{{ feature.title }}</h4>
              <p class="feature-description">{{ feature.description }}</p>
            </el-card>
          </el-col>
        </el-row>
      </section>

      <section class="carousel-section">
        <div class="content-container">
          <h3 class="section-title">城市风光速览</h3>
          <p class="section-subtitle">
            精选城市景观，感受都市与自然融合的魅力。
          </p>
        </div>
        <el-carousel :interval="5000" arrow="always" height="420px" class="hero-carousel">
          <el-carousel-item v-for="item in carouselItems" :key="item.jpg">
            <div class="carousel-content">
              <img :src="item.jpg" :alt="item.title" class="carousel-image" />
              <div class="carousel-caption">
                <span>{{ item.title }}</span>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </section>

      <section class="highlight-section content-container">
        <el-row :gutter="24">
          <el-col
            v-for="card in highlightCards"
            :key="card.title"
            :md="12"
            :sm="24"
            class="highlight-column"
          >
            <el-card shadow="hover" class="highlight-card">
              <div class="highlight-content">
                <div class="highlight-copy">
                  <h4>{{ card.title }}</h4>
                  <p>{{ card.description }}</p>
                </div>
                <el-image :src="card.svg" fit="contain" class="highlight-illustration" />
              </div>
            </el-card>
          </el-col>
        </el-row>
      </section>

      <section class="cta-section">
        <div class="cta-card content-container">
          <div class="cta-copy">
            <h3>{{ ctaCopy.title }}</h3>
            <p>{{ ctaCopy.description }}</p>
          </div>
          <div class="cta-actions">
            <el-button type="primary" size="medium" round @click="goTo('/login')">
              立即登录
            </el-button>
            <el-button size="medium" round plain @click="goTo('/register')">
              注册账号
            </el-button>
          </div>
        </div>
      </section>

      <footer class="footer">
        <p>© POWER BY @202224111240🧠...Maybe🐵</p>
      </footer>
    </el-main>
  </el-container>
</template>

<style scoped>
.index-layout {
  min-height: 100vh;
  background-color: #f5f8ff;
}

.index-header {
  background: #ffffff;
  box-shadow: 0 6px 24px rgba(31, 56, 94, 0.06);
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  height: 88px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
}

.logo {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  transition: transform 0.2s ease;
}

.logo:hover {
  transform: scale(1.05);
}

.logo-text h1 {
  margin: 0;
  font-size: 20px;
  color: #1f385e;
  font-weight: 700;
}

.logo-text span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #dd4643;
}

.nav-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-menu {
  background: transparent;
  border-bottom: none;
}

.login-button {
  font-weight: 600;
  border-radius: 18px;
  padding: 10px 22px;
}

.index-main {
  padding: 40px 0 64px;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  box-sizing: border-box;
}

.hero-section {
  padding: 72px 0 48px;
  background: linear-gradient(135deg, #e1f4ff 0%, #f7fbff 48%, #f8f1ff 100%);
}

.hero-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 48px;
}

.hero-copy {
  max-width: 560px;
}

.hero-eyebrow {
  color: #007ab8;
  font-weight: 600;
  margin-bottom: 8px;
  letter-spacing: 0.08em;
}

.hero-title {
  font-size: 42px;
  line-height: 1.2;
  color: #1f385e;
  margin-bottom: 16px;
}

.hero-description {
  font-size: 16px;
  color: #5f6e8b;
  line-height: 1.8;
  margin-bottom: 28px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.hero-stats {
  display: flex;
  gap: 28px;
  flex-wrap: wrap;
}

.stat-item {
  min-width: 120px;
}

.stat-value {
  display: block;
  font-size: 30px;
  font-weight: 700;
  color: #0056b3;
}

.stat-label {
  color: #5f6e8b;
  font-size: 14px;
}

.hero-visual {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}

.hero-visual img {
  width: 100%;
  max-width: 460px;
}

.feature-section {
  margin-top: 72px;
}

.section-title {
  font-size: 28px;
  color: #1f385e;
  text-align: center;
  margin-bottom: 8px;
}

.section-subtitle {
  color: #5f6e8b;
  text-align: center;
  margin-bottom: 32px;
  line-height: 1.6;
}

.feature-card {
  border-radius: 18px;
  min-height: 220px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 18px 36px rgba(31, 56, 94, 0.12);
}

.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #42b983, #00a7d3);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  margin-bottom: 18px;
  color: #ffffff;
}

.feature-title {
  font-size: 20px;
  margin-bottom: 10px;
  color: #1f385e;
}

.feature-description {
  margin: 0;
  color: #5f6e8b;
  line-height: 1.6;
}

.carousel-section {
  margin-top: 96px;
}

.hero-carousel {
  margin-top: 24px;
}

.carousel-content {
  position: relative;
  height: 100%;
  border-radius: 24px;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-caption {
  position: absolute;
  left: 32px;
  bottom: 32px;
  background: rgba(0, 0, 0, 0.55);
  color: #ffffff;
  padding: 12px 24px;
  border-radius: 999px;
  font-weight: 600;
}

.highlight-section {
  margin-top: 96px;
}

.highlight-card {
  border-radius: 22px;
  padding: 12px 18px;
}

.highlight-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.highlight-copy h4 {
  font-size: 24px;
  color: #1f385e;
  margin-bottom: 12px;
}

.highlight-copy p {
  margin: 0;
  color: #5f6e8b;
  line-height: 1.7;
}

.highlight-illustration {
  width: 220px;
}

.cta-section {
  margin: 112px 0 80px;
}

.cta-card {
  background: linear-gradient(120deg, #0056b3 0%, #42b983 100%);
  border-radius: 24px;
  padding: 40px 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
  color: #ffffff;
}

.cta-copy h3 {
  font-size: 28px;
  margin: 0 0 12px;
}

.cta-copy p {
  margin: 0;
  font-size: 16px;
  line-height: 1.7;
  max-width: 520px;
  color: rgba(255, 255, 255, 0.9);
}

.cta-actions {
  display: flex;
  gap: 12px;
}

.cta-actions .el-button {
  border-color: rgba(255, 255, 255, 0.65);
  color: #ffffff;
}

.cta-actions .el-button.is-plain {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.35);
}

.cta-actions .el-button.is-plain:hover {
  background: rgba(255, 255, 255, 0.28);
}

.footer {
  text-align: center;
  color: #7d8ea6;
  font-size: 13px;
}

@media (max-width: 1100px) {
  .hero-inner {
    flex-direction: column-reverse;
    text-align: center;
  }

  .hero-copy {
    max-width: none;
  }

  .hero-actions {
    justify-content: center;
  }

  .hero-stats {
    justify-content: center;
  }

  .hero-visual {
    justify-content: center;
  }
}

@media (max-width: 900px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    height: auto;
    padding: 16px 24px;
  }

  .nav-wrapper {
    width: 100%;
    justify-content: space-between;
  }

  .hero-section {
    padding: 56px 0 36px;
  }

  .carousel-section,
  .highlight-section {
    margin-top: 72px;
  }

  .cta-card {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 600px) {
  .header-content {
    padding: 16px;
  }

  .nav-wrapper {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .stat-item {
    min-width: unset;
  }

  .cta-actions {
    width: 100%;
    flex-direction: column;
  }

  .cta-actions .el-button {
    width: 100%;
  }
}
</style>

<style>
.hero-carousel .el-carousel__item {
  display: flex;
  align-items: stretch;
  background: transparent;
}
</style>
