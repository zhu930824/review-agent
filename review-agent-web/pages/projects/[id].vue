<template>
  <div class="space-y-6">
    <div class="animate-slide-up">
      <UButton to="/projects" variant="ghost" color="gray" icon="i-heroicons-arrow-left" size="sm">返回</UButton>
      <h2 class="mt-2 text-2xl font-bold text-slate-800 dark:text-white">{{ project?.name ?? '加载中...' }}</h2>
      <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">{{ project?.description }}</p>
    </div>

    <div class="panel-soft p-6 animate-slide-up stagger-2">
      <div class="flex items-center justify-between mb-5">
        <div class="flex items-center gap-3">
          <div class="icon-soft icon-soft-info" style="width:40px;height:40px;border-radius:12px;">
            <UIcon name="i-heroicons-information-circle" class="h-5 w-5" />
          </div>
          <h3 class="text-lg font-bold text-slate-800 dark:text-white">基本信息</h3>
        </div>
        <UButton
          v-if="project?.status === 'ERROR'"
          variant="solid"
          size="sm"
          icon="i-heroicons-arrow-path"
          :loading="retrying"
          @click="handleRetryClone"
        >
          重新克隆
        </UButton>
      </div>
      <dl class="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div>
          <dt class="text-sm text-slate-500 font-medium">仓库地址</dt>
          <dd class="mt-1 text-sm font-semibold text-slate-800 dark:text-white">{{ project?.repoUrl }}</dd>
        </div>
        <div>
          <dt class="text-sm text-slate-500 font-medium">默认分支</dt>
          <dd class="mt-1 text-sm font-semibold text-slate-800 dark:text-white">{{ project?.defaultBranch }}</dd>
        </div>
        <div>
          <dt class="text-sm text-slate-500 font-medium">状态</dt>
          <dd class="mt-1 flex items-center gap-2">
            <span class="badge-soft" :class="statusBadgeClass(project?.status ?? '')">{{ statusLabel(project?.status ?? '') }}</span>
            <span v-if="project?.status === 'ERROR'" class="text-xs text-red-500">{{ project?.description }}</span>
          </dd>
        </div>
        <div>
          <dt class="text-sm text-slate-500 font-medium">创建时间</dt>
          <dd class="mt-1 text-sm font-semibold text-slate-800 dark:text-white">{{ project?.createdAt }}</dd>
        </div>
      </dl>
    </div>

    <div class="soft-card-flat animate-slide-up stagger-3">
      <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-700/50">
        <div class="flex items-center gap-3">
          <div class="icon-soft icon-soft-success" style="width:40px;height:40px;border-radius:12px;">
            <UIcon name="i-heroicons-magnifying-glass" class="h-5 w-5" />
          </div>
          <h3 class="text-lg font-bold text-slate-800 dark:text-white">审查记录</h3>
        </div>
        <UButton :to="`/reviews/create?projectId=${project?.id}`" variant="solid" size="sm" icon="i-heroicons-plus">
          发起审查
        </UButton>
      </div>
      <div class="p-4">
        <UTable :rows="reviews" :columns="reviewColumns" :loading="loading" :empty-state="{ icon: 'i-heroicons-magnifying-glass', label: '暂无审查记录' }">
          <template #status-data="{ row }">
            <span class="badge-soft" :class="statusBadgeClass(row.status)">{{ statusLabel(row.status) }}</span>
          </template>
          <template #actions-data="{ row }">
            <UButton :to="`/reviews/${row.id}`" variant="ghost" size="xs" icon="i-heroicons-arrow-right">查看</UButton>
          </template>
        </UTable>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Project } from '~/types/project'
import type { Review } from '~/types/review'
import type { PageResult } from '~/types/api'

const route = useRoute()
const { get, post } = useApi()
const projectId = computed(() => Number(route.params.id))
const loading = ref(false)
const retrying = ref(false)
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

function statusBadgeClass(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'badge-soft-warning', CLONING: 'badge-soft-info', READY: 'badge-soft-success', ERROR: 'badge-soft-error',
    RUNNING: 'badge-soft-info', COMPLETED: 'badge-soft-success', FAILED: 'badge-soft-error',
  }
  return map[status] ?? 'badge-soft-info'
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

async function handleRetryClone() {
  retrying.value = true
  try {
    await post(`/projects/${projectId.value}/retry-clone`)
    await loadData()
  } catch {
    // useApi 统一处理错误
  } finally {
    retrying.value = false
  }
}

onMounted(loadData)
</script>