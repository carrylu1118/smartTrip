import api from './index'

/**
 * AI 聊天 API
 */

// 发送消息
export function sendMessage(data) {
  return api.post('/ai/chat', data)
}

// 获取会话历史
export function getHistory(conversationId) {
  return api.get(`/ai/chat/history/${conversationId}`)
}

// 获取用户会话列表
export function getConversations() {
  return api.get('/ai/chat/conversations')
}

// 获取用户全部聊天记录（跨会话，时间倒序）
export function getAllMessages() {
  return api.get('/ai/chat/messages')
}

/**
 * 流式发送消息 (SSE)
 * @param {Object} data - { conversationId, message }
 * @param {Function} onToken - 每个 token 回调
 * @param {Function} onDone  - 流结束回调 (conversationId)
 * @param {Function} onError - 错误回调
 * @returns {Function} cancel - 取消请求
 */
export function sendMessageStream(data, onToken, onDone, onError) {
  const token = localStorage.getItem('SESSION_TOKEN_KEY')
  const controller = new AbortController()

  fetch('/ai/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'SESSION_TOKEN_KEY': token || '',
    },
    body: JSON.stringify(data),
    signal: controller.signal,
  }).then(async (response) => {
    if (!response.ok) {
      onError(new Error('HTTP ' + response.status))
      return
    }
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      let eventName = ''
      for (const line of lines) {
        if (line.startsWith('event:')) {
          eventName = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          const content = line.slice(5)
          if (eventName === 'done') {
            try {
              const meta = JSON.parse(content)
              onDone(meta.conversationId)
            } catch { onDone('') }
          } else if (content.startsWith('[DONE:')) {
            const cid = content.slice(6, -1)
            onDone(cid)
          } else {
            onToken(content)
          }
          eventName = ''
        }
      }
    }
  }).catch((err) => {
    if (err.name !== 'AbortError') {
      onError(err)
    }
  })

  return () => controller.abort()
}
