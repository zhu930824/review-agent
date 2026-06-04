<template>
  <a-space direction="vertical" :size="16" style="width:100%">
    <!-- 标题栏 -->
    <a-space style="width:100%;justify-content:space-between;flex-wrap:wrap">
      <div>
        <h2 style="margin:0">发起 Pre-PR 审查</h2>
        <p style="margin-top:4px;color:#94a3b8;font-size:13px">选择项目、分支和审查策略，生成提交前质量结论</p>
      </div>
      <RouterLink to="/settings/models">
        <a-button>
          <template #icon><SettingOutlined /></template>
          模型配置
        </a-button>
      </RouterLink>
    </a-space>

    <a-row :gutter="16">
      <!-- 左侧主表单 -->
      <a-col :xl="16" :span="24">
        <a-card size="small" style="margin-bottom:16px">
          <a-form layout="vertical" @finish="handleSubmit">
            <!-- 项目/分支选择 -->
            <a-row :gutter="16">
              <a-col :md="8" :span="24">
                <a-form-item label="选择项目" required>
                  <a-select
                    v-model:value="form.projectId"
                    placeholder="选择项目"
                    :loading="loadingProjects"
                    show-search
                    option-filter-prop="label"
                    style="width:100%"
                  >
                    <a-select-option v-for="opt in projectOptions" :key="opt.value" :value="opt.value">
                      {{ opt.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :span="24">
                <a-form-item label="源分支" required>
                  <a-select
                    v-model:value="form.sourceBranch"
                    placeholder="选择源分支"
                    :loading="loadingBranches"
                    :disabled="!form.projectId"
                    show-search
                    option-filter-prop="label"
                    style="width:100%"
                  >
                    <a-select-option v-for="opt in branchOptions" :key="opt.value" :value="opt.value">
                      {{ opt.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :span="24">
                <a-form-item label="目标分支" required>
                  <a-select
                    v-model:value="form.targetBranch"
                    placeholder="选择目标分支"
                    :loading="loadingBranches"
                    :disabled="!form.projectId"
                    show-search
                    option-filter-prop="label"
                    style="width:100%"
                  >
                    <a-select-option v-for="opt in branchOptions" :key="opt.value" :value="opt.value">
                      {{ opt.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>

            <a-divider>审查策略</a-divider>

            <!-- 加载中 -->
            <div v-if="loadingStrategies" style="text-align:center;padding:32px 0">
              <a-spin />
            </div>

            <!-- 策略卡片 -->
            <a-row v-else :gutter="12">
              <a-col v-for="strategy in strategyOptions" :key="strategy.id" :lg="12" :span="24" style="margin-bottom:12px">
                <a-card
                  hoverable
                  size="small"
                  :body-style="{ padding: '16px' }"
                  :class="{ 'ant-card-selected': form.strategyKey === strategy.strategyKey }"
                  :style="form.strategyKey === strategy.strategyKey ? { borderColor: '#6366f1', boxShadow: '0 0 0 2px rgba(99,102,241,0.2)' } : {}"
                  @click="selectStrategy(strategy)"
                >
                  <a-space :size="8" style="width:100%;justify-content:space-between">
                    <a-space :size="8">
                      <ApiOutlined style="font-size:20px;color:#6366f1" />
                      <span style="font-weight:600;font-size:13px">{{ strategy.name }}</span>
                    </a-space>
                    <a-tag color="processing">{{ strategy.reviewMode }}</a-tag>
                  </a-space>
                  <div style="font-size:12px;color:#94a3b8;margin-top:8px;line-height:1.5">
                    {{ strategy.description }}
                  </div>
                  <a-space :size="4" wrap style="margin-top:8px">
                    <a-tag v-for="item in strategy.recommendedFor || []" :key="item">{{ item }}</a-tag>
                  </a-space>
                </a-card>
              </a-col>
            </a-row>

            <!-- 策略角色编排 -->
            <a-card size="small" style="background:#f8fafc;margin-top:12px">
              <a-space :size="8" style="width:100%;justify-content:space-between">
                <div>
                  <div style="font-weight:600;font-size:13px">策略模型编排</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:2px">提交时会编译为后端兼容的 modelsConfig</div>
                </div>
                <a-tag color="processing">{{ selectedStrategy?.reviewMode || '-' }}</a-tag>
              </a-space>
              <div style="margin-top:12px">
                <a-space direction="vertical" :size="4" style="width:100%">
                  <div
                    v-for="label in selectedRoleLabels"
                    :key="label"
                    style="display:flex;align-items:center;gap:8px;padding:8px 12px;border-radius:10px;
                           background:linear-gradient(135deg,rgba(99,102,241,0.05),rgba(168,85,247,0.05));
                           font-size:13px"
                  >
                    <ApiOutlined style="color:#6366f1;font-size:14px" />
                    <span>{{ label }}</span>
                  </div>
                </a-space>
              </div>
            </a-card>

            <!-- MCP 开关 -->
            <a-form-item label="MCP 外部工具" style="margin-top:12px">
              <a-switch v-model:checked="form.mcpEnabled" />
            </a-form-item>

            <!-- 高级 JSON 配置 -->
            <a-collapse style="margin-top:8px">
              <a-collapse-panel key="advanced" header="高级 JSON 配置">
                <a-textarea
                  v-model:value="form.modelsConfigOverride"
                  :rows="6"
                  :placeholder="compiledJsonPreview"
                />
                <div style="margin-top:8px;font-size:12px;color:#94a3b8">
                  填写后将覆盖策略自动生成的模型配置。</div>
              </a-collapse-panel>
            </a-collapse>

            <!-- 提交按钮 -->
            <a-space :size="8" style="margin-top:16px">
              <a-button type="primary" html-type="submit" :loading="submitting">
                <template #icon><PlayCircleOutlined /></template>
                开始审查              </a-button>
              <RouterLink to="/">
                <a-button>取消</a-button>
              </RouterLink>
            </a-space>
          </a-form>
        </a-card>
      </a-col>

      <!-- 右侧边栏 -->
      <a-col :xl="8" :span="24">
        <!-- 质量闸门 -->
        <a-card size="small" style="margin-bottom:16px">
          <template #title>
            <a-space :size="8">
              <SafetyOutlined style="font-size:16px;color:#ef4444" />
              <span style="font-weight:600;font-size:14px">质量闸门</span>
            </a-space>
          </template>
          <a-space direction="vertical" :size="12" style="width:100%">
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">阻断级别</div>
              <a-space :size="4" wrap>
                <a-tag v-for="level in gatePolicy.blockOn" :key="level" color="red">{{ level }}</a-tag>
              </a-space>
            </div>
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">需人工复核</div>
              <a-space :size="4" wrap>
                <a-tag v-for="level in gatePolicy.requireHumanReviewOn" :key="level" color="orange">{{ level }}</a-tag>
              </a-space>
            </div>
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">建议关注</div>
              <a-space :size="4" wrap>
                <a-tag v-for="level in gatePolicy.advisoryOn" :key="level" color="blue">{{ level }}</a-tag>
              </a-space>
            </div>
          </a-space>
        </a-card>

        <!-- 配置预览 -->
        <a-card size="small" title="配置预览">
          <template #title>
            <a-space :size="8">
              <CodeOutlined style="font-size:16px;color:#94a3b8" />
              <span style="font-weight:600;font-size:14px">配置预览</span>
            </a-space>
          </template>
          <pre style="max-height:384px;overflow:auto;border-radius:12px;
                     background:rgba(15,23,42,0.05);padding:16px;
                     font-size:12px;font-family:monospace;margin:0">{{ compiledJsonPreview }}</pre>
        </a-card>
      </a-col>
    </a-row>
  </a-space>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useApi } from '@/composables/useApi'
import { SettingOutlined, ApiOutlined, PlayCircleOutlined, SafetyOutlined, CodeOutlined } from '@ant-design/icons-vue'
import type { Project } from '@/types/project'
import type { Review } from '@/types/review'
import type { PageResult } from '@/types/api'
import { buildReviewCreatePayload, compileReviewCreateConfig, getReviewCreateEndpoint, getReviewCreateRoleLabels } from '@/utils/reviewCreate'

interface ApiReviewStrategy {
  id: number
  strategyKey: string
  name: string
  reviewMode: string
  description: string
  recommendedFor: string[]
  blockOn: string[]
  requireHumanReviewOn: string[]
  advisoryOn: string[]
  enabled: boolean
  roleBindings: { role: string; modelProfileName: string }[]
}

const router = useRouter()
const route = useRoute()
const { get, post } = useApi()
const submitting = ref(false)
const loadingStrategies = ref(false)
const loadingProjects = ref(false)
const loadingBranches = ref(false)

const projectOptions = ref<{ label: string; value: string }[]>([])
const branchOptions = ref<{ label: string; value: string }[]>([])
const strategyOptions = ref<ApiReviewStrategy[]>([])

const form = reactive({
  projectId: (route.query.projectId as string) || '',
  strategyId: '',
  strategyKey: '',
  sourceBranch: '',
  targetBranch: '',
  mcpEnabled: false,
  modelsConfigOverride: '',
})

const selectedStrategy = computed(() => strategyOptions.value.find(s => s.strategyKey === form.strategyKey))
const gatePolicy = computed(() => ({
  blockOn: selectedStrategy.value?.blockOn || [],
  requireHumanReviewOn: selectedStrategy.value?.requireHumanReviewOn || [],
  advisoryOn: selectedStrategy.value?.advisoryOn || [],
}))
const selectedRoleLabels = computed(() => {
  return getReviewCreateRoleLabels(selectedStrategy.value)
})

const compiledConfig = computed(() => compileReviewCreateConfig(selectedStrategy.value, form.mcpEnabled))
const compiledJsonPreview = computed(() => JSON.stringify(compiledConfig.value.modelsConfig, null, 2))

function selectStrategy(strategy: ApiReviewStrategy) {
  form.strategyId = strategy.strategyKey
  form.strategyKey = strategy.strategyKey
}

async function loadProjects() {
  loadingProjects.value = true
  try {
    const res = await get<PageResult<Project>>('/projects?pageNum=1&pageSize=100')
    if (res.data) {
      projectOptions.value = res.data.records
        .filter(p => p.status === 'READY')
        .map(p => ({ label: p.name, value: String(p.id) }))
    }
  } catch (e) { console.error('加载项目列表失败', e) }
  finally { loadingProjects.value = false }
}

async function loadBranches(projectId: string) {
  if (!projectId) {
    branchOptions.value = []
    return
  }
  loadingBranches.value = true
  try {
    const res = await get<string[]>(`/projects/${projectId}/branches`)
    if (res.data) {
      branchOptions.value = res.data.map(b => ({ label: b, value: b }))
    }
  } catch (e) { console.error('加载分支列表失败', e) }
  finally { loadingBranches.value = false }
}

watch(() => form.projectId, (newVal) => {
  form.sourceBranch = ''
  form.targetBranch = ''
  loadBranches(newVal)
})

async function loadStrategies() {
  loadingStrategies.value = true
  try {
    const res = await get<ApiReviewStrategy[]>('/model-config/strategies')
    if (res.data?.length) {
      strategyOptions.value = res.data.filter(s => s.enabled)
      if (strategyOptions.value.length && !form.strategyKey) {
        form.strategyKey = strategyOptions.value[0].strategyKey
        form.strategyId = strategyOptions.value[0].strategyKey
      }
    }
  } catch (e) { console.error('加载策略失败', e) }
  finally { loadingStrategies.value = false }
}

async function handleSubmit() {
  if (!form.projectId || !form.sourceBranch || !form.targetBranch || !form.strategyKey) return
  submitting.value = true
  try {
    const res = await post<Review>(getReviewCreateEndpoint(true), buildReviewCreatePayload(form, selectedStrategy.value!))
    if (res.data) router.push(`/reviews/${res.data.id}`)
  } catch { }
  finally { submitting.value = false }
}

onMounted(() => { loadProjects(); loadStrategies(); if (form.projectId) loadBranches(form.projectId) })
</script>

