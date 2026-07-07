<template>
  <div class="ai-route-page">
    <van-nav-bar title="智能问路" left-arrow @click-left="router.back" fixed>
      <template #right>
        <van-icon name="clock-o" size="20" color="#666" @click="loadHistory" />
      </template>
    </van-nav-bar>

    <!-- 聊天消息区 -->
    <div ref="chatList" class="chat-list">
      <div v-if="messages.length === 0 && !loading" class="empty-hint">
        <div class="empty-icon">🤖</div>
        <div class="empty-text">你好！我是智驾游AI助手</div>
        <div class="empty-sub">可以问我路线规划、出行建议等问题</div>
      </div>

      <div v-if="loading" class="loading-hint">
        <van-loading size="24" />
        <span style="margin-left:8px;color:#999">加载历史记录...</span>
      </div>

      <template v-for="(msg, idx) in messages" :key="idx">
        <!-- 会话切换分隔线 -->
        <div
          v-if="idx === 0 || messages[idx - 1].conversationId !== msg.conversationId"
          class="conv-divider"
        >
          <span>{{ formatConvTime(msg.createdTime) }}</span>
        </div>

        <div :class="['chat-bubble', isUser(msg.role) ? 'bubble-user' : 'bubble-ai']">
          <div v-if="!isUser(msg.role)" class="avatar-ai">🤖</div>
          <div class="bubble-content">{{ msg.content }}</div>
          <div v-if="isUser(msg.role)" class="avatar-user">👤</div>
        </div>
      </template>
    </div>

    <!-- 底部输入区 -->
    <div class="input-bar">
      <input
        v-model="inputText"
        class="input-field"
        placeholder="输入你的问题..."
        @keyup.enter="send"
      />
      <button class="send-btn" :disabled="!inputText.trim() || sending" @click="send">
        {{ sending ? '...' : '发送' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import * as aiApi from '@/api/ai'

const router = useRouter()

const conversationId = ref('')
const messages = ref([])
const inputText = ref('')
const sending = ref(false)
const loading = ref(false)
const chatList = ref(null)

function isUser(role) {
  return role === 'user'
}

function formatConvTime(t) {
  if (!t) return ''
  // 取日期部分
  const s = String(t)
  const date = s.substring(0, 10)
  const now = new Date()
  const today = now.toISOString().substring(0, 10)
  return date === today ? '今天' : date
}

function scrollToBottom() {
  nextTick(() => {
    if (chatList.value) {
      chatList.value.scrollTop = chatList.value.scrollHeight
    }
  })
}

// 加载全部历史记录
async function loadHistory() {
  loading.value = true
  try {
    const res = await aiApi.getAllMessages()
    if (res && res.code === 200 && res.data && res.data.length > 0) {
      // 后端返回倒序（最新在前），反转为正序显示
      const list = [...res.data].reverse()
      messages.value = list
      // 定位到最后一个会话
      if (list.length > 0) {
        conversationId.value = list[list.length - 1].conversationId || ''
      }
      scrollToBottom()
    }
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

async function send() {
  const text = inputText.value.trim()
  if (!text || sending.value) return

  messages.value.push({ role: 'user', content: text, conversationId: conversationId.value })
  inputText.value = ''
  scrollToBottom()

  // 先插入占位 AI 气泡，流式填充
  const aiMsg = { role: 'assistant', content: '', conversationId: conversationId.value }
  messages.value.push(aiMsg)
  sending.value = true

  aiApi.sendMessageStream(
    { conversationId: conversationId.value, message: text },
    // onToken: 逐字追加
    (token) => {
      aiMsg.content += token
      scrollToBottom()
    },
    // onDone: 流结束
    (newCid) => {
      if (newCid) conversationId.value = newCid
      aiMsg.conversationId = conversationId.value
      if (!aiMsg.content) aiMsg.content = '(空回复)'
      sending.value = false
      scrollToBottom()
    },
    // onError
    () => {
      aiMsg.content = aiMsg.content || '网络异常，请检查连接后重试。'
      sending.value = false
      scrollToBottom()
    }
  )
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.ai-route-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f0f0;
}

/* 聊天列表 */
.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 56px 12px 12px;
  -webkit-overflow-scrolling: touch;
}

.empty-hint {
  text-align: center;
  margin-top: 80px;
}
.empty-icon {
  font-size: 56px;
  margin-bottom: 16px;
}
.empty-text {
  font-size: 18px;
  font-weight: 600;
  color: #1A1A2E;
  margin-bottom: 6px;
}
.empty-sub {
  font-size: 13px;
  color: #9CA3AF;
}

.loading-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 60px;
}

/* 会话分隔线 */
.conv-divider {
  text-align: center;
  margin: 20px 0 12px;
}
.conv-divider span {
  display: inline-block;
  padding: 4px 14px;
  font-size: 12px;
  color: #999;
  background: #e8e8e8;
  border-radius: 10px;
}

/* 聊天气泡 */
.chat-bubble {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 8px;
}
.bubble-ai {
  justify-content: flex-start;
}
.bubble-user {
  justify-content: flex-end;
}

.avatar-ai,
.avatar-user {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.avatar-ai {
  background: #E8F5E9;
}
.avatar-user {
  background: #FFF3ED;
}

.bubble-content {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 15px;
  line-height: 1.6;
  word-break: break-word;
}
.bubble-ai .bubble-content {
  background: #fff;
  color: #1A1A2E;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
}
.bubble-user .bubble-content {
  background: linear-gradient(135deg, #FF6B35, #FF8A5C);
  color: #fff;
  border-bottom-right-radius: 4px;
}

/* 输入栏 */
.input-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fff;
  border-top: 1px solid #eee;
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
}
.input-bar .input-field {
  flex: 1;
  height: 40px;
  border-radius: 20px;
  background: #f5f5f5;
  border: 1px solid #EBEDF0;
  padding: 0 16px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}
.input-bar .input-field:focus {
  border-color: #FF6B35;
}
.send-btn {
  height: 40px;
  padding: 0 20px;
  border: none;
  border-radius: 20px;
  background: linear-gradient(135deg, #FF6B35, #FF8A5C);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
}
.send-btn:disabled {
  opacity: 0.4;
}
</style>
