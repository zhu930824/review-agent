<template>
  <div>
    <!-- Header -->
    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px">
      <a-space direction="vertical" :size="4">
        <a-typography-title :level="4" style="margin: 0">项目管理</a-typography-title>
        <a-typography-text type="secondary">管理您的代码仓库项目</a-typography-text>
      </a-space>
      <a-button type="primary" @click="showCreateModal = true">
        <template #icon><PlusOutlined /></template>
        创建项目
      </a-button>
    </div>

    <!-- 项目网格 -->
    <a-row v-if="projects.length" :gutter="[24, 24]">
      <a-col v-for="project in projects" :key="project.id" :xs="24" :lg="12" :xxl="8">
        <ProjectCard :project="project" />
      </a-col>
    </a-row>

    <!-- 空状态 -->
    <a-empty
      v-if="!projects.length && !loading"
      description="暂无项目，点击右上角按钮创建第一个项目"
      style="margin-top: 48px"
    >
      <template #image>
        <FolderOpenOutlined style="font-size: 64px; color: #d1d5db" />
      </template>
    </a-empty>

    <!-- 加载状态 -->
    <div v-if="loading" style="display: flex; justify-content: center; padding: 48px 0">
      <a-spin size="large" />
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" style="display: flex; justify-content: center; margin-top: 24px">
      <a-pagination v-model:current="currentPage" :total="total" :page-size="pageSize" show-size-changer />
    </div>

    <!-- 创建项目 Modal -->
    <a-modal
      v-model:open="showCreateModal"
      title="创建项目"
      :confirm-loading="creating"
      @ok="handleCreate"
      width="520px"
    >
      <a-form layout="vertical" :model="createForm">
        <a-form-item label="项目名称" required>
          <a-input v-model:value="createForm.name" placeholder="输入项目名称">
            <template #prefix><FolderOutlined /></template>
          </a-input>
        </a-form-item>

        <a-form-item label="项目描述">
          <a-textarea v-model:value="createForm.description" placeholder="简要描述项目（可选）" :rows="3" />
        </a-form-item>

        <a-form-item label="Git 仓库地址" required>
          <a-input v-model:value="createForm.repoUrl" placeholder="https://github.com/owner/repo">
            <template #prefix><LinkOutlined /></template>
          </a-input>
        </a-form-item>

        <a-form-item label="默认分支">
          <a-input v-model:value="createForm.defaultBranch" placeholder="main" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { PlusOutlined, FolderOutlined, LinkOutlined, FolderOpenOutlined } from '@ant-design/icons-vue'
import { useApi } from '@/composables/useApi'
import type { Project } from '@/types/project'
import type { PageResult } from '@/types/api'

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

