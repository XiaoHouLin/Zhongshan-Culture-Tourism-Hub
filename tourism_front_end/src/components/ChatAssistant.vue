<script>
import { apiRear } from '@/axios';

const GUEST_LIMIT = 3;

export default {
  name: 'ChatAssistant',
  props: {
    isLoggedIn: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      visible: false,
      input: '',
      sending: false,
      quickQuestions: [
        '中山必去景点有哪些？',
        '中山一日游路线推荐',
        '中山特色美食有哪些？',
        '古镇灯饰城怎么逛？'
      ],
      messages: [
        {
          role: 'assistant',
          content: '你好，我是中山文旅智能助理。欢迎咨询中山市旅游相关问题。'
        }
      ],
      guestCount: 0
    };
  },
  computed: {
    remainingCount() {
      if (this.isLoggedIn) {
        return null;
      }
      return Math.max(GUEST_LIMIT - this.guestCount, 0);
    }
  },
  created() {
    this.syncGuestCount();
  },
  methods: {
    toggleChat() {
      this.visible = !this.visible;
      if (this.visible) {
        this.$nextTick(() => this.scrollToBottom());
      }
    },
    syncGuestCount() {
      const stored = Number(localStorage.getItem('guestChatCount') || '0');
      this.guestCount = Number.isFinite(stored) ? stored : 0;
    },
    canSend() {
      const content = this.input.trim();
      if (!content) {
        return false;
      }
      if (!this.isLoggedIn && this.guestCount >= GUEST_LIMIT) {
        this.$message.warning('游客仅可咨询3次，请登录后继续使用');
        this.$router.push('/login').catch(() => {});
        return false;
      }
      return true;
    },
    async sendMessage() {
      if (!this.canSend() || this.sending) {
        return;
      }
      const content = this.input.trim();
      this.input = '';
      this.messages.push({ role: 'user', content });
      this.scrollToBottom();
      this.sending = true;
      try {
        const payload = {
          messages: this.messages.slice(-8).map(item => ({
            role: item.role,
            content: item.content
          }))
        };
        const res = await apiRear.post('/chat', payload, { skipAuth: true, skipAuthRedirect: true });
        if (res.data && res.data.code === 1) {
          const reply = res.data.data && res.data.data.reply ? res.data.data.reply : '暂时无法回答，请稍后再试。';
          this.messages.push({ role: 'assistant', content: reply });
        } else {
          const message = (res.data && res.data.message) || '暂时无法回答，请稍后再试。';
          this.messages.push({ role: 'assistant', content: message });
        }
        if (!this.isLoggedIn) {
          this.guestCount += 1;
          localStorage.setItem('guestChatCount', String(this.guestCount));
        }
      } catch (error) {
        this.messages.push({ role: 'assistant', content: '服务异常，请稍后再试。' });
      } finally {
        this.sending = false;
        this.scrollToBottom();
      }
    },
    handleKeydown(event) {
      if (!event) {
        return;
      }
      if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
        event.preventDefault();
        this.sendMessage();
      }
    },
    applyQuickQuestion(question) {
      if (!question) {
        return;
      }
      this.input = question;
      this.$nextTick(() => {
        this.sendMessage();
      });
    },
    scrollToBottom() {
      const body = this.$refs.chatBody;
      if (!body) {
        return;
      }
      this.$nextTick(() => {
        body.scrollTop = body.scrollHeight;
      });
    }
  }
};
</script>

<template>
  <div class="chat-assistant">
    <div v-if="!visible" class="chat-bubble">想了解更多中山文化请问我</div>
    <button class="chat-button" @click="toggleChat" aria-label="打开中山文旅聊天助手">
      <i class="el-icon-chat-dot-round"></i>
    </button>
    <div v-if="visible" class="chat-panel">
      <div class="chat-header">
        <div class="chat-title">
          <span>中山文旅智能助理</span>
          <small>仅回答中山市旅游相关问题</small>
        </div>
        <button class="chat-close" @click="toggleChat" aria-label="关闭聊天">×</button>
      </div>
      <div class="chat-window">
        <div class="chat-suggest">
          <span class="suggest-title">快捷推荐问题</span>
          <div class="suggest-list">
            <button
              v-for="question in quickQuestions"
              :key="question"
              class="suggest-item"
              @click="applyQuickQuestion(question)"
            >
              {{ question }}
            </button>
          </div>
        </div>
        <div class="chat-body" ref="chatBody">
          <div v-for="(item, index) in messages" :key="index" class="chat-row" :class="item.role">
            <div class="chat-message">{{ item.content }}</div>
          </div>
        </div>
        <div v-if="!isLoggedIn" class="chat-limit">
          游客剩余 {{ remainingCount }} 次，登录后不限。
        </div>
        <div class="chat-input">
          <el-input
            v-model="input"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 4 }"
            placeholder="请输入中山市旅游相关问题（Ctrl+Enter 发送）"
            maxlength="200"
            clearable
            @keydown.native="handleKeydown"
          ></el-input>
          <el-button type="primary" :loading="sending" @click="sendMessage">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-assistant {
  position: fixed;
  right: 28px;
  bottom: 28px;
  z-index: 60;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.chat-bubble {
  background: #ffffff;
  color: #1c2c27;
  padding: 10px 14px;
  border-radius: 16px;
  box-shadow: 0 12px 28px rgba(31, 56, 94, 0.18);
  font-size: 12px;
  max-width: 180px;
}

.chat-button {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: #c75a31;
  color: #fff;
  font-size: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16px 30px rgba(31, 56, 94, 0.2);
  cursor: pointer;
}

.chat-button:hover {
  background: #a2472b;
}

.chat-panel {
  position: fixed;
  right: 24px;
  bottom: 96px;
  width: 380px;
  height: 560px;
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 20px 40px rgba(31, 56, 94, 0.2);
  border: 1px solid rgba(31, 56, 94, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 60;
}

.chat-header {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, rgba(31, 122, 109, 0.95), rgba(199, 90, 49, 0.92));
  color: #ffffff;
}

.chat-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-title span {
  font-size: 14px;
  font-weight: 600;
}

.chat-title small {
  font-size: 12px;
  opacity: 0.85;
}

.chat-close {
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
}

.chat-close:hover {
  background: rgba(255, 255, 255, 0.3);
}

.chat-window {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px 16px 16px;
  flex: 1;
  min-height: 0;
}

.chat-body {
  background: #f7f4ef;
  border-radius: 12px;
  padding: 14px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  min-height: 220px;
}

.chat-row {
  display: flex;
  margin-bottom: 10px;
}

.chat-row.user {
  justify-content: flex-end;
}

.chat-message {
  max-width: 240px;
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
  background: #ffffff;
  color: #1c2c27;
  box-shadow: 0 8px 18px rgba(31, 56, 94, 0.08);
}

.chat-row.user .chat-message {
  background: #1f7a6d;
  color: #ffffff;
}

.chat-limit {
  font-size: 12px;
  color: #7b8a83;
}

.chat-suggest {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggest-title {
  font-size: 12px;
  color: #7b8a83;
}

.suggest-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggest-item {
  border: 1px solid rgba(31, 122, 109, 0.2);
  background: #f5faf8;
  color: #1f7a6d;
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  cursor: pointer;
}

.suggest-item:hover {
  background: #e6f4f0;
}

.chat-input {
  display: grid;
  grid-template-columns: 1fr 84px;
  gap: 8px;
  align-items: end;
  background: #f6f8f7;
  border-radius: 12px;
  padding: 8px;
}

.chat-input ::v-deep .el-textarea__inner {
  border-radius: 10px;
  border: 1px solid rgba(31, 56, 94, 0.12);
  font-size: 13px;
  line-height: 1.6;
}

.chat-input ::v-deep .el-textarea__inner:focus {
  border-color: #1f7a6d;
}

@media (max-width: 768px) {
  .chat-panel {
    width: calc(100vw - 32px);
    right: 16px;
    bottom: 92px;
  }
}
</style>
