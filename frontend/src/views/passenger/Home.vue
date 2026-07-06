<template>
  <div class="page-container">
    <AppHeader title="乘客端 - 黑马顺风车" :show-back="false" @click-right="onSwitchRole">
      <template #right><van-icon name="user-o" size="20" color="#FF6B35" /></template>
    </AppHeader>

    <div class="card" style="margin-top: 16px">
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
      <div class="form-item flex-between">
        <label class="form-label">闪电确认</label>
        <van-switch v-model="form.quickConfirm" size="26" active-color="#FF6B35" />
      </div>
    </div>

    <div style="padding: 20px 16px">
      <button class="btn-primary" :disabled="publishing" @click="onPublish">
        {{ publishing ? '发布中...' : '发布行程' }}
      </button>
    </div>
    <TabBar />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import AppHeader from '@/components/AppHeader.vue'
import TabBar from '@/components/TabBar.vue'
import { useAuthStore } from '@/stores/auth'
import * as strokeApi from '@/api/stroke'
import { getUserInfo } from '@/api/account'

const router = useRouter()
const auth = useAuthStore()
const publishing = ref(false)

const form = reactive({
  departureTime: '',
  startAddr: '', startGeoLng: '', startGeoLat: '',
  endAddr: '', endGeoLng: '', endGeoLat: '',
  quickConfirm: false,
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

function onSwitchRole() {
  const newRole = auth.currentRole === 0 ? 1 : 0
  doSwitch(newRole)
}

async function doSwitch(newRole) {
  if (newRole === 1) {
    try {
      const res = await getUserInfo()
      const user = res.data?.[0] || res.data || {}
      if (user.role !== 1) return showToast('请先认证为车主')
    } catch { return showToast('网络异常，请重试') }
  }
  auth.setRole(newRole)
  router.push(newRole === 1 ? '/driver/home' : '/passenger/home')
}

async function onPublish() {
  if (!form.departureTime) return showToast('请选择出发时间')
  if (!form.startAddr) return showToast('请选择出发地')
  if (!form.endAddr) return showToast('请选择目的地')

  publishing.value = true
  try {
    const res = await strokeApi.publish({
      departureTime: form.departureTime + ':00.000+0000',
      startAddr: form.startAddr, startGeoLng: form.startGeoLng, startGeoLat: form.startGeoLat,
      endAddr: form.endAddr, endGeoLng: form.endGeoLng, endGeoLat: form.endGeoLat,
      quantity: 1,
      quickConfirm: form.quickConfirm ? 1 : 0,
      role: 0, status: 0,
    })
    if (res.code === 200 || res.code === 200) {
      showToast('行程发布成功')
      localStorage.removeItem('from'); localStorage.removeItem('to'); localStorage.removeItem('time')
      router.push('/passenger/trip')
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
.form-item { margin-bottom: 16px; }
.form-label { display: block; margin-bottom: 6px; font-size: 14px; font-weight: 600; color: var(--text-primary); }
.form-item .input-field { margin-top: 0; }
.switch-btn { font-size: 14px; color: var(--color-primary); font-weight: 500; }
</style>
