<template>
  <aside class="flex w-64 flex-col m-3 animate-slide-up bg-white dark:bg-slate-800 rounded-2xl shadow-lg shadow-slate-200/50 dark:shadow-slate-900/50">
    <!-- Logo Section -->
    <div class="flex items-center gap-3 px-5 py-5 mb-2">
      <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-500 shadow-lg shadow-indigo-500/25">
        <UIcon name="i-heroicons-shield-check" class="h-6 w-6 text-white" />
      </div>
      <div>
        <span class="text-lg font-bold text-slate-800 dark:text-white">Review Agent</span>
        <p class="text-xs text-slate-400 dark:text-slate-500 font-medium">AI Code Review</p>
      </div>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 space-y-1.5 px-3 py-2">
      <template v-for="item in navItems" :key="item.to">
        <NuxtLink
          :to="item.to"
          class="flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-semibold transition-all duration-200"
          :class="isActive(item.to)
            ? 'bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-md shadow-indigo-500/25'
            : 'text-slate-500 hover:bg-white hover:text-slate-800 hover:shadow-sm dark:text-slate-400 dark:hover:bg-slate-700/50 dark:hover:text-white'"
        >
          <UIcon :name="item.icon" class="h-5 w-5" />
          <span>{{ item.label }}</span>
        </NuxtLink>
      </template>
    </nav>

    <!-- Footer -->
    <div class="px-3 py-3">
      <div class="rounded-2xl bg-gradient-to-r from-indigo-50 to-purple-50 dark:from-indigo-900/20 dark:to-purple-900/20 p-4">
        <div class="flex items-center gap-3">
          <div class="flex h-9 w-9 items-center justify-center rounded-xl bg-white shadow-sm dark:bg-slate-700/50">
            <UIcon name="i-heroicons-sparkles" class="h-5 w-5 text-indigo-500" />
          </div>
          <div>
            <p class="text-sm font-bold text-slate-700 dark:text-slate-300">Pro Plan</p>
            <p class="text-xs text-slate-400 dark:text-slate-500">Upgrade for more</p>
          </div>
        </div>
      </div>
      <p class="mt-3 text-center text-xs text-slate-400 dark:text-slate-500">v1.0.0</p>
    </div>
  </aside>
</template>

<script setup lang="ts">
const route = useRoute()

interface NavItem {
  label: string
  to: string
  icon: string
}

const navItems: NavItem[] = [
  { label: '仪表盘', to: '/', icon: 'i-heroicons-chart-bar' },
  { label: '项目管理', to: '/projects', icon: 'i-heroicons-folder' },
  { label: '发起审查', to: '/reviews/create', icon: 'i-heroicons-play-circle' },
  { label: '治理中心', to: '/governance', icon: 'i-heroicons-squares-2x2' },
  { label: '运营中心', to: '/operations', icon: 'i-heroicons-chart-pie' },
  { label: '模型配置', to: '/settings/models', icon: 'i-heroicons-cog-8-tooth' },
]

function isActive(to: string): boolean {
  if (to === '/') return route.path === '/'
  return route.path.startsWith(to)
}
</script>