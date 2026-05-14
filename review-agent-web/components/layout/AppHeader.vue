<template>
  <header
    class="flex h-16 items-center justify-between border-b border-gray-200 bg-white px-6 dark:border-gray-700 dark:bg-gray-800"
  >
    <!-- 左侧：Logo / 标题 -->
    <div class="flex items-center gap-3">
      <UIcon name="i-heroicons-code-bracket-square" class="h-7 w-7 text-primary-500" />
      <h1 class="text-xl font-semibold text-gray-900 dark:text-white">
        Code Review Agent
      </h1>
    </div>

    <!-- 右侧：用户信息预留区 -->
    <div class="flex items-center gap-4">
      <UButton
        variant="ghost"
        color="gray"
        icon="i-heroicons-bell"
        size="md"
        aria-label="通知"
      />
      <UAvatar
        src=""
        alt="用户头像"
        size="sm"
        class="cursor-pointer"
      />
    </div>
  </header>
</template>

<script setup lang="ts">
// components/layout/AppHeader.vue - 顶部导航栏
// 展示应用标题与用户信息区
</script>
