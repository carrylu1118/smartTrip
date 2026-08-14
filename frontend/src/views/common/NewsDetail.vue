<template>
  <div class="page-container">
    <AppHeader title="资讯详情" show-back />

    <article v-if="detail" class="detail">
      <h1 class="detail__title">{{ detail.title }}</h1>
      <div class="detail__meta">
        <span class="detail__tag">{{ detail.dictLabel }}</span>
        <span class="detail__time">{{ formatDate(detail.createTime) }}</span>
      </div>
      <img v-if="detail.pic" class="detail__img" :src="detail.pic" alt="" />
      <div class="detail__content" v-html="detail.content"></div>
    </article>

    <div v-else-if="loading" class="state">加载中…</div>
    <div v-else class="state">内容不存在</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getNewsDetail } from '@/api/news'
import { formatDate } from '@/utils'
import AppHeader from '@/components/AppHeader.vue'

const route = useRoute()
const id = Number(route.params.id)

const detail = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getNewsDetail(id)
    detail.value = res.data?.[0] || null
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail {
  padding: 20px 16px 32px;
}
.detail__title {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--text-primary);
}
.detail__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 12px 0 16px;
}
.detail__tag {
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: var(--color-primary);
  background: rgba(255, 255, 255, 0);
  border: 1px solid var(--color-primary);
}
.detail__time {
  font-size: 12px;
  color: var(--text-hint);
}
.detail__img {
  width: 100%;
  border-radius: 12px;
  margin-bottom: 16px;
  display: block;
}
.detail__content :deep(p) {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-primary);
  margin: 12px 0;
}
.detail__content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}

.state {
  padding: 60px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
</style>
