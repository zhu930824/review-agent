<template>
  <header class="flex h-16 items-center justify-between mx-3 mt-3 mb-0 px-6 bg-white dark:bg-slate-800 rounded-2xl shadow-sm animate-fade-in">
    <!-- Left: Page Context -->
    <div class="flex items-center gap-4">
      <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-500 shadow-md shadow-indigo-500/25">
        <UIcon name="i-heroicons-code-bracket-square" class="h-5 w-5 text-white" />
      </div>
      <div>
        <h1 class="text-base font-bold text-slate-800 dark:text-white">Code Review Agent</h1>
        <p class="text-xs text-slate-400 dark:text-slate-500 font-medium">AI-Powered Quality Assurance</p>
      </div>
    </div>

    <!-- Right: Actions -->
    <div class="flex items-center gap-2">
      <!-- Search -->
      <button class="flex h-10 items-center gap-2 rounded-xl bg-slate-50 dark:bg-slate-700/50 px-4 text-sm text-slate-400 transition-all hover:bg-slate-100 dark:hover:bg-slate-700 shadow-sm">
        <UIcon name="i-heroicons-magnifying-glass" class="h-4 w-4" />
        <span class="hidden sm:inline">搜索...</span>
        <kbd class="hidden sm:inline-flex h-5 items-center rounded-md bg-white dark:bg-slate-600 px-1.5 text-xs text-slate-400 shadow-sm">⌘K</kbd>
      </button>

      <!-- Notification -->
      <button class="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-50 dark:bg-slate-700/50 text-slate-400 hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-700 dark:hover:text-slate-200 transition-all shadow-sm" aria-label="通知">
        <UIcon name="i-heroicons-bell" class="h-5 w-5" />
      </button>

      <!-- Theme Toggle -->
      <button class="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-50 dark:bg-slate-700/50 text-slate-400 hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-700 dark:hover:text-slate-200 transition-all shadow-sm" @click="toggleColorMode" aria-label="切换主题">
        <UIcon :name="colorModeIcon" class="h-5 w-5" />
      </button>

      <!-- User Avatar -->
      <div class="flex items-center gap-3 ml-1 rounded-xl bg-slate-50 dark:bg-slate-700/50 px-3 py-1.5 shadow-sm cursor-pointer hover:bg-slate-100 dark:hover:bg-slate-700 transition-all">
        <UAvatar src="" alt="用户" size="sm" />
        <div class="hidden sm:block">
          <p class="text-sm font-bold text-slate-700 dark:text-slate-200">Admin</p>
          <p class="text-xs text-slate-400 dark:text-slate-500">管理员</p>
        </div>
        <UIcon name="i-heroicons-chevron-down" class="h-4 w-4 text-slate-400" />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
const colorMode = useColorMode()

const colorModeIcon = computed(() =>
  colorMode.value === 'dark' ? 'i-heroicons-sun' : 'i-heroicons-moon'
)

function toggleColorMode() {
  colorMode.preference = colorMode.value === 'dark' ? 'light' : 'dark'
}
</script>