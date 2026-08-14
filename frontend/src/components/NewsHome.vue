<template>
  <div class="news-home">
    <!-- 顶部轮播（旅行社，纯图） -->
    <div v-if="banners.length" class="banner">
      <van-swipe
        class="banner-swipe"
        :autoplay="3500"
        :show-indicators="banners.length > 1"
        indicator-color="var(--color-primary)"
      >
        <van-swipe-item v-for="b in banners" :key="b.id">
          <img class="banner-img" :src="b.pic" alt="" @click="goDetail(b)" />
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- 分类板块 -->
    <section v-for="s in sections" :key="s.category" class="section">
      <div class="section-head">
        <span class="section-bar"></span>
        <h2 class="section-title">{{ s.label }}</h2>
        <button class="section-more" @click="goList(s)">
          更多
          <van-icon name="arrow" size="12" />
        </button>
      </div>

      <!-- 首条：全宽焦点大卡 -->
      <article v-if="s.list[0]" class="hero" @click="goDetail(s.list[0])">
        <div class="hero-cover">
          <img class="hero-img" :src="s.list[0].pic" alt="" loading="lazy" />
        </div>
        <h3 class="hero-title">{{ s.list[0].title }}</h3>
      </article>

      <!-- 其余：三列紧凑小卡 -->
      <div v-if="s.list.length > 1" class="mini-grid">
        <article
          v-for="item in s.list.slice(1)"
          :key="item.id"
          class="mini"
          @click="goDetail(item)"
        >
          <div class="mini-cover">
            <img class="mini-img" :src="item.pic" alt="" loading="lazy" />
          </div>
          <h4 class="mini-title">{{ item.title }}</h4>
        </article>
      </div>
    </section>

    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="!banners.length && !sections.length" class="state">暂无资讯</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNewsHome } from '@/api/news'

const router = useRouter()

const banners = ref([])
const sections = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getNewsHome()
    const home = res.data?.[0] || {}
    banners.value = home.banners || []
    sections.value = home.sections || []
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
})

function goList(section) {
  router.push({
    path: '/news/list',
    query: { category: section.category, label: section.label },
  })
}

function goDetail(item) {
  router.push(`/news/detail/${item.id}`)
}
</script>

<style scoped>
.news-home {
  padding-bottom: 4px;
}

/* ---- 顶部轮播 ---- */
.banner {
  margin: 16px 16px 0;
}
.banner-swipe {
  border-radius: 18px;
  overflow: hidden;
  height: 190px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}
.banner-img {
  width: 100%;
  height: 190px;
  object-fit: cover;
  display: block;
  cursor: pointer;
}

/* ---- 板块 ---- */
.section {
  margin: 28px 16px 0;
}
.section-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}
.section-bar {
  width: 4px;
  height: 18px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--color-primary), var(--color-primary-light));
  flex-shrink: 0;
}
.section-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: var(--text-primary);
}
.section-more {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 2px;
  border: none;
  background: transparent;
  padding: 4px 0 4px 8px;
  font-size: 13px;
  color: var(--text-hint);
  cursor: pointer;
}

/* ---- 首条焦点大卡 ---- */
.hero {
  background: var(--bg-card);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.15s;
}
.hero:active {
  transform: scale(0.98);
}
.hero-cover {
  aspect-ratio: 16 / 9;
  background: #eef0f3;
  overflow: hidden;
}
.hero-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.hero-title {
  padding: 12px 14px 14px;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.45;
  color: var(--text-primary);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

/* ---- 三列紧凑小卡 ---- */
.mini-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-top: 10px;
}
.mini {
  background: var(--bg-card);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.15s;
}
.mini:active {
  transform: scale(0.96);
}
.mini-cover {
  aspect-ratio: 1 / 1;
  background: #eef0f3;
  overflow: hidden;
}
.mini-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.mini-title {
  padding: 8px 10px 10px;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
  color: var(--text-primary);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

/* ---- 状态 ---- */
.state {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-hint);
}
</style>
