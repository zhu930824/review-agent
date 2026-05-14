// nuxt.config.ts - Nuxt 3 应用配置
export default defineNuxtConfig({
  devtools: { enabled: true },

  modules: [
    '@nuxt/ui',
    '@nuxtjs/tailwindcss',
  ],

  // 开发服务器配置
  devServer: {
    port: 3000,
  },

  // 运行时配置：后端 API 地址
  runtimeConfig: {
    public: {
      apiBaseUrl: process.env.API_BASE_URL || 'http://localhost:8080/api',
    },
  },

  // 全局应用配置
  app: {
    head: {
      title: 'Code Review Agent',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      ],
    },
  },

  // 全局 CSS（如需自定义样式可在此引入）
  css: [],

  // 兼容性设置
  compatibilityDate: '2024-11-01',

  // TypeScript 严格模式
  typescript: {
    strict: true,
  },
})
