<template>
  <aside
    class="flex w-60 flex-col border-r border-gray-200 bg-white dark:border-gray-700 dark:bg-gray-800"
  >
    <div class="flex h-16 items-center gap-2 border-b border-gray-200 px-5 dark:border-gray-700">
      <UIcon name="i-heroicons-shield-check" class="h-6 w-6 text-primary-500" />
      <span class="text-lg font-bold text-gray-900 dark:text-white">Review Agent</span>
    </div>

    <nav class="flex-1 space-y-1 px-3 py-4">
      <template v-for="item in navItems" :key="item.to">
        <UButton
          :to="item.to"
          :icon="item.icon"
          :variant="isActive(item.to) ? 'soft' : 'ghost'"
          :color="isActive(item.to) ? 'primary' : 'gray'"
          block
          size="md"
          class="justify-start text-sm"
        >
          {{ item.label }}
        </UButton>
      </template>
    </nav>

    <div class="border-t border-gray-200 px-5 py-3 dark:border-gray-700">
      <p class="text-xs text-gray-400 dark:text-gray-500">Nuxt 3 + Nuxt UI</p>
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
  { label: '仪表盘', to: '/', icon: 'i-heroicons-home' },
  { label: '项目管理', to: '/projects', icon: 'i-heroicons-folder' },
  { label: '发起审查', to: '/reviews/create', icon: 'i-heroicons-magnifying-glass-circle' },
  { label: '治理中心', to: '/governance', icon: 'i-heroicons-squares-2x2' },
  { label: '运营中心', to: '/operations', icon: 'i-heroicons-chart-bar-square' },
  { label: '模型配置', to: '/settings/models', icon: 'i-heroicons-cog-6-tooth' },
]

function isActive(to: string): boolean {
  if (to === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(to)
}
</script>
