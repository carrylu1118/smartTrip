<template>
  <div class="page-container">
    <AppHeader title="个人中心" />

    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="user-card__inner">
        <img :src="user.avatar || '/img/default-header.jpg'" class="person-img" />
        <div class="user-card__info">
          <div class="user-card__name">{{ user.useralias || '未设置昵称' }}</div>
          <div class="user-card__status">
            <span class="status-tag" :class="user.status == 1 ? 'status-done' : 'status-pending'">
              实名{{ user.status == 1 ? '已认证' : '未认证' }}
            </span>
            <span class="status-tag" :class="user.role == 1 ? 'status-done' : 'status-pending'">
              车主{{ user.role == 1 ? '已认证' : '未认证' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能列表 -->
    <van-cell-group class="menu-group">
      <van-cell title="修改密码" is-link to="/common/password" icon="lock" />
      <van-cell title="修改头像" is-link to="/common/photo" icon="photograph" />
      <van-cell title="收款码" is-link to="/common/paycode" icon="qr" />
      <van-cell title="完善资料" is-link to="/common/edit" icon="user-o" />
      <van-cell title="车辆信息" is-link to="/common/car-info" icon="logistics" />
      <van-cell title="实名认证" is-link to="/common/user-verify" icon="certificate" />
      <van-cell title="车主认证" is-link to="/common/car-verify" icon="passed" />
      <van-cell title="行程统计" is-link to="/common/account" icon="chart-trending-o" />
      <van-cell title="我的消息" is-link to="/common/messages" icon="comment-o">
        <template #value>
          <span v-if="unreadCount > 0" class="unread-dot"></span>
        </template>
      </van-cell>
      <van-cell title="更换主题" is-link icon="gem-o" @click="showTheme = true" />
    </van-cell-group>

    <!-- 退出登录 -->
    <div class="logout-wrap">
      <button class="btn-secondary" @click="handleLogout">退出登录</button>
    </div>

    <!-- 主题选择弹层 -->
    <van-popup v-model:show="showTheme" position="bottom" round>
      <div class="theme-panel">
        <div class="theme-panel__title">更换主题</div>
        <div
          v-for="t in themeStore.themes"
          :key="t.key"
          class="theme-item"
          @click="onSelectTheme(t.key)"
        >
          <span class="theme-item__dot" :style="{ background: t.primary }"></span>
          <div class="theme-item__text">
            <div class="theme-item__name">{{ t.name }}</div>
            <div class="theme-item__desc">{{ t.desc }}</div>
          </div>
          <van-icon v-if="themeStore.current === t.key" name="success" :color="t.primary" />
        </div>
      </div>
    </van-popup>

    <TabBar />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { getUserInfo, getAuthInfo } from '@/api/account'
import { useNoticeMessages } from '@/composables/useNotice'
import AppHeader from '@/components/AppHeader.vue'
import TabBar from '@/components/TabBar.vue'

const router = useRouter()
const auth = useAuthStore()
const themeStore = useThemeStore()

const user = ref({})
const authInfo = ref({})
const showTheme = ref(false)
const { unreadCount } = useNoticeMessages()

onMounted(async () => {
  try {
    const [userRes, authRes] = await Promise.all([
      getUserInfo(),
      getAuthInfo(),
    ])
    user.value = userRes.data?.[0] || userRes || {}
    authInfo.value = authRes.data?.[0] || authRes || {}
  } catch (e) {
    // ignore
  }
})

function handleLogout() {
  showConfirmDialog({
    title: '提示',
    message: '确定要退出登录吗？',
  }).then(() => {
    auth.logout()
    router.push('/login')
  }).catch(() => {})
}

function onSelectTheme(key) {
  themeStore.setTheme(key)
  showTheme.value = false
}
</script>

<style scoped>
.user-card {
  margin: 16px;
  padding: 24px 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  box-shadow: 0 4px 16px var(--color-primary-shadow);
}

.user-card__inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.person-img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 2px solid #fff;
  object-fit: cover;
  background: #fff;
}

.user-card__info {
  flex: 1;
}

.user-card__name {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 8px;
}

.user-card__status {
  display: flex;
  gap: 12px;
}

.status-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(255,255,255,0.25);
  color: #fff;
}

.menu-group {
  margin: 0 16px;
}

.menu-group :deep(.van-cell__value) {
  overflow: visible;
  flex: none;
}

.unread-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #ee0a24;
  border-radius: 50%;
  vertical-align: middle;
}

.logout-wrap {
  padding: 24px 16px;
}

.logout-wrap .btn-secondary {
  color: var(--color-primary);
  border-color: var(--color-primary);
}
.switch-btn { font-size: 14px; color: var(--color-primary); font-weight: 500; }

.theme-panel {
  padding: 24px 20px 32px;
}
.theme-panel__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  text-align: center;
}
.theme-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 8px;
  cursor: pointer;
}
.theme-item__dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  flex-shrink: 0;
}
.theme-item__text {
  flex: 1;
  min-width: 0;
}
.theme-item__name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.theme-item__desc {
  margin-top: 2px;
  font-size: 12px;
  color: var(--text-hint);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
