<template>
  <div class="page-container">
    <AppHeader :title="label || '资讯列表'" show-back />

    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
    >
      <article
        v-for="item in list"
        :key="item.id"
        class="news-item"
        @click="goDetail(item)"
      >
        <div class="news-item__cover">
          <img class="news-item__img" :src="item.pic" alt="" loading="lazy" />
        </div>
        <div class="news-item__body">
          <h3 class="news-item__title">{{ item.title }}</h3>
          <p class="news-item__time">{{ formatDate(item.createTime) }}</p>
        </div>
      </article>
    </van-list>

    <div v-if="!loading && !list.length" class="state">暂无内容</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNewsList } from '@/api/news'
import { formatDate } from '@/utils'
import AppHeader from '@/components/AppHeader.vue'

const route = useRoute()
const router = useRouter()

const category = Number(route.query.category)
const label = route.query.label || ''
const PAGE_SIZE = 10

const list = ref([])
const page = ref(0)
const total = ref(0)
const loading = ref(false)
const finished = ref(false)

async function onLoad() {
  const next = page.value + 1
  const res = await getNewsList(category, next, PAGE_SIZE)
  const data = res.data?.[0] || {}
  list.value = list.value.concat(data.list || [])
  total.value = data.total || 0
  page.value = next
  if (list.value.length >= total.value) {
    finished.value = true
  }
  loading.value = false
}

function goDetail(item) {
  router.push(`/news/detail/${item.id}`)
}
</script>

<style scoped>
.news-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  margin: 0 16px 12px;
  background: var(--bg-card);
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  cursor: pointer;
}
.news-item__cover {
  width: 110px;
  height: 82px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #eef0f3;
}
.news-item__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.news-item__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.news-item__title {
  font-size: 15px;
  font-weight: 500;
  line-height: 1.4;
  color: var(--text-primary);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.news-item__time {
  font-size: 12px;
  color: var(--text-hint);
}

.state {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
</style>
