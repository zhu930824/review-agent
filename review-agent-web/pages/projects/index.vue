<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white">项目管理</h2>
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">管理您的代码仓库项目</p>
      </div>
      <UButton to="/projects/create" variant="solid" color="primary" icon="i-heroicons-plus">
        创建项目
      </UButton>
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
      <ProjectCard v-for="project in projects" :key="project.id" :project="project" />
    </div>

    <UCard v-if="!projects.length && !loading" class="text-center">
      <div class="py-12">
        <UIcon name="i-heroicons-folder-open" class="mx-auto h-12 w-12 text-gray-300" />
        <p class="mt-4 text-gray-500">暂无项目，点击右上角按钮创建第一个项目</p>
      </div>
    </UCard>

    <div v-if="loading" class="flex justify-center py-12">
      <UIcon name="i-heroicons-arrow-path" class="h-8 w-8 animate-spin text-primary-500" />
    </div>

    <div v-if="totalPages > 1" class="flex justify-center">
      <UPagination v-model="currentPage" :total="total" :page-size="pageSize" />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Project } from '~/types/project'
import type { PageResult } from '~/types/api'

const { get } = useApi()
const loading = ref(false)
const projects = ref<Project[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

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

watch(currentPage, loadProjects)
onMounted(loadProjects)
</script>
