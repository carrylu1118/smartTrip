<template>
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
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const role = computed(() => auth.currentRole == 0 ? 'passenger' : 'driver')

const tabs = computed(() => [
  { name: 'home', label: '首页', icon: 'home-o', to: `/${role.value}/home` },
  { name: 'trip', label: '行程', icon: 'orders-o', to: `/${role.value}/trip` },
  { name: 'order', label: '订单', icon: 'bill-o', to: `/${role.value}/order` },
  { name: 'center', label: '我的', icon: 'user-o', to: '/common/center' },
])

const active = computed(() => {
  const path = route.path
  if (path.startsWith('/common/')) return 3
  const m = tabs.value.findIndex(t => path.startsWith(t.to))
  return m >= 0 ? m : 0
})
</script>
