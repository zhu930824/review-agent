<template>
  <div class="flex h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 左侧边导航栏 -->
    <LayoutAppSidebar />

    <!-- 右侧主内容区 -->
    <div class="flex flex-1 flex-col overflow-hidden">
      <!-- 顶部栏 -->
      <LayoutAppHeader />

      <!-- 页面内容 -->
      <main class="flex-1 overflow-y-auto p-6">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
// layouts/default.vue - 默认布局：侧边栏 + 顶栏 + 内容区
// 此布局会被 NuxtLayout 自动应用于所有页面
</script>
