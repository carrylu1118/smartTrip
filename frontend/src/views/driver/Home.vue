<template>
  <div class="page">
    <AppHeader title="司机端 - 黑马顺风车" @click-right="onSwitchRole">
      <template #right><van-icon name="logistics" size="20" color="#FF6B35" /></template>
    </AppHeader>

    <div class="content">
      <div class="card">
        <div class="form-item">
          <label class="form-label">出发时间</label>
          <input v-model="form.departureTime" type="datetime-local" class="input-field" />
        </div>
        <div class="form-item">
          <label class="form-label">出发地</label>
          <input :value="form.startAddr" readonly class="input-field" placeholder="点击选择出发地" @click="goMap('f')" />
        </div>
        <div class="form-item">
          <label class="form-label">目的地</label>
          <input :value="form.endAddr" readonly class="input-field" placeholder="点击选择目的地" @click="goMap('t')" />
        </div>
        <div class="form-item">
          <label class="form-label">座位数: {{ form.quantity }}</label>
          <input v-model.number="form.quantity" type="range" min="1" max="5" step="1" class="range-input" />
        </div>
      </div>

      <div class="btn-wrap">
        <button class="btn-primary" :disabled="submitting" @click="onPublish">
          {{ submitting ? '发布中...' : '发布行程' }}
        </button>
      </div>
    </div>

    <TabBar />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAuthStore } from '@/stores/auth'
import * as strokeApi from '@/api/stroke'
import AppHeader from '@/components/AppHeader.vue'
import TabBar from '@/components/TabBar.vue'

const router = useRouter()
const auth = useAuthStore()
const submitting = ref(false)

const form = reactive({
  departureTime: '',
  startAddr: '', startGeoLng: '', startGeoLat: '',
  endAddr: '', endGeoLng: '', endGeoLat: '',
  quantity: 3,
})

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

onMounted(() => { loadFromStorage() })

function goMap(type) {
  localStorage.setItem('time', form.departureTime)
  router.push(`/map?type=${type}`)
}

function onSwitchRole() { auth.setRole(0); router.push('/passenger/home') }

async function onPublish() {
  if (!form.departureTime) return showToast('请选择出发时间')
  if (!form.startAddr) return showToast('请选择出发地')
  if (!form.endAddr) return showToast('请选择目的地')

  submitting.value = true
  try {
    const res = await strokeApi.publish({
      departureTime: form.departureTime + ':00.000+0000',
      startAddr: form.startAddr, startGeoLng: form.startGeoLng, startGeoLat: form.startGeoLat,
      endAddr: form.endAddr, endGeoLng: form.endGeoLng, endGeoLat: form.endGeoLat,
      quantity: form.quantity,
      role: 1, status: 0,
    })
    if (res.code === 200 || res.code === 0) {
      showToast('行程发布成功')
      localStorage.removeItem('from'); localStorage.removeItem('to'); localStorage.removeItem('time')
      router.push('/driver/trip')
    } else {
      showToast(res.message || res.msg || '发布失败')
    }
  } catch {
    showToast('发布失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #F2F4F8; padding-bottom: 60px; }
.content { padding: 12px; }
.form-item { margin-bottom: 14px; }
.form-label { display: block; margin-bottom: 4px; font-size: 14px; font-weight: 600; color: #1A1A2E; }
.form-item .input-field { margin-top: 0; }
.range-input { width: 100%; accent-color: #FF6B35; }
.btn-wrap { padding: 16px 0; }
.switch-text { font-size: 14px; color: #FF6B35; font-weight: 500; }
</style>
