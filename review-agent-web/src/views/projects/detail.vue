<template>
  <div>
    <!-- Header -->
    <div style="margin-bottom: 24px">
      <a-button type="text" style="padding: 0; margin-bottom: 8px" @click="router.push('/projects')">
        <ArrowLeftOutlined />
        返回
      </a-button>
      <a-typography-title :level="3" style="margin: 0">
        {{ project?.name ?? '加载中...' }}
      </a-typography-title>
    </div>

    <!-- 基本信息卡片 -->
    <a-card :bordered="false" style="border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); margin-bottom: 24px">
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px">
        <a-space :size="12">
          <div style="width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border-radius: 12px; background: #e6f4ff; color: #1677ff">
            <InfoCircleOutlined style="font-size: 20px" />
          </div>
          <a-typography-title :level="5" style="margin: 0">基本信息</a-typography-title>
        </a-space>
        <a-space :size="8" wrap>
          <a-button size="small" @click="openEditProject">
            <template #icon><EditOutlined /></template>
            编辑
          </a-button>
          <a-popconfirm
            title="确认删除这个项目？"
            ok-text="删除"
            cancel-text="取消"
            @confirm="handleDeleteProject"
          >
            <a-button size="small" danger :loading="deleting">
              <template #icon><DeleteOutlined /></template>
              删除
            </a-button>
          </a-popconfirm>
          <a-button
            v-if="project?.status === 'ERROR'"
            type="primary"
            size="small"
            :loading="retrying"
            @click="handleRetryClone"
          >
            <template #icon><ReloadOutlined /></template>
            重新克隆
          </a-button>
        </a-space>
      </div>

      <a-descriptions :column="{ xs: 1, sm: 2 }" bordered size="small">
        <a-descriptions-item label="项目描述" :span="{ xs: 1, sm: 2 }">
          {{ project?.description || '暂无描述' }}
        </a-descriptions-item>
        <a-descriptions-item label="仓库地址">
          <a-typography-text copyable>{{ project?.repoUrl }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="默认分支">{{ project?.defaultBranch }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusTagColor(project?.status ?? '')">{{ statusLabel(project?.status ?? '') }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ project?.createdAt }}</a-descriptions-item>
        <a-descriptions-item v-if="project?.status === 'ERROR' && project?.cloneErrorMessage" label="克隆失败信息" :span="2">
          <a-typography-text type="danger">{{ project.cloneErrorMessage }}</a-typography-text>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>

    <!-- 审查记录卡片 -->
    <a-card :bordered="false" style="border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06)">
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px">
        <a-space :size="12">
          <div style="width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border-radius: 12px; background: #f6ffed; color: #52c41a">
            <SearchOutlined style="font-size: 20px" />
          </div>
          <a-typography-title :level="5" style="margin: 0">审查记录</a-typography-title>
        </a-space>
        <a-button type="primary" size="small" @click="router.push(`/reviews/create?projectId=${project?.id}`)">
          <template #icon><PlusOutlined /></template>
          发起审查
        </a-button>
      </div>

      <a-table
        :dataSource="reviews"
        :columns="reviewColumns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusTagColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-button type="link" size="small" @click="router.push(`/reviews/${record.id}`)">
              查看
              <ArrowRightOutlined />
            </a-button>
          </template>
        </template>
        <template #emptyText>
          <a-empty description="暂无审查记录" />
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="editModalOpen"
      title="编辑项目"
      ok-text="保存"
      cancel-text="取消"
      :confirm-loading="updating"
      @ok="handleUpdateProject"
    >
      <a-form layout="vertical" :model="editForm">
        <a-form-item label="项目名称" required>
          <a-input v-model:value="editForm.name" placeholder="输入项目名称" />
        </a-form-item>
        <a-form-item label="项目描述">
          <a-textarea v-model:value="editForm.description" placeholder="简要描述项目（可选）" :rows="3" />
        </a-form-item>
        <a-form-item label="Git 仓库地址" required>
          <a-input v-model:value="editForm.repoUrl" placeholder="https://github.com/owner/repo" />
        </a-form-item>
        <a-form-item label="默认分支">
          <a-input v-model:value="editForm.defaultBranch" placeholder="main" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useApi } from '@/composables/useApi'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, InfoCircleOutlined, ReloadOutlined, SearchOutlined, PlusOutlined, ArrowRightOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import type { Project } from '@/types/project'
import type { Review } from '@/types/review'
import type { PageResult } from '@/types/api'

const router = useRouter()
const route = useRoute()
const { get, post, put, del } = useApi()
const projectId = computed(() => Number(route.params.id))
const loading = ref(false)
const retrying = ref(false)
const updating = ref(false)
const deleting = ref(false)
const editModalOpen = ref(false)
const project = ref<Project | null>(null)
const reviews = ref<Review[]>([])
const editForm = reactive({
  name: '',
  description: '',
  repoUrl: '',
  defaultBranch: 'main',
})

const reviewColumns = [
  { title: '源分支', dataIndex: 'sourceBranch', key: 'sourceBranch' },
  { title: '目标分支', dataIndex: 'targetBranch', key: 'targetBranch' },
  { title: '模式', dataIndex: 'reviewMode', key: 'reviewMode' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '操作', key: 'actions', width: 100 },
]

function statusTagColor(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'gold',
    CLONING: 'processing',
    READY: 'success',
    ERROR: 'error',
    RUNNING: 'processing',
    COMPLETED: 'success',
    FAILED: 'error',
  }
  return map[status] ?? 'default'
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

function openEditProject() {
  if (!project.value) return
  Object.assign(editForm, {
    name: project.value.name || '',
    description: project.value.description || '',
    repoUrl: project.value.repoUrl || '',
    defaultBranch: project.value.defaultBranch || 'main',
  })
  editModalOpen.value = true
}

async function handleUpdateProject() {
  if (!editForm.name || !editForm.repoUrl) {
    message.warning('请填写项目名称和仓库地址')
    return
  }
  updating.value = true
  try {
    const res = await put<Project>(`/projects/${projectId.value}`, {
      name: editForm.name,
      description: editForm.description || undefined,
      repoUrl: editForm.repoUrl,
      defaultBranch: editForm.defaultBranch || 'main',
    })
    if (res.data) project.value = res.data
    editModalOpen.value = false
    message.success('项目已更新')
  } catch (e) {
    console.error('更新项目失败', e)
    message.error('更新项目失败')
  } finally {
    updating.value = false
  }
}

async function handleDeleteProject() {
  deleting.value = true
  try {
    await del(`/projects/${projectId.value}`)
    message.success('项目已删除')
    router.push('/projects')
  } catch (e) {
    console.error('删除项目失败', e)
    message.error('删除项目失败')
  } finally {
    deleting.value = false
  }
}

onMounted(loadData)
</script>
