import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      // 代理所有 /api 的请求
      '/api': {
        target: 'http://localhost:9080', // 您的后端服务地址
        changeOrigin: true,
        // 如果需要，可以重写路径
        // rewrite: (path) => path.replace(/^\/api/, '') 
      }
    }
  }
})
