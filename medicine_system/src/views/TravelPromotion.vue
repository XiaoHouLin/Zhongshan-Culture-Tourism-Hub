<script>
export default {
  name: 'TravelPromotion',
  data() {
    return {
      destinations: [
        {
          name: '云海仙境 · 张家界',
          summary: '穿梭石英砂岩峰林，邂逅沉浸式悬崖玻璃栈道。',
          tag: '山地自然',
          icon: '⛰️'
        },
        {
          name: '风雅水乡 · 乌镇',
          summary: '青瓦白墙、乌篷船与烟火气，共写慢生活的江南诗意。',
          tag: '人文古镇',
          icon: '🌉'
        },
        {
          name: '星辰大漠 · 阿拉善',
          summary: '骑乘骆驼踏浪黄沙，夜宿银河之下的星空营地。',
          tag: '沙漠探险',
          icon: '🌌'
        }
      ],
      souvenirs: [
        {
          name: '云想霓裳丝巾',
          origin: '张家界',
          description: '以砂岩脉络为纹样，象征山川与云雾的交织。',
          icon: '🧣'
        },
        {
          name: '水乡手作香囊',
          origin: '乌镇',
          description: '糅合桂花与青蒿香气，融入蓝印花布手工针脚。',
          icon: '🪡'
        },
        {
          name: '星辰砂砾琉璃',
          origin: '阿拉善',
          description: '将大漠晚霞的色阶凝入玻璃，珍藏旅途余晖。',
          icon: '✨'
        }
      ]
    };
  }
};
</script>

<template>
  <div class="promotion-container">
    <section class="hero-card">
      <h1>旅•拾光</h1>
      <p>精选目的地灵感与文创灵感，为你的下一程旅途注入故事与温度。</p>
      <el-button type="primary" round @click="$router.push('/register')">
        立即启程
      </el-button>
    </section>

    <section class="section-wrapper">
      <h2>热门灵感</h2>
      <el-row :gutter="20">
        <el-col :span="8" v-for="item in destinations" :key="item.name">
          <el-card shadow="hover" class="info-card gradient-card">
            <div class="card-icon">{{ item.icon }}</div>
            <h3>{{ item.name }}</h3>
            <p class="tag">{{ item.tag }}</p>
            <p class="summary">{{ item.summary }}</p>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="section-wrapper">
      <h2>文创精选</h2>
      <el-row :gutter="20">
        <el-col :span="8" v-for="souvenir in souvenirs" :key="souvenir.name">
          <el-card shadow="hover" class="info-card">
            <div class="card-icon">{{ souvenir.icon }}</div>
            <h3>{{ souvenir.name }}</h3>
            <p class="tag">灵感来自 · {{ souvenir.origin }}</p>
            <p class="summary">{{ souvenir.description }}</p>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="section-wrapper tips-card">
      <h2>旅行顾问贴士</h2>
      <ul>
        <li>合理规划文创库存，结合旺季和节庆补足限量礼盒。</li>
        <li>运用订单看板追踪高需求线路，及时调整推荐话术。</li>
        <li>结合用户画像推送「目的地 + 文创」主题礼包，提升复购率。</li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.promotion-container {
  padding: 40px 60px;
  background: linear-gradient(135deg, #f0f6ff 0%, #ffffff 100%);
}

.hero-card {
  background: radial-gradient(circle at top left, #8bc6ec, #9599e2);
  border-radius: 18px;
  padding: 50px 60px;
  color: #fff;
  margin-bottom: 40px;
  box-shadow: 0 20px 45px rgba(114, 140, 214, 0.35);
  text-align: left;
}

.hero-card h1 {
  font-size: 48px;
  margin-bottom: 10px;
  letter-spacing: 4px;
}

.hero-card p {
  font-size: 18px;
  margin-bottom: 25px;
  max-width: 520px;
}

.section-wrapper {
  margin-bottom: 40px;
}

.section-wrapper h2 {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #3c5a99;
}

.info-card {
  border-radius: 16px;
  min-height: 220px;
}

.gradient-card {
  background: linear-gradient(145deg, rgba(83, 137, 245, 0.15), rgba(241, 143, 1, 0.1));
}

.card-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.tag {
  font-size: 14px;
  color: #5c7cfa;
  font-weight: bold;
  margin-bottom: 12px;
}

.summary {
  font-size: 15px;
  color: #555;
  line-height: 1.6;
}

.tips-card {
  background: #f9fbff;
  border: 1px solid rgba(83, 137, 245, 0.2);
  border-radius: 18px;
  padding: 30px;
}

.tips-card ul {
  padding-left: 20px;
  color: #444;
  line-height: 1.8;
}
</style>
