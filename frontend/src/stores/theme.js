import { defineStore } from 'pinia'
import { ref } from 'vue'
import { themes, getTheme, DEFAULT_THEME_KEY } from '@/theme/themes'

const THEME_KEY = 'app-theme'

export const useThemeStore = defineStore('theme', () => {
  // ---- state ----
  const current = ref(DEFAULT_THEME_KEY)

  // ---- actions ----
  function applyTheme(theme) {
    const root = document.documentElement.style
    root.setProperty('--color-primary', theme.primary)
    root.setProperty('--color-primary-light', theme.primaryLight)
    root.setProperty('--color-primary-dark', theme.primaryDark)
    root.setProperty('--color-primary-shadow', theme.shadow)
    // 同步 Vant 主色，让未显式指定颜色的 Vant 组件跟随主题
    root.setProperty('--van-primary-color', theme.primary)
  }

  function setTheme(key) {
    const theme = getTheme(key)
    current.value = theme.key
    localStorage.setItem(THEME_KEY, theme.key)
    applyTheme(theme)
  }

  function init() {
    const saved = localStorage.getItem(THEME_KEY)
    const theme = getTheme(saved)
    current.value = theme.key
    applyTheme(theme)
  }

  return {
    current,
    themes,
    setTheme,
    init,
  }
})
