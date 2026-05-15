<template>
  <div class="min-h-screen bg-soft-gradient relative overflow-hidden">
    <!-- Decorative Soft Blobs -->
    <div class="blob-soft blob-soft-1"></div>
    <div class="blob-soft blob-soft-2"></div>
    <div class="blob-soft blob-soft-3"></div>

    <!-- Main Layout -->
    <div class="flex h-screen relative z-10">
      <!-- Sidebar -->
      <LayoutAppSidebar />

      <!-- Main Content Area -->
      <div class="flex flex-1 flex-col overflow-hidden">
        <!-- Header -->
        <LayoutAppHeader />

        <!-- Page Content -->
        <main class="flex-1 overflow-y-auto p-6">
          <slot />
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// layouts/default.vue - Soft UI 默认布局
</script>