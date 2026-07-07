<template>
  <div class="page-container">
    <AppHeader title="乘客端 - 黑马顺风车" :show-back="false" @click-left="onSwitchRole">
      <template #left><van-icon name="user-o" size="20" color="#FF6B35" /></template>
      <template #right><van-icon name="service-o" size="20" color="#FF6B35" @click.stop="goAiRoute" /></template>
    </AppHeader>

    <div class="welcome">
      <div class="welcome-icon">🚗</div>
      <div class="welcome-text">欢迎使用智驾游系统</div>
      <div class="welcome-sub">点击下方 <span class="highlight">+</span> 按钮发布行程</div>
    </div>

    <TabBar />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import AppHeader from '@/components/AppHeader.vue'
import TabBar from '@/components/TabBar.vue'
import { useAuthStore } from '@/stores/auth'
import { getUserInfo } from '@/api/account'

const router = useRouter()
const auth = useAuthStore()

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

function goAiRoute() { router.push('/common/ai-route') }
</script>

<style scoped>
.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 30vh;
}
.welcome-icon {
  font-size: 64px;
  margin-bottom: 24px;
}
.welcome-text {
  font-size: 22px;
  font-weight: 700;
  color: #1A1A2E;
  margin-bottom: 8px;
}
.welcome-sub {
  font-size: 14px;
  color: #9CA3AF;
}
.highlight {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35, #FF8A5C);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  vertical-align: middle;
  margin: 0 2px;
}
</style>
