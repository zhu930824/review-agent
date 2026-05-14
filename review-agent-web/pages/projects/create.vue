<template>
  <div class="grid gap-6">
    <div class="animate-slide-up">
      <h2 class="text-2xl font-bold text-slate-800 dark:text-white">创建项目</h2>
      <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">添加一个新的代码仓库进行审查</p>
    </div>

    <div class="soft-card-flat max-w-2xl p-6 animate-slide-up stagger-2">
      <form class="space-y-5" @submit.prevent="handleSubmit">
        <UFormGroup label="项目名称" name="name" required>
          <UInput v-model="form.name" placeholder="输入项目名称" icon="i-heroicons-folder" size="md" />
        </UFormGroup>

        <UFormGroup label="项目描述" name="description">
          <UTextarea v-model="form.description" placeholder="简要描述项目（可选）" :rows="3" size="md" />
        </UFormGroup>

        <UFormGroup label="Git 仓库地址" name="repoUrl" required>
          <UInput v-model="form.repoUrl" placeholder="https://github.com/owner/repo" icon="i-heroicons-link" size="md" />
        </UFormGroup>

        <UFormGroup label="默认分支" name="defaultBranch">
          <UInput v-model="form.defaultBranch" placeholder="main" size="md" />
        </UFormGroup>

        <div class="flex items-center gap-3 pt-2">
          <UButton type="submit" variant="solid" :loading="submitting">创建项目</UButton>
          <UButton to="/projects" variant="ghost" color="gray">取消</UButton>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Project } from '~/types/project'

const { post } = useApi()
const submitting = ref(false)

const form = reactive({
  name: '',
  description: '',
  repoUrl: '',
  defaultBranch: 'main',
})

async function handleSubmit() {
  if (!form.name || !form.repoUrl) return
  submitting.value = true
  try {
    const res = await post<Project>('/projects', {
      name: form.name,
      repoUrl: form.repoUrl,
      defaultBranch: form.defaultBranch || 'main',
      description: form.description || undefined,
    })
    if (res.data) {
      navigateTo(`/projects/${res.data.id}`)
    }
  } catch {
    // useApi 统一处理错误
  } finally {
    submitting.value = false
  }
}
</script>
