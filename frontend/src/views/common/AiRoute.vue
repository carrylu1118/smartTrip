<template>
  <div class="ai-route-page">
    <van-nav-bar title="智能问路" left-arrow @click-left="router.back" fixed>
      <template #right>
        <van-icon name="comment-o" size="20" color="#666" @click="showConvSheet = true" />
      </template>
    </van-nav-bar>

    <!-- 会话管理弹出层 -->
    <van-action-sheet
      v-model:show="showConvSheet"
      title="会话管理"
      :actions="convActions"
      cancel-text="关闭"
      @select="onConvSelect"
    />

    <!-- 聊天消息区 -->
    <div ref="chatList" class="chat-list">
      <div v-if="messages.length === 0 && !loading" class="empty-hint">
        <div class="empty-icon">🤖</div>
        <div class="empty-text">你好！我是智驾游AI助手</div>
        <div class="empty-sub">可以问我路线规划、出行建议等问题</div>
      </div>

      <div v-if="loading" class="loading-hint">
        <van-loading size="24" />
        <span style="margin-left:8px;color:#999">加载中...</span>
      </div>

      <template v-for="(msg, idx) in messages" :key="idx">
        <div
          v-if="idx === 0 || messages[idx - 1].conversationId !== msg.conversationId"
          class="conv-divider"
        >
          <span>{{ formatConvTime(msg.createdTime) }}</span>
        </div>

        <div :class="['chat-bubble', isUser(msg.role) ? 'bubble-user' : 'bubble-ai']">
          <div v-if="!isUser(msg.role)" class="avatar-ai">🤖</div>
          <div v-if="isUser(msg.role)" class="bubble-content">{{ msg.content }}</div>
          <div v-else class="bubble-content" v-html="renderMd(msg.content)"></div>
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
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import * as aiApi from '@/api/ai'

const router = useRouter()

const CONVERSATION_ID_KEY = 'AI_CURRENT_CONVERSATION_ID'
const conversationId = ref(sessionStorage.getItem(CONVERSATION_ID_KEY) || '')
const messages = ref([])
const inputText = ref('')
const sending = ref(false)
const loading = ref(false)
const chatList = ref(null)
const showConvSheet = ref(false)
const convList = ref([])

function generateConversationId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `conv-${Date.now()}-${Math.random().toString(36).slice(2, 12)}`
}

function setConversationId(cid) {
  conversationId.value = cid
  if (cid) {
    sessionStorage.setItem(CONVERSATION_ID_KEY, cid)
  } else {
    sessionStorage.removeItem(CONVERSATION_ID_KEY)
  }
}

function createNewConversation() {
  setConversationId(generateConversationId())
  messages.value = []
}

// 会话列表，动态构建 action sheet 选项
const convActions = computed(() => {
  const isNew = !conversationId.value
  const items = [{
    name: '创建新对话',
    color: isNew ? 'var(--color-primary)' : undefined,
    cid: ''
  }]
  convList.value.forEach(conv => {
    const cid = conv.conversationId || conv.conversation_id || ''
    const time = conv.lastTime || conv.last_time || ''
    const active = cid === conversationId.value
    items.push({
      name: (active ? '✓ ' : '') + (time ? time.substring(0, 16) : '未知时间'),
      color: active ? 'var(--color-primary)' : undefined,
      cid: cid
    })
  })
  return items
})

function isUser(role) { return role === 'user' }

function renderMd(text) {
  if (!text) return ''
  let html = text
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')
  html = html.replace(/^### (.+)$/gm, '<h4>$1</h4>')
  html = html.replace(/^## (.+)$/gm, '<h3>$1</h3>')
  html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
  html = html.replace(/^\d+\. (.+)$/gm, '<li>$1</li>')
  html = html.replace(/((?:<li>.*?<\/li>\n?)+)/g, '<ul>$1</ul>')
  html = html.replace(/\n\n/g, '<br><br>')
  html = html.replace(/\n/g, '<br>')
  return html
}

function formatConvTime(t) {
  if (!t) return ''
  const s = String(t)
  return s.substring(0, 10)
}

function scrollToBottom() {
  nextTick(() => {
    if (chatList.value) {
      chatList.value.scrollTop = chatList.value.scrollHeight
    }
  })
}

// 加载会话列表
async function loadConvList() {
  try {
    const res = await aiApi.getConversations()
    if (res && res.code === 200 && res.data) {
      convList.value = Array.isArray(res.data) ? res.data : []
    }
  } catch { /* ignore */ }
}

// 加载指定会话的历史消息
async function loadHistory(cid) {
  loading.value = true
  try {
    const res = await aiApi.getHistory(cid)
    if (res && res.code === 200 && res.data) {
      const list = Array.isArray(res.data) ? res.data : []
      // 后端返回时间升序，无需反转
      messages.value = list.map(m => ({
        ...m,
        role: m.role === 'user' ? 'user' : 'assistant',
        createdTime: m.createdTime || m.time
      }))
      scrollToBottom()
    }
  } catch {
    messages.value = []
  } finally {
    loading.value = false
  }
}

// 会话选择处理
function onConvSelect(action) {
  showConvSheet.value = false
  if (action.name === '创建新对话') {
    // 前端先生成会话 ID，确保本轮所有消息始终归属同一会话
    createNewConversation()
  } else {
    // 切换到旧会话
    setConversationId(action.cid)
    loadHistory(action.cid)
  }
}

async function send() {
  const text = inputText.value.trim()
  if (!text || sending.value) return

  // 首次没有历史会话时也必须由前端创建 ID，禁止向后端发送空 conversationId
  if (!conversationId.value) {
    setConversationId(generateConversationId())
  }

  messages.value.push({ role: 'user', content: text, conversationId: conversationId.value })
  inputText.value = ''
  scrollToBottom()

  messages.value.push({ role: 'assistant', content: '', conversationId: conversationId.value })
  const aiIdx = messages.value.length - 1
  sending.value = true

  aiApi.sendMessageStream(
    { conversationId: conversationId.value, message: text },
    (token) => {
      messages.value[aiIdx].content += token
      scrollToBottom()
    },
    () => {
      if (!messages.value[aiIdx].content) {
        messages.value[aiIdx].content = '(空回复)'
      }
      sending.value = false
      scrollToBottom()
      // 刷新会话列表（可能有新的 conversationId）
      loadConvList()
    },
    () => {
      messages.value[aiIdx].content = messages.value[aiIdx].content || '网络异常，请检查连接后重试。'
      sending.value = false
      scrollToBottom()
    }
  )
}

onMounted(async () => {
  await loadConvList()

  // 优先恢复本标签页暂存的会话；没有暂存时再加载最近一次历史会话
  if (conversationId.value) {
    await loadHistory(conversationId.value)
  } else if (convList.value.length > 0) {
    const first = convList.value[0]
    const cid = first.conversationId || first.conversation_id || ''
    if (cid) {
      setConversationId(cid)
      await loadHistory(cid)
    }
  }
})
</script>

<style scoped>
.ai-route-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f0f0;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 56px 12px 12px;
  -webkit-overflow-scrolling: touch;
}

.empty-hint { text-align: center; margin-top: 80px; }
.empty-icon { font-size: 56px; margin-bottom: 16px; }
.empty-text { font-size: 18px; font-weight: 600; color: #1A1A2E; margin-bottom: 6px; }
.empty-sub { font-size: 13px; color: #9CA3AF; }

.loading-hint { display: flex; align-items: center; justify-content: center; margin-top: 60px; }

.conv-divider { text-align: center; margin: 20px 0 12px; }
.conv-divider span {
  display: inline-block; padding: 4px 14px; font-size: 12px; color: #999;
  background: #e8e8e8; border-radius: 10px;
}

.chat-bubble { display: flex; align-items: flex-start; margin-bottom: 16px; gap: 8px; }
.bubble-ai { justify-content: flex-start; }
.bubble-user { justify-content: flex-end; }

.avatar-ai, .avatar-user { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; }
.avatar-ai { background: #E8F5E9; }
.avatar-user { background: #FFF3ED; }

.bubble-content { max-width: 70%; padding: 10px 14px; border-radius: 16px; font-size: 15px; line-height: 1.6; word-break: break-word; }
.bubble-ai .bubble-content { background: #fff; color: #1A1A2E; border-bottom-left-radius: 4px; box-shadow: 0 1px 2px rgba(0,0,0,0.06); }
.bubble-ai .bubble-content :deep(strong) { font-weight: 700; }
.bubble-ai .bubble-content :deep(h3) { font-size: 15px; font-weight: 700; margin: 8px 0 4px; }
.bubble-ai .bubble-content :deep(h4) { font-size: 14px; font-weight: 700; margin: 6px 0 2px; }
.bubble-ai .bubble-content :deep(ul) { padding-left: 16px; margin: 4px 0; }
.bubble-ai .bubble-content :deep(li) { margin: 2px 0; }
.bubble-ai .bubble-content :deep(code) { background: rgba(0,0,0,0.06); padding: 1px 5px; border-radius: 4px; font-size: 13px; font-family: monospace; }
.bubble-user .bubble-content { background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light)); color: #fff; border-bottom-right-radius: 4px; }

.input-bar { display: flex; align-items: center; gap: 8px; padding: 8px 12px; background: #fff; border-top: 1px solid #eee; padding-bottom: calc(8px + env(safe-area-inset-bottom)); }
.input-bar .input-field { flex: 1; height: 40px; border-radius: 20px; background: #f5f5f5; border: 1px solid #EBEDF0; padding: 0 16px; font-size: 14px; outline: none; transition: border-color 0.2s; }
.input-bar .input-field:focus { border-color: var(--color-primary); }
.send-btn { height: 40px; padding: 0 20px; border: none; border-radius: 20px; background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light)); color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; white-space: nowrap; flex-shrink: 0; }
.send-btn:disabled { opacity: 0.4; }
</style>
