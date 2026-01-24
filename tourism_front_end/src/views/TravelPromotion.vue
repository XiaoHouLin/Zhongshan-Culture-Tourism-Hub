<script>
export default {
  name: 'TravelPromotion',
  data() {
    return {
      highlights: [
        {
          name: '孙中山故里 · 南朗',
          summary: '探访翠亨村与故居纪念馆，感受近代史脉络。',
          tag: '历史人文',
          icon: '🏛️'
        },
        {
          name: '石岐老城',
          summary: '骑楼街巷与城市老字号串联，适合慢步寻味。',
          tag: '城市记忆',
          icon: '🏘️'
        },
        {
          name: '岐江滨水夜景',
          summary: '桥畔夜色与滨水步道，开启中山的夜生活。',
          tag: '夜游推荐',
          icon: '🌃'
        },
        {
          name: '古镇灯饰',
          summary: '灯都产业与文创体验并重，适合打卡选购。',
          tag: '产业文旅',
          icon: '💡'
        },
        {
          name: '五桂山山水',
          summary: '绿道徒步与森林氧吧，让旅程更治愈。',
          tag: '自然慢游',
          icon: '🌿'
        }
      ],
      routes: [
        {
          title: '城市记忆线',
          duration: '1日',
          stops: ['孙中山故居', '孙文西路', '石岐老街', '石岐乳鸽']
        },
        {
          title: '岐江夜游线',
          duration: '半日',
          stops: ['岐江桥夜景', '西区滨水', '兴中广场']
        },
        {
          title: '灯饰文创线',
          duration: '半日',
          stops: ['古镇灯饰城', '灯都产业展厅', '文创选购']
        },
        {
          title: '山水度假线',
          duration: '1-2日',
          stops: ['五桂山', '紫马岭公园', '三乡温泉']
        }
      ],
      souvenirs: [
        {
          name: '古镇灯影小夜灯',
          origin: '古镇镇',
          description: '以灯都设计为灵感，营造温暖的居家氛围。',
          icon: '🛋️'
        },
        {
          name: '菊花文创茶礼',
          origin: '小榄镇',
          description: '菊花文化入茶，适合送礼与日常品饮。',
          icon: '🌼'
        },
        {
          name: '岐江夜景明信片',
          origin: '石岐街道',
          description: '记录江畔灯火与骑楼街景的旅行记忆。',
          icon: '📮'
        },
        {
          name: '孙中山故里纪念册',
          origin: '南朗镇',
          description: '收藏城市精神与历史故事的纸质文创。',
          icon: '📘'
        }
      ],
      tips: [
        '夜游建议选择岐江桥周边与西区滨水步道，拍摄灯火倒影。',
        '文创商城支持游客浏览，加入购物车与下单会跳转登录。',
        '节庆季（菊花会、民俗活动）建议提前预约与规划路线。',
        '亲子出行可搭配五桂山 + 紫马岭公园，行程轻松舒缓。'
      ],
      revealObserver: null
    };
  },
  methods: {
    goTo(route) {
      if (route) {
        this.$router.push(route).catch(() => {});
      }
    },
    initReveal() {
      const elements = this.$el.querySelectorAll('.reveal, .stagger');
      if (!elements.length) {
        return;
      }
      if (!('IntersectionObserver' in window)) {
        elements.forEach(el => el.classList.add('is-visible'));
        return;
      }
      this.revealObserver = new IntersectionObserver(
        entries => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              entry.target.classList.add('is-visible');
              this.revealObserver.unobserve(entry.target);
            }
          });
        },
        { threshold: 0.15 }
      );
      elements.forEach(el => this.revealObserver.observe(el));
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initReveal();
    });
  },
  beforeDestroy() {
    if (this.revealObserver) {
      this.revealObserver.disconnect();
      this.revealObserver = null;
    }
  }
};
</script>

<template>
  <div class="promotion-container">
    <section class="hero-card reveal">
      <div class="hero-copy">
        <p class="hero-eyebrow">中山目的地灵感</p>
        <h1>岭南慢城 · 中山旅程</h1>
        <p>精选中山核心片区与主题线路，搭配文创灵感，轻松规划慢游体验。</p>
        <div class="hero-actions">
          <el-button type="primary" round @click="goTo('/travel-experiences')">
            查看景点导览
          </el-button>
          <el-button round plain @click="goTo('/culturalProducts')">
            逛文创商城
          </el-button>
        </div>
      </div>
      <div class="hero-panel">
        <div class="hero-panel-item">
          <span>5+</span>
          <small>核心片区</small>
        </div>
        <div class="hero-panel-item">
          <span>4</span>
          <small>主题路线</small>
        </div>
        <div class="hero-panel-item">
          <span>游客可浏览</span>
          <small>文创免登录</small>
        </div>
      </div>
    </section>

    <section class="section-wrapper reveal">
      <div class="section-header">
        <h2>核心片区推荐</h2>
        <p>围绕城市记忆、产业文旅与山水慢游，完成中山打卡。</p>
      </div>
      <el-row :gutter="20">
        <el-col :md="8" :sm="12" :xs="24" v-for="(item, index) in highlights" :key="item.name">
          <el-card shadow="hover" class="info-card stagger" :style="{ transitionDelay: `${index * 0.08}s` }">
            <div class="card-top">
              <span class="card-icon">{{ item.icon }}</span>
              <span class="card-tag">{{ item.tag }}</span>
            </div>
            <h3>{{ item.name }}</h3>
            <p class="summary">{{ item.summary }}</p>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="section-wrapper reveal">
      <div class="section-header">
        <h2>主题路线规划</h2>
        <p>根据时间与兴趣选线，一键对接城市导览。</p>
      </div>
      <el-row :gutter="20">
        <el-col :md="12" :sm="24" v-for="(route, index) in routes" :key="route.title">
          <el-card shadow="hover" class="route-card stagger" :style="{ transitionDelay: `${index * 0.08}s` }">
            <div class="route-header">
              <h3>{{ route.title }}</h3>
              <span class="route-duration">{{ route.duration }}</span>
            </div>
            <div class="route-stops">
              <span v-for="stop in route.stops" :key="stop" class="route-stop">{{ stop }}</span>
            </div>
            <el-button type="text" class="route-link" @click="goTo('/')">查看首页详情 →</el-button>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="section-wrapper reveal">
      <div class="section-header">
        <h2>文创灵感</h2>
        <p>从灯饰到纪念册，把中山的光与味带回家。</p>
      </div>
      <el-row :gutter="20">
        <el-col :md="6" :sm="12" :xs="24" v-for="(souvenir, index) in souvenirs" :key="souvenir.name">
          <el-card shadow="hover" class="info-card stagger" :style="{ transitionDelay: `${index * 0.08}s` }">
            <div class="card-top">
              <span class="card-icon">{{ souvenir.icon }}</span>
              <span class="card-tag">灵感来自 · {{ souvenir.origin }}</span>
            </div>
            <h3>{{ souvenir.name }}</h3>
            <p class="summary">{{ souvenir.description }}</p>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="tips-card reveal">
      <h2>出行贴士</h2>
      <ul>
        <li v-for="tip in tips" :key="tip">{{ tip }}</li>
      </ul>
      <div class="tips-actions">
        <el-button type="primary" round @click="goTo('/')">返回首页</el-button>
        <el-button round plain @click="goTo('/culturalProducts')">进入文创商城</el-button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.promotion-container {
  --ink: #1c2c27;
  --ink-soft: #40534c;
  --accent: #c75a31;
  --teal: #1f7a6d;
  --paper: #ffffff;
  padding: 48px 60px 64px;
  background:
    radial-gradient(circle at 15% 12%, rgba(31, 122, 109, 0.12), transparent 45%),
    radial-gradient(circle at 85% 18%, rgba(199, 90, 49, 0.12), transparent 50%),
    linear-gradient(135deg, #f7f5f1 0%, #f5f9fb 55%, #ffffff 100%);
  min-height: 100vh;
  color: var(--ink);
}

.hero-card {
  background: linear-gradient(120deg, rgba(31, 122, 109, 0.95), rgba(199, 90, 49, 0.9));
  border-radius: 22px;
  padding: 48px 56px;
  color: #fff;
  margin-bottom: 40px;
  box-shadow: 0 20px 45px rgba(31, 56, 94, 0.25);
  display: flex;
  gap: 32px;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.hero-copy h1 {
  font-size: 42px;
  margin-bottom: 12px;
  letter-spacing: 2px;
}

.hero-copy p {
  font-size: 16px;
  margin-bottom: 22px;
  max-width: 520px;
  line-height: 1.7;
}

.hero-eyebrow {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  margin-bottom: 10px;
  opacity: 0.85;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-panel {
  display: grid;
  gap: 14px;
  min-width: 200px;
}

.hero-panel-item {
  background: rgba(255, 255, 255, 0.18);
  padding: 12px 16px;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-weight: 600;
}

.hero-panel-item small {
  font-size: 12px;
  opacity: 0.85;
}

.section-wrapper {
  margin-bottom: 40px;
}

.section-header h2 {
  font-size: 26px;
  font-weight: bold;
  margin-bottom: 10px;
  color: var(--ink);
}

.section-header p {
  color: var(--ink-soft);
  line-height: 1.6;
  margin-bottom: 20px;
}

.info-card {
  border-radius: 16px;
  min-height: 210px;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.card-icon {
  font-size: 40px;
}

.card-tag {
  font-size: 12px;
  color: var(--accent);
  font-weight: bold;
}

.summary {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
}

.route-card {
  border-radius: 16px;
  min-height: 180px;
}

.route-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.route-duration {
  font-size: 12px;
  color: var(--ink-soft);
}

.route-stops {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.route-stop {
  background: rgba(31, 122, 109, 0.1);
  color: var(--teal);
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.route-link {
  color: var(--accent);
  font-weight: 600;
}

.tips-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 12px 28px rgba(31, 56, 94, 0.12);
}

.tips-card ul {
  padding-left: 20px;
  color: #444;
  line-height: 1.8;
}

.tips-actions {
  margin-top: 18px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.reveal,
.stagger {
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.7s ease, transform 0.7s ease;
}

.reveal.is-visible,
.stagger.is-visible {
  opacity: 1;
  transform: translateY(0);
}

@media (max-width: 900px) {
  .promotion-container {
    padding: 32px 24px 48px;
  }

  .hero-card {
    padding: 36px 28px;
  }
}

@media (max-width: 680px) {
  .hero-copy h1 {
    font-size: 32px;
  }

  .hero-actions {
    flex-direction: column;
  }
}
</style>
