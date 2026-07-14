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
          <a-button v-if="canManageProject" size="small" @click="openEditProject">
            <template #icon><EditOutlined /></template>
            编辑
          </a-button>
          <a-popconfirm
            v-if="canManageMembers"
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
            v-if="canManageProject && project?.status === 'ERROR'"
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

    <a-card :bordered="false" style="border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); margin-bottom: 24px">
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px">
        <a-space :size="12">
          <div style="width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border-radius: 12px; background: #f0fdf4; color: #16a34a">
            <BranchesOutlined style="font-size: 20px" />
          </div>
          <a-typography-title :level="5" style="margin: 0">仓库分支</a-typography-title>
        </a-space>
        <a-button size="small" :loading="branchesLoading" @click="loadBranches">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </div>

      <a-spin :spinning="branchesLoading">
        <a-alert
          v-if="branchesError"
          type="warning"
          show-icon
          :message="branchesError"
          style="margin-bottom: 12px"
        />
        <a-space v-if="branches.length" wrap :size="[8, 8]">
          <span
            v-for="branch in branches"
            :key="branch"
            style="display:inline-flex;align-items:center;gap:4px"
          >
            <a-tag
              :color="branch === project?.defaultBranch ? 'processing' : 'default'"
              style="padding: 4px 8px; margin-inline-end: 0"
            >
              {{ branch }}
            </a-tag>
            <a-button
              v-if="branch !== project?.defaultBranch"
              type="link"
              size="small"
              style="padding: 0 4px"
              @click="startReviewFromBranch(branch)"
            >
              发起审查
            </a-button>
          </span>
        </a-space>
        <a-empty v-else-if="!branchesError" description="暂无分支数据" :image="undefined" />
      </a-spin>
    </a-card>

    <a-card
      v-if="gitLabReviewPolicy?.configured"
      :bordered="false"
      style="border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); margin-bottom: 24px"
    >
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px">
        <a-space :size="12">
          <div style="width:40px;height:40px;display:flex;align-items:center;justify-content:center;border-radius:8px;background:#fff7e6;color:#d97706">
            <SafetyOutlined style="font-size:20px" />
          </div>
          <div>
            <a-typography-title :level="5" style="margin:0">GitLab 自动审核策略</a-typography-title>
            <div style="font-size:12px;color:#64748b;margin-top:2px">控制进入 AI 审核队列的 Merge Request</div>
          </div>
        </a-space>
        <a-button v-if="canManageIntegration" type="primary" size="small" :loading="policySaving" @click="saveGitLabReviewPolicy">保存策略</a-button>
      </div>
      <a-row :gutter="16">
        <a-col :xs="24" :md="8">
          <a-form-item label="自动审核" style="margin-bottom:12px">
            <a-switch v-model:checked="gitLabReviewPolicy.autoReviewEnabled" :disabled="!canManageIntegration" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :md="8">
          <a-form-item label="包含 Draft / WIP" style="margin-bottom:12px">
            <a-switch v-model:checked="gitLabReviewPolicy.reviewDrafts" :disabled="!canManageIntegration" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :md="8">
          <a-form-item label="自动回流 MR 摘要" style="margin-bottom:12px">
            <a-switch v-model:checked="gitLabReviewPolicy.publishSummaryEnabled" :disabled="!canManageIntegration" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :md="24">
          <a-form-item label="目标分支规则" style="margin-bottom:0">
            <a-textarea
              v-model:value="gitLabReviewPolicy.targetBranchPattern"
              :rows="2"
              :disabled="!canManageIntegration"
              placeholder="main, release/*（逗号或换行分隔；留空表示全部）"
              style="width:100%"
            />
          </a-form-item>
        </a-col>
      </a-row>
    </a-card>

    <a-card :bordered="false" style="border-radius:8px;box-shadow:0 1px 3px rgba(0,0,0,0.06);margin-bottom:24px">
      <div style="display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:16px">
        <a-space :size="12">
          <div style="width:40px;height:40px;display:flex;align-items:center;justify-content:center;border-radius:8px;background:#eef2ff;color:#4f46e5">
            <TeamOutlined style="font-size:20px" />
          </div>
          <div>
            <a-typography-title :level="5" style="margin:0">项目成员</a-typography-title>
            <div style="font-size:12px;color:#64748b;margin-top:2px">仓库、Review 和 GitLab 策略均按项目角色隔离</div>
          </div>
        </a-space>
        <a-space v-if="canManageMembers" :size="8" wrap>
          <a-input v-model:value="memberForm.username" size="small" placeholder="用户名" style="width:160px" />
          <a-select v-model:value="memberForm.role" size="small" style="width:140px">
            <a-select-option value="OWNER">Owner</a-select-option>
            <a-select-option value="MAINTAINER">Maintainer</a-select-option>
            <a-select-option value="REVIEWER">Reviewer</a-select-option>
          </a-select>
          <a-button type="primary" size="small" :loading="memberSaving" @click="addProjectMember">
            <template #icon><UserAddOutlined /></template>
            添加成员
          </a-button>
        </a-space>
      </div>
      <a-list :data-source="projectMembers" :loading="membersLoading" size="small">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta :description="`${item.email || 'No email'} · ${item.status}`">
              <template #title>
                {{ item.displayName || item.username }}
                <span style="margin-left:6px;color:#94a3b8;font-size:12px">@{{ item.username }}</span>
                <a-tag v-if="item.currentUser" color="blue" style="margin-left:6px">当前用户</a-tag>
              </template>
            </a-list-item-meta>
            <a-space :size="6">
              <a-select
                v-if="canManageMembers"
                :value="item.role"
                size="small"
                style="width:140px"
                :loading="updatingMemberId === item.userId"
                @change="updateProjectMemberRole(item, $event)"
              >
                <a-select-option value="OWNER">Owner</a-select-option>
                <a-select-option value="MAINTAINER">Maintainer</a-select-option>
                <a-select-option value="REVIEWER">Reviewer</a-select-option>
              </a-select>
              <a-tag v-else>{{ item.role }}</a-tag>
              <a-popconfirm
                v-if="canManageMembers"
                title="确认移除该项目成员？"
                ok-text="移除"
                cancel-text="取消"
                @confirm="removeProjectMember(item)"
              >
                <a-button type="link" size="small" danger :loading="removingMemberId === item.userId">移除</a-button>
              </a-popconfirm>
            </a-space>
          </a-list-item>
        </template>
      </a-list>
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
import { useAccess } from '@/composables/useAccess'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, InfoCircleOutlined, ReloadOutlined, SearchOutlined, PlusOutlined, ArrowRightOutlined, EditOutlined, DeleteOutlined, BranchesOutlined, SafetyOutlined, TeamOutlined, UserAddOutlined } from '@ant-design/icons-vue'
import type { Project, ProjectGitLabReviewPolicy, ProjectMember, ProjectMemberRole } from '@/types/project'
import type { Review } from '@/types/review'
import type { PageResult } from '@/types/api'

const router = useRouter()
const route = useRoute()
const { get, post, put, del } = useApi()
const { can } = useAccess()
const projectId = computed(() => Number(route.params.id))
const loading = ref(false)
const retrying = ref(false)
const updating = ref(false)
const deleting = ref(false)
const policySaving = ref(false)
const editModalOpen = ref(false)
const project = ref<Project | null>(null)
const reviews = ref<Review[]>([])
const branches = ref<string[]>([])
const branchesLoading = ref(false)
const branchesError = ref('')
const gitLabReviewPolicy = ref<ProjectGitLabReviewPolicy | null>(null)
const projectMembers = ref<ProjectMember[]>([])
const membersLoading = ref(false)
const memberSaving = ref(false)
const updatingMemberId = ref<number | null>(null)
const removingMemberId = ref<number | null>(null)
const memberForm = reactive({ username: '', role: 'REVIEWER' as ProjectMemberRole })
const currentProjectMember = computed(() => projectMembers.value.find(member => member.currentUser) ?? null)
const canManageMembers = computed(() => can('ACCESS_MANAGE') || currentProjectMember.value?.role === 'OWNER')
const canManageProject = computed(() => can('ACCESS_MANAGE') || ['OWNER', 'MAINTAINER'].includes(currentProjectMember.value?.role || ''))
const canManageIntegration = computed(() => can('ACCESS_MANAGE') || currentProjectMember.value?.role === 'OWNER')
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
    const [projectRes, reviewsRes, branchesRes, policyRes, membersRes] = await Promise.all([
      get<Project>(`/projects/${projectId.value}`),
      get<PageResult<Review>>(`/reviews?projectId=${projectId.value}&pageNum=1&pageSize=20`),
      get<string[]>(`/projects/${projectId.value}/branches`).catch(() => null),
      get<ProjectGitLabReviewPolicy>(`/projects/${projectId.value}/gitlab-review-policy`).catch(() => null),
      get<ProjectMember[]>(`/projects/${projectId.value}/members`).catch(() => null),
    ])
    if (projectRes.data) project.value = projectRes.data
    if (reviewsRes.data) reviews.value = reviewsRes.data.records
    branches.value = branchesRes?.data ?? []
    branchesError.value = branchesRes ? '' : '分支列表暂时不可用，请确认仓库已克隆完成后重试'
    gitLabReviewPolicy.value = policyRes?.data ?? null
    projectMembers.value = membersRes?.data ?? []
  } catch (e) {
    console.error('加载项目详情失败', e)
  } finally {
    loading.value = false
  }
}

async function loadProjectMembers() {
  membersLoading.value = true
  try {
    const res = await get<ProjectMember[]>(`/projects/${projectId.value}/members`)
    projectMembers.value = res.data ?? []
  } catch (e) {
    console.error('加载项目成员失败', e)
  } finally {
    membersLoading.value = false
  }
}

async function addProjectMember() {
  if (!memberForm.username.trim()) {
    message.warning('请输入用户名')
    return
  }
  memberSaving.value = true
  try {
    await put<ProjectMember>(`/projects/${projectId.value}/members`, {
      username: memberForm.username.trim(),
      role: memberForm.role,
    })
    memberForm.username = ''
    await loadProjectMembers()
    message.success('项目成员已保存')
  } catch (e) {
    console.error('保存项目成员失败', e)
    message.error(e instanceof Error ? e.message : '保存项目成员失败')
  } finally {
    memberSaving.value = false
  }
}

async function updateProjectMemberRole(member: ProjectMember, role: ProjectMemberRole) {
  updatingMemberId.value = member.userId
  try {
    await put<ProjectMember>(`/projects/${projectId.value}/members`, { username: member.username, role })
    await loadProjectMembers()
    message.success('项目角色已更新')
  } catch (e) {
    console.error('更新项目角色失败', e)
    message.error(e instanceof Error ? e.message : '更新项目角色失败')
  } finally {
    updatingMemberId.value = null
  }
}

async function removeProjectMember(member: ProjectMember) {
  removingMemberId.value = member.userId
  try {
    await del(`/projects/${projectId.value}/members/${member.userId}`)
    if (member.currentUser && !can('ACCESS_MANAGE')) {
      router.push('/projects')
      return
    }
    await loadProjectMembers()
    message.success('项目成员已移除')
  } catch (e) {
    console.error('移除项目成员失败', e)
    message.error(e instanceof Error ? e.message : '移除项目成员失败')
  } finally {
    removingMemberId.value = null
  }
}

async function saveGitLabReviewPolicy() {
  if (!gitLabReviewPolicy.value) return
  policySaving.value = true
  try {
    const res = await put<ProjectGitLabReviewPolicy>(`/projects/${projectId.value}/gitlab-review-policy`, {
      autoReviewEnabled: gitLabReviewPolicy.value.autoReviewEnabled,
      reviewDrafts: gitLabReviewPolicy.value.reviewDrafts,
      publishSummaryEnabled: gitLabReviewPolicy.value.publishSummaryEnabled,
      targetBranchPattern: gitLabReviewPolicy.value.targetBranchPattern || null,
    })
    if (res.data) gitLabReviewPolicy.value = res.data
    message.success('GitLab 自动审核策略已保存')
  } catch (e) {
    console.error('保存 GitLab 自动审核策略失败', e)
    message.error('保存 GitLab 自动审核策略失败')
  } finally {
    policySaving.value = false
  }
}

async function loadBranches() {
  branchesLoading.value = true
  branchesError.value = ''
  try {
    const res = await get<string[]>(`/projects/${projectId.value}/branches`)
    branches.value = res.data ?? []
  } catch (e) {
    console.error('加载项目分支失败', e)
    branchesError.value = '分支列表暂时不可用，请确认仓库已克隆完成后重试'
  } finally {
    branchesLoading.value = false
  }
}

function startReviewFromBranch(branch: string) {
  const query = new URLSearchParams({
    projectId: String(projectId.value),
    sourceBranch: branch,
    targetBranch: project.value?.defaultBranch || 'main',
  })
  router.push(`/reviews/create?${query.toString()}`)
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
