<template>
  <div class="message-center">
    <el-badge :value="unreadCount" :hidden="!unreadCount" class="message-badge">
      <el-button class="message-button" icon="el-icon-bell" @click="openDialog" circle></el-button>
    </el-badge>

    <el-dialog
      title="消息中心"
      :visible.sync="visible"
      width="420px"
      top="15vh"
      :append-to-body="true"
      :modal-append-to-body="true"
      :close-on-click-modal="false"
      custom-class="message-dialog"
      center
    >
      <div v-if="messages.length" class="message-list" v-loading="loading">
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-item"
          :class="{ unread: !message.read }"
          @click="handleMessageClick(message)"
        >
          <div class="message-title">
            <span>{{ message.title }}</span>
            <span class="message-time">{{ message.time }}</span>
          </div>
          <p class="message-content">{{ message.content }}</p>
          <div class="message-actions">
            <el-button type="text" @click.stop="viewDetail(message)">查看详情</el-button>
            <el-button type="text" @click.stop="markAsRead(message)">标记已读</el-button>
            <el-button type="text" @click.stop="removeItem(message)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无消息"></el-empty>
    </el-dialog>
  </div>
</template>

<script>
import {apiRear} from '@/axios';
import {jwtDecode} from 'jwt-decode';

export default {
  name: 'MessageCenter',
  props: {
    uid: {
      type: [String, Number],
      default: ''
    }
  },
  data() {
    return {
      visible: false,
      loading: false,
      messages: []
    };
  },
  computed: {
    unreadCount() {
      return this.messages.filter(item => !item.read).length;
    },
    currentUid() {
      if (this.uid) {
        return this.uid;
      }
      const token = sessionStorage.getItem('token');
      if (!token) {
        return '';
      }
      try {
        const payload = jwtDecode(token);
        return payload && payload.uid ? payload.uid : '';
      } catch (error) {
        return '';
      }
    },
    isLoggedIn() {
      return Boolean(this.currentUid);
    }
  },
  watch: {
    uid() {
      this.refresh();
    }
  },
  mounted() {
    this.refresh();
  },
  methods: {
    async refresh() {
      if (!this.isLoggedIn) {
        this.messages = [];
        return;
      }
      this.loading = true;
      try {
        const res = await apiRear.get('/messages');
        const list = Array.isArray(res.data.data) ? res.data.data : [];
        this.messages = list.map(item => ({
          ...item,
          read: Number(item.isRead) === 1,
          time: this.formatTime(item.createTime)
        }));
      } catch (error) {
        this.messages = [];
      } finally {
        this.loading = false;
      }
    },
    openDialog() {
      if (!this.isLoggedIn) {
        this.$message.warning('请登录后查看消息中心');
        return;
      }
      this.refresh();
      this.visible = true;
    },
    async handleMessageClick(message) {
      if (!message.read) {
        await this.markAsRead(message);
      }
    },
    async viewDetail(message) {
      if (!message) {
        return;
      }
      await this.markAsRead(message);
      const target = this.resolveMessageRoute(message);
      if (target) {
        this.visible = false;
        this.$router.push(target);
      }
    },
    async markAsRead(message) {
      if (!message || message.read) {
        return;
      }
      try {
        await apiRear.post('/messages/read', {id: message.id});
        message.read = true;
        message.isRead = 1;
      } catch (error) {
        // ignore
      }
    },
    async removeItem(message) {
      if (!message) {
        return;
      }
      try {
        await apiRear.post('/messages/delete', {id: message.id});
        this.messages = this.messages.filter(item => item.id !== message.id);
      } catch (error) {
        // ignore
      }
    },
    resolveMessageRoute(message) {
      const type = message.messageType || message.type || '';
      const refId = message.refId || message.refID || message.orderId || '';
      if (type === 'ORDER_PENDING' && refId) {
        return {path: '/billPayment', query: {orderId: refId}};
      }
      if ((type === 'ORDER_PICKUP' || type === 'ORDER_REFUND') && refId) {
        sessionStorage.setItem('openOrderDetailId', String(refId));
        return {path: '/userInfo'};
      }
      if (refId) {
        sessionStorage.setItem('openOrderDetailId', String(refId));
        return {path: '/userInfo'};
      }
      return {path: '/userInfo'};
    },
    formatTime(value) {
      if (!value) {
        return '';
      }
      const safe = typeof value === 'string' ? value.replace(' ', 'T') : value;
      const date = new Date(safe);
      if (Number.isNaN(date.getTime())) {
        return value;
      }
      const pad = num => String(num).padStart(2, '0');
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
    }
  }
};
</script>

<style scoped>
.message-center {
  display: inline-flex;
  align-items: center;
}

.message-button {
  width: 40px;
  height: 40px;
  border: none;
  color: #ffffff;
  background: linear-gradient(135deg, #1f7a6d, #c75a31);
  box-shadow: 0 10px 22px rgba(31, 122, 109, 0.25);
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 420px;
  overflow: auto;
}

.message-item {
  border-radius: 14px;
  padding: 12px 14px;
  border: 1px solid rgba(31, 122, 109, 0.12);
  background: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  transition: all 0.2s ease;
}

.message-item.unread {
  border-color: rgba(199, 90, 49, 0.4);
  background: rgba(255, 248, 242, 0.95);
}

.message-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(25, 45, 40, 0.12);
}

.message-title {
  display: flex;
  justify-content: space-between;
  font-weight: 700;
  color: #3a3a3a;
  font-size: 14px;
}

.message-time {
  font-weight: 500;
  font-size: 12px;
  color: rgba(64, 54, 44, 0.6);
}

.message-content {
  margin: 8px 0 6px;
  font-size: 13px;
  color: rgba(64, 54, 44, 0.85);
  line-height: 1.6;
}

.message-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

:deep(.message-dialog) {
  border-radius: 18px;
}

:deep(.el-dialog__wrapper) {
  z-index: 3000;
}
</style>
