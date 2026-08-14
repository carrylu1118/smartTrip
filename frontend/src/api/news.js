import api from './index'

/**
 * 资讯 API
 */

// 首页数据（顶部轮播 + 分类板块）
export function getNewsHome() {
  return api.get('/news/home')
}

// 某分类文章分页
export function getNewsList(category, page = 1, size = 10) {
  return api.get('/news/list', { params: { category, page, size } })
}

// 文章详情
export function getNewsDetail(id) {
  return api.get(`/news/${id}`)
}
