<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between animate-slide-up">
      <div>
        <h2 class="text-2xl font-bold text-slate-800 dark:text-white">项目管理</h2>
        <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">管理您的代码仓库项目</p>
      </div>
      <button class="soft-button-primary rounded-full px-5" @click="showCreateModal = true">
        <UIcon name="i-heroicons-plus" class="h-5 w-5" />
        创建项目
      </button>
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3 -mt-6">
      <ProjectCard v-for="project in projects" :key="project.id" :project="project" />
    </div>

    <!-- Empty State -->
    <div v-if="!projects.length && !loading" class="stat-card p-12 text-center animate-slide-up stagger-2">
      <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-100 to-purple-100 dark:from-indigo-900/30 dark:to-purple-900/30">
        <UIcon name="i-heroicons-folder-open" class="h-8 w-8 text-indigo-400" />
      </div>
      <p class="mt-4 text-base font-bold text-slate-600 dark:text-slate-300">暂无项目</p>
      <p class="mt-1 text-sm text-slate-400">点击右上角按钮创建第一个项目</p>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-white dark:bg-slate-800 shadow-md">
        <UIcon name="i-heroicons-arrow-path" class="h-6 w-6 animate-spin text-indigo-500" />
      </div>
    </div>

    <div v-if="totalPages > 1" class="flex justify-center">
      <UPagination v-model="currentPage" :total="total" :page-size="pageSize" />
    </div>

    <!-- Create Project Modal -->
    <UModal v-model="showCreateModal" :ui="{ width: 'sm:max-w-lg', margin: '-mt-20' }">
      <div class="p-6">
        <h3 class="text-xl font-bold text-slate-800 dark:text-white mb-4">创建项目</h3>
        <form class="space-y-5" @submit.prevent="handleCreate">
          <UFormGroup label="项目名称" name="name" required>
            <UInput v-model="createForm.name" placeholder="输入项目名称" icon="i-heroicons-folder" size="md" />
          </UFormGroup>

          <UFormGroup label="项目描述" name="description">
            <UTextarea v-model="createForm.description" placeholder="简要描述项目（可选）" :rows="3" size="md" />
          </UFormGroup>

          <UFormGroup label="Git 仓库地址" name="repoUrl" required>
            <UInput v-model="createForm.repoUrl" placeholder="https://github.com/owner/repo" icon="i-heroicons-link" size="md" />
          </UFormGroup>

          <UFormGroup label="默认分支" name="defaultBranch">
            <UInput v-model="createForm.defaultBranch" placeholder="main" size="md" />
          </UFormGroup>

          <div class="flex items-center gap-3 pt-2">
            <UButton type="submit" variant="solid" :loading="creating" class="px-6">创建项目</UButton>
            <UButton variant="ghost" color="gray" @click="showCreateModal = false">取消</UButton>
          </div>
        </form>
      </div>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import type { Project } from '~/types/project'
import type { PageResult } from '~/types/api'

const { get, post } = useApi()
const loading = ref(false)
const projects = ref<Project[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const showCreateModal = ref(false)
const creating = ref(false)
const createForm = reactive({
  name: '',
  description: '',
  repoUrl: '',
  defaultBranch: 'main',
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

async function loadProjects() {
  loading.value = true
  try {
    const res = await get<PageResult<Project>>(`/projects?pageNum=${currentPage.value}&pageSize=${pageSize.value}`)
    if (res.data) {
      projects.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    console.error('加载项目列表失败', e)
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!createForm.name || !createForm.repoUrl) return
  creating.value = true
  try {
    const res = await post<Project>('/projects', {
      name: createForm.name,
      repoUrl: createForm.repoUrl,
      defaultBranch: createForm.defaultBranch || 'main',
      description: createForm.description || undefined,
    })
    if (res.data) {
      showCreateModal.value = false
      Object.assign(createForm, { name: '', description: '', repoUrl: '', defaultBranch: 'main' })
      await loadProjects()
    }
  } catch {
    // useApi 统一处理错误
  } finally {
    creating.value = false
  }
}

watch(currentPage, loadProjects)
onMounted(loadProjects)
</script>