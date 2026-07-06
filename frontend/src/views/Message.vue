<template>
  <div class="message-page">
    <van-nav-bar title="即时消息" left-arrow @click-left="router.back" fixed />

    <!-- 消息列表 -->
    <div ref="msgList" class="msg-list">
      <div v-if="messages.length === 0" class="empty-hint">暂无消息，发送一条吧~</div>
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        :class="['msg-bubble', msg.senderId === currentUserId ? 'msg-mine' : 'msg-other']"
      >
        <div class="msg-text">{{ msg.message || msg.content }}</div>
      </div>
    </div>

    <!-- 底部输入区 -->
    <div class="input-bar">
      <input
        v-model="text"
        class="input-field"
        placeholder="输入消息..."
        @keyup.enter="send"
      />
      <button class="send-btn" :disabled="!text.trim()" @click="send">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import * as noticeApi from '@/api/notice'
import { addNoticeMsg } from '@/composables/useNotice'

const router = useRouter()
const route = useRoute()

const receiverId = route.query.r || ''
const tripId = route.query.t || ''
const currentUserId = localStorage.getItem('SESSION_TOKEN_KEY') || ''

const messages = ref([])
const text = ref('')
const msgList = ref(null)

let ws = null

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (msgList.value) {
      msgList.value.scrollTop = msgList.value.scrollHeight
    }
  })
}

// 加载历史消息
async function loadHistory() {
  try {
    const res = await noticeApi.list({ receiverId, tripId })
    if (res && res.code === 200 && res.data) {
      messages.value = Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || [])
    } else if (Array.isArray(res)) {
      messages.value = res
    }
    scrollToBottom()
  } catch (err) {
    console.error('Load history error:', err)
  }
}

// 初始化 WebSocket
function initWebSocket() {
  const token = localStorage.getItem('SESSION_TOKEN_KEY')
  if (!token) { console.warn('WebSocket: no token'); return }

  // 开发环境连后端端口9999，生产环境同host
  let host = window.location.host
  if (host.includes(':3000') || host === 'localhost:3000') {
    host = window.location.hostname + ':9999'
  }
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const url = `${protocol}//${host}/notice/ws/socket?SESSION_TOKEN_KEY=${token}`
  console.log('WebSocket connecting to:', url)

  ws = new WebSocket(url)
  ws.onopen = () => {
    console.log('WebSocket connected')
  }
  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      // 同步到全局消息列表
      addNoticeMsg(data)
      // 只追加属于当前会话的消息（对方发来的）
      if (
        data &&
        data.senderId !== currentUserId &&
        String(data.receiverId || '') === String(currentUserId)
      ) {
        messages.value.push(data)
        scrollToBottom()
      }
    } catch {
      // 非 JSON 消息忽略
    }
  }
  ws.onclose = () => {
    console.log('WebSocket closed')
  }
  ws.onerror = (err) => {
    console.error('WebSocket error:', err)
  }
}

// 发送消息
function send() {
  const msg = text.value.trim()
  if (!msg) return
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    // 尝试重连后发送
    initWebSocket()
    setTimeout(() => {
      if (ws && ws.readyState === WebSocket.OPEN) {
        doSend(msg)
      }
    }, 500)
    return
  }
  doSend(msg)
}

function doSend(msg) {
  const payload = JSON.stringify({
    receiverId,
    tripId,
    message: msg,
  })
  ws.send(payload)

  // 本地追加自己发送的消息
  messages.value.push({
    senderId: currentUserId,
    receiverId,
    tripId,
    message: msg,
    timestamp: new Date().toISOString(),
  })
  text.value = ''
  scrollToBottom()
}

onMounted(() => {
  loadHistory()
  initWebSocket()
})

onBeforeUnmount(() => {
  if (ws) {
    ws.close()
    ws = null
  }
})
</script>

<style scoped>
.message-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f0f0;
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 56px 12px 12px;
  -webkit-overflow-scrolling: touch;
}

.empty-hint {
  text-align: center;
  color: var(--text-hint);
  font-size: 14px;
  margin-top: 60px;
}

.msg-bubble {
  max-width: 75%;
  margin-bottom: 12px;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 15px;
  line-height: 1.5;
  word-break: break-word;
}

.msg-mine {
  margin-left: auto;
  background: linear-gradient(135deg, #FF6B35, #FF8A5C);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-other {
  margin-right: auto;
  background: #fff;
  color: var(--text-primary);
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
}

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
  height: 38px;
  border-radius: 19px;
  background: #f5f5f5;
  border: none;
  padding: 0 16px;
}

.send-btn {
  height: 38px;
  padding: 0 18px;
  border: none;
  border-radius: 19px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
}
.send-btn:disabled {
  opacity: 0.4;
}
</style>
