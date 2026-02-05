<script>

import {apiRear} from "@/axios";
import {jwtDecode} from "jwt-decode";

const productImageContext = require.context('@/assets/culturalProducts', false, /\.png$/);
const productImages = productImageContext.keys().reduce((acc, key) => {
  const fileName = key.replace('./', '').replace(/\.png$/, '');
  acc[fileName] = productImageContext(key);
  return acc;
}, {});
const fallbackImage = productImages['1'];

export default {
  data() {
    return {
      tableData: [],
      userInfo: [],
      total: 0,
      test: 100,
      dangerColor: '#e83a3a',
      orderId: null,
      loading: true,
      purchaseDialogVisible: false,
      orderState: false, // true为已有未处理的订单
      productIndex: 0,
      purchaseQuantity: 0,
      currentProductTotal: 0,
      priceRange: [],
      maxPrice: 0,
      cartTotal: 0,
      shoppingCart: [],
      shoppingDrawer: false,
      direction: 'rtl',//right to left
      maxAvailableStock: 0,
      inputLimit: 100000000,
      page: 1,
      pageSize: 21,
      selectedProduct: {
        productCode: '',
        productName: '',
        purchaseNum: 0,
        price: 0
      },
      search: {
        page: 1,
        pageSize: 21,
        productCode: '',
        productName: '',
        displayMode: '',
        highlight: '',
        category: '',
        minPrice: null,
        maxPrice: null,
        minStock: null,
        maxStock: null
      },
      classifyButtons: [
        {text: '🏞️全系文创', isClicked: false},
        {text: '🏯城市地标', isClicked: false},
        {text: '🧧民俗非遗', isClicked: false},
        {text: '🛶山水生态', isClicked: false},
        {text: '🎨艺术设计', isClicked: false},
        {text: '🍵人气伴手礼', isClicked: false},
        {text: '🎎民族风情', isClicked: false},
        {text: '⛰️户外主题', isClicked: false},
        {text: '🚂铁路文创', isClicked: false},
        {text: '🛳️邮轮精选', isClicked: false},
        {text: '🏖️海岛风光', isClicked: false},
        {text: '🌌夜游精品', isClicked: false},
        {text: '🏟️赛事主题', isClicked: false},
        {text: '🏰国潮经典', isClicked: false},
        {text: '🌸节庆限定', isClicked: false}
      ],
      typeButtons: [
        {text: '✨全部', isClicked: false},
        {text: '🛒线上发售', isClicked: false},
        {text: '🏬线下限定', isClicked: false},
      ],
      quickFilters: [
        {key: 'hot', label: '🔥热销商品'},
        {key: 'trend', label: '✨时下流行'},
        {key: 'latest', label: '🆕最新商品'},
        {key: 'history', label: '🧾历史购买', requiresLogin: true},
        {key: 'recommend', label: '🎯为你推荐', requiresLogin: true}
      ],
      activeQuickFilter: 'hot',
      trendSelection: [],
      historyProductCodes: [],
      historyCategories: [],
      travelEmojis: [
        {emoji: '🏞️', label: '山水自然'},
        {emoji: '🏛️', label: '人文历史'},
        {emoji: '🌊', label: '滨海慢游'},
        {emoji: '🌃', label: '夜游城市'}
      ],
      activeTravelEmoji: '🏞️',
      selectedProductIsOffline: false
    };
  },
  created() {
    this.loadItems();
    if (this.isLoggedIn) {
      this.getShoppingCart();
      this.loadOrderHistory();
      // 添加 beforeunload 事件监听器
      window.addEventListener('beforeunload', () => this.handlePay(false), {once: true});
    }
  },
  computed: {
    isLoggedIn() {
      return Boolean(sessionStorage.getItem('token'));
    },
    isPanelRoute() {
      return this.$route.path.startsWith('/userPanel');
    },
    authority() {
      const token = sessionStorage.getItem('token');
      if (!token) {
        return null;
      }
      try {
        return jwtDecode(token).authority;
      } catch (error) {
        return null;
      }
    },
    hasPanelAside() {
      return this.isPanelRoute && this.authority === 1;
    },
    filteredTableData() {
      const data = [...this.tableData];
      const normalizeNumber = value => {
        const parsed = Number(value);
        return Number.isFinite(parsed) ? parsed : 0;
      };
      switch (this.activeQuickFilter) {
        case 'hot':
          return data.sort((a, b) => normalizeNumber(b.sales) - normalizeNumber(a.sales));
        case 'trend':
          return data.filter(item => this.trendSelection.includes(item.productCode));
        case 'latest':
          return data.sort((a, b) => {
            const aTime = a.createTime ? new Date(a.createTime).getTime() : 0;
            const bTime = b.createTime ? new Date(b.createTime).getTime() : 0;
            if (aTime !== bTime) {
              return bTime - aTime;
            }
            const aCode = normalizeNumber(String(a.productCode).replace(/\D/g, ''));
            const bCode = normalizeNumber(String(b.productCode).replace(/\D/g, ''));
            return bCode - aCode;
          });
        case 'history':
          return data.filter(item => this.historyProductCodes.includes(item.productCode));
        case 'recommend':
          return data.filter(
            item =>
              this.historyCategories.includes(item.category) &&
              !this.historyProductCodes.includes(item.productCode)
          );
        default:
          return data;
      }
    },
    tableDataWithImages() {
      // Use productCode image when available, otherwise fallback to 1.png
      return this.filteredTableData.map((item, index) => ({
        ...item,
        imageUrl: this.resolveProductImage(item.productCode, index)
      }));
    },
    activeProduct() {
      return this.tableData[Number(this.productIndex)] || null;
    },
    cartItemsDetailed() {
      return this.shoppingCart.map((item, index) => {
        const matched = this.tableDataWithImages.find(product => product.productCode === item.productCode);
        return {
          ...item,
          imageUrl: matched ? matched.imageUrl : this.resolveProductImage(item.productCode, index),
          stock: matched && typeof matched.stock !== 'undefined' ? matched.stock : null
        };
      });
    }
  },
  methods: {
    resolveProductImage(productCode) {
      if (productCode && productImages[productCode]) {
        return productImages[productCode];
      }
      return fallbackImage || '';
    },
    isOfflineProduct(item) {
      return item && item.displayMode && String(item.displayMode).includes('线下');
    },
    getUidFromToken() {
      const token = sessionStorage.getItem('token');
      return token ? jwtDecode(token).uid : null;
    },
    requireLogin(actionText) {
      this.$confirm(`游客模式无法${actionText}，是否前往登录？`, '请先登录', {
        confirmButtonText: '去登录',
        cancelButtonText: '先逛逛',
        type: 'warning'
      }).then(() => {
        this.$router.push('/login');
      }).catch(() => {});
    },
    applyQuickFilter(filter) {
      if (filter.requiresLogin && !this.isLoggedIn) {
        this.requireLogin('查看该筛选');
        return;
      }
      this.activeQuickFilter = filter.key;
      if (filter.key === 'trend') {
        const source = this.tableData.slice();
        const shuffled = source.sort(() => Math.random() - 0.5);
        this.trendSelection = shuffled.slice(0, Math.min(9, shuffled.length)).map(item => item.productCode);
      } else {
        this.trendSelection = [];
      }
    },
    async loadOrderHistory() {
      if (!this.isLoggedIn) {
        return;
      }
      const uid = this.getUidFromToken();
      if (!uid) {
        return;
      }
      try {
        const orderRes = await apiRear.get(`/order/getOrderByUID=${uid}`);
        const orders = Array.isArray(orderRes.data.data) ? orderRes.data.data : [];
        const recentOrders = orders.slice(-5);
        const historyCodes = new Set();
        for (const order of recentOrders) {
          if (!order || !order.orderId) {
            continue;
          }
          const detailRes = await apiRear.get(`/order/getOrderDetailByOrderId=${order.orderId}`);
          const details = Array.isArray(detailRes.data.data) ? detailRes.data.data : [];
          details.forEach(detail => {
            if (detail.productCode) {
              historyCodes.add(detail.productCode);
            }
          });
        }
        this.historyProductCodes = Array.from(historyCodes);
        await this.loadHistoryCategories();
      } catch (err) {
        this.$message.error('无法获取历史购买记录');
      }
    },
    async loadHistoryCategories() {
      if (!this.historyProductCodes.length) {
        this.historyCategories = [];
        return;
      }
      const categories = new Set();
      const codes = this.historyProductCodes.slice(0, 10);
      for (const code of codes) {
        const inPage = this.tableData.find(item => item.productCode === code);
        if (inPage && inPage.category) {
          categories.add(inPage.category);
          continue;
        }
        try {
          const res = await apiRear.get(`/cultural-products/${code}`);
          if (res.data && res.data.data && res.data.data.category) {
            categories.add(res.data.data.category);
          }
        } catch (error) {
          // ignore single product fetch failures
        }
      }
      this.historyCategories = Array.from(categories);
    },
    findMaxPrice() {
      // 初始值设为负无穷大，确保任何价格都能被比较
      return this.tableData.reduce((max, item) => {
        return item.price > max ? item.price : max;
      }, -Infinity);
    },
    handlePositiveIntegerInput(field, max) {
      const value = this.search[field];
      const filteredValue = value.toString().replace(/\D/g, '');
      const parsedValue = parseInt(filteredValue, 10);
      const positiveInteger = !isNaN(parsedValue) && parsedValue >= 0 ? Math.min(parsedValue, max) : '';
      this.$set(this.search, field, positiveInteger);
    },
    async handlePay(isButtonTriggered = false) {
      if (!this.isLoggedIn) {
        this.requireLogin('??');
        return;
      }
      if (this.shoppingCart.length <= 0) {
        this.$message.error('??????');
        return;
      }
      if (!this.validateCartBeforeCheckout()) {
        return;
      }
      this.loading = true;
      try {
        let response = null;
        if (this.orderId === null) {
          response = await apiRear.post('/order/add', {
            uid: this.userInfo.uid,
            totalProductPrice: this.cartTotal,
            shoppingCart: this.shoppingCart,
          });
          if (response.data.code !== 1) {
            throw new Error(response.data.message || '??????');
          }
          this.orderId = response.data.data;
        } else {
          const orderIdValue = Array.isArray(this.orderId) ? this.orderId.at(0) : this.orderId;
          response = await apiRear.post('/order/update', {
            uid: this.userInfo.uid,
            orderId: orderIdValue,
            totalProductPrice: this.cartTotal,
            shoppingCart: this.shoppingCart,
          });
          if (response.data.code === 0) {
            const message = response.data.message || '';
            if (message.includes('??') || message.includes('??')) {
              this.orderId = null;
              response = await apiRear.post('/order/add', {
                uid: this.userInfo.uid,
                totalProductPrice: this.cartTotal,
                shoppingCart: this.shoppingCart,
              });
              if (response.data.code !== 1) {
                throw new Error(response.data.message || '??????');
              }
              this.orderId = response.data.data;
            } else {
              throw new Error(message || '??????');
            }
          }
        }
      } catch (error) {
        this.$message.error(error && error.message ? error.message : '???????');
        this.loading = false;
        return;
      }
      if (!isButtonTriggered) {
        this.loading = false;
        return;
      }
      if (this.userInfo.balance < this.cartTotal) {
        this.$confirm('??????????????????', '????', {
          confirmButtonText: '????',
          cancelButtonText: '????',
          type: 'warning'
        }).then(() => {
          localStorage.removeItem('shoppingCart');
          this.loading = false;
          this.$router.push('/billPayment');
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '?????'
          });
          this.loading = false;
        })
      } else {
        localStorage.removeItem('shoppingCart');
        this.loading = false;
        await this.$router.push('/billPayment');
      }
    },

    handleButtonClick(buttons, index, searchField) {
      buttons.forEach((button, i) => {
        button.isClicked = i === index;
      });
      let value = buttons[index].text.replace(/[^\u4e00-\u9fa5a-zA-Z0-9]/g, '');
      if (index === 0) {
        value = '';
      }
      if (searchField === 'displayMode') {
        if (index === 1) {
          value = '线上';
        } else if (index === 2) {
          value = '线下';
        }
      }
      this.search[searchField] = value;
      this.handle_loadItems();
    },
    handle_loadItems() {
      this.loading = true;
      setTimeout(() => {
        // 模拟加载完成后的一些操作
        this.loadItems();
      }, 500);
    },
    async loadItems() {
      try {
        const res = await apiRear.get('/cultural-products', {params: this.search});
        this.tableData = res.data.data.rows;
        this.total = res.data.data.total;
        this.maxPrice = this.findMaxPrice();
        this.priceRange = [0, this.maxPrice];
        if (this.activeQuickFilter === 'trend') {
          const shuffled = this.tableData.slice().sort(() => Math.random() - 0.5);
          this.trendSelection = shuffled.slice(0, Math.min(9, shuffled.length)).map(item => item.productCode);
        } else {
          this.trendSelection = [];
        }
        if (this.isLoggedIn && !this.historyCategories.length) {
          this.loadHistoryCategories();
        }
      } catch (err) {
        this.$message.error('未知错误,请重新登录后再试');
      } finally {
        this.loading = false;
      }
    },
    async getUserInfo() {
      if (!this.isLoggedIn) {
        return;
      }
      try {
        const token = sessionStorage.getItem('token');
        const uid = token ? jwtDecode(token).uid : null;
        if (!uid) {
          return;
        }
        const info = await apiRear.get(`/userInfo/${uid}`);
        this.userInfo = info.data.data;
      } catch (err) {
        this.$message.error('无法获取用户数据！');
      }
    },
    async getShoppingCart() {
      if (!this.isLoggedIn) {
        return;
      }
      try {
        await this.getUserInfo(); //确保用户信息已加载
        if (this.userInfo.uid !== undefined) {
          apiRear.get(`/order/getOrderInfoBy=${this.userInfo.uid}`).then(info => {
            if (Array.isArray(info.data.data.orderId) && info.data.data.orderId.length > 0) {
              this.orderId = info.data.data.orderId;
              this.cartTotal = info.data.data.totalPrice;
              apiRear.get(`/order/getOrderByOrderId=${this.orderId}`).then(res => {
                this.shoppingCart = res.data.data;
              })
            }
          })
        }
      } catch (err) {
        this.$message.error('无法获取购物车数据！');
      }
    },
    handlePageChange(newPage) {
      this.loading = true;
      this.search.page = newPage;
      this.loadItems();
    },
    selectTravelEmoji(emoji) {
      this.activeTravelEmoji = emoji;
    },
    handleAddToCart(item) {
      if (!this.isLoggedIn) {
        this.requireLogin('加入购物车');
        return;
      }
      const index = this.tableData.findIndex(product => product.productCode === item.productCode);
      if (index < 0) {
        return;
      }
      this.purchaseDialogVisible = true;
      this.purchaseQuantity = 0;
      this.currentProductTotal = '0.00';
      this.maxAvailableStock = this.tableData[index].stock;
      this.selectedProductIsOffline = this.isOfflineProduct(this.tableData[index]);
      this.productIndex = index;
    },
    purchaseHandle() {
      if (!this.isLoggedIn) {
        this.requireLogin('下单');
        return;
      }
      if (this.selectedProductIsOffline) {
        this.$message.warning('门店限定商品仅支持线下购买');
        return;
      }
      if (!this.isValidQuantity(this.purchaseQuantity, this.maxAvailableStock)) {
        return;
      }
      const purchasedProducts = this.shoppingCart.find(item => item.productCode === this.tableData[this.productIndex].productCode);
      if (purchasedProducts != null) {
        if (this.tableData[this.productIndex].stock - purchasedProducts.purchaseNum - this.purchaseQuantity < 0) {
          this.$message.error('购买数量不能大于剩余数量');
          return;
        } else {
          this.shoppingCart[this.shoppingCart.indexOf(purchasedProducts)].purchaseNum += this.purchaseQuantity;
          this.updateCartTotal();
          localStorage.setItem('shoppingCart', JSON.stringify(this.shoppingCart));
          this.purchaseDialogVisible = false;
          return;
        }
      }
      this.selectedProduct.productCode = this.tableData[this.productIndex].productCode;
      this.selectedProduct.purchaseNum = this.purchaseQuantity;
      this.selectedProduct.productName = this.tableData[this.productIndex].productName;
      this.selectedProduct.price = this.tableData[this.productIndex].price;
      const newPurchase = JSON.parse(JSON.stringify(this.selectedProduct));
      this.shoppingCart.push(newPurchase);
      this.cartTotal = 0;
      this.updateCartTotal();
      localStorage.setItem('shoppingCart', JSON.stringify(this.shoppingCart));
      this.purchaseDialogVisible = false;
    },
    updateCurrentProductTotal() {
      if (this.selectedProductIsOffline) {
        this.currentProductTotal = '0.00';
        return;
      }
      this.currentProductTotal = (this.purchaseQuantity * this.tableData[this.productIndex].price).toFixed(2);
    },
    updateCartTotal() {
      this.cartTotal = this.shoppingCart.reduce((total, item) => {
        return parseFloat((total + item.purchaseNum * item.price).toFixed(2));
      }, 0);
    },
    updateCartQuantity(item, value) {
      if (!this.isLoggedIn) {
        this.requireLogin('管理购物车');
        return;
      }
      const quantity = Number(value);
      const stock = this.getCartStock(item.productCode);
      if (!this.isValidQuantity(quantity, stock)) {
        return;
      }
      const target = this.shoppingCart.find(cartItem => cartItem.productCode === item.productCode);
      if (!target) {
        return;
      }
      target.purchaseNum = quantity;
      this.updateCartTotal();
      localStorage.setItem('shoppingCart', JSON.stringify(this.shoppingCart));
    },
    getCartStock(productCode) {
      const product = this.tableDataWithImages.find(item => item.productCode === productCode);
      if (!product || typeof product.stock === 'undefined') {
        return null;
      }
      return product.stock;
    },
    removeFromCart(productCode, productName) {
      if (!this.isLoggedIn) {
        this.requireLogin('管理购物车');
        return;
      }
      this.$confirm('此操作会删除所有的『' + productName + '』，确定吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.shoppingCart.splice(this.shoppingCart.indexOf(this.shoppingCart.find(item => item.productCode === productCode)), 1);
        this.updateCartTotal();
        localStorage.setItem('shoppingCart', JSON.stringify(this.shoppingCart));
        this.$message.success('已删除购物车所有的『' + productName + '』！');
      }).catch(() => {
        this.$message.info('已取消');
      });
    },
    editCartItem(productCode, productName, purchaseNum) {
      if (!this.isLoggedIn) {
        this.requireLogin('管理购物车');
        return;
      }
      this.$prompt('请输入『' + productName + '』数量', '修改', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: purchaseNum,
        inputType: 'number',
        inputPattern: /^\d+$/,
        inputErrorMessage: '请输入正整数',
        closeOnClickModal: false
      }).then(({value}) => {
        const purchasedDrug = this.shoppingCart.find(item => item.productCode === productCode);
        const stock = this.getCartStock(productCode);
        const quantity = Number(value);
        if (!this.isValidQuantity(quantity, stock)) {
          return;
        }
        purchasedDrug.purchaseNum = quantity;
        this.updateCartTotal();
        localStorage.setItem('shoppingCart', JSON.stringify(this.shoppingCart));
        this.$message({
          type: 'success',
          message: '『' + productName + '』' + '数量已修改为：' + quantity
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '取消修改'
        });
      });
    },
    handleOpenCart() {
      if (!this.isLoggedIn) {
        this.requireLogin('查看购物车');
        return;
      }
      this.shoppingDrawer = true;
    },
    validateCartBeforeCheckout() {
      for (const item of this.shoppingCart) {
        const stock = this.getCartStock(item.productCode);
        const product = this.tableData.find(productItem => productItem.productCode === item.productCode);
        if (product && this.isOfflineProduct(product)) {
          this.$message.error('门店限定商品无法在线购买');
          return false;
        }
        const quantity = Number(item.purchaseNum);
        if (!this.isValidQuantity(quantity, stock)) {
          return false;
        }
      }
      return true;
    },
    async clearCart() {
      if (!this.isLoggedIn) {
        this.requireLogin('管理购物车');
        return;
      }
      if (!this.shoppingCart.length) {
        this.$message.info('购物车已为空');
        return;
      }
      this.$confirm('确定要清空购物车吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.shoppingCart = [];
        this.cartTotal = 0;
        localStorage.removeItem('shoppingCart');
        const orderIdValue = Array.isArray(this.orderId) ? this.orderId.at(0) : this.orderId;
        if (orderIdValue) {
          apiRear.post('/order/update', {
            uid: this.userInfo.uid,
            orderId: orderIdValue,
            totalProductPrice: 0,
            shoppingCart: []
          }).finally(() => {
            this.$message.success('购物车已清空');
          });
        } else {
          this.$message.success('购物车已清空');
        }
      }).catch(() => {});
    },
    isValidQuantity(quantity, stock) {
      if (!Number.isFinite(quantity) || quantity <= 0 || !Number.isInteger(quantity)) {
        this.$message.error('购买数量必须为正整数');
        return false;
      }
      if (stock !== null && quantity > stock) {
        this.$message.error('购买数量不能大于剩余数量');
        return false;
      }
      return true;
    }
  },
};
</script>


<template>
  <el-container class="shop-layout" :class="{ 'with-aside': hasPanelAside }">
    <el-main class="shop-main">
      <button class="back-button" @click="$router.push('/indexView')">
        <i class="el-icon-arrow-left"></i>
      </button>
      <div class="shop-top-grid">
        <el-card shadow="hover" class="border shop-top-card shop-top-card--search">
          <p class="strong" style="font-size: 130%;margin: 10px">🔍查询</p>
          <div style="display: flex;margin: 10px;">
            <div style="width: 90%;">
              <el-input placeholder="请输入文创产品名" v-model="search.productName" class="input-with-select">
                <el-button slot="append"
                           icon="el-icon-search"
                           class="search-button"
                           @click="handle_loadItems"
                ></el-button>
              </el-input>
            </div>
            <el-button type="primary"
                       class="refresh-button"
                       @click="()=>{handle_loadItems();this.search.productName = '';}"
                       size="mini"
            >
              <i class="el-icon-refresh"></i>
            </el-button>
          </div>
        </el-card>
        <el-card v-if="isLoggedIn" shadow="hover" class="border shop-top-card">
          <p class="strong" style="font-size: 130%;margin: 10px">😣旅行偏好</p>
          <div class="travel-emoji-grid">
            <button
              v-for="item in travelEmojis"
              :key="item.emoji"
              type="button"
              class="travel-emoji-btn"
              :class="{ active: activeTravelEmoji === item.emoji }"
              @click="selectTravelEmoji(item.emoji)"
            >
              <span>{{ item.emoji }}</span>
            </button>
          </div>
          <p class="travel-emoji-tip">选择一个最贴合你的旅行动线</p>
          <p class="strong symptom-text" style="font-size: 13px;margin: 10px;text-align: center;">
            {{ userInfo.csymptom ? userInfo.csymptom : "暂无数据" }}
          </p>
        </el-card>
        <el-card v-else shadow="hover" class="border shop-top-card">
          <p class="strong" style="font-size: 130%;margin: 10px">👋游客模式</p>
          <p class="strong symptom-text" style="font-size: 13px;margin: 10px;text-align: center;">
            登录后可记录偏好与购买。
          </p>
        </el-card>
        <el-card v-if="isLoggedIn" shadow="hover" class="border shop-top-card shop-top-card--balance">
          <p class="strong" style="font-size: 130%;margin: 10px;text-align: left;">💴余额</p>
          <p class="strong" style="font-size: 200%;margin: 10px;text-align: center;">
            ￥：{{
              userInfo.balance !== undefined && userInfo.balance !== null ? (userInfo.balance === 0 ? '0.00' : userInfo.balance.toFixed(2)) : "unknown"
            }}
          </p>
        </el-card>
        <el-card v-else shadow="hover" class="border shop-top-card shop-top-card--balance">
          <p class="strong" style="font-size: 130%;margin: 10px;text-align: left;">💴余额</p>
          <p class="strong" style="font-size: 95%;margin: 10px;text-align: center;">
            登录后可查看余额与订单信息
          </p>
        </el-card>
      </div>
      <div class="shop-quick-filters">
        <el-button
          v-for="filter in quickFilters"
          :key="filter.key"
          round
          :class="['quick-filter-btn', { active: activeQuickFilter === filter.key }]"
          @click="applyQuickFilter(filter)"
        >
          {{ filter.label }}
        </el-button>
      </div>

      <div class="infinite-scroll-container">
        <el-dialog
            title="文创产品详情"
            :visible.sync="purchaseDialogVisible"
            width="60%"
            center>
          <div class="product-detail">
            <div class="detail-media">
              <el-image
                class="detail-image"
                :src="resolveProductImage(activeProduct && activeProduct.productCode)"
                fit="cover"
              ></el-image>
              <div class="detail-tags">
                <el-tag v-if="activeProduct" effect="dark" type="success">{{ activeProduct.category }}</el-tag>
                <el-tag v-if="activeProduct" effect="dark" type="warning">{{ activeProduct.displayMode }}</el-tag>
                <el-tag v-if="selectedProductIsOffline" effect="dark" type="danger">门店限定</el-tag>
                <el-tag v-if="activeProduct" effect="dark" type="info">库存 {{ activeProduct.stock }}</el-tag>
              </div>
            </div>
            <div class="detail-info">
              <p class="detail-eyebrow">中山文创 · 产品详情</p>
              <h3 class="detail-title">{{ activeProduct ? activeProduct.productName : '未找到对应文创产品信息' }}</h3>
              <p class="detail-code">编号：{{ activeProduct ? activeProduct.productCode : '--' }}</p>
              <p class="detail-highlight">
                {{ activeProduct ? activeProduct.highlight : '暂无产品亮点说明' }}
              </p>
              <p v-if="selectedProductIsOffline" class="detail-offline-note">门店限定商品仅支持线下购买</p>
              <div class="detail-price">
                ￥{{ activeProduct ? Number(activeProduct.price).toFixed(2) : '--' }}
              </div>
              <div class="detail-quantity">
                <span>购买数量</span>
                <el-input-number
                  v-model="purchaseQuantity"
                  :disabled="selectedProductIsOffline"
                  :min="1"
                  :precision="0"
                  :step="1"
                  :max="maxAvailableStock"
                  @input="updateCurrentProductTotal"
                  label="购买数量">
                </el-input-number>
              </div>
              <div class="detail-total">总计：￥{{ currentProductTotal }}</div>
            </div>
          </div>
          <span slot="footer" class="dialog-footer">
              <el-button @click="purchaseDialogVisible = false">取 消</el-button>
              <el-button type="primary" :disabled="selectedProductIsOffline" @click="purchaseHandle">确 定</el-button>
             </span>
        </el-dialog>

        <!--购物车结算抽屉-->
        <el-drawer
            title="购物车"
            :visible.sync="shoppingDrawer"
            :direction="direction"
            size="45%"
        >
          <div class="cart-drawer">
            <div class="cart-header">
              <div>
                <h3>购物车</h3>
                <p>共 {{ shoppingCart.length }} 件商品</p>
              </div>
              <el-button type="text" class="clear-cart-btn" @click="clearCart">一键清空</el-button>
            </div>
            <el-empty v-if="!cartItemsDetailed.length" description="购物车暂时为空"></el-empty>
            <div v-else class="cart-list">
              <el-card
                v-for="item in cartItemsDetailed"
                :key="item.productCode"
                shadow="hover"
                class="cart-item-card"
              >
                <div class="cart-item">
                  <img :src="item.imageUrl" :alt="item.productName" class="cart-item-image" />
                  <div class="cart-item-info">
                    <div class="cart-item-title">
                      <span>{{ item.productName }}</span>
                      <span class="cart-item-code">编号 {{ item.productCode }}</span>
                    </div>
                    <div class="cart-item-meta">
                      <span>单价：￥{{ Number(item.price).toFixed(2) }}</span>
                      <span v-if="item.stock !== null">库存：{{ item.stock }}</span>
                    </div>
                    <div class="cart-item-actions">
                      <el-input-number
                        :min="1"
                        :max="item.stock || inputLimit"
                        :step="1"
                        :precision="0"
                        :value="Number(item.purchaseNum)"
                        size="mini"
                        @change="value => updateCartQuantity(item, value)"
                      ></el-input-number>
                      <div class="cart-item-subtotal">
                        小计 ￥{{ (item.purchaseNum * item.price).toFixed(2) }}
                      </div>
                      <el-button
                        type="text"
                        size="mini"
                        :style="{color: dangerColor}"
                        @click="removeFromCart(item.productCode, item.productName)"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
            <div class="cart-footer-actions">
              <div class="cart-footer-total">合计：￥{{ cartTotal.toFixed(2) }}</div>
              <el-button
                type="primary"
                icon="el-icon-s-order"
                :disabled="!cartItemsDetailed.length"
                @click="handlePay(true)"
              >
                去结算
              </el-button>
            </div>
          </div>
        </el-drawer>
        <div style="width: 100%;display: flex;overflow: hidden">
          <el-card v-loading="loading" shadow="hover" style="width: 90%;margin-right: 2%" class="border">
            <div v-if="!loading && tableDataWithImages.length === 0"
                 style="display: flex; justify-content: center; align-items: center; height: 100vh;">
              <div style="text-align: center;">
                <p style="font-size: 200px; margin: 0;">🧐</p>
                <p class="strong" style="font-size: 50px; margin: 0;">
                  啊哦，没有找到任何文创产品。
                </p>
                <p class="strong" style="color: #6a6a6e; font-size: 30px; margin-top: 20px;">
                  尝试重置搜索，或是重置筛选条件。
                </p>
              </div>
            </div>
            <el-row :gutter="15">
              <el-col :span="8" v-for="(item, index) in tableDataWithImages" :key="item.productCode || index">
                <el-card shadow="hover" class="card">
                  <div class="card-content">
                    <div class="image-container">
                      <img
                          :src="item.imageUrl"
                          :alt="`文创产品图片 ${index + 1}`"
                          class="image">
                    </div>
                    <div class="text-container">
                      <p style="font-size: 25px" class="center strong">『{{ item.productName }}』</p>
                      <p class="strong">分类：{{ item.category }}</p>
                      <div style="display: flex;justify-content: space-between; align-items: center;">
                        <p class="strong">库存：{{ item.stock }}</p>
                        <el-tag v-if="isOfflineProduct(item)" class="strong" type="warning" effect="dark">门店限定</el-tag>
                        <el-tag v-if="item.stock <= 0" class="strong" type="danger" effect="dark">已售罄
                        </el-tag>
                      </div>
                      <div style="display: flex;justify-content: space-between">
                        <h1 class="strong" style="margin-bottom: 5px; color: #409EFF">￥ {{ item.price }}</h1>
                        <el-button type="primary" icon="el-icon-shopping-cart-2" size="mini"
                                   :disabled="item.stock <= 0"
                                   round style="font-size: 20px" @click="handleAddToCart(item)">
                        </el-button>
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </el-card>
          <div style="display: block">
            <div style=" width: 275px">
              <p class="strong" style="font-size: 25px;margin: 0 0 12px 0">🧭全系文创，任你挑。</p>
              <el-card shadow="hover" class="border" style="display: flex; flex-direction: column">
                <el-button v-for="(button, index) in classifyButtons"
                           :key="index"
                           @click="handleButtonClick(classifyButtons,index,'category')"
                           :underline="false"
                           style="border: none !important;"
                           :class="{ 'strong classify': true, 'clicked': button.isClicked }">
                  {{ button.text }}
                </el-button>
              </el-card>
            </div>

            <div style=" width: 275px">
              <p class="strong" style="font-size: 25px;margin: 80px 0 12px 0">💴旅行省心妙招，</p>
              <p class="strong" style="font-size: 25px;margin: 5px 0 12px 0;color: #6a6a6e">挑选最合适的纪念品。</p>
              <el-card shadow="hover" class="border" style="display: flex; flex-direction: column">
                <div class="block">
                  <p class="strong" style="font-size: 20px;margin: 5px 0 12px 20px">价格(CNY￥)：</p>
                  <el-input class="shengqian-el-input" v-model.number="search.minPrice"
                            @input="handlePositiveIntegerInput('minPrice', inputLimit)"></el-input>
                  <span>至</span>
                  <el-input class="shengqian-el-input" v-model.number="search.maxPrice"
                            @input="handlePositiveIntegerInput('maxPrice', inputLimit)"></el-input>

                  <p class="strong" style="font-size: 20px;margin: 10px 0 12px 20px">库存：</p>
                  <el-input class="shengqian-el-input" v-model.number="search.minStock"
                            @input="handlePositiveIntegerInput('minStock', inputLimit)"></el-input>
                  <span>至</span>
                  <el-input class="shengqian-el-input" v-model.number="search.maxStock"
                            @input="handlePositiveIntegerInput('maxStock', inputLimit)"></el-input>
                  <el-button :underline="false"
                             style="border: none !important;"
                             @click="handle_loadItems"
                             class="strong classify">
                    ✅确认
                  </el-button>
                  <el-button :underline="false"
                             style="border: none !important;"
                             @click="()=>{
                               this.search.minPrice='';
                               this.search.maxPrice='';
                               this.search.minStock='';
                               this.search.maxStock='';
                               this.handle_loadItems();
                             }"
                             class="strong classify">
                    🔁重置
                  </el-button>
                </div>
              </el-card>
            </div>
            <div style=" width: 275px">
              <p class="strong" style="font-size: 25px;margin: 80px 0 12px 0">🎒比纪念更实用。</p>
              <el-card shadow="hover" class="border" style="display: flex; flex-direction: column">
                <el-button v-for="(button, index) in typeButtons"
                           :key="index"
                           @click="handleButtonClick(typeButtons,index,'displayMode')"
                           :underline="false"
                           style="border: none !important;"
                           :class="{ 'strong classify': true, 'clicked': button.isClicked }">
                  {{ button.text }}
                </el-button>
              </el-card>
            </div>
          </div>
        </div>
        <div class="footer-bar">
          <div class="footer-cart">
            <el-badge :value="shoppingCart.length" class="item" style="padding: 0">
              <el-button
                icon="el-icon-shopping-cart-full"
                class="cart-fab"
                @click="handleOpenCart"
                circle
              ></el-button>
            </el-badge>
            <div class="footer-cart-info">
              <span class="footer-cart-label">购物车 · {{ shoppingCart.length }} 件</span>
              <span class="footer-cart-total">￥{{ cartTotal.toFixed(2) }}</span>
            </div>
          </div>
          <el-button
              type="primary"
              class="checkout-btn"
              @click="handlePay(true)"
              round>立即结算
          </el-button>
        </div>
      </div>
    </el-main>
    <el-footer style="height: 10px">
      <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          :hide-on-single-page="true"
          @current-change="handlePageChange"
          style="padding-bottom: 6%;display: flex; justify-content: center;"
      >
      </el-pagination>
    </el-footer>
  </el-container>
</template>

<style scoped>
.infinite-scroll-container {
  max-width: 100%;
  margin: 0;
}

.shop-layout {
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

.shop-main {
  padding: 24px 32px 90px;
  background: transparent;
}

.back-button {
  position: fixed;
  top: 20px;
  left: 20px;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: rgba(31, 122, 109, 0.12);
  color: var(--teal);
  border: 1px solid rgba(31, 122, 109, 0.35);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 20;
}

.back-button:hover {
  background: rgba(31, 122, 109, 0.22);
}

.product-detail {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) minmax(260px, 1.2fr);
  gap: 24px;
  align-items: stretch;
}

.detail-media {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-image {
  width: 100%;
  height: auto;
  aspect-ratio: 1 / 1;
  border-radius: 18px;
  overflow: hidden;
  background: rgba(31, 122, 109, 0.08);
}

.shop-layout :deep(.detail-image .el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-eyebrow {
  font-size: 12px;
  letter-spacing: 0.26em;
  color: rgba(64, 54, 44, 0.65);
  text-transform: uppercase;
  margin: 0;
}

.detail-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
  color: var(--ink);
}

.detail-code {
  font-size: 13px;
  color: rgba(64, 54, 44, 0.6);
  margin: 0;
}

.detail-highlight {
  font-size: 14px;
  line-height: 1.7;
  color: rgba(64, 54, 44, 0.8);
  margin: 0;
}

.detail-price {
  font-size: 26px;
  font-weight: 700;
  color: var(--teal);
}

.detail-quantity {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
  color: rgba(64, 54, 44, 0.8);
}

.detail-total {
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(31, 122, 109, 0.12);
  font-weight: 700;
  color: var(--teal);
}

.search-button {
  background-color: var(--accent);
  color: #ffffff;
  border-color: var(--accent);
}

.refresh-button {
  margin-left: 10px;
  border-radius: 15px;
}

.shop-layout :deep(.el-button--primary) {
  background-color: var(--accent);
  border-color: var(--accent);
}

.shop-layout :deep(.el-button--primary:hover),
.shop-layout :deep(.el-button--primary:focus) {
  background-color: var(--accent-strong);
  border-color: var(--accent-strong);
}

.shop-top-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr 0.8fr;
  gap: 16px;
  align-items: stretch;
  margin-bottom: 22px;
}

.shop-top-card {
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 120px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.shop-top-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 40px rgba(31, 56, 94, 0.12);
}

.shop-top-card--search {
  min-height: 120px;
}

.shop-top-card--balance {
  text-align: left;
}

.travel-emoji-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin: 6px 10px 2px;
}

.travel-emoji-btn {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  border: 1px solid rgba(31, 122, 109, 0.18);
  background: rgba(31, 122, 109, 0.08);
  font-size: 22px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.travel-emoji-btn.active {
  transform: scale(1.12);
  background: rgba(199, 90, 49, 0.18);
  border-color: rgba(199, 90, 49, 0.5);
  box-shadow: 0 10px 20px rgba(199, 90, 49, 0.2);
}

.travel-emoji-tip {
  margin: 4px 10px 8px;
  font-size: 12px;
  color: var(--ink-soft);
  text-align: center;
}

.detail-offline-note {
  margin: 0;
  color: #c75a31;
  font-weight: 600;
  font-size: 13px;
}

.shop-quick-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  margin: 12px 0 26px;
}

.quick-filter-btn {
  background: #ffffff;
  border: 1px solid rgba(199, 90, 49, 0.25);
  color: var(--ink);
  padding: 10px 18px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.quick-filter-btn.active,
.quick-filter-btn:hover {
  background: rgba(199, 90, 49, 0.12);
  border-color: rgba(199, 90, 49, 0.6);
  color: var(--accent);
  transform: translateY(-2px);
}

.image-container {
  flex: 0 0 70%; /* 占据卡片内容高度的70% */
  position: relative;
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 使图片覆盖整个容器 */
}

.text-container {
  flex: 0 0 30%; /* 占据卡片内容高度的30% */
  padding: 10px;
}

.card {
  margin: 20px;
  border-radius: 20px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-6px);
  box-shadow: 0 18px 36px rgba(31, 56, 94, 0.12);
}

.strong {
  font-weight: bold;
}

::v-deep .el-card__body {
  padding: 0;
}

.footer-bar {
  display: flex;
  z-index: 1000;
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  background: rgba(255, 255, 255, 0.92);
  justify-content: space-between;
  padding: 12px 24px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(6px);
  align-items: center;
}

.shop-layout.with-aside .footer-bar {
  left: 220px;
  width: calc(100% - 220px);
}

.footer-cart {
  display: flex;
  align-items: center;
  gap: 14px;
}

.cart-fab {
  width: 46px;
  height: 46px;
  color: #ffffff;
  background: linear-gradient(135deg, #1f7a6d, #c75a31);
  border: none;
}

.footer-cart-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  color: var(--ink);
}

.footer-cart-label {
  font-size: 12px;
  color: var(--ink-soft);
}

.footer-cart-total {
  font-size: 20px;
  font-weight: 700;
  color: var(--accent);
}

.checkout-btn {
  min-width: 140px;
}

.clear-cart-btn {
  color: #e83a3a;
  font-weight: 600;
}

.cart-item-code {
  font-size: 12px;
  color: var(--ink-soft);
}

.cart-footer-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.cart-footer-total {
  font-size: 16px;
  font-weight: 700;
  color: var(--accent);
}

.cart-drawer {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 16px;
  padding: 12px 8px 20px;
}

.cart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.cart-header h3 {
  margin: 0;
  font-size: 18px;
}

.cart-header p {
  margin: 4px 0 0;
  color: #5f6b66;
  font-size: 13px;
}

.cart-total {
  font-size: 16px;
  color: var(--accent);
}

.cart-list {
  padding-right: 6px;
  max-height: calc(100vh - 280px);
  overflow: auto;
}

.cart-item-card {
  border-radius: 16px;
  margin-bottom: 14px;
}

.cart-item {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.cart-item-image {
  width: 90px;
  height: 90px;
  border-radius: 12px;
  object-fit: cover;
  background: rgba(31, 122, 109, 0.08);
}

.cart-item-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.cart-item-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-weight: 600;
}

.cart-item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: var(--ink-soft);
  font-size: 13px;
}

.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cart-item-subtotal {
  font-weight: 600;
  color: var(--accent);
}

.cart-footer-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

.card-image-container {
  width: 24vh;
  height: 24vh;
  border-radius: 10px;
}

.products-info-card {
  padding: 10px;
  margin: 15px;
  border-radius: 10px;
  background-color: #d4e7fa;
}

.center {
  display: flex;
  justify-content: center;
}

.border {
  border-radius: 18px;
  background: var(--paper);
  border: 1px solid rgba(31, 122, 109, 0.12);
  box-shadow: var(--shadow-soft);
}

.classify {
  font-size: 20px;
  padding: 15px 30px 15px 10px;
  margin: 10px;
  width: 90%;
  border-radius: 15px;
  background: rgba(31, 122, 109, 0.08);
  color: var(--ink);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.clicked {
  background-color: rgba(199, 90, 49, 0.16); /* 点击后的背景颜色 */
  color: var(--accent);
}

.symptom-text {
  max-height: 75px; /* 设置最大高度 */
  overflow: hidden; /* 隐藏超出部分的内容 */
  text-overflow: ellipsis; /* 显示省略号 */
  display: -webkit-box; /* 使用弹性盒子模型 */
  line-clamp: 3;
  -webkit-line-clamp: 3; /* 限制显示的行数 */
  -webkit-box-orient: vertical; /* 垂直排列 */
}

.shengqian-el-input {
  width: 100px;
  margin: 10px
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.shop-top-card,
.shop-quick-filters,
.card {
  animation: fadeUp 0.6s ease both;
}

@media (max-width: 1200px) {
  .shop-top-grid {
    grid-template-columns: 1fr;
  }

  .product-detail {
    grid-template-columns: 1fr;
  }

  .detail-image {
    height: auto;
    aspect-ratio: 1 / 1;
  }
}
</style>
