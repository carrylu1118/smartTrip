<template>
  <div class="tabbar-wrapper">
    <van-tabbar :model-value="active" :fixed="true" :safe-area-inset-bottom="true" active-color="#FF6B35">
      <van-tabbar-item
        v-for="item in tabs"
        :key="item.name"
        :icon="item.icon"
        @click="router.push(item.to)"
      >
        {{ item.label }}
      </van-tabbar-item>
    </van-tabbar>

    <!-- 居中圆形发布按钮 -->
    <div class="center-btn" @click="openPublish">
      <van-icon name="plus" size="26" color="#fff" />
    </div>

    <!-- 发布行程浮窗 -->
    <van-popup
      v-model:show="showPopup"
      position="bottom"
      round
      :style="{ height: 'auto', maxHeight: '85vh' }"
      teleport="body"
    >
      <div class="popup-content">
        <div class="popup-header">
          <span class="popup-title">发布行程</span>
          <van-icon name="cross" size="20" color="#999" @click="closePublish" />
        </div>

        <div class="popup-body">
          <div class="form-item">
            <label class="form-label">出发时间</label>
            <input v-model="form.departureTime" type="datetime-local" class="input-field" />
          </div>
          <div class="form-item">
            <label class="form-label">出发地</label>
            <input
              :value="form.startAddr"
              readonly
              class="input-field"
              placeholder="点击选择出发地"
              @click="goMap('f')"
            />
          </div>
          <div class="form-item">
            <label class="form-label">目的地</label>
            <input
              :value="form.endAddr"
              readonly
              class="input-field"
              placeholder="点击选择目的地"
              @click="goMap('t')"
            />
          </div>

          <!-- 司机端：座位数 -->
          <div v-if="isDriver" class="form-item">
            <label class="form-label">座位数：{{ form.quantity }}</label>
            <input
              v-model.number="form.quantity"
              type="range"
              min="1"
              max="5"
              step="1"
              class="range-input"
            />
          </div>

          <!-- 乘客端：闪电确认 -->
          <div v-if="!isDriver" class="form-item flex-between">
            <label class="form-label">闪电确认</label>
            <van-switch v-model="form.quickConfirm" size="26" active-color="#FF6B35" />
          </div>

          <button class="btn-primary" :disabled="publishing" @click="onPublish">
            {{ publishing ? '发布中...' : '发布行程' }}
          </button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAuthStore } from '@/stores/auth'
import * as strokeApi from '@/api/stroke'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const role = computed(() => (auth.currentRole == 0 ? 'passenger' : 'driver'))
const isDriver = computed(() => auth.currentRole === 1)

const tabs = computed(() => [
  { name: 'home',  label: '首页', icon: 'home-o',   to: `/${role.value}/home` },
  { name: 'trip',  label: '行程', icon: 'orders-o', to: `/${role.value}/trip` },
  { name: 'order', label: '订单', icon: 'bill-o',   to: `/${role.value}/order` },
  { name: 'center',label: '我的', icon: 'user-o',   to: '/common/center' },
])

const active = computed(() => {
  const path = route.path
  if (path.startsWith('/common/')) return 3
  const m = tabs.value.findIndex(t => path.startsWith(t.to))
  return m >= 0 ? m : 0
})

// ---- 发布浮窗 ----
const PUBLISHING_FLAG = 'publishing_trip'
const showPopup = ref(false)

const form = reactive({
  departureTime: '',
  startAddr: '',
  startGeoLng: '',
  startGeoLat: '',
  endAddr: '',
  endGeoLng: '',
  endGeoLat: '',
  quantity: 3,
  quickConfirm: false,
})

const publishing = ref(false)

function parseLocation(raw, addrField, lngField, latField) {
  if (!raw) return
  const parts = raw.split('#')
  form[addrField] = parts[0] || ''
  if (parts[1]) {
    const coords = parts[1].split(',')
    form[lngField] = coords[0] || ''
    form[latField] = coords[1] || ''
  }
}

function loadFromStorage() {
  const f = localStorage.getItem('from')
  const t = localStorage.getItem('to')
  const time = localStorage.getItem('time')
  if (f) parseLocation(f, 'startAddr', 'startGeoLng', 'startGeoLat')
  if (t) parseLocation(t, 'endAddr', 'endGeoLng', 'endGeoLat')
  if (time) form.departureTime = time
}

function openPublish() {
  // 重置为当前角色的默认值
  form.quantity = isDriver.value ? 3 : 1
  form.quickConfirm = false
  loadFromStorage()
  localStorage.setItem(PUBLISHING_FLAG, '1')
  showPopup.value = true
}

function closePublish() {
  showPopup.value = false
  localStorage.removeItem(PUBLISHING_FLAG)
}

function goMap(type) {
  localStorage.setItem('time', form.departureTime)
  router.push(`/map?type=${type}`)
}

// 组件挂载时（如从地图页 router.back() 返回）恢复浮窗状态
onMounted(() => {
  if (localStorage.getItem(PUBLISHING_FLAG) === '1') {
    loadFromStorage()
    showPopup.value = true
  }
})

// 路由切换：离开首页时关闭浮窗，从其他Tab切回首页时自动重开
watch(() => route.path, (path) => {
  const isHome = path.startsWith(`/${role.value}/home`)
  if (isHome && localStorage.getItem(PUBLISHING_FLAG) === '1') {
    loadFromStorage()
    showPopup.value = true
  } else if (!isHome && showPopup.value) {
    showPopup.value = false
  }
})

async function onPublish() {
  if (!form.departureTime) return showToast('请选择出发时间')
  if (!form.startAddr) return showToast('请选择出发地')
  if (!form.endAddr) return showToast('请选择目的地')

  publishing.value = true
  try {
    const res = await strokeApi.publish({
      departureTime: form.departureTime + ':00.000+0000',
      startAddr: form.startAddr,
      startGeoLng: form.startGeoLng,
      startGeoLat: form.startGeoLat,
      endAddr: form.endAddr,
      endGeoLng: form.endGeoLng,
      endGeoLat: form.endGeoLat,
      quantity: form.quantity,
      quickConfirm: form.quickConfirm ? 1 : 0,
      role: isDriver.value ? 1 : 0,
      status: 0,
    })
    if (res.code === 200 || res.code === 0) {
      showToast('行程发布成功')
      localStorage.removeItem('from')
      localStorage.removeItem('to')
      localStorage.removeItem('time')
      localStorage.removeItem(PUBLISHING_FLAG)
      showPopup.value = false
      // 用 query 参数强制触发路由变化，确保行程列表重新加载
      router.replace({ path: `/${role.value}/trip`, query: { _t: Date.now() } })
    } else {
      showToast(res.message || res.msg || '发布失败')
    }
  } catch {
    showToast('发布失败，请重试')
  } finally {
    publishing.value = false
  }
}
</script>

<style scoped>
.tabbar-wrapper {
  position: relative;
}

/* 居中圆形按钮 */
.center-btn {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35, #FF8A5C);
  box-shadow: 0 4px 14px rgba(255, 107, 53, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}
.center-btn:active {
  transform: translateX(-50%) scale(0.92);
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
}

/* 浮窗 */
.popup-content {
  display: flex;
  flex-direction: column;
  max-height: 85vh;
}
.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 12px;
  border-bottom: 1px solid #f0f0f0;
}
.popup-title {
  font-size: 18px;
  font-weight: 700;
  color: #1A1A2E;
}
.popup-body {
  padding: 16px 20px 24px;
  overflow-y: auto;
}

/* 表单 */
.form-item {
  margin-bottom: 16px;
}
.form-label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #1A1A2E;
}
.form-item .input-field {
  margin-top: 0;
}
.range-input {
  width: 100%;
  accent-color: #FF6B35;
}
</style>
