<template>
  <div class="page">
    <AppHeader title="司机行程" @click-right="onSwitchRole">
      <template #right><van-icon name="logistics" size="20" color="#FF6B35" /></template>
    </AppHeader>
    <div class="content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <div v-for="item in list" :key="item.id" class="trip-card" @click="onCardClick(item)">
          <div class="card-header">
            <span class="status-tag" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span>
            <span class="time">{{ formatDate(item.departureTime) }}</span>
          </div>
          <div class="card-route">
            <div class="route-item"><span class="dot from-dot"></span><span class="addr">{{ item.startAddr }}</span></div>
            <div class="route-item"><span class="dot to-dot"></span><span class="addr">{{ item.endAddr }}</span></div>
          </div>
          <div class="card-footer"><span>座位: {{ item.quantity || 0 }} 座</span><van-icon name="arrow" /></div>
        </div>
      </van-pull-refresh>
      <van-empty v-if="!loading && list.length === 0" description="暂无行程" />
    </div>
    <TabBar />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import * as strokeApi from '@/api/stroke'
import { formatDate } from '@/utils'
import AppHeader from '@/components/AppHeader.vue'
import TabBar from '@/components/TabBar.vue'

const router = useRouter()
const auth = useAuthStore()
const list = ref([])
const loading = ref(true)
const refreshing = ref(false)

function onSwitchRole() { auth.setRole(0); router.push('/passenger/trip') }

const statusMap = { 0: '邀请中', 1: '已发车', 3: '已送达' }
const statusClassMap = { 0: 'status-inviting', 1: 'status-departed', 3: 'status-delivered' }
const statusLabel = (s) => statusMap[s] || '未知'
const statusClass = (s) => statusClassMap[s] || ''

const onCardClick = (item) => {
  const s = item.status
  if (s === 0) router.push(`/driver/trip-detail/${item.id}`)
  else if (s === 1) router.push(`/driver/trip-confirmed/${item.id}`)
  else router.push(`/driver/trip-info/${item.id}`)
}

async function fetchList() {
  loading.value = true
  try {
    const res = await strokeApi.list(auth.currentRole)
    list.value = (res && res.data && Array.isArray(res.data)) ? res.data : (Array.isArray(res) ? res : [])
  } catch {} finally { loading.value = false }
}

async function onRefresh() { refreshing.value = true; await fetchList(); refreshing.value = false }

onMounted(fetchList)
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f8fa; padding-bottom: 60px; }
.content { padding: 12px; }
.trip-card { background: #fff; border-radius: 10px; padding: 14px; margin-bottom: 10px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); cursor: pointer; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.status-tag { font-size: 12px; padding: 2px 8px; border-radius: 4px; font-weight: 500; }
.status-inviting { background: #fff3e0; color: #ff9800; }
.status-departed { background: #e3f2fd; color: #2196f3; }
.status-delivered { background: #e8f5e9; color: #4caf50; }
.time { font-size: 13px; color: #999; }
.card-route { margin-bottom: 10px; }
.route-item { display: flex; align-items: center; margin-bottom: 4px; }
.dot { width: 8px; height: 8px; border-radius: 50%; margin-right: 8px; flex-shrink: 0; }
.from-dot { background: #4caf50; } .to-dot { background: #f44336; }
.addr { font-size: 14px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-footer { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #999; }
.switch-btn { font-size: 14px; color: #FF6B35; font-weight: 500; }
</style>
