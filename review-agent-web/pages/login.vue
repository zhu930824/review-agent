<template>
  <section class="grid items-center gap-8 lg:grid-cols-[minmax(0,0.92fr)_minmax(420px,480px)]">
    <div class="hidden lg:grid lg:gap-6">
      <div>
        <p class="text-sm font-bold uppercase tracking-[0.18em] text-indigo-500">Quality Gateway</p>
        <h1 class="mt-4 max-w-xl text-4xl font-extrabold leading-tight text-slate-900 dark:text-white">
          进入你的 AI 代码审查控制台
        </h1>
        <p class="mt-4 max-w-lg text-base leading-7 text-slate-500 dark:text-slate-400">
          统一管理项目、发起 Pre-PR 审查，并把模型审查、质量门禁和治理策略放在同一个工作流里。
        </p>
      </div>

      <div class="grid max-w-xl grid-cols-3 gap-4">
        <div v-for="item in highlights" :key="item.label" class="panel-soft p-4">
          <UIcon :name="item.icon" class="h-5 w-5 text-indigo-500" />
          <p class="mt-3 text-sm font-bold text-slate-700 dark:text-slate-200">{{ item.label }}</p>
          <p class="mt-1 text-xs leading-5 text-slate-400">{{ item.value }}</p>
        </div>
      </div>
    </div>

    <div class="panel-soft w-full p-6 sm:p-8">
      <div class="mb-7">
        <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-500 shadow-sm dark:bg-indigo-500/10">
          <UIcon name="i-heroicons-arrow-right-on-rectangle" class="h-6 w-6" />
        </div>
        <h2 class="mt-5 text-2xl font-extrabold text-slate-900 dark:text-white">欢迎回来</h2>
        <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">登录后继续处理代码审查任务。</p>
      </div>

      <form class="grid gap-5" @submit.prevent="handleLogin">
        <UFormGroup label="用户名">
          <UInput v-model="form.username" icon="i-heroicons-user" size="lg" placeholder="请输入用户名" autocomplete="username" />
        </UFormGroup>

        <UFormGroup label="密码">
          <UInput v-model="form.password" icon="i-heroicons-lock-closed" size="lg" type="password" placeholder="请输入密码" autocomplete="current-password" />
        </UFormGroup>

        <p v-if="errorMessage" class="rounded-lg bg-red-50 px-3 py-2 text-sm font-semibold text-red-500 dark:bg-red-500/10">
          {{ errorMessage }}
        </p>

        <button class="soft-button soft-button-primary h-12 w-full rounded-sm" type="submit" :disabled="loading">
          <UIcon v-if="loading" name="i-heroicons-arrow-path" class="h-5 w-5 animate-spin" />
          <UIcon v-else name="i-heroicons-lock-open" class="h-5 w-5" />
          登录
        </button>
      </form>

      <div class="mt-6 flex items-center justify-center gap-2 text-sm text-slate-500">
        <span>还没有账号？</span>
        <NuxtLink class="font-extrabold text-indigo-500 hover:text-indigo-600" to="/register">创建账号</NuxtLink>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'auth' })

const { login } = useAuth()
const loading = ref(false)
const errorMessage = ref('')
const form = reactive({
  username: '',
  password: '',
})

const highlights = [
  { label: 'Pre-PR', value: '审查准入', icon: 'i-heroicons-code-bracket-square' },
  { label: 'Gate', value: '质量门禁', icon: 'i-heroicons-shield-check' },
  { label: 'Models', value: '多模型协作', icon: 'i-heroicons-cpu-chip' },
]

async function handleLogin() {
  if (!form.username || !form.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    await login({
      username: form.username,
      password: form.password,
    })
    await navigateTo('/')
  } catch (error: any) {
    errorMessage.value = error?.data?.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>
