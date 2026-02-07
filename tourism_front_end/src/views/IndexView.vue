
<script>
import {jwtDecode} from 'jwt-decode';
import MessageCenter from '@/components/MessageCenter.vue';
import ChatAssistant from '@/components/ChatAssistant.vue';
// TODO: 首页逻辑维护记录，文案统一使用 UTF-8 编码
const AMAP_SCRIPT_ID = 'amap-js-sdk';
const AMAP_UI_SCRIPT_ID = 'amap-ui-sdk';

export default {
  name: 'IndexView',
  components: {
    MessageCenter,
    ChatAssistant
  },
  data() {
    return {
      navLinks: [
        { label: '首页', route: '/' },
        { label: '景点导览', route: '/travel-experiences' },
        { label: '文创商城', route: '/culturalProducts', emphasis: true }
      ],
      token: sessionStorage.getItem('token') || '',
      currentUser: null,
      heroContent: {
        eyebrow: '中山市城市文化旅游特色展示系统',
        title: '遇见中山：岐江水岸 · 孙中山故里 · 灯饰之都',
        description:
          '串联城市文化、景区资源与文创服务，提供导览、路线、活动与商城一体化入口，让游客快速认识中山并高效安排行程。',
        stats: [
          { value: '6+', label: '主题慢游线路' },
          { value: '12+', label: '精选景点与街区' },
          { value: '游客可浏览', label: '文创商城免登录' }
        ],
        tags: ['孙中山故里', '岐江夜景', '古镇灯饰', '小榄菊花']
      },
      shopSpotlight: {
        eyebrow: '核心入口',
        title: '文创商城 · 中山城市礼遇',
        description:
          '游客可直接浏览文创商品，加入购物车与购买将自动引导登录，保障订单与权益。',
        highlights: [
          { title: '灯饰文创', description: '古镇灯艺与家居设计精选' },
          { title: '侨乡手作', description: '非遗工艺与岭南手作' },
          { title: '城市礼盒', description: '主题纪念与节庆限定' }
        ],
        stats: [
          { value: '免登录浏览', label: '游客友好' },
          { value: '登录后下单', label: '交易安全' }
        ]
      },
      quickEntries: [
        {
          title: '景点导览',
          description: '景点、街区与主题线路一站汇总。',
          action: '查看线路',
          route: '/travel-experiences',
          tag: '必看',
          icon: '🧭'
        },
        {
          title: '文创商城',
          description: '游客可免登录浏览中山文创与灯饰设计。',
          action: '立即逛商城',
          route: '/culturalProducts',
          tag: '核心',
          icon: '🛍️',
          featured: true
        },
        {
          title: '行程管理',
          description: '登录后可收藏景点与管理订单。',
          action: '立即登录',
          route: '/login',
          tag: '账户',
          icon: '👤'
        }
      ],
      cityIntro: {
        title: '城市介绍',
        description:
          '中山位于珠江口西岸，连接广州、珠海等大湾区核心城市。这里既有孙中山故里的人文底蕴，也有岭南水乡与现代产业交织的生活气息，是一座适合慢游与深度体验的城市。',
        quote: '以岐江为脉，慢慢读懂中山。',
        facts: [
          { label: '城市名片', value: '孙中山故里 · 侨乡文化' },
          { label: '城市气质', value: '岭南水乡 · 慢生活' },
          { label: '产业印象', value: '灯饰之都 · 智造新城' },
          { label: '代表味道', value: '石岐乳鸽 · 小榄菊花宴' }
        ]
      },
      mapOverview: {
        title: '城市地图与镇区',
        description:
          '以石岐为传统中心，东区承载商务与城市活力，西区与岐江滨水形成休闲带，北部古镇是灯饰产业核心，南部三乡与五桂山连接山水度假资源。',
        tips: ['建议以石岐老城为起点', '夜游岐江桥与西区滨水', '古镇灯饰城适合白天打卡'],
        note: '提示：鼠标悬停镇区轮廓查看代表景点与美食。'
      },
      mapTags: ['孙文西路骑楼', '岐江夜景', '古镇灯饰博览', '小榄菊花会', '詹园', '五桂山绿道'],
      experienceCards: [
        {
          icon: '🏛️',
          title: '历史街区漫游',
          description: '孙文西路骑楼、石岐老街与城市地标，适合步行与城市摄影。'
        },
        {
          icon: '🌿',
          title: '山水慢生活',
          description: '五桂山绿道、紫马岭公园与南部乡村，让旅程有呼吸感。'
        },
        {
          icon: '💡',
          title: '产业文旅探索',
          description: '古镇灯饰产业链展示与文创体验，感受中山制造与设计力。'
        }
      ],
      carouselItems: [
        { jpg: '/1.jpg', title: '岐江夜景' },
        { jpg: '/2.jpg', title: '孙文西路骑楼' },
        { jpg: '/3.jpg', title: '五桂山绿道' },
        { jpg: '/4.jpg', title: '古镇灯饰' }
      ],
      cultureCards: [
        {
          title: '孙中山故里文化',
          description: '故居纪念馆与相关展陈，呈现中山城市精神与近代史脉络。'
        },
        {
          title: '小榄菊花会',
          description: '秋冬时节的传统民俗盛会，花展与市集兼具地方特色。'
        },
        {
          title: '醉龙舞与民俗',
          description: '岭南传统舞蹈与庙会巡游，展现中山民间生活气息。'
        }
      ],
      foodList: ['石岐乳鸽', '沙溪扣肉', '神湾菠萝', '小榄脆肉鲩', '三乡濑粉'],
      nightRoute: ['岐江桥夜景', '孙文西路夜市', '兴中广场'],
      routeCards: [
        {
          title: '经典一日线',
          duration: '8-10小时',
          tag: '首次必选',
          stops: ['孙中山故居', '孙文西路', '岐江桥夜景', '石岐乳鸽']
        },
        {
          title: '文创轻游线',
          duration: '4-6小时',
          tag: '文创体验',
          stops: ['古镇灯饰城', '灯都生态廊道', '文创选购']
        },
        {
          title: '山水亲子线',
          duration: '半日',
          tag: '自然放松',
          stops: ['五桂山', '紫马岭公园', '城市绿道']
        },
        {
          title: '温泉度假线',
          duration: '1-2日',
          tag: '慢享生活',
          stops: ['三乡温泉', '中山詹园', '乡村小住']
        }
      ],
      shopIntro: {
        title: '文创商城入口',
        subtitle: '把中山的灯与味带回家',
        description:
          '聚合灯饰文创、非遗手作与城市伴手礼，游客可直接浏览商品，登录后即可下单与管理订单。',
        features: [
          { title: '灯饰文创', description: '古镇灯艺与家居设计精选' },
          { title: '地方非遗', description: '传统工艺与节庆限定' },
          { title: '旅行伴手礼', description: '城市主题纪念与礼盒' }
        ]
      },
      shopTags: ['设计灯具', '岭南手作', '城市礼盒', '节庆限定'],
      ctaCopy: {
        title: '开启你的中山慢游计划',
        description: '收藏景点、规划路线、加入文创清单，让每一次出发都更从容。'
      },
      logoInfo: {
        url: '/zqxy.jpg'
      },
      amapKey:
        (typeof window !== 'undefined' && window.AMAP_KEY) ||
        process.env.VUE_APP_AMAP_KEY ||
        '9827c6fd18aa7e77238f230af42469cf',
      amapSecurityCode:
        (typeof window !== 'undefined' && window.AMAP_SECURITY_CODE) ||
        process.env.VUE_APP_AMAP_SECURITY_CODE ||
        'c21177cb56c8467218eecf96a7bbcca2',
      mapLoading: true,
      mapBaseReady: false,
      mapLoadTimer: null,
      mapInstance: null,
      mapPolygons: [],
      mapLabels: [],
      mapDistrictExplorer: null,
      mapDistrictLayer: null,
      townPolygonGroups: Object.create(null),
      hoveredTownName: '',
      mapReady: false,
      mapError: '',
      geojsonUrl: '/geojson/zhongshan_towns.json',
      defaultTownImage: '/towns/zhongshan-default.jpg',
      activeTown: {
        name: '中山市',
        description: '珠江西岸的岭南水乡城市，灯饰产业与历史人文相映成趣。',
        sights: ['孙中山故居', '岐江夜景', '孙文西路', '五桂山'],
        foods: ['石岐乳鸽', '小榄菊花茶', '沙溪扣肉'],
        image: '/towns/zhongshan-default.jpg'
      },
      townInfo: {
        石岐街道: {
          description: '老城核心街区，骑楼街巷与岐江夜景交织。',
          sights: ['孙文西路骑楼', '岐江桥', '兴中广场'],
          foods: ['石岐乳鸽', '老字号糖水']
        },
        东区街道: {
          description: '城市活力片区，公园与城市广场集聚。',
          sights: ['紫马岭公园', '中山纪念公园'],
          foods: ['广式早茶', '特色茶点']
        },
        西区街道: {
          description: '滨水休闲带，适合慢行与夜游。',
          sights: ['岐江滨水', '西区码头'],
          foods: ['河鲜小炒', '岭南家常']
        },
        南区街道: {
          description: '人文园林与乡村风景相伴。',
          sights: ['中山詹园', '南区田园'],
          foods: ['农家菜', '本地点心']
        },
        五桂山街道: {
          description: '山水度假核心，森林绿道与溪谷景观。',
          sights: ['五桂山风景区', '逍遥谷'],
          foods: ['山泉鱼', '山野菜']
        },
        南朗镇: {
          description: '孙中山故里所在地，历史人文浓厚。',
          sights: ['孙中山故居', '翠亨村', '崖口村'],
          foods: ['崖口海鲜', '海鲜粥']
        },
        小榄镇: {
          description: '菊花文化与商贸小镇，节庆氛围浓。',
          sights: ['小榄菊花园', '菊花文化广场'],
          foods: ['小榄菊花宴', '脆肉鲩']
        },
        古镇镇: {
          description: '灯饰产业名镇，工业与文创并重。',
          sights: ['灯饰城', '灯都产业展厅'],
          foods: ['古镇小吃', '岭南甜品']
        },
        沙溪镇: {
          description: '传统饮食名镇，扣肉文化代表。',
          sights: ['沙溪古村', '乡村风貌'],
          foods: ['沙溪扣肉', '手工米食']
        },
        三乡镇: {
          description: '温泉与山水度假结合的慢生活区域。',
          sights: ['三乡温泉', '雍陌古村'],
          foods: ['三乡濑粉', '温泉宴']
        },
        坦洲镇: {
          description: '滨水休闲片区，连接湾区生活。',
          sights: ['坦洲滨水公园', '湿地绿道'],
          foods: ['河鲜美食', '家常小炒']
        },
        神湾镇: {
          description: '菠萝之乡，农业体验丰富。',
          sights: ['神湾菠萝基地', '田园观光'],
          foods: ['神湾菠萝', '菠萝甜品']
        },
        大涌镇: {
          description: '红木文化与传统工艺汇聚。',
          sights: ['红木文化街', '工艺展示区'],
          foods: ['农家菜', '传统点心']
        },
        港口镇: {
          description: '水乡风貌与滨江绿道相融合。',
          sights: ['港口滨水绿道', '田园风光'],
          foods: ['河鲜汤品', '地道小吃']
        },
        民众镇: {
          description: '水乡田园，适合亲子慢游。',
          sights: ['民众水乡', '田园体验'],
          foods: ['水乡小吃', '农家风味']
        },
        黄圃镇: {
          description: '腊味名镇，传统手作保留完整。',
          sights: ['腊味街区', '传统作坊'],
          foods: ['黄圃腊味', '腊味饭']
        },
        阜沙镇: {
          description: '田园生态与乡村体验为主。',
          sights: ['阜沙田园', '乡村绿道'],
          foods: ['乡村家常', '本地小吃']
        },
        三角镇: {
          description: '水网交织的滨水小镇。',
          sights: ['三角滨水', '乡村公园'],
          foods: ['河鲜美食', '岭南小炒']
        },
        横栏镇: {
          description: '花木产业名片，四季皆有绿意。',
          sights: ['花木园区', '花卉观赏带'],
          foods: ['乡村风味', '本地小点']
        },
        东凤镇: {
          description: '产业小镇风貌，城市更新有序。',
          sights: ['产业展厅', '城镇街区'],
          foods: ['本地小吃', '传统点心']
        },
        南头镇: {
          description: '传统街巷与生活气息并存。',
          sights: ['南头老街', '社区广场'],
          foods: ['传统点心', '家常菜']
        },
        板芙镇: {
          description: '田园乡村与生态绿道交织。',
          sights: ['板芙绿道', '乡村景观'],
          foods: ['乡村菜', '地道小吃']
        }
      },
      revealObserver: null
    };
  },
  computed: {
    currentPath() {
      return this.$route.path;
    },
    isLoggedIn() {
      return Boolean(this.token);
    },
    isCustomer() {
      return this.currentUser && this.currentUser.authority === 3;
    },
    displayName() {
      return this.currentUser && this.currentUser.username ? this.currentUser.username : '游客';
    },
    avatarText() {
      if (!this.currentUser || !this.currentUser.username) {
        return '游';
      }
      return this.currentUser.username.trim().slice(0, 1);
    }
  },
  watch: {
    $route() {
      this.refreshUser();
    }
  },
  methods: {
    refreshUser() {
      const token = sessionStorage.getItem('token');
      this.token = token || '';
      if (!token) {
        this.currentUser = null;
        return;
      }
      try {
        this.currentUser = jwtDecode(token);
      } catch (error) {
        this.currentUser = null;
        this.token = '';
        sessionStorage.removeItem('token');
      }
    },
    getOrderStatus(order) {
      if (order && order.orderStatus !== undefined && order.orderStatus !== null) {
        return Number(order.orderStatus);
      }
      if (order && order.pickupStatus === 1) {
        return 3;
      }
      if (order && order.pickupStatus === 0) {
        return 1;
      }
      if (order && order.completeDate) {
        return 3;
      }
      return 0;
    },
    isActive(route) {
      if (route === '/' && (this.currentPath === '/' || this.currentPath === '/indexView')) {
        return true;
      }
      return this.currentPath === route;
    },
    goTo(route) {
      if (route) {
        this.$router.push(route).catch(() => {});
      }
    },
    handleLogout() {
      sessionStorage.removeItem('token');
      localStorage.removeItem('shoppingCart');
      this.token = '';
      this.currentUser = null;
      this.$message.success('已退出登录');
      if (this.$route.path !== '/indexView') {
        this.$router.push('/indexView').catch(() => {});
      }
    },
    handleUserCommand(command) {
      if (command === 'profile') {
        this.$router.push('/userInfo').catch(() => {});
        return;
      }
      if (command === 'orders') {
        sessionStorage.setItem('openOrders', '1');
        this.$router.push('/userInfo').catch(() => {});
        return;
      }
      if (command === 'logout') {
        this.handleLogout();
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
    },
    cleanupReveal() {
      if (this.revealObserver) {
        this.revealObserver.disconnect();
        this.revealObserver = null;
      }
    },
    async initAmap() {
      this.mapLoading = true;
      this.mapError = '';
      this.mapReady = false;
      this.mapBaseReady = false;
      if (this.mapLoadTimer) {
        clearTimeout(this.mapLoadTimer);
      }
      this.mapLoadTimer = setTimeout(() => {
        if (!this.mapBaseReady && !this.mapError) {
          this.mapError = '地图加载超时，请检查 Key、安全密钥或网络。';
          this.mapLoading = false;
        }
      }, 9000);
      try {
        const AMap = await this.loadAmap();
        let AMapUI = null;
        try {
          AMapUI = await this.loadAmapUI();
        } catch (error) {
          AMapUI = null;
        }
        this.createMap(AMap, AMapUI);
      } catch (error) {
        this.mapError = '地图加载失败，请检查高德 Key 或网络连接。';
      } finally {
        if (this.mapError) {
          this.mapLoading = false;
          if (this.mapLoadTimer) {
            clearTimeout(this.mapLoadTimer);
            this.mapLoadTimer = null;
          }
        }
      }
    },
    loadAmap() {
      if (window.AMap) {
        return Promise.resolve(window.AMap);
      }
      if (this.amapSecurityCode) {
        window._AMapSecurityConfig = {
          securityJsCode: this.amapSecurityCode
        };
      }
      return new Promise((resolve, reject) => {
        const existing = document.getElementById(AMAP_SCRIPT_ID);
        if (existing) {
          const timer = setInterval(() => {
            if (window.AMap) {
              clearInterval(timer);
              resolve(window.AMap);
            }
          }, 60);
          setTimeout(() => {
            clearInterval(timer);
            reject(new Error('AMap load timeout'));
          }, 8000);
          return;
        }
        const script = document.createElement('script');
        script.id = AMAP_SCRIPT_ID;
        script.src = `https://webapi.amap.com/maps?v=2.0&key=${this.amapKey}&plugin=AMap.DistrictSearch,AMap.Scale,AMap.ToolBar`;
        script.crossOrigin = 'anonymous';
        script.async = true;
        script.onload = () => {
          if (window.AMap) {
            resolve(window.AMap);
          } else {
            reject(new Error('AMap not available'));
          }
        };
        script.onerror = () => reject(new Error('AMap load failed'));
        document.head.appendChild(script);
      });
    },
    loadAmapUI() {
      if (window.AMapUI && window.AMapUI.loadUI) {
        return Promise.resolve(window.AMapUI);
      }
      return new Promise((resolve, reject) => {
        const existing = document.getElementById(AMAP_UI_SCRIPT_ID);
        if (existing) {
          const timer = setInterval(() => {
            if (window.AMapUI && window.AMapUI.loadUI) {
              clearInterval(timer);
              resolve(window.AMapUI);
            }
          }, 60);
          setTimeout(() => {
            clearInterval(timer);
            reject(new Error('AMapUI load timeout'));
          }, 8000);
          return;
        }
        const script = document.createElement('script');
        script.id = AMAP_UI_SCRIPT_ID;
        script.src = 'https://webapi.amap.com/ui/1.1/main.js?v=1.1.1';
        script.crossOrigin = 'anonymous';
        script.async = true;
        script.onload = () => {
          if (window.AMapUI && window.AMapUI.loadUI) {
            resolve(window.AMapUI);
          } else {
            reject(new Error('AMapUI not available'));
          }
        };
        script.onerror = () => reject(new Error('AMapUI load failed'));
        document.head.appendChild(script);
      });
    },
    createMap(AMap, AMapUI) {
      if (!document.getElementById('zs-amap')) {
        this.mapLoading = false;
        return;
      }
      const map = new AMap.Map('zs-amap', {
        zoom: 10.4,
        center: [113.391, 22.521],
        viewMode: '2D',
        zooms: [9, 13],
        mapStyle: 'amap://styles/whitesmoke'
      });
      this.mapInstance = map;
      this.mapBaseReady = true;
      if (this.mapLoadTimer) {
        clearTimeout(this.mapLoadTimer);
        this.mapLoadTimer = null;
      }
      this.mapPolygons = [];
      this.townPolygonGroups = Object.create(null);
      this.hoveredTownName = '';
      if (AMap.DistrictLayer && AMap.DistrictLayer.Province) {
        const layer = new AMap.DistrictLayer.Province({
          adcode: ['440000'],
          depth: 2,
          styles: {
            fill: 'rgba(0,0,0,0)',
            'city-stroke': 'rgba(31, 122, 109, 0.18)',
            'province-stroke': 'rgba(199, 90, 49, 0.25)'
          }
        });
        layer.setMap(map);
        this.mapDistrictLayer = layer;
      }
      AMap.plugin(['AMap.Scale', 'AMap.ToolBar'], () => {
        map.addControl(new AMap.Scale());
        map.addControl(new AMap.ToolBar({ position: 'LT' }));
      });
      this.renderDistrictExplorer(AMapUI, AMap, map);
    },
    renderDistrictExplorer(AMapUI, AMap, map) {
      if (!AMapUI || !AMapUI.loadUI) {
        this.renderTownPolygonsBySearch(AMap, map);
        return;
      }
      AMapUI.loadUI(['geo/DistrictExplorer'], DistrictExplorer => {
        const districtExplorer = new DistrictExplorer({
          map,
          eventSupport: true
        });
        this.mapDistrictExplorer = districtExplorer;
        const targetAdcode = 442000;
        districtExplorer.loadAreaNode(targetAdcode, (error, areaNode) => {
          if (error) {
            this.mapError = '中山市镇区数据加载失败，请稍后重试。';
            this.mapLoading = false;
            if (this.mapLoadTimer) {
              clearTimeout(this.mapLoadTimer);
              this.mapLoadTimer = null;
            }
            return;
          }
          districtExplorer.clearFeaturePolygons();
          const subFeatures = areaNode.getSubFeatures();
          if (!subFeatures || !subFeatures.length) {
            this.renderTownPolygonsBySearch(AMap, map);
            return;
          }
          const distinctNames = new Set(
            subFeatures
              .map(feature => feature && feature.properties && feature.properties.name)
              .filter(Boolean)
              .map(name => this.normalizeTownName(name))
          );
          if (distinctNames.size <= 1) {
            this.renderTownPolygonsBySearch(AMap, map);
            return;
          }
          const townBaseStyle = this.getTownBaseStyle();
          const rootStyle = this.getRootStyle();
          const subPolygons = districtExplorer.renderSubFeatures(areaNode, {
            cursor: 'pointer',
            bubble: true,
            strokeColor: townBaseStyle.strokeColor,
            strokeWeight: townBaseStyle.strokeWeight,
            strokeOpacity: townBaseStyle.strokeOpacity,
            fillColor: townBaseStyle.fillColor,
            fillOpacity: townBaseStyle.fillOpacity,
            zIndex: townBaseStyle.zIndex
          });
          const parentPolygon = districtExplorer.renderParentFeature(areaNode, {
            strokeColor: rootStyle.strokeColor,
            strokeWeight: rootStyle.strokeWeight,
            strokeOpacity: rootStyle.strokeOpacity,
            fillColor: rootStyle.fillColor,
            fillOpacity: rootStyle.fillOpacity,
            zIndex: rootStyle.zIndex
          });
          this.mapPolygons = [];
          this.townPolygonGroups = Object.create(null);
          this.hoveredTownName = '';
          if (Array.isArray(subPolygons)) {
            this.mapPolygons.push(...subPolygons);
          }
          if (parentPolygon) {
            this.mapPolygons.push(parentPolygon);
            if (parentPolygon.setOptions) {
              parentPolygon.setOptions({ ...rootStyle, bubble: false });
            }
          }
          if (Array.isArray(subPolygons)) {
            subPolygons.forEach((polygon, index) => {
              const feature = subFeatures[index];
              const rawName =
                feature && feature.properties && feature.properties.name ? feature.properties.name : `镇区${index + 1}`;
              const townName = this.normalizeTownName(rawName);
              if (!this.townPolygonGroups[townName]) {
                this.townPolygonGroups[townName] = [];
              }
              this.townPolygonGroups[townName].push(polygon);
              polygon.setExtData({ townName });
              polygon.setOptions({ ...townBaseStyle, bubble: true });
              polygon.on('mouseover', () => {
                this.highlightTown(townName);
              });
              polygon.on('mouseout', () => {
                this.resetTownHighlight(townName);
              });
            });
          }
          this.clearMapLabels(map);
          this.mapLabels = subFeatures.map(feature => {
            return new AMap.Text({
              text: feature.properties.name,
              position: feature.properties.center,
              offset: new AMap.Pixel(0, 0),
              style: {
                color: '#1c2c27',
                fontSize: '12px',
                padding: '2px 6px',
                backgroundColor: 'rgba(255,255,255,0.85)',
                borderRadius: '10px',
                border: '1px solid rgba(31,122,109,0.2)'
              }
            });
          });
          map.add(this.mapLabels);
          districtExplorer.on('featureMouseover', (e, feature) => {
            if (feature && feature.properties && feature.properties.name) {
              this.highlightTown(feature.properties.name);
            }
            if (districtExplorer.setHoverFeature) {
              districtExplorer.setHoverFeature(feature);
            }
          });
          districtExplorer.on('featureMouseout', () => {
            if (districtExplorer.setHoverFeature) {
              districtExplorer.setHoverFeature(null);
            }
          });
          if (this.mapPolygons.length) {
            map.setFitView(this.mapPolygons, false, [60, 60, 60, 60]);
          }
          this.mapReady = true;
          this.mapLoading = false;
          if (this.mapLoadTimer) {
            clearTimeout(this.mapLoadTimer);
            this.mapLoadTimer = null;
          }
        });
      });
    },
    renderTownPolygonsBySearch(AMap, map) {
      this.mapPolygons = [];
      this.clearMapLabels(map);
      this.townPolygonGroups = Object.create(null);
      this.hoveredTownName = '';
      const districtSearch = new AMap.DistrictSearch({
        level: 'city',
        subdistrict: 1,
        extensions: 'all'
      });
      districtSearch.search('中山市', (status, result) => {
        if (status !== 'complete' || !result.districtList || !result.districtList.length) {
          this.mapError = '中山市边界加载失败，请检查 Key 或安全密钥配置。';
          this.mapLoading = false;
          return;
        }
        const city = result.districtList[0];
        this.drawDistrict(AMap, map, city, { isRoot: true, name: '中山市' });
        const subdistricts = city.districtList || [];
        if (!subdistricts.length) {
          this.loadGeojsonTowns(AMap, map);
          return;
        }
        let pending = subdistricts.length;
        let hasTown = false;
        const townNames = new Set();
        subdistricts.forEach(sub => {
          const keyword = sub.adcode || sub.name;
          const subSearch = new AMap.DistrictSearch({
            level: 'district',
            subdistrict: 0,
            extensions: 'all'
          });
          subSearch.search(keyword, (subStatus, subResult) => {
            pending -= 1;
            if (subStatus === 'complete' && subResult.districtList && subResult.districtList.length) {
              const detail = subResult.districtList[0];
              const normalizedName = this.normalizeTownName(sub.name || detail.name || '');
              const isValidTown =
                detail.boundaries &&
                detail.boundaries.length &&
                detail.name !== city.name &&
                detail.adcode !== city.adcode;
              if (isValidTown) {
                hasTown = true;
                townNames.add(normalizedName || detail.name || sub.name);
                this.drawDistrict(AMap, map, detail, { name: sub.name, addLabel: true });
              }
            }
            if (pending === 0) {
              if (!hasTown || townNames.size <= 1) {
                this.loadGeojsonTowns(AMap, map);
                return;
              } else {
                this.mapError = '';
                if (this.mapPolygons.length) {
                  map.setFitView(this.mapPolygons, false, [60, 60, 60, 60]);
                }
              }
              this.mapReady = hasTown;
              this.mapLoading = false;
              if (this.mapLoadTimer) {
                clearTimeout(this.mapLoadTimer);
                this.mapLoadTimer = null;
              }
            }
          });
        });
      });
    },
    async loadGeojsonTowns(AMap, map) {
      try {
        const response = await fetch(this.geojsonUrl, { cache: 'no-store' });
        if (!response.ok) {
          throw new Error('geojson not found');
        }
        const geojson = await response.json();
        this.renderGeojsonTowns(AMap, map, geojson);
      } catch (error) {
        this.mapError = '暂未获取镇区边界数据，可导入本地 GeoJSON 作为补充。';
        this.mapLoading = false;
        if (this.mapLoadTimer) {
          clearTimeout(this.mapLoadTimer);
          this.mapLoadTimer = null;
        }
      }
    },
    renderGeojsonTowns(AMap, map, geojson) {
      const features = geojson && geojson.features ? geojson.features : [];
      if (!features.length) {
        this.mapError = 'GeoJSON 数据为空，请检查文件格式。';
        this.mapLoading = false;
        return;
      }
      this.mapPolygons = [];
      this.clearMapLabels(map);
      this.townPolygonGroups = Object.create(null);
      this.hoveredTownName = '';
      const townBaseStyle = this.getTownBaseStyle();
      features.forEach(feature => {
        const name = feature.properties && feature.properties.name ? feature.properties.name : '镇区';
        const townName = this.normalizeTownName(name);
        const geometry = feature.geometry || {};
        const type = geometry.type;
        const coordinates = geometry.coordinates || [];
        const addPolygon = ring => {
          const polygon = new AMap.Polygon({
            path: ring,
            ...townBaseStyle,
            bubble: true
          });
          polygon.setMap(map);
          this.mapPolygons.push(polygon);
          if (!this.townPolygonGroups[townName]) {
            this.townPolygonGroups[townName] = [];
          }
          this.townPolygonGroups[townName].push(polygon);
          polygon.on('mouseover', () => {
            this.highlightTown(townName);
          });
          polygon.on('mouseout', () => {
            this.resetTownHighlight(townName);
          });
        };
        if (type === 'Polygon') {
          coordinates.forEach(ring => addPolygon(ring));
        } else if (type === 'MultiPolygon') {
          coordinates.forEach(polygonGroup => {
            polygonGroup.forEach(ring => addPolygon(ring));
          });
        }
        const center =
          (feature.properties && feature.properties.center) ||
          this.getGeojsonCenter(coordinates);
        if (center && Array.isArray(center) && center.length === 2) {
          const label = new AMap.Text({
            text: name,
            position: center,
            offset: new AMap.Pixel(0, 0),
            style: {
              color: '#1c2c27',
              fontSize: '12px',
              padding: '2px 6px',
              backgroundColor: 'rgba(255,255,255,0.85)',
              borderRadius: '10px',
              border: '1px solid rgba(31,122,109,0.2)'
            }
          });
          map.add(label);
          this.mapLabels.push(label);
        }
      });
      if (this.mapPolygons.length) {
        map.setFitView(this.mapPolygons, false, [60, 60, 60, 60]);
      }
      this.mapError = '';
      this.mapReady = true;
      this.mapLoading = false;
      if (this.mapLoadTimer) {
        clearTimeout(this.mapLoadTimer);
        this.mapLoadTimer = null;
      }
    },
    getGeojsonCenter(coordinates) {
      if (!coordinates || !coordinates.length) {
        return null;
      }
      const ring =
        Array.isArray(coordinates[0][0]) && Array.isArray(coordinates[0][0][0])
          ? coordinates[0][0]
          : coordinates[0];
      if (!Array.isArray(ring) || !ring.length) {
        return null;
      }
      let sumLng = 0;
      let sumLat = 0;
      ring.forEach(point => {
        if (Array.isArray(point) && point.length >= 2) {
          sumLng += Number(point[0]);
          sumLat += Number(point[1]);
        }
      });
      const count = ring.length || 1;
      return [sumLng / count, sumLat / count];
    },
    clearMapLabels(map) {
      if (this.mapLabels && this.mapLabels.length) {
        map.remove(this.mapLabels);
        this.mapLabels = [];
      }
    },
    resolveDistrictCenter(district) {
      if (!district) {
        return null;
      }
      if (Array.isArray(district.center)) {
        return district.center;
      }
      if (typeof district.center === 'string') {
        const parts = district.center.split(',').map(value => Number(value));
        if (parts.length === 2 && parts.every(num => !Number.isNaN(num))) {
          return parts;
        }
      }
      if (Array.isArray(district.centroid)) {
        return district.centroid;
      }
      return null;
    },
    getRootStyle() {
      return {
        strokeColor: '#c75a31',
        strokeWeight: 2.0,
        strokeOpacity: 0.6,
        fillColor: 'rgba(199, 90, 49, 0.03)',
        fillOpacity: 0.04,
        zIndex: 80
      };
    },
    getTownBaseStyle() {
      return {
        strokeColor: '#4aa997',
        strokeWeight: 1.8,
        strokeOpacity: 0.9,
        fillColor: 'rgba(74, 169, 151, 0.08)',
        fillOpacity: 0.16,
        zIndex: 120
      };
    },
    getTownHoverStyle() {
      return {
        strokeColor: '#c75a31',
        strokeWeight: 2.4,
        strokeOpacity: 0.95,
        fillColor: 'rgba(199, 90, 49, 0.14)',
        fillOpacity: 0.32,
        zIndex: 140
      };
    },
    resetTownHighlightByName(name) {
      if (!name || !this.townPolygonGroups[name]) {
        return;
      }
      const baseStyle = this.getTownBaseStyle();
      this.townPolygonGroups[name].forEach(polygon => {
        polygon.setOptions({ ...baseStyle, bubble: true });
      });
    },
    highlightTown(name) {
      const normalized = this.normalizeTownName(name);
      if (this.hoveredTownName && this.hoveredTownName !== normalized) {
        this.resetTownHighlightByName(this.hoveredTownName);
      }
      const hoverStyle = this.getTownHoverStyle();
      if (this.townPolygonGroups[normalized] && this.townPolygonGroups[normalized].length) {
        this.townPolygonGroups[normalized].forEach(polygon => {
          polygon.setOptions({ ...hoverStyle, bubble: true });
        });
      }
      this.hoveredTownName = normalized;
      this.setActiveTown(normalized);
    },
    resetTownHighlight(name) {
      const normalized = this.normalizeTownName(name);
      if (this.hoveredTownName !== normalized) {
        return;
      }
      this.resetTownHighlightByName(normalized);
      this.hoveredTownName = '';
    },
    drawDistrict(AMap, map, district, options) {
      if (!district || !district.boundaries || !district.boundaries.length) {
        return;
      }
      const isRoot = options && options.isRoot;
      const name = options && options.name ? options.name : district.name;
      const townName = this.normalizeTownName(name);
      const shouldLabel = options && options.addLabel;
      const rootStyle = this.getRootStyle();
      const townBaseStyle = this.getTownBaseStyle();
      district.boundaries.forEach(path => {
        const polygon = new AMap.Polygon({
          path,
          ...(isRoot ? rootStyle : townBaseStyle),
          bubble: !isRoot,
          zIndex: isRoot ? rootStyle.zIndex : townBaseStyle.zIndex
        });
        polygon.setMap(map);
        this.mapPolygons.push(polygon);
        if (!isRoot) {
          if (!this.townPolygonGroups[townName]) {
            this.townPolygonGroups[townName] = [];
          }
          this.townPolygonGroups[townName].push(polygon);
          polygon.on('mouseover', () => {
            this.highlightTown(townName);
          });
          polygon.on('mouseout', () => {
            this.resetTownHighlight(townName);
          });
        }
      });
      if (!isRoot && shouldLabel) {
        const center = this.resolveDistrictCenter(district);
        if (center) {
          const label = new AMap.Text({
            text: name,
            position: center,
            offset: new AMap.Pixel(0, 0),
            style: {
              color: '#1c2c27',
              fontSize: '12px',
              padding: '2px 6px',
              backgroundColor: 'rgba(255,255,255,0.85)',
              borderRadius: '10px',
              border: '1px solid rgba(31,122,109,0.2)'
            }
          });
          map.add(label);
          this.mapLabels.push(label);
        }
      }
    },
    normalizeTownName(name) {
      if (this.townInfo[name]) {
        return name;
      }
      const trimmed = name.replace(/(市|区|镇|街道)$/g, '');
      const match = Object.keys(this.townInfo).find(key => key.includes(trimmed));
      return match || name;
    },
    getTownImage(name) {
      if (!name) {
        return this.defaultTownImage;
      }
      const normalized = this.normalizeTownName(name);
      return `/towns/${encodeURIComponent(normalized)}.jpg`;
    },
    handleTownImageError() {
      if (this.activeTown.image !== this.defaultTownImage) {
        this.activeTown.image = this.defaultTownImage;
      }
    },
    setActiveTown(name) {
      const normalized = this.normalizeTownName(name);
      const info = this.townInfo[normalized];
      if (info) {
        this.activeTown = {
          name: normalized,
          description: info.description,
          sights: info.sights,
          foods: info.foods,
          image: this.getTownImage(normalized)
        };
      } else {
        this.activeTown = {
          name,
          description: '镇区风貌以岭南水乡与现代生活交融为特色。',
          sights: ['特色街区', '城市公园', '滨水绿道'],
          foods: ['岭南家常', '地道小吃'],
          image: this.getTownImage(name)
        };
      }
    }
  },
  created() {
    this.refreshUser();
  },
  mounted() {
    if (typeof this.tip === 'function') {
      this.tip();
    }
    this.$nextTick(() => {
      this.initReveal();
      this.initAmap();
      this.activeTown.image = this.getTownImage(this.activeTown.name);
    });
  },
  beforeDestroy() {
    this.cleanupReveal();
    if (this.mapLoadTimer) {
      clearTimeout(this.mapLoadTimer);
      this.mapLoadTimer = null;
    }
    if (this.mapInstance) {
      this.clearMapLabels(this.mapInstance);
      if (this.mapDistrictLayer) {
        this.mapDistrictLayer.setMap(null);
        this.mapDistrictLayer = null;
      }
      this.mapDistrictExplorer = null;
      this.mapInstance.destroy();
      this.mapInstance = null;
    }
  }
};
</script>

<template>
  <el-container class="index-layout">
    <el-header class="index-header" height="88px">
      <div class="nav-inner">
        <div class="brand" @click="goTo('/')">
          <el-image class="brand-logo" :src="logoInfo.url" fit="cover" alt="中山文旅标识" />
          <div class="brand-text">
            <h1>中山市城市文化旅游特色展示系统</h1>
          </div>
        </div>
        <div class="nav-links">
          <button
            v-for="item in navLinks"
            :key="item.route"
            class="nav-link"
            :class="{ 'is-active': isActive(item.route), 'nav-link--emphasis': item.emphasis }"
            @click="goTo(item.route)"
          >
            <span>{{ item.label }}</span>
            <span v-if="item.emphasis" class="nav-badge">主入口</span>
          </button>
        </div>
          <div class="nav-actions">
            <message-center
              class="nav-message-center"
              :uid="currentUser && currentUser.uid"
            />
            <template v-if="!isLoggedIn">
              <el-button type="default" plain class="nav-action" @click="goTo('/login')">登录</el-button>
              <el-button type="primary" class="nav-action" @click="goTo('/register')">注册</el-button>
            </template>
            <template v-else>
              <el-dropdown @command="handleUserCommand">
                <span class="user-trigger">
                  <el-avatar :size="36" class="user-avatar">{{ avatarText }}</el-avatar>
                  <span class="user-name">{{ displayName }}</span>
                  <i class="el-icon-arrow-down"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="profile">修改资料</el-dropdown-item>
                  <el-dropdown-item divided command="orders">查看订单</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <el-button type="default" plain class="nav-action nav-action--logout" @click="handleLogout">
                退出登录
              </el-button>
            </template>
          </div>
      </div>
    </el-header>

    <el-main class="index-main">
      <section class="hero-section">
        <div class="hero-inner content-container">
          <div class="hero-copy fade-up">
            <p class="hero-eyebrow">{{ heroContent.eyebrow }}</p>
            <h2 class="hero-title">{{ heroContent.title }}</h2>
            <p class="hero-description">
              {{ heroContent.description }}
            </p>
            <div class="hero-tags">
              <span v-for="tag in heroContent.tags" :key="tag" class="hero-tag">
                {{ tag }}
              </span>
            </div>
            <div class="hero-actions">
              <el-button type="primary" size="medium" round class="hero-shop-button" @click="goTo('/culturalProducts')">
                进入文创商城
              </el-button>
              <el-button size="medium" round plain @click="goTo('/travel-experiences')">
                开始导览
              </el-button>
            </div>
            <div class="hero-stats">
              <div v-for="stat in heroContent.stats" :key="stat.label" class="stat-item">
                <span class="stat-value">{{ stat.value }}</span>
                <span class="stat-label">{{ stat.label }}</span>
              </div>
            </div>
          </div>
          <div class="hero-visual fade-up delay-1">
            <div class="hero-media">
              <div class="hero-media-header">
                <span class="hero-media-title">城市影像</span>
                <span class="hero-media-subtitle">岐江夜景 · 骑楼街 · 五桂山</span>
              </div>
              <el-carousel :interval="4500" arrow="always" height="520px" class="hero-carousel hero-carousel--top">
                <el-carousel-item v-for="item in carouselItems" :key="item.jpg">
                  <div class="carousel-content">
                    <img :src="item.jpg" :alt="item.title" class="carousel-image" />
                    <div class="carousel-caption">
                      <span>{{ item.title }}</span>
                    </div>
                  </div>
                </el-carousel-item>
              </el-carousel>
            </div>
          </div>
        </div>
      </section>

      <section class="shop-spotlight content-container reveal">
        <div class="spotlight-card">
          <div class="spotlight-copy">
            <p class="spotlight-eyebrow">{{ shopSpotlight.eyebrow }}</p>
            <h3>{{ shopSpotlight.title }}</h3>
            <p>{{ shopSpotlight.description }}</p>
            <div class="spotlight-actions">
              <el-button type="primary" round @click="goTo('/culturalProducts')">立即逛商城</el-button>
              <el-button round plain @click="goTo('/travel-experiences')">先看景点</el-button>
            </div>
            <div class="spotlight-stats">
              <div v-for="stat in shopSpotlight.stats" :key="stat.label" class="spotlight-stat">
                <span>{{ stat.value }}</span>
                <small>{{ stat.label }}</small>
              </div>
            </div>
          </div>
          <div class="spotlight-grid">
            <div v-for="feature in shopSpotlight.highlights" :key="feature.title" class="spotlight-item">
              <h4>{{ feature.title }}</h4>
              <p>{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </section>

      <section class="quick-entry-section content-container reveal">
        <div class="section-header">
          <h3 class="section-title">功能入口</h3>
          <p class="section-subtitle">从导览到商城，快速抵达你需要的服务。</p>
        </div>
        <el-row :gutter="24">
          <el-col
            v-for="(entry, index) in quickEntries"
            :key="entry.title"
            :md="8"
            :sm="12"
            :xs="24"
          >
            <el-card
              class="quick-card stagger"
              :class="{ 'quick-card--featured': entry.featured }"
              shadow="hover"
              :style="{ transitionDelay: `${index * 0.08}s` }"
              @click.native="goTo(entry.route)"
            >
              <div class="quick-icon">{{ entry.icon }}</div>
              <div class="quick-body">
                <div class="quick-title">
                  <span>{{ entry.title }}</span>
                  <span class="quick-tag">{{ entry.tag }}</span>
                </div>
                <p class="quick-description">{{ entry.description }}</p>
                <span class="quick-action">{{ entry.action }} →</span>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </section>

      <section class="city-intro-section content-container reveal">
        <el-row :gutter="32">
          <el-col :md="14" :sm="24">
            <div class="intro-copy">
              <h3 class="section-title">{{ cityIntro.title }}</h3>
              <p class="intro-description">{{ cityIntro.description }}</p>
              <div class="intro-quote">{{ cityIntro.quote }}</div>
            </div>
          </el-col>
          <el-col :md="10" :sm="24">
            <div class="intro-card">
              <h4>城市关键词</h4>
              <div class="intro-facts">
                <div v-for="fact in cityIntro.facts" :key="fact.label" class="intro-fact">
                  <span class="intro-fact-label">{{ fact.label }}</span>
                  <span class="intro-fact-value">{{ fact.value }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </section>

      <section class="map-section section-surface">
        <div class="content-container">
          <div class="section-header reveal">
            <h3 class="section-title">{{ mapOverview.title }}</h3>
            <p class="section-subtitle">地图化呈现中山镇区轮廓与旅行动线。</p>
          </div>
          <el-row :gutter="32">
            <el-col :md="16" :sm="24">
              <div class="map-card reveal">
                <div class="map-visual">
                  <div id="zs-amap" class="amap-container"></div>
                  <div v-if="!mapBaseReady" class="map-fallback map-fallback--loading">
                    <p>地图加载中...</p>
                    <small>首次加载需要几秒，请稍候。</small>
                  </div>
                  <div class="map-hint">鼠标悬停镇区查看特色</div>
                </div>
                <p v-if="mapLoading && mapBaseReady" class="map-status">镇区数据加载中...</p>
                <p v-else-if="mapError" class="map-status map-status--error">{{ mapError }}</p>
                <div class="map-tags">
                  <span v-for="tag in mapTags" :key="tag" class="map-tag">{{ tag }}</span>
                </div>
              </div>
            </el-col>
            <el-col :md="8" :sm="24">
              <div class="map-detail-card reveal">
                <div class="map-detail-header">
                  <h4>{{ activeTown.name }}</h4>
                  <span class="map-detail-chip">镇区特色</span>
                </div>
                <div class="map-detail-media">
                  <img :src="activeTown.image" :alt="activeTown.name" @error="handleTownImageError" />
                </div>
                <p class="map-detail-description">{{ activeTown.description }}</p>
                <div class="map-detail-group">
                  <h5>代表景点</h5>
                  <div class="map-detail-tags">
                    <span v-for="spot in activeTown.sights" :key="spot" class="map-detail-tag-item">{{ spot }}</span>
                  </div>
                </div>
                <div class="map-detail-group">
                  <h5>特色美食</h5>
                  <div class="map-detail-tags">
                    <span
                      v-for="food in activeTown.foods"
                      :key="food"
                      class="map-detail-tag-item is-food"
                    >
                      {{ food }}
                    </span>
                  </div>
                </div>
                <p class="map-detail-note">{{ mapOverview.note }}</p>
                <el-button type="primary" round @click="goTo('/travel-experiences')">
                  查看推荐线路
                </el-button>
              </div>
            </el-col>
          </el-row>
        </div>
      </section>

      <section class="experience-section section-surface">
        <div class="content-container">
          <h3 class="section-title reveal">城市特色体验</h3>
          <p class="section-subtitle reveal">
            城市记忆、自然慢游与产业文化三条主线，构成中山的旅行气质。
          </p>
          <el-row :gutter="24">
            <el-col
              v-for="(feature, index) in experienceCards"
              :key="feature.title"
              :md="8"
              :sm="12"
              :xs="24"
            >
              <el-card
                class="experience-card stagger"
                shadow="hover"
                :style="{ transitionDelay: `${index * 0.08}s` }"
              >
                <div class="experience-icon">{{ feature.icon }}</div>
                <h4 class="experience-title">{{ feature.title }}</h4>
                <p class="experience-description">{{ feature.description }}</p>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </section>

      <section class="culture-section section-surface">
        <div class="content-container">
          <el-row :gutter="32">
            <el-col :md="14" :sm="24">
              <h3 class="section-title reveal">文化与节庆</h3>
              <p class="section-subtitle reveal">城市故事在节庆与非遗中延续。</p>
              <div class="culture-grid">
                <div
                  v-for="(card, index) in cultureCards"
                  :key="card.title"
                  class="culture-card stagger"
                  :style="{ transitionDelay: `${index * 0.1}s` }"
                >
                  <h4>{{ card.title }}</h4>
                  <p>{{ card.description }}</p>
                </div>
              </div>
            </el-col>
            <el-col :md="10" :sm="24">
              <div class="food-card reveal">
                <h4>中山味道</h4>
                <p>从市井小吃到岭南家常，用味道记住旅程。</p>
                <div class="food-tags">
                  <span v-for="food in foodList" :key="food" class="food-tag">{{ food }}</span>
                </div>
                <div class="night-route">
                  <h5>夜游建议</h5>
                  <ul>
                    <li v-for="stop in nightRoute" :key="stop">{{ stop }}</li>
                  </ul>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </section>

      <section class="route-section content-container reveal">
        <h3 class="section-title">推荐路线</h3>
        <p class="section-subtitle">按时间与兴趣组合，轻松完成行程规划。</p>
        <el-row :gutter="24">
          <el-col
            v-for="(route, index) in routeCards"
            :key="route.title"
            :md="12"
            :sm="24"
          >
            <el-card
              class="route-card stagger"
              shadow="hover"
              :style="{ transitionDelay: `${index * 0.08}s` }"
            >
              <div class="route-header">
                <span class="route-tag">{{ route.tag }}</span>
                <span class="route-duration">{{ route.duration }}</span>
              </div>
              <h4 class="route-title">{{ route.title }}</h4>
              <div class="route-stops">
                <span v-for="stop in route.stops" :key="stop" class="route-stop">{{ stop }}</span>
              </div>
              <el-button type="text" class="route-link" @click="goTo('/travel-experiences')">
                查看详情 →
              </el-button>
            </el-card>
          </el-col>
        </el-row>
      </section>

      <section class="shop-section">
        <div class="content-container">
          <div class="shop-card reveal">
            <div class="shop-copy">
              <p class="shop-eyebrow">{{ shopIntro.title }}</p>
              <h3>{{ shopIntro.subtitle }}</h3>
              <p>{{ shopIntro.description }}</p>
              <div class="shop-tags">
                <span v-for="tag in shopTags" :key="tag" class="shop-tag">{{ tag }}</span>
              </div>
              <el-button type="primary" round class="shop-button" @click="goTo('/culturalProducts')">
                进入文创商城
              </el-button>
            </div>
            <div class="shop-features">
              <div v-for="feature in shopIntro.features" :key="feature.title" class="shop-feature">
                <h4>{{ feature.title }}</h4>
                <p>{{ feature.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="cta-section">
        <div class="cta-card content-container reveal">
          <div class="cta-copy">
            <h3>{{ ctaCopy.title }}</h3>
            <p>{{ ctaCopy.description }}</p>
          </div>
          <div class="cta-actions">
            <el-button type="primary" size="medium" round @click="goTo('/travel-experiences')">
              开始导览
            </el-button>
            <el-button size="medium" round plain @click="goTo('/register')">
              注册账号
            </el-button>
          </div>
        </div>
      </section>

      <footer class="footer">
        <p>© 中山市城市文化旅游特色展示系统 · 学生项目</p>
      </footer>
    </el-main>
    <chat-assistant :is-logged-in="isLoggedIn" />
  </el-container>
</template>

<style scoped>
.index-layout {
  --font-display: 'STZhongsong', 'FangSong', 'Songti SC', 'STSong', serif;
  --font-body: 'HarmonyOS Sans SC', 'Source Han Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --ink: #1c2c27;
  --ink-soft: #40534c;
  --accent: #c75a31;
  --accent-strong: #a2472b;
  --teal: #1f7a6d;
  --gold: #d4a14a;
  --mist: #f8f3ea;
  --sky: #e7f3f7;
  --paper: #ffffff;
  --shadow-soft: 0 20px 45px rgba(25, 45, 40, 0.12);
  min-height: 100vh;
  background:
    radial-gradient(circle at 12% 10%, rgba(31, 122, 109, 0.12), transparent 45%),
    radial-gradient(circle at 85% 18%, rgba(199, 90, 49, 0.12), transparent 50%),
    repeating-linear-gradient(90deg, rgba(31, 122, 109, 0.05) 0, rgba(31, 122, 109, 0.05) 1px, transparent 1px, transparent 120px),
    repeating-linear-gradient(0deg, rgba(31, 122, 109, 0.04) 0, rgba(31, 122, 109, 0.04) 1px, transparent 1px, transparent 120px),
    linear-gradient(180deg, #f9f4ec 0%, #f5f9fb 50%, #ffffff 100%);
  color: var(--ink);
  font-family: var(--font-body);
}

.index-header {
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(8px);
  box-shadow: 0 8px 30px rgba(31, 56, 94, 0.08);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  height: 88px;
}

  .nav-inner {
    position: relative;
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
  min-height: 80px;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  justify-self: start;
}

.brand-logo {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  transition: transform 0.2s ease;
}

.brand-logo:hover {
  transform: scale(1.05);
}

.brand-text h1 {
  margin: 0;
  font-size: 18px;
  color: var(--ink);
  font-family: var(--font-display);
  font-weight: 700;
  line-height: 1.2;
}

.brand-text span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--accent);
  letter-spacing: 0.08em;
  line-height: 1.2;
}

  .nav-links {
    display: flex;
    align-items: center;
    gap: 10px;
    justify-content: center;
    flex-wrap: nowrap;
    height: 100%;
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
  }

.nav-link {
  border: none;
  background: rgba(31, 122, 109, 0.08);
  color: var(--ink);
  padding: 8px 18px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  line-height: 1;
  transition: all 0.2s ease;
}

.nav-link.is-active {
  background: rgba(199, 90, 49, 0.16);
  color: var(--accent);
}

.nav-link--emphasis {
  background: linear-gradient(135deg, rgba(199, 90, 49, 0.95), rgba(31, 122, 109, 0.9));
  color: #ffffff;
}

.nav-link--emphasis.is-active {
  color: #ffffff;
  background: linear-gradient(135deg, rgba(199, 90, 49, 1), rgba(31, 122, 109, 1));
}

.nav-badge {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 6px;
  border-radius: 999px;
  font-size: 10px;
}

  .nav-actions {
    display: flex;
    align-items: center;
    gap: 10px;
    height: 100%;
    justify-self: end;
  }

  .nav-message-center {
    margin-right: 2px;
  }

  .user-trigger {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    color: var(--ink);
    font-weight: 600;
  }

  .user-avatar {
    background: var(--teal);
    color: #fff;
    font-weight: 600;
  }

  .user-name {
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .nav-action--logout {
    margin-left: 4px;
  }

.nav-action {
  border-radius: 18px;
  padding: 10px 18px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.index-main {
  padding: 32px 0 80px;
  background: transparent;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  box-sizing: border-box;
}

.section-header {
  text-align: center;
  margin-bottom: 32px;
}

.section-title {
  font-size: 28px;
  color: var(--ink);
  margin-bottom: 8px;
  font-family: var(--font-display);
  letter-spacing: 0.04em;
}

.section-subtitle {
  color: var(--ink-soft);
  line-height: 1.6;
}

.section-surface {
  margin-top: 88px;
  padding: 72px 0;
  position: relative;
  overflow: hidden;
}

.section-surface::before {
  content: none;
}

.hero-section {
  padding: 72px 0 48px;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: none;
}

.hero-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 48px;
  position: relative;
  z-index: 1;
}

.hero-copy {
  max-width: 560px;
}

.hero-eyebrow {
  color: var(--teal);
  font-weight: 600;
  margin-bottom: 8px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-size: 12px;
}

.hero-title {
  font-size: 40px;
  line-height: 1.2;
  color: var(--ink);
  margin-bottom: 16px;
  font-family: var(--font-display);
}

.hero-description {
  font-size: 16px;
  color: var(--ink-soft);
  line-height: 1.8;
  margin-bottom: 20px;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;
}

.hero-tag {
  background: rgba(31, 122, 109, 0.12);
  color: var(--teal);
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
}

.hero-shop-button {
  padding: 12px 22px;
  box-shadow: 0 14px 26px rgba(199, 90, 49, 0.2);
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
  font-size: 22px;
  font-weight: 700;
  color: var(--accent);
}

.stat-label {
  color: var(--ink-soft);
  font-size: 13px;
}

.hero-visual {
  flex: 1;
  display: flex;
  align-items: stretch;
  justify-content: flex-end;
}

.hero-media {
  width: 100%;
  max-width: 700px;
  background: var(--paper);
  border-radius: 24px;
  padding: 18px 18px 14px;
  box-shadow: var(--shadow-soft);
}

.hero-media-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.hero-media-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink);
}

.hero-media-subtitle {
  font-size: 12px;
  color: var(--ink-soft);
}

.shop-spotlight {
  margin-top: 48px;
}

.spotlight-card {
  background: linear-gradient(120deg, rgba(31, 122, 109, 0.9), rgba(199, 90, 49, 0.9));
  border-radius: 28px;
  padding: 32px;
  color: #ffffff;
  display: flex;
  gap: 32px;
  align-items: center;
  box-shadow: 0 24px 60px rgba(31, 56, 94, 0.2);
  flex-wrap: wrap;
}

.spotlight-copy {
  flex: 1 1 320px;
}

.spotlight-eyebrow {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  opacity: 0.85;
  margin: 0 0 8px;
}

.spotlight-copy h3 {
  font-size: 28px;
  margin: 0 0 12px;
  font-family: var(--font-display);
}

.spotlight-copy p {
  margin: 0 0 16px;
  line-height: 1.7;
}

.spotlight-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.spotlight-stats {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.spotlight-stat {
  background: rgba(255, 255, 255, 0.18);
  padding: 8px 14px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-weight: 600;
}

.spotlight-stat small {
  font-size: 12px;
  font-weight: 500;
  opacity: 0.85;
}

.spotlight-grid {
  display: grid;
  gap: 14px;
  flex: 1 1 260px;
}

.spotlight-item {
  background: rgba(255, 255, 255, 0.16);
  border-radius: 16px;
  padding: 14px 16px;
}

.spotlight-item h4 {
  margin: 0 0 6px;
  font-size: 16px;
}

.spotlight-item p {
  margin: 0;
  font-size: 13px;
  opacity: 0.9;
}

.quick-entry-section {
  margin-top: 64px;
}

.quick-card {
  border-radius: 20px;
  padding: 18px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.quick-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 40px rgba(31, 56, 94, 0.12);
}

.quick-card--featured {
  background: linear-gradient(135deg, rgba(199, 90, 49, 0.12), rgba(31, 122, 109, 0.08));
  border: 1px solid rgba(199, 90, 49, 0.2);
}

.quick-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: rgba(31, 122, 109, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}

.quick-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--ink);
  margin-bottom: 6px;
}

.quick-tag {
  font-size: 12px;
  background: var(--accent);
  color: #ffffff;
  padding: 2px 8px;
  border-radius: 999px;
}

.quick-description {
  margin: 0 0 10px;
  color: var(--ink-soft);
  line-height: 1.6;
}

.quick-action {
  color: var(--accent);
  font-weight: 600;
  font-size: 13px;
}

.city-intro-section {
  margin-top: 72px;
}

.intro-copy {
  padding-right: 12px;
}

.intro-description {
  line-height: 1.8;
  color: var(--ink-soft);
}

.intro-quote {
  margin-top: 18px;
  padding: 14px 18px;
  border-left: 3px solid var(--accent);
  background: rgba(199, 90, 49, 0.08);
  color: var(--ink);
  border-radius: 12px;
  font-weight: 600;
}

.intro-card {
  background: var(--paper);
  border-radius: 20px;
  padding: 22px;
  box-shadow: var(--shadow-soft);
}

.intro-card h4 {
  margin: 0 0 16px;
  font-size: 18px;
  color: var(--ink);
}

.intro-facts {
  display: grid;
  gap: 12px;
}

.intro-fact {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(31, 122, 109, 0.08);
}

.intro-fact-label {
  font-size: 12px;
  color: var(--ink-soft);
}

.intro-fact-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
}

.map-section {
  position: relative;
}

.map-card {
  background: var(--paper);
  border-radius: 24px;
  padding: 24px;
  box-shadow: var(--shadow-soft);
  position: relative;
  z-index: 1;
}

.map-visual {
  position: relative;
  height: 520px;
  border-radius: 18px;
  overflow: hidden;
  background: rgba(31, 122, 109, 0.05);
}

.amap-container {
  width: 100%;
  height: 100%;
}

.map-fallback {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 24px;
  gap: 8px;
  color: var(--ink-soft);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(6px);
  z-index: 2;
}

.map-fallback--error {
  background: rgba(255, 255, 255, 0.96);
}

.map-hint {
  position: absolute;
  left: 16px;
  bottom: 16px;
  background: rgba(28, 44, 39, 0.72);
  color: #ffffff;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
}

.map-tags {
  margin-top: 18px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.map-status {
  margin: 12px 0 0;
  font-size: 13px;
  color: var(--ink-soft);
}

.map-status--error {
  color: var(--accent);
}

.map-tag {
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(31, 122, 109, 0.1);
  color: var(--teal);
  font-size: 12px;
  font-weight: 600;
}

.map-detail-card {
  background: var(--paper);
  border-radius: 20px;
  padding: 24px;
  box-shadow: var(--shadow-soft);
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.map-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.map-detail-header h4 {
  margin: 0;
  font-size: 18px;
  color: var(--ink);
}

.map-detail-chip {
  background: rgba(199, 90, 49, 0.12);
  color: var(--accent);
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  font-weight: 600;
}

.map-detail-description {
  margin: 0;
  color: var(--ink-soft);
  line-height: 1.7;
}

.map-detail-media {
  width: 100%;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(31, 122, 109, 0.12);
  background: rgba(31, 122, 109, 0.06);
  box-shadow: 0 12px 30px rgba(31, 56, 94, 0.1);
}

.map-detail-media img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

.map-detail-group h5 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--ink);
}

.map-detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.map-detail-tag-item {
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(31, 122, 109, 0.1);
  color: var(--teal);
  font-size: 12px;
  font-weight: 600;
}

.map-detail-tag-item.is-food {
  background: rgba(199, 90, 49, 0.12);
  color: var(--accent);
}

.map-detail-note {
  font-size: 12px;
  color: var(--ink-soft);
  margin: 0;
}

.experience-section {
  position: relative;
}

.experience-card {
  border-radius: 20px;
  min-height: 220px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.experience-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 18px 36px rgba(31, 56, 94, 0.12);
}

.experience-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #1f7a6d, #d4a14a);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  margin-bottom: 18px;
  color: #ffffff;
}

.experience-title {
  font-size: 20px;
  margin-bottom: 10px;
  color: var(--ink);
}

.experience-description {
  margin: 0;
  color: var(--ink-soft);
  line-height: 1.6;
}

.hero-carousel {
  margin-top: 0;
}

.hero-carousel--top {
  border-radius: 18px;
  overflow: hidden;
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
  background: rgba(28, 44, 39, 0.72);
  color: #ffffff;
  padding: 10px 20px;
  border-radius: 999px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.culture-section {
  position: relative;
}

.culture-grid {
  display: grid;
  gap: 18px;
}

.culture-card {
  background: var(--paper);
  border-radius: 18px;
  padding: 18px 20px;
  box-shadow: var(--shadow-soft);
}

.culture-card h4 {
  margin: 0 0 8px;
  font-size: 18px;
  color: var(--ink);
}

.culture-card p {
  margin: 0;
  color: var(--ink-soft);
  line-height: 1.6;
}

.food-card {
  background: rgba(31, 122, 109, 0.1);
  border-radius: 20px;
  padding: 22px;
  height: 100%;
}

.food-card h4 {
  margin: 0 0 8px;
  font-size: 18px;
  color: var(--ink);
}

.food-card p {
  margin: 0 0 16px;
  color: var(--ink-soft);
}

.food-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.food-tag {
  background: #ffffff;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  color: var(--ink);
  box-shadow: 0 6px 16px rgba(31, 56, 94, 0.08);
}

.night-route h5 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--ink);
}

.night-route ul {
  margin: 0;
  padding-left: 18px;
  color: var(--ink-soft);
}

.route-section {
  margin-top: 96px;
}

.route-card {
  border-radius: 20px;
  padding: 20px 22px;
}

.route-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.route-title {
  margin: 0 0 10px;
  font-size: 20px;
  color: var(--ink);
}

.route-duration {
  font-size: 12px;
  color: var(--ink-soft);
}

.route-tag {
  background: rgba(199, 90, 49, 0.12);
  color: var(--accent);
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.route-stops {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 6px;
}

.route-stop {
  background: rgba(31, 122, 109, 0.1);
  color: var(--teal);
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.route-link {
  color: var(--accent);
  font-weight: 600;
}

.shop-section {
  margin-top: 88px;
  padding: 64px 0;
}

.shop-card {
  background: var(--paper);
  border-radius: 26px;
  padding: 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  box-shadow: var(--shadow-soft);
  flex-wrap: wrap;
}

.shop-copy h3 {
  margin: 8px 0 12px;
  font-size: 28px;
  font-family: var(--font-display);
}

.shop-copy p {
  margin: 0 0 16px;
  color: var(--ink-soft);
  line-height: 1.7;
}

.shop-eyebrow {
  margin: 0;
  color: var(--teal);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.shop-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 18px;
}

.shop-tag {
  background: rgba(31, 122, 109, 0.1);
  color: var(--teal);
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
}

.shop-button {
  margin-top: 4px;
}

.shop-features {
  display: grid;
  gap: 16px;
}

.shop-feature {
  background: rgba(231, 243, 247, 0.6);
  border-radius: 18px;
  padding: 16px 18px;
}

.shop-feature h4 {
  margin: 0 0 6px;
  font-size: 16px;
  color: var(--ink);
}

.shop-feature p {
  margin: 0;
  color: var(--ink-soft);
  line-height: 1.5;
}

.cta-section {
  margin: 88px 0 72px;
}

.cta-card {
  background: linear-gradient(120deg, #1f7a6d 0%, #c75a31 100%);
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
  font-size: 26px;
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

.fade-up {
  animation: fadeUp 0.9s ease both;
}

.delay-1 {
  animation-delay: 0.15s;
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

.el-button--primary {
  background-color: var(--accent);
  border-color: var(--accent);
}

.el-button--primary:hover,
.el-button--primary:focus {
  background-color: var(--accent-strong);
  border-color: var(--accent-strong);
}

.el-button.is-plain {
  color: var(--accent);
  border-color: rgba(199, 90, 49, 0.5);
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

  @media (max-width: 1100px) {
    .hero-inner {
      flex-direction: column;
      text-align: center;
    }

  .hero-actions {
    justify-content: center;
  }

  .hero-stats {
    justify-content: center;
  }

  .hero-visual {
    align-items: center;
  }

  .intro-copy {
    padding-right: 0;
  }

    .nav-inner {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      padding: 16px 24px;
      gap: 12px;
    }
  
    .nav-links {
      justify-content: flex-start;
      flex-wrap: wrap;
      position: static;
      transform: none;
    }
  }

@media (max-width: 900px) {
  .hero-carousel--top {
    height: 420px !important;
  }

  .map-visual {
    height: 420px;
  }

  .map-section {
    padding: 56px 0;
  }

  .shop-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .cta-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .spotlight-card {
    flex-direction: column;
    align-items: flex-start;
  }
}

  @media (max-width: 680px) {
  .hero-title {
    font-size: 32px;
  }

  .hero-carousel--top {
    height: 320px !important;
  }

  .map-visual {
    height: 340px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .hero-media {
    max-width: none;
  }

    .nav-inner {
      align-items: flex-start;
    }
  
    .nav-links {
      width: 100%;
      justify-content: flex-start;
      position: static;
      transform: none;
    }

  .nav-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .cta-actions {
    width: 100%;
    flex-direction: column;
  }

  .cta-actions .el-button {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .fade-up,
  .reveal,
  .stagger {
    animation: none !important;
    transition: none !important;
    transform: none !important;
    opacity: 1 !important;
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
