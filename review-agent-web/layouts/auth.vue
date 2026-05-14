<template>
  <div class="relative min-h-screen overflow-hidden bg-soft-gradient">
    <div class="blob-soft blob-soft-1"></div>
    <div class="blob-soft blob-soft-2"></div>
    <div class="blob-soft blob-soft-3"></div>

    <div class="relative z-10 mx-auto grid min-h-screen w-full max-w-6xl content-center px-5 py-8 sm:px-8">
      <div class="mb-8 flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-500 shadow-lg shadow-indigo-500/25">
          <UIcon name="i-heroicons-shield-check" class="h-6 w-6 text-white" />
        </div>
        <div>
          <p class="text-base font-extrabold text-slate-800 dark:text-white">Review Agent</p>
          <p class="text-xs font-semibold text-slate-400 dark:text-slate-500">AI Code Review</p>
        </div>
      </div>

      <slot />
    </div>
  </div>
</template>
