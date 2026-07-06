import { ref } from 'vue'
import { showToast } from 'vant'

// ---- 单例状态 ----
const messages = ref([])
const unreadCount = ref(0)
let ws = null
let reconnectTimer = null

// ---- 内部 ----
function persist() {
  try {
    localStorage.setItem('_msg', JSON.stringify(messages.value))
  } catch { /* ignore */ }
}

// ---- 对外 API ----

/**
 * 添加一条消息到全局列表（不弹 toast）
 * 供 Message.vue 聊天页收到 WebSocket 消息时同步调用
 */
export function addNoticeMsg(data) {
  if (!data) return

  const clean = { ...data }
  delete clean.read
  delete clean.receiverUseralias
  delete clean.receiverId
  delete clean.retripIdad
  delete clean.vO
  delete clean.tripId
  delete clean.createdTime

  const dup = messages.value.find(
    m => m.senderId === clean.senderId && m.message === clean.message
  )
  if (!dup) {
    messages.value.push(clean)
    unreadCount.value = messages.value.length
    persist()
  }
}

/**
 * 初始化全局 WebSocket（登录后调用）
 */
export function initNoticeSocket() {
  const token = localStorage.getItem('SESSION_TOKEN_KEY')
  if (!token) return

  // 从 localStorage 恢复已有消息
  try {
    const raw = localStorage.getItem('_msg')
    if (raw) {
      messages.value = JSON.parse(raw)
      unreadCount.value = messages.value.length
    }
  } catch {
    messages.value = []
  }

  let host = window.location.host
  if (host.includes(':3000') || host === 'localhost:3000') {
    host = window.location.hostname + ':9999'
  }
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const url = `${protocol}//${host}/notice/ws/socket?SESSION_TOKEN_KEY=${token}`

  ws = new WebSocket(url)

  ws.onopen = () => console.log('[notice] WebSocket connected')

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      addNoticeMsg(data)
      showToast({
        message: '您有新消息，请注意查看！',
        duration: 2000,
        position: 'top',
      })
    } catch {
      // 非 JSON 消息忽略
    }
  }

  ws.onclose = () => {
    console.log('[notice] WebSocket closed, reconnecting in 5s...')
    reconnectTimer = setTimeout(() => initNoticeSocket(), 5000)
  }

  ws.onerror = (err) => console.error('[notice] WebSocket error:', err)
}

/**
 * 关闭 WebSocket（登出时调用）
 */
export function closeNoticeSocket() {
  if (reconnectTimer) clearTimeout(reconnectTimer)
  if (ws) {
    ws.onclose = null
    ws.close()
    ws = null
  }
}

/**
 * 删除一条消息
 */
export function removeNoticeMsg(index) {
  messages.value.splice(index, 1)
  unreadCount.value = messages.value.length
  persist()
}

/** 清空所有消息 */
export function clearNoticeMsg() {
  messages.value = []
  unreadCount.value = 0
  localStorage.removeItem('_msg')
}

/** 响应式消息列表 */
export function useNoticeMessages() {
  return { messages, unreadCount }
}
