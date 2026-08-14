// 主题配置：贴合颜色的中文名 + 主色三件套
export const DEFAULT_THEME_KEY = 'sunset'

export const themes = [
  { key: 'sunset', name: '落日晖', desc: '日暮橘橙，如归途落日余晖', primary: '#FF6B35', primaryLight: '#FF8A5C', primaryDark: '#E55A2B', shadow: 'rgba(255, 107, 53, 0.4)' },
  { key: 'ocean', name: '海屿蓝', desc: '澄海静蓝，似旅途海屿晴空', primary: '#1677FF', primaryLight: '#5B9CF8', primaryDark: '#0F5BD1', shadow: 'rgba(22, 119, 255, 0.4)' },
  { key: 'teal', name: '青岚渡', desc: '山涧青岚，清新平缓旅途色调', primary: '#10B981', primaryLight: '#34D399', primaryDark: '#0D9668', shadow: 'rgba(16, 185, 129, 0.4)' },
  { key: 'sakura', name: '落樱绯', desc: '浅樱绯粉，温柔轻快出行氛围', primary: '#EB4E7D', primaryLight: '#F48FB1', primaryDark: '#D63B6C', shadow: 'rgba(235, 78, 125, 0.4)' },
]

export function getTheme(key) {
  return themes.find((t) => t.key === key) || themes[0]
}
