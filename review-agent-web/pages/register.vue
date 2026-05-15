<template>
  <section class="grid items-center gap-8 lg:grid-cols-[minmax(0,0.9fr)_minmax(460px,540px)]">
    <div class="hidden lg:grid lg:gap-6">
      <div>
        <p class="text-sm font-bold uppercase tracking-[0.18em] text-indigo-500">Team Onboarding</p>
        <h1 class="mt-4 max-w-xl text-4xl font-extrabold leading-tight text-slate-900 dark:text-white">
          为你的审查团队创建工作入口
        </h1>
        <p class="mt-4 max-w-lg text-base leading-7 text-slate-500 dark:text-slate-400">
          注册后即可进入项目管理、发起审查，并使用现有模型配置和治理策略。
        </p>
      </div>

      <div class="panel-soft max-w-xl p-5">
        <div class="grid gap-4">
          <div v-for="item in steps" :key="item.title" class="flex items-start gap-4">
            <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-indigo-50 text-indigo-500 shadow-sm dark:bg-indigo-500/10">
              <UIcon :name="item.icon" class="h-5 w-5" />
            </div>
            <div>
              <p class="text-sm font-extrabold text-slate-700 dark:text-slate-200">{{ item.title }}</p>
              <p class="mt-1 text-xs leading-5 text-slate-400">{{ item.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="panel-soft w-full p-6 sm:p-8">
      <div class="mb-7">
        <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-500 shadow-sm dark:bg-indigo-500/10">
          <UIcon name="i-heroicons-user-plus" class="h-6 w-6" />
        </div>
        <h2 class="mt-5 text-2xl font-extrabold text-slate-900 dark:text-white">创建账号</h2>
        <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">填写基础信息后进入 Review Agent。</p>
      </div>

      <form class="grid gap-5" @submit.prevent="handleRegister">
        <div class="grid gap-5 sm:grid-cols-2">
          <UFormGroup label="用户名">
            <UInput v-model="form.username" icon="i-heroicons-user" size="lg" placeholder="reviewer" autocomplete="username" />
          </UFormGroup>

          <UFormGroup label="显示名称">
            <UInput v-model="form.displayName" icon="i-heroicons-identification" size="lg" placeholder="Admin" autocomplete="name" />
          </UFormGroup>
        </div>

        <UFormGroup label="邮箱">
          <UInput v-model="form.email" icon="i-heroicons-envelope" size="lg" type="email" placeholder="admin@example.com" autocomplete="email" />
        </UFormGroup>

        <UFormGroup label="密码">
          <UInput v-model="form.password" icon="i-heroicons-lock-closed" size="lg" type="password" placeholder="至少 6 位" autocomplete="new-password" />
        </UFormGroup>

        <p v-if="errorMessage" class="rounded-lg bg-red-50 px-3 py-2 text-sm font-semibold text-red-500 dark:bg-red-500/10">
          {{ errorMessage }}
        </p>

        <button class="soft-button soft-button-primary h-12 w-full rounded-sm" type="submit" :disabled="loading">
          <UIcon v-if="loading" name="i-heroicons-arrow-path" class="h-5 w-5 animate-spin" />
          <UIcon v-else name="i-heroicons-sparkles" class="h-5 w-5" />
          注册并进入
        </button>
      </form>

      <div class="mt-6 flex items-center justify-center gap-2 text-sm text-slate-500">
        <span>已有账号？</span>
        <NuxtLink class="font-extrabold text-indigo-500 hover:text-indigo-600" to="/login">返回登录</NuxtLink>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'auth' })

const { register } = useAuth()
const loading = ref(false)
const errorMessage = ref('')
const form = reactive({
  username: '',
  displayName: '',
  email: '',
  password: '',
})

const steps = [
  { title: '创建身份', desc: '保存登录账号和团队展示名称。', icon: 'i-heroicons-identification' },
  { title: '进入工作台', desc: '注册成功后自动进入项目首页。', icon: 'i-heroicons-squares-2x2' },
  { title: '开始审查', desc: '使用已有策略发起 Pre-PR 审查。', icon: 'i-heroicons-play-circle' },
]

async function handleRegister() {
  if (!form.username || !form.displayName || !form.password) {
    errorMessage.value = '请填写用户名、显示名称和密码'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    await register({
      username: form.username,
      displayName: form.displayName,
      email: form.email || undefined,
      password: form.password,
    })
    await navigateTo('/')
  } catch (error: any) {
    errorMessage.value = error?.data?.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>
