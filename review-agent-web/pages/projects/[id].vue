<template>
  <div class="space-y-6">
    <div>
      <UButton to="/projects" variant="ghost" color="gray" icon="i-heroicons-arrow-left" size="sm">返回</UButton>
      <h2 class="mt-2 text-2xl font-bold text-gray-900 dark:text-white">{{ project?.name ?? '加载中...' }}</h2>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ project?.description }}</p>
    </div>

    <UCard>
      <template #header>
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white">基本信息</h3>
      </template>
      <dl class="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div>
          <dt class="text-sm text-gray-500">仓库地址</dt>
          <dd class="mt-1 text-sm text-gray-900 dark:text-white">{{ project?.repoUrl }}</dd>
        </div>
        <div>
          <dt class="text-sm text-gray-500">默认分支</dt>
          <dd class="mt-1 text-sm text-gray-900 dark:text-white">{{ project?.defaultBranch }}</dd>
        </div>
        <div>
          <dt class="text-sm text-gray-500">状态</dt>
          <dd class="mt-1">
            <UBadge :color="statusColor(project?.status ?? '')" :label="statusLabel(project?.status ?? '')" variant="subtle" size="xs" />
          </dd>
        </div>
        <div>
          <dt class="text-sm text-gray-500">创建时间</dt>
          <dd class="mt-1 text-sm text-gray-900 dark:text-white">{{ project?.createdAt }}</dd>
        </div>
      </dl>
    </UCard>

    <UCard>
      <template #header>
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">审查记录</h3>
          <UButton :to="`/reviews/create?projectId=${project?.id}`" variant="solid" color="primary" size="sm" icon="i-heroicons-plus">
            发起审查
          </UButton>
        </div>
      </template>
      <UTable :rows="reviews" :columns="reviewColumns" :loading="loading" :empty-state="{ icon: 'i-heroicons-magnifying-glass', label: '暂无审查记录' }">
        <template #status-data="{ row }">
          <UBadge :color="statusColor(row.status)" :label="statusLabel(row.status)" variant="subtle" size="xs" />
        </template>
        <template #actions-data="{ row }">
          <UButton :to="`/reviews/${row.id}`" variant="ghost" color="primary" size="xs" icon="i-heroicons-arrow-right">查看</UButton>
        </template>
      </UTable>
    </UCard>
  </div>
</template>

<script setup lang="ts">
import type { Project } from '~/types/project'
import type { Review } from '~/types/review'
import type { PageResult } from '~/types/api'

const route = useRoute()
const { get } = useApi()
const projectId = computed(() => Number(route.params.id))
const loading = ref(false)
const project = ref<Project | null>(null)
const reviews = ref<Review[]>([])

const reviewColumns = [
  { key: 'sourceBranch', label: '源分支' },
  { key: 'targetBranch', label: '目标分支' },
  { key: 'reviewMode', label: '模式' },
  { key: 'status', label: '状态' },
  { key: 'createdAt', label: '时间' },
  { key: 'actions', label: '操作' },
]

function statusColor(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'orange', CLONING: 'blue', READY: 'green', ERROR: 'red',
    RUNNING: 'blue', COMPLETED: 'green', FAILED: 'red',
  }
  return map[status] ?? 'gray'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = {
    PENDING: '等待中', CLONING: '克隆中', READY: '就绪', ERROR: '错误',
    RUNNING: '审查中', COMPLETED: '已完成', FAILED: '失败',
  }
  return map[status] ?? status
}

async function loadData() {
  loading.value = true
  try {
    const [projectRes, reviewsRes] = await Promise.all([
      get<Project>(`/projects/${projectId.value}`),
      get<PageResult<Review>>(`/reviews?projectId=${projectId.value}&pageNum=1&pageSize=20`),
    ])
    if (projectRes.data) project.value = projectRes.data
    if (reviewsRes.data) reviews.value = reviewsRes.data.records
  } catch (e) {
    console.error('加载项目详情失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
