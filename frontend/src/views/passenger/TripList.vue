<template>
  <div class="page-container">
    <AppHeader title="乘客行程" @click-right="onSwitchRole">
      <template #right><van-icon name="user-o" size="20" color="#FF6B35" /></template>
    </AppHeader>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh" style="min-height: 70vh">
      <van-loading v-if="loading" style="display:block; margin: 60px auto" />
      <div v-for="item in list" :key="item.id" class="card trip-card" @click="onItemClick(item)">
        <div class="flex-between">
          <span class="text-lg text-bold">{{ formatTime(item.departureTime) }}</span>
          <span :style="{ color: statusColor(item.status) }" class="text-bold">{{ statusLabel(item.status) }}</span>
        </div>
        <div class="flex-between mt-sm">
          <span class="text-secondary">{{ item.startAddr }} → {{ item.endAddr }}</span>
          <span v-if="item.quickConfirm == 1" class="quick-tag">⚡闪电</span>
        </div>
      </div>
      <van-empty v-if="!loading && list.length === 0" description="暂无行程" />
    </van-pull-refresh>
    <TabBar />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import AppHeader from '@/components/AppHeader.vue'
import TabBar from '@/components/TabBar.vue'
import { useAuthStore } from '@/stores/auth'
import * as strokeApi from '@/api/stroke'
import { getUserInfo } from '@/api/account'

const router = useRouter()
const auth = useAuthStore()
const list = ref([])
const loading = ref(true)
const refreshing = ref(false)

function onSwitchRole() {
  const newRole = auth.currentRole == 0 ? 1 : 0
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
  router.push(newRole === 1 ? '/driver/trip' : '/passenger/trip')
}
function formatTime(s) { return s ? String(s).substring(0, 16) : '' }
function statusLabel(s) { return ({ 0:'邀请中',1:'已确认',2:'已上车',3:'已下车',4:'已取消' })[s] || '未知' }
function statusColor(s) { return ({ 0:'#F59E0B',1:'#3B82F6',2:'#E5A100',3:'#10B981',4:'#EF4444' })[s] || '#9CA3AF' }

function onItemClick(item) {
  const s = Number(item.status)
  if (s === 0) router.push(`/passenger/trip-detail/${item.id}`)
  else if (s === 1 || s === 2) router.push(`/passenger/trip-confirmed/${item.id}`)
  else router.push(`/passenger/trip-info/${item.id}`)
}

async function fetchList() {
  loading.value = true
  try {
    const res = await strokeApi.list(auth.currentRole)
    list.value = (res && res.data && Array.isArray(res.data)) ? res.data : (Array.isArray(res) ? res : [])
  } catch (e) {}
  finally { loading.value = false }
}

async function onRefresh() { refreshing.value = true; await fetchList(); refreshing.value = false }

onMounted(fetchList)
</script>

<style scoped>
.trip-card { cursor: pointer; }
.trip-card:active { background: #f9f9f9; }
.quick-tag { font-size: 11px; color: #FF6B35; background: #FFF3ED; padding: 2px 8px; border-radius: 10px; }
.switch-btn { font-size: 14px; color: #FF6B35; font-weight: 500; }
</style>
