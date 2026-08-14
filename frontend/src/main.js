import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useThemeStore } from './stores/theme'
import 'vant/lib/index.css'
import './style.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

// 挂载前应用持久化的主题，避免首屏闪烁
useThemeStore(pinia).init()

app.mount('#app')
