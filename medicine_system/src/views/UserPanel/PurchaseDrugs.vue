<script>

import {apiRear} from "@/axios";
import {toInteger} from "lodash/lang";
import {jwtDecode} from "jwt-decode";

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
      drugsNum: 0,
      purchaseDrugsNum: 0,
      drugPrice: 0,
      drugsPrice: [],
      maxPrice: 0,
      AllDrugsPrice: 0,
      shoppingCart: [],
      shoppingDrawer: false,
      direction: 'rtl',//right to left
      maxDrugsNum: 0,
      inputLimit: 100000000,
      page: 1,
      pageSize: 21,
      purchasedDrugsInfo: {
        mno: '',
        mname: '',
        purchaseNum: 0,
        price: 0
      },
      search: {
        page: 1,
        pageSize: 21,
        mno: '',
        mname: '',
        mmode: '',
        mefficacy: '',
        mtype: '',
        minMprice: null,
        maxMprice: null,
        minMnum: null,
        maxMnum: null
      },
      images: [
        require('@/assets/drugsImage/1.png'),
        require('@/assets/drugsImage/2.png'),
        require('@/assets/drugsImage/3.png'),
        require('@/assets/drugsImage/4.png'),
        require('@/assets/drugsImage/5.png'),
        require('@/assets/drugsImage/6.png'),
        require('@/assets/drugsImage/7.png'),
        require('@/assets/drugsImage/8.png'),
        require('@/assets/drugsImage/9.png'),
        require('@/assets/drugsImage/10.png'),
        require('@/assets/drugsImage/11.png'),
        require('@/assets/drugsImage/12.png'),
        require('@/assets/drugsImage/13.png'),
        require('@/assets/drugsImage/14.png'),
        require('@/assets/drugsImage/15.png'),
        require('@/assets/drugsImage/16.png'),
        require('@/assets/drugsImage/17.png'),
        require('@/assets/drugsImage/18.png'),
        require('@/assets/drugsImage/19.png'),
        require('@/assets/drugsImage/20.png'),
        require('@/assets/drugsImage/21.png')
      ],
      classifyButtons: [
        {text: '💊全部药物', isClicked: false},
        {text: '🧊解热镇痛药', isClicked: false},
        {text: '🤧抗感冒药', isClicked: false},
        {text: '🌼抗过敏药', isClicked: false},
        {text: '🤒止咳药', isClicked: false},
        {text: '💉抗生素', isClicked: false},
        {text: '🦠抗病毒药', isClicked: false},
        {text: '🍄抗真菌药', isClicked: false},
        {text: '🍽️消化系统药', isClicked: false},
        {text: '❤️心血管系统药', isClicked: false},
        {text: '🧠精神科药', isClicked: false},
        {text: '🩹皮肤科药', isClicked: false},
        {text: '🔄内分泌系统药', isClicked: false},
        {text: '🛡️免疫调节药', isClicked: false},
        {text: '🎗️抗肿瘤药', isClicked: false}
      ],
      typeButtons: [
        {text: '✨全部', isClicked: false},
        {text: '👄内服', isClicked: false},
        {text: '💧外用', isClicked: false},
      ]
    };
  },
  created() {
    this.loadItems();
    this.getShoppingCart();
    // 添加 beforeunload 事件监听器
    window.addEventListener('beforeunload', () => this.handlePay(false), {once: true});
  },
  computed: {
    tableDataWithImages() {
      //index % this.images.length  顺序显示
      return this.tableData.map((item, index) => ({
        ...item,
        imageUrl: this.images[(index % this.tableData.length)]
      }));
    },
  },
  methods: {
    toInteger,
    findMaxPrice() {
      // 初始值设为负无穷大，确保任何价格都能被比较
      return this.tableData.reduce((max, item) => {
        return item.mprice > max ? item.mprice : max;
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
      if (this.shoppingCart.length <= 0) {
        this.$message.error('购物车为空！');
        return;
      }
      this.loading = true;
      try {
        if (this.orderId === null) {
          await apiRear.post('/order/add', {
            uid: this.userInfo.uid,
            allDrugPrice: this.AllDrugsPrice,
            shoppingCart: this.shoppingCart,
          }).then(
              res => {
                this.orderId = res.data.data;
                console.log(this.orderId)
              }
          );
        } else {
          await apiRear.post('/order/update', {
            uid: this.userInfo.uid,
            orderId: this.orderId.at(0),
            allDrugPrice: this.AllDrugsPrice,
            shoppingCart: this.shoppingCart,
          });
        }
      } catch (error) {
        this.$message.error('订单保存失败！');
      }
      if (!isButtonTriggered) {
        this.loading = false;
        return;
      }
      if (this.userInfo.balance < this.AllDrugsPrice) {
        this.$confirm('您的余额不足，是否还要前往结算页面？', '余额不足', {
          confirmButtonText: '前往结算',
          cancelButtonText: '继续购物',
          type: 'warning'
        }).then(() => {
          localStorage.removeItem('shoppingCart');
          this.loading = false;
          this.$router.push('/billPayment');
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消结算'
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
        const res = await apiRear.get('/drugsManagement', {params: this.search});
        this.tableData = res.data.data.rows;
        this.total = res.data.data.total;
        this.maxPrice = this.findMaxPrice();
        this.drugsPrice = [0, this.maxPrice];
      } catch (err) {
        this.$message.error('未知错误,请重新登录后再试');
      } finally {
        this.loading = false;
      }
    },
    async getUserInfo() {
      try {
        const info = await apiRear.get(`/userInfo/${jwtDecode(sessionStorage.getItem('token') || '').uid}`);
        this.userInfo = info.data.data;
      } catch (err) {
        this.$message.error('无法获取用户数据！');
      }
    },
    async getShoppingCart() {
      try {
        await this.getUserInfo(); //确保用户信息已加载
        if (this.userInfo.uid !== undefined) {
          apiRear.get(`/order/getOrderInfoBy=${this.userInfo.uid}`).then(info => {
            if (Array.isArray(info.data.data.orderId) && info.data.data.orderId.length > 0) {
              this.orderId = info.data.data.orderId;
              this.AllDrugsPrice = info.data.data.totalPrice;
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
    handleAddToCart(index) {
      this.purchaseDialogVisible = true;
      this.purchaseDrugsNum = 0;
      this.maxDrugsNum = this.tableData[index].mnum;
      this.drugsNum = index;
    },
    purchaseHandle() {
      const purchasedDrugs = this.shoppingCart.find(item => item.mno === this.tableData[this.drugsNum].mno);
      if (purchasedDrugs != null) {
        if (this.tableData[this.drugsNum].mnum - purchasedDrugs.purchaseNum - this.purchaseDrugsNum < 0) {
          this.$message.error('购买数量不能大于剩余数量');
          return;
        } else {
          this.shoppingCart[this.shoppingCart.indexOf(purchasedDrugs)].purchaseNum += this.purchaseDrugsNum;
          this.updateAllDrugPrice();
          localStorage.setItem('shoppingCart', JSON.stringify(this.shoppingCart));
          this.purchaseDialogVisible = false;
          return;
        }
      }
      this.purchasedDrugsInfo.mno = this.tableData[this.drugsNum].mno;
      this.purchasedDrugsInfo.purchaseNum = this.purchaseDrugsNum;
      this.purchasedDrugsInfo.mname = this.tableData[this.drugsNum].mname;
      this.purchasedDrugsInfo.price = this.tableData[this.drugsNum].mprice;
      const newPurchase = JSON.parse(JSON.stringify(this.purchasedDrugsInfo));
      this.shoppingCart.push(newPurchase);
      this.AllDrugsPrice = 0;
      this.updateAllDrugPrice();
      localStorage.setItem('shoppingCart', JSON.stringify(this.shoppingCart));
      this.purchaseDialogVisible = false;
    },
    updateDrugPrice() {
      this.drugPrice = (this.purchaseDrugsNum * this.tableData[this.drugsNum].mprice).toFixed(2);
    },
    updateAllDrugPrice() {
      this.AllDrugsPrice = this.shoppingCart.reduce((total, item) => {
        return parseFloat((total + item.purchaseNum * item.price).toFixed(2));
      }, 0);
    },
    shoppingCartDeleteDrug(mno, mname) {
      this.$confirm('此操作会删除所有的『' + mname + '』，确定吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.shoppingCart.splice(this.shoppingCart.indexOf(this.shoppingCart.find(item => item.mno === mno)), 1);
        this.updateAllDrugPrice();
        localStorage.setItem('shoppingCart', JSON.stringify(this.shoppingCart));
        this.$message.success('已删除购物车所有的『' + mname + '』！');
      }).catch(() => {
        this.$message.info('已取消');
      });
    },
    shoppingCartModifyDrug(mno, mname, purchaseNum) {
      this.$prompt('请输入『' + mname + '』数量', '修改', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: purchaseNum,
        inputType: 'number',
        inputPattern: /^\d+$/,
        inputErrorMessage: '请输入正整数',
        closeOnClickModal: false
      }).then(({value}) => {
        const purchasedDrug = this.shoppingCart.find(item => item.mno === mno);
        if (this.tableData[this.drugsNum].mnum < value) {
          this.$message.error('购买数量不能大于剩余数量');
        } else if (value === '0') {
          this.shoppingCartDeleteDrug(mno, mname);
        } else {
          purchasedDrug.purchaseNum = value;
          this.updateAllDrugPrice();
          localStorage.setItem('shoppingCart', JSON.stringify(this.shoppingCart));
          this.$message({
            type: 'success',
            message: '『' + mname + '』' + '数量已修改为：' + value
          });
        }
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '取消修改'
        });
      });
    }
  },
};
</script>


<template>
  <el-container>
    <el-main>
      <div style="display: flex;justify-content: space-between;max-height: 105px;">
        <el-card shadow="hover" class="border" style="width: 550px;">
          <p class="strong" style="font-size: 130%;margin: 10px">🔍查询</p>
          <div style="display: flex;margin: 10px;">
            <div style="width: 90%;">
              <el-input placeholder="请输入药物名" v-model="search.mname" class="input-with-select">
                <el-button slot="append"
                           icon="el-icon-search"
                           style="background-color: #409EFF; color: white"
                           @click="handle_loadItems"
                ></el-button>
              </el-input>
            </div>
            <el-button type="primary"
                       style="margin-left: 10px;border-radius: 15px"
                       @click="()=>{handle_loadItems();this.search.mname = '';}"
                       size="mini"
            >
              <i class="el-icon-refresh"></i>
            </el-button>
          </div>
        </el-card>
        <el-card shadow="hover" class="border" style="width: 450px;">
          <p class="strong" style="font-size: 130%;margin: 10px">😣症状</p>
          <p class="strong symptom-text" style="font-size: 13px;margin: 10px;text-align: center;">
            {{ userInfo.csymptom ? userInfo.csymptom : "暂无数据" }}
          </p>
        </el-card>
        <el-card shadow="hover" class="border"
                 style="width: 25%;
                        display: flex;
                        flex-direction: column;
                        justify-content: center;">
          <p class="strong" style="font-size: 130%;margin: 10px;text-align: left;">💴余额</p>
          <p class="strong" style="font-size: 200%;margin: 10px;text-align: center;">
            ￥：{{
              userInfo.balance !== undefined && userInfo.balance !== null ? (userInfo.balance === 0 ? '0.00' : userInfo.balance.toFixed(2)) : "unknown"
            }}
          </p>
        </el-card>
      </div>
      <div style="width: 100%;display: flex;justify-content: center;">
        <el-row style="display: flex;justify-content: center;">
          <el-col :span="6">
            <el-card shadow="hover" style="width: 200px;margin: 20px" class="border">
              <p class="strong" style="font-size: 20px;margin: 10px;padding-left:28px">🔥热销商品</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" style="width: 200px;margin: 20px" class="border">
              <p class="strong" style="font-size: 20px;margin: 10px;padding-left:28px">⏰时下流行</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" style="width: 200px;margin: 20px" class="border">
              <p class="strong" style="font-size: 20px;margin: 10px;padding-left:28px">🆕最新商品</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" style="width: 200px;margin: 20px" class="border">
              <p class="strong" style="font-size: 20px;margin: 10px;padding-left:28px">🤩历史购买</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" style="width: 200px;margin: 20px" class="border">
              <p class="strong" style="font-size: 20px;margin: 10px;padding-left:28px">🐵为你推荐</p>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div class="infinite-scroll-container">
        <el-dialog
            title="药物详情"
            :visible.sync="purchaseDialogVisible"
            width="30%"
            center>
          <div class="demo-image center">
            <el-image
                class="card-image-container"
                :src="images[toInteger(drugsNum)]"
            ></el-image>
          </div>
          <div style="display: flex;align-items: center;flex-direction: column;">
            <p v-if="tableData[drugsNum]" style="font-size: 3vh;margin-bottom: 1vh" class="strong">
              {{ tableData[drugsNum].mname }}</p>
            <p v-if="tableData[drugsNum]" style="font-size: 95%;margin-bottom: 1vh" class="strong">
              药物编号：{{ tableData[drugsNum].mno }}</p>
            <p v-else style="font-size: 3vh;margin-bottom: 1vh" class="strong">未找到对应药物信息</p>
          </div>
          <el-card shadow="hover" class="drugs-info-card">
            <span v-if="tableData[drugsNum]" style="font-size: 100%" class="strong">
              分类：{{ tableData[drugsNum].mtype }}</span>
            <span v-else style="font-size: 100%" class="strong">未找到对应药物分类</span>
          </el-card>
          <el-card shadow="hover" class="drugs-info-card">
            <span v-if="tableData[drugsNum]" style="font-size: 100%" class="strong">
              使用方式：{{ tableData[drugsNum].mmode }}</span>
            <span v-else style="font-size: 100%" class="strong">未找到对应药物使用方式</span>
          </el-card>
          <el-card shadow="hover" class="drugs-info-card">
            <span v-if="tableData[drugsNum]" style="font-size: 100%" class="strong">
              药效：{{ tableData[drugsNum].mefficacy }}</span>
            <span v-else style="font-size: 100%" class="strong">未找到对应药物药效</span>
          </el-card>
          <el-card shadow="hover" class="drugs-info-card">
            <span v-if="tableData[drugsNum]" style="font-size: 100%" class="strong">
              剩余数量：{{ tableData[drugsNum].mnum }}</span>
            <span v-else style="font-size: 100%" class="strong">未找到对应药物剩余数量</span>
          </el-card>
          <el-card class="drugs-info-card">
            <span style="font-size: 100%" class="strong">购买数量：</span>
            <div class="center">
              <el-input-number
                  v-model="purchaseDrugsNum"
                  :min="1"
                  :precision="0"
                  :max="maxDrugsNum"
                  @input="updateDrugPrice"
                  label="购买数量">
              </el-input-number>
            </div>
          </el-card>
          <el-card class="drugs-info-card strong" style="color: #409EFF;font-size: 3vh;margin: 0">
            总计：￥{{ drugPrice }}
          </el-card>
          <span slot="footer" class="dialog-footer">
              <el-button @click="purchaseDialogVisible = false">取 消</el-button>
              <el-button type="primary" @click="purchaseHandle">确 定</el-button>
             </span>
        </el-dialog>

        <!--购物车结算抽屉-->
        <el-drawer
            title="购物车"
            :visible.sync="shoppingDrawer"
            :direction="direction"
            size="40%"
        >
          <el-card shadow="always" class="center" style="margin: 10px">
            <el-table :data="shoppingCart" v-loading="loading" height="780">
              <el-table-column label="药物编号" prop="mno" width="110%"></el-table-column>
              <el-table-column label="名称" prop="mname" width="130%"></el-table-column>
              <el-table-column label="数量" prop="purchaseNum" width="130%"></el-table-column>
              <el-table-column label="单价" prop="price" width="130%"></el-table-column>
              <el-table-column
                  fixed="right"
                  label="操作"
                  width="100">
                <template slot-scope="scope">
                  <el-button @click="shoppingCartModifyDrug(scope.row.mno,scope.row.mname,scope.row.purchaseNum)"
                             type="text"
                             :underline="true"
                             size="small">
                    修改
                  </el-button>
                  <el-button @click="shoppingCartDeleteDrug(scope.row.mno,scope.row.mname)"
                             type="text"
                             size="small"
                             :style="{color: dangerColor}">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-drawer>
        <div style="width: 100%;display: flex;overflow: hidden">
          <el-card v-loading="loading" shadow="hover" style="width: 90%;margin-right: 2%" class="border">
            <div v-if="!loading && tableData.length === 0"
                 style="display: flex; justify-content: center; align-items: center; height: 100vh;">
              <div style="text-align: center;">
                <p style="font-size: 200px; margin: 0;">🧐</p>
                <p class="strong" style="font-size: 50px; margin: 0;">
                  啊哦，没有找到任何药物。
                </p>
                <p class="strong" style="color: #6a6a6e; font-size: 30px; margin-top: 20px;">
                  尝试重置搜索，或是重置筛选条件。
                </p>
              </div>
            </div>
            <el-row :gutter="15">
              <el-col :span="8" v-for="(item, index) in tableDataWithImages" :key="index">
                <el-card shadow="hover" class="card">
                  <div class="card-content">
                    <div class="image-container">
                      <img
                          :src="item.imageUrl"
                          :alt="`药物图片 ${index + 1}`"
                          class="image">
                    </div>
                    <div class="text-container">
                      <p style="font-size: 25px" class="center strong">『{{ item.mname }}』</p>
                      <p class="strong">分类: {{ item.mtype }}</p>
                      <div style="display: flex;justify-content: space-between; align-items: center;">
                        <p class="strong">数量: {{ item.mnum }}</p>
                        <el-tag v-if="tableData[index].mnum <= 0" class="strong" type="danger" effect="dark">已售罄
                        </el-tag>
                      </div>
                      <div style="display: flex;justify-content: space-between">
                        <h1 class="strong" style="margin-bottom: 5px; color: #409EFF">￥ {{ item.mprice }}</h1>
                        <el-button type="primary" icon="el-icon-shopping-cart-2" size="mini"
                                   :disabled="tableData[index].mnum <= 0"
                                   round style="font-size: 20px" @click="handleAddToCart(index)">
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
              <p class="strong" style="font-size: 25px;margin: 0 0 12px 0">👨‍⚕️全部药物，随你挑。</p>
              <el-card shadow="hover" class="border" style="display: flex; flex-direction: column">
                <el-button v-for="(button, index) in classifyButtons"
                           :key="index"
                           @click="handleButtonClick(classifyButtons,index,'mtype')"
                           :underline="false"
                           style="border: none !important;"
                           :class="{ 'strong classify': true, 'clicked': button.isClicked }">
                  {{ button.text }}
                </el-button>
              </el-card>
            </div>

            <div style=" width: 275px">
              <p class="strong" style="font-size: 25px;margin: 80px 0 12px 0">💴省钱妙招，</p>
              <p class="strong" style="font-size: 25px;margin: 5px 0 12px 0;color: #6a6a6e">总有一招适合你。</p>
              <el-card shadow="hover" class="border" style="display: flex; flex-direction: column">
                <div class="block">
                  <p class="strong" style="font-size: 20px;margin: 5px 0 12px 20px">价格(CNY￥)：</p>
                  <el-input class="shengqian-el-input" v-model.number="search.minMprice"
                            @input="handlePositiveIntegerInput('minMprice', inputLimit)"></el-input>
                  <span>至</span>
                  <el-input class="shengqian-el-input" v-model.number="search.maxMprice"
                            @input="handlePositiveIntegerInput('maxMprice', inputLimit)"></el-input>

                  <p class="strong" style="font-size: 20px;margin: 10px 0 12px 20px">库存：</p>
                  <el-input class="shengqian-el-input" v-model.number="search.minMnum"
                            @input="handlePositiveIntegerInput('minMnum', inputLimit)"></el-input>
                  <span>至</span>
                  <el-input class="shengqian-el-input" v-model.number="search.maxMnum"
                            @input="handlePositiveIntegerInput('maxMnum', inputLimit)"></el-input>
                  <el-button :underline="false"
                             style="border: none !important;"
                             @click="handle_loadItems"
                             class="strong classify">
                    ✅确认
                  </el-button>
                  <el-button :underline="false"
                             style="border: none !important;"
                             @click="()=>{
                               this.search.minMprice='';
                               this.search.maxMprice='';
                               this.search.minMnum='';
                               this.search.maxMnum='';
                               handle_loadItems();
                             }"
                             class="strong classify">
                    🔁重置
                  </el-button>
                </div>
              </el-card>
            </div>
            <div style=" width: 275px">
              <p class="strong" style="font-size: 25px;margin: 80px 0 12px 0">🩺不止是拿来吃。</p>
              <el-card shadow="hover" class="border" style="display: flex; flex-direction: column">
                <el-button v-for="(button, index) in typeButtons"
                           :key="index"
                           @click="handleButtonClick(typeButtons,index,'mmode')"
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
          <div style="display: flex">
            <el-badge :value="shoppingCart.length" class="item" style="padding: 0">
              <el-button
                  icon="el-icon-shopping-cart-full"
                  style="
                    width: 45px;
                    height: 45px;
                    margin-left: 20px;
                    color: white;
                    background-color: #409EFF"
                  @click="shoppingDrawer = true"
                  circle></el-button>
            </el-badge>
            <div style="color: #409EFF">
              <span class="strong" style="margin-left: 20px">
                ￥：</span>
              <span class="strong" style="font-size: 200%">{{ AllDrugsPrice.toFixed(2) }}
              </span>
            </div>
          </div>
          <el-button
              type="primary"
              style="width: 10%;margin-right: 30px"
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
  left: 200px;
  width: calc(100% - 200px);
  background-color: white;
  justify-content: space-between;
  padding: 10px 0;
  box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.1); /* 添加阴影效果 */
}

.card-image-container {
  width: 24vh;
  height: 24vh;
  border-radius: 10px;
}

.drugs-info-card {
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
  border-radius: 10px;
}

.classify {
  font-size: 20px;
  padding: 15px 30px 15px 10px;
  margin: 10px;
  width: 90%;
  border-radius: 15px;
}

.clicked {
  background-color: #d7eafd; /* 点击后的背景颜色 */
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
</style>
