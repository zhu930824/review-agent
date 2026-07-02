<template>
  <a-space direction="vertical" :size="16" style="width:100%">
    <a-space style="width:100%;justify-content:space-between;align-items:flex-start;flex-wrap:wrap">
      <div>
        <h2 style="margin:0">模型配置</h2>
        <p style="margin-top:4px;color:#94a3b8;font-size:13px">管理模型供应商、模型档案、审查策略和质量闸门</p>
      </div>
      <a-button type="primary" @click="openCreateProvider">
        <template #icon><PlusOutlined /></template>
        新增供应商
      </a-button>
    </a-space>

    <!-- Provider Cards -->
    <a-row :gutter="16">
      <a-col v-for="(provider, index) in providerCounts" :key="provider.providerId" :lg="8" :span="24" style="margin-bottom:16px">
        <a-card size="small" hoverable>
          <a-space direction="vertical" :size="4" style="width:100%">
            <div style="font-weight:600;font-size:14px">{{ provider.providerName }}</div>
            <div style="font-size:12px;color:#94a3b8">{{ provider.enabledProfiles }} / {{ provider.totalProfiles }} 个模型启用</div>
            <a-tag :color="provider.enabled ? 'green' : 'default'">{{ provider.enabled ? '已启用' : '未启用' }}</a-tag>
            <a-space :size="4" style="margin-top:8px">
              <a-button size="small" @click="openEditProvider(provider.providerRaw)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-popconfirm
                title="确认删除这个模型供应商？"
                ok-text="删除"
                cancel-text="取消"
                @confirm="deleteProvider(provider.providerRaw)"
              >
                <a-button size="small" danger :loading="deletingProviderId === provider.providerRaw.id">
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <a-card size="small" style="margin-bottom:16px">
      <template #title>
        <a-space :size="8">
          <ApiOutlined style="font-size:20px;color:#4f46e5" />
          <span style="font-weight:600">模型调用烟测</span>
        </a-space>
      </template>
      <template #extra>
        <a-tag :color="smokeTestResult?.status === 'SUCCESS' ? 'green' : smokeTestResult?.status === 'FAILED' ? 'red' : 'default'">
          {{ smokeTestResult?.status || '未测试' }}
        </a-tag>
      </template>

      <a-row :gutter="[16, 8]">
        <a-col :xs="24" :lg="10">
          <a-form layout="vertical">
            <a-form-item label="测试模型">
              <a-select
                v-model:value="smokeTestForm.profileId"
                placeholder="选择一个模型档案"
                :options="smokeProfileOptions"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="测试 Prompt">
              <a-textarea
                v-model:value="smokeTestForm.prompt"
                :rows="4"
                placeholder="输入一条最小测试 Prompt"
              />
            </a-form-item>
            <a-space :size="8" wrap>
              <a-button type="primary" :loading="smokeTesting" @click="runSmokeTest">
                发起测试调用
              </a-button>
              <a-button @click="resetSmokeTestPrompt">恢复默认 Prompt</a-button>
            </a-space>
          </a-form>
        </a-col>
        <a-col :xs="24" :lg="14">
          <div v-if="smokeTestResult" style="height:100%;border-radius:10px;background:#f8fafc;padding:14px">
            <a-space :size="8" wrap style="margin-bottom:10px">
              <a-tag :color="smokeTestResult.status === 'SUCCESS' ? 'green' : 'red'">{{ smokeTestResult.status }}</a-tag>
              <a-tag>{{ smokeTestResult.provider || '-' }}</a-tag>
              <a-tag>{{ smokeTestResult.modelName || '-' }}</a-tag>
              <a-tag>{{ smokeTestResult.promptVersion || '-' }}</a-tag>
            </a-space>
            <div v-if="smokeTestResult.errorMessage" style="font-size:13px;color:#dc2626;line-height:1.5;margin-bottom:10px">
              {{ smokeTestResult.errorMessage }}
            </div>
            <pre v-if="smokeTestResult.content" style="max-height:160px;overflow:auto;margin:0 0 10px;padding:12px;border-radius:8px;background:#fff;font-size:12px;white-space:pre-wrap">{{ smokeTestResult.content }}</pre>
            <a-space :size="8" wrap>
              <a-tag>Prompt Tokens {{ smokeTestResult.promptTokens ?? '-' }}</a-tag>
              <a-tag>Completion Tokens {{ smokeTestResult.completionTokens ?? '-' }}</a-tag>
              <a-tag>Cost Micro Cents {{ smokeTestResult.costMicroCents ?? '-' }}</a-tag>
            </a-space>
          </div>
          <div v-else style="height:100%;min-height:178px;border-radius:10px;background:#f8fafc;padding:16px;color:#64748b;font-size:13px;line-height:1.6">
            用当前后端注入的 ModelInvocationPort 发起一次最小调用。未配置真实 HTTP adapter 时，后端会返回 FAILED 和可读的配置提示；配置完成后会记录模型调用遥测。
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!-- Main Grid -->
    <a-row :gutter="16">
      <!-- 模型档案表格 -->
      <a-col :xl="16" :span="24">
        <a-card size="small" :title="null" style="margin-bottom:16px">
          <template #title>
            <a-space :size="8">
              <ApiOutlined style="font-size:20px;color:#4f46e5" />
              <span style="font-weight:600">模型档案</span>
            </a-space>
          </template>
          <template #extra>
            <a-space :size="8">
              <a-tag color="processing">{{ modelProfiles.length }} 个可用</a-tag>
              <a-button size="small" type="primary" @click="openCreateProfile">
                <template #icon><PlusOutlined /></template>
                新增档案
              </a-button>
            </a-space>
          </template>
          <a-table
            :columns="modelColumns"
            :data-source="modelProfiles"
            :loading="loading"
            :pagination="false"
            row-key="id"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'displayName'">
                <div>
                  <div style="font-weight:600;font-size:13px">{{ record.displayName }}</div>
                  <div style="font-size:12px;color:#94a3b8;font-family:monospace">{{ record.modelName }}</div>
                </div>
              </template>
              <template v-else-if="column.key === 'providerName'">
                <a-tag>{{ record.providerName || record.providerId }}</a-tag>
              </template>
              <template v-else-if="column.key === 'capabilityTags'">
                <a-space :size="4" wrap>
                  <a-tag v-for="tag in record.capabilityTags" :key="tag" color="processing">{{ tag }}</a-tag>
                </a-space>
              </template>
              <template v-else-if="column.key === 'enabled'">
                <a-tag :color="record.enabled ? 'green' : 'default'">{{ record.enabled ? '启用' : '停用' }}</a-tag>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space :size="4">
                  <a-button type="link" size="small" @click="openEditProfile(record)">
                    <template #icon><EditOutlined /></template>
                    编辑
                  </a-button>
                  <a-popconfirm
                    title="确认删除这个模型档案？"
                    ok-text="删除"
                    cancel-text="取消"
                    @confirm="deleteProfile(record)"
                  >
                    <a-button type="link" size="small" danger :loading="deletingProfileId === record.id">
                      <template #icon><DeleteOutlined /></template>
                      删除
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <!-- 审查策略侧边栏 -->
      <a-col :xl="8" :span="24">
        <a-card size="small" title="审查策略" style="margin-bottom:16px">
          <template #title>
            <a-space :size="8">
              <AppstoreOutlined style="font-size:16px;color:#4f46e5" />
              <span style="font-weight:600;font-size:14px">审查策略</span>
            </a-space>
          </template>
          <a-space direction="vertical" :size="8" style="width:100%">
            <a-card
              v-for="strategy in strategySummaries"
              :key="strategy.id"
              size="small"
              :body-style="{ padding: '12px' }"
              style="background:#f8fafc"
            >
              <a-space :size="8" style="width:100%;justify-content:space-between">
                <div>
                  <div style="font-weight:600;font-size:13px">{{ strategy.name }}</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:2px">{{ strategy.description }}</div>
                </div>
                <a-space :size="4">
                  <a-tag color="processing">{{ strategy.reviewMode }}</a-tag>
                  <a-button type="link" size="small" @click="openEditStrategy(strategy)">
                    <template #icon><EditOutlined /></template>
                  </a-button>
                  <a-popconfirm
                    title="确认删除这个审查策略？"
                    ok-text="删除"
                    cancel-text="取消"
                    @confirm="deleteStrategy(strategy)"
                  >
                    <a-button type="link" size="small" danger :loading="deletingStrategyId === strategy.id">
                      <template #icon><DeleteOutlined /></template>
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </a-space>

              <!-- 角色绑定 -->
              <div style="margin-top:8px">
                <a-space v-for="label in strategy.roleLabels" :key="label" :size="4" style="font-size:12px">
                  <ApiOutlined style="color:#6366f1;font-size:12px" />
                  <span>{{ label }}</span>
                </a-space>
              </div>

              <!-- 闸门策略 -->
              <a-space :size="4" wrap style="margin-top:8px">
                <a-tag v-for="level in strategy.gatePolicy?.blockOn || []" :key="level" color="red">阻断 {{ level }}</a-tag>
                <a-tag v-for="level in strategy.gatePolicy?.requireHumanReviewOn || []" :key="level" color="orange">复核 {{ level }}</a-tag>
              </a-space>
            </a-card>
            <a-button block type="dashed" @click="openCreateStrategy">
              <template #icon><PlusOutlined /></template>
              新增审查策略
            </a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:open="profileModalOpen"
      :title="editingProfileId ? '编辑模型档案' : '新增模型档案'"
      ok-text="保存"
      cancel-text="取消"
      :confirm-loading="profileSaving"
      @ok="saveProfile"
    >
      <a-form layout="vertical" :model="profileForm">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="供应商" required>
              <a-select
                v-model:value="profileForm.providerId"
                placeholder="选择供应商"
                :options="providerOptions"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="档案 Key" required>
              <a-input v-model:value="profileForm.profileKey" placeholder="qwen-plus-default" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="显示名称" required>
              <a-input v-model:value="profileForm.displayName" placeholder="通义千问 Plus" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="模型名称" required>
              <a-input v-model:value="profileForm.modelName" placeholder="qwen-plus" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="能力标签">
          <a-input v-model:value="profileForm.capabilityTagsText" placeholder="code-review, security, performance" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="默认温度">
              <a-input-number
                v-model:value="profileForm.defaultTemperature"
                :min="0"
                :max="2"
                :step="0.1"
                style="width:100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="超时秒数">
              <a-input-number
                v-model:value="profileForm.timeoutSeconds"
                :min="1"
                :max="600"
                style="width:100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="启用状态">
              <a-switch v-model:checked="profileForm.enabled" checked-children="启用" un-checked-children="停用" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="providerModalOpen"
      :title="editingProviderId ? '编辑模型供应商' : '新增模型供应商'"
      ok-text="保存"
      cancel-text="取消"
      :confirm-loading="providerSaving"
      @ok="saveProvider"
    >
      <a-form layout="vertical" :model="providerForm">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="供应商 Key" required>
              <a-input v-model:value="providerForm.providerKey" placeholder="dashscope" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="供应商名称" required>
              <a-input v-model:value="providerForm.name" placeholder="DashScope" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="供应商类型">
              <a-input v-model:value="providerForm.providerType" placeholder="OPENAI_COMPATIBLE" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="API Key 环境变量">
              <a-input v-model:value="providerForm.apiKeyEnv" placeholder="DASHSCOPE_API_KEY" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="Endpoint">
          <a-input v-model:value="providerForm.endpoint" placeholder="https://dashscope.aliyuncs.com/compatible-mode/v1" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="providerForm.description" :rows="3" placeholder="供应商说明" />
        </a-form-item>
        <a-form-item label="启用状态">
          <a-switch v-model:checked="providerForm.enabled" checked-children="启用" un-checked-children="停用" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="strategyModalOpen"
      :title="editingStrategyId ? '编辑审查策略' : '新增审查策略'"
      ok-text="保存"
      cancel-text="取消"
      :confirm-loading="strategySaving"
      width="760px"
      @ok="saveStrategy"
    >
      <a-form layout="vertical" :model="strategyForm">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="策略 Key" required>
              <a-input v-model:value="strategyForm.strategyKey" placeholder="quality-gate" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="策略名称" required>
              <a-input v-model:value="strategyForm.name" placeholder="质量闸门" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="审查模式">
              <a-select v-model:value="strategyForm.reviewMode" :options="reviewModeOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="描述">
          <a-textarea v-model:value="strategyForm.description" :rows="2" placeholder="策略适用场景" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="推荐场景">
              <a-input v-model:value="strategyForm.recommendedForText" placeholder="pre-pr, release" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="阻断级别">
              <a-input v-model:value="strategyForm.blockOnText" placeholder="BLOCKER, CRITICAL" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="人工复核级别">
              <a-input v-model:value="strategyForm.requireHumanReviewOnText" placeholder="MAJOR, HIGH" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="建议关注级别">
              <a-input v-model:value="strategyForm.advisoryOnText" placeholder="MINOR, MEDIUM" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="角色绑定">
          <a-space direction="vertical" :size="8" style="width:100%">
            <a-row
              v-for="(binding, index) in strategyForm.roleBindings"
              :key="index"
              :gutter="8"
              align="middle"
            >
              <a-col :span="8">
                <a-select v-model:value="binding.role" :options="roleOptions" placeholder="角色" />
              </a-col>
              <a-col :span="10">
                <a-select
                  v-model:value="binding.modelProfileId"
                  :options="modelProfileOptions"
                  placeholder="模型档案"
                  show-search
                  option-filter-prop="label"
                />
              </a-col>
              <a-col :span="4">
                <a-input-number
                  v-model:value="binding.temperature"
                  :min="0"
                  :max="2"
                  :step="0.1"
                  style="width:100%"
                />
              </a-col>
              <a-col :span="2">
                <a-button type="link" danger @click="removeStrategyBinding(index)">
                  <template #icon><DeleteOutlined /></template>
                </a-button>
              </a-col>
            </a-row>
            <a-button type="dashed" block @click="addStrategyBinding">
              <template #icon><PlusOutlined /></template>
              添加角色绑定
            </a-button>
          </a-space>
        </a-form-item>
        <a-form-item label="启用状态">
          <a-switch v-model:checked="strategyForm.enabled" checked-children="启用" un-checked-children="停用" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-space>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ApiOutlined, AppstoreOutlined, PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useApi } from '@/composables/useApi'

interface ApiModelProvider { id: number; providerKey: string; name: string; providerType: string; endpoint: string; apiKeyEnv: string; enabled: boolean; description: string; profiles?: ApiModelProfile[] }
interface ApiModelProfile { id: number; providerId: number; providerName?: string; profileKey: string; modelName: string; displayName: string; capabilityTags: string[]; defaultTemperature: number; timeoutSeconds: number; enabled: boolean }
interface ApiStrategyRoleBinding { id?: number; role: string; modelProfileId?: number; modelProfileName?: string; temperature?: number; skills?: string[] }
interface ApiReviewStrategy { id: number; strategyKey: string; name: string; reviewMode: string; description: string; recommendedFor: string[]; blockOn: string[]; requireHumanReviewOn: string[]; advisoryOn: string[]; enabled: boolean; roleBindings: ApiStrategyRoleBinding[] }
interface ApiSmokeTestResult { status: 'SUCCESS' | 'FAILED'; provider?: string; modelName?: string; promptVersion?: string; content?: string; promptTokens?: number; completionTokens?: number; costMicroCents?: number; errorMessage?: string }

const { get, post, put, del } = useApi()
const loading = ref(false)
const smokeTesting = ref(false)
const providerModalOpen = ref(false)
const providerSaving = ref(false)
const deletingProviderId = ref<number | null>(null)
const editingProviderId = ref<number | null>(null)
const strategyModalOpen = ref(false)
const strategySaving = ref(false)
const deletingStrategyId = ref<number | null>(null)
const editingStrategyId = ref<number | null>(null)
const profileModalOpen = ref(false)
const profileSaving = ref(false)
const deletingProfileId = ref<number | null>(null)
const editingProfileId = ref<number | null>(null)
const providers = ref<ApiModelProvider[]>([])
const modelProfiles = ref<ApiModelProfile[]>([])
const strategySummaries = ref<(ApiReviewStrategy & { roleLabels: string[]; gatePolicy: { blockOn: string[]; requireHumanReviewOn: string[] } })[]>([])
const defaultSmokeTestPrompt = 'Reply with OK to confirm the model invocation path is configured.'
const smokeTestForm = reactive({
  profileId: undefined as number | undefined,
  prompt: defaultSmokeTestPrompt,
})
const providerForm = reactive({
  providerKey: '',
  name: '',
  providerType: 'OPENAI_COMPATIBLE',
  endpoint: '',
  apiKeyEnv: '',
  enabled: true,
  description: '',
})
const profileForm = reactive({
  providerId: undefined as number | undefined,
  profileKey: '',
  modelName: '',
  displayName: '',
  capabilityTagsText: '',
  defaultTemperature: 0.3,
  timeoutSeconds: 60,
  enabled: true,
})
const strategyForm = reactive({
  strategyKey: '',
  name: '',
  reviewMode: 'AGENT',
  description: '',
  recommendedForText: '',
  blockOnText: 'BLOCKER',
  requireHumanReviewOnText: 'MAJOR',
  advisoryOnText: 'MINOR',
  enabled: true,
  roleBindings: [] as ApiStrategyRoleBinding[],
})
const smokeTestResult = ref<ApiSmokeTestResult | null>(null)

const modelColumns = [
  { title: '模型', key: 'displayName', dataIndex: 'displayName' },
  { title: '供应商', key: 'providerName', dataIndex: 'providerName' },
  { title: '能力标签', key: 'capabilityTags', dataIndex: 'capabilityTags' },
  { title: '温度', key: 'defaultTemperature', dataIndex: 'defaultTemperature' },
  { title: '超时', key: 'timeoutSeconds', dataIndex: 'timeoutSeconds' },
  { title: '状态', key: 'enabled', dataIndex: 'enabled' },
  { title: '操作', key: 'actions', width: 140 },
]

const providerCounts = computed(() => providers.value.map(provider => {
  const profiles = modelProfiles.value.filter(p => p.providerId === provider.id)
  return { providerId: provider.providerKey, providerName: provider.name, enabled: provider.enabled, totalProfiles: profiles.length, enabledProfiles: profiles.filter(p => p.enabled).length, providerRaw: provider }
}))
const smokeProfileOptions = computed(() => modelProfiles.value
  .filter(profile => profile.enabled)
  .map(profile => ({
    value: profile.id,
    label: `${profile.displayName} · ${profile.providerName || profile.providerId}`,
  })))
const selectedSmokeProfile = computed(() => modelProfiles.value.find(profile => profile.id === smokeTestForm.profileId))
const providerOptions = computed(() => providers.value.map(provider => ({
  value: provider.id,
  label: provider.name,
})))
const modelProfileOptions = computed(() => modelProfiles.value.map(profile => ({
  value: profile.id,
  label: `${profile.displayName} · ${profile.providerName || profile.providerId}`,
})))
const reviewModeOptions = [
  { value: 'AGENT', label: 'AGENT' },
  { value: 'MULTI_AGENT', label: 'MULTI_AGENT' },
  { value: 'CROSS_CHECK', label: 'CROSS_CHECK' },
]
const roleOptions = [
  { value: 'WORKER', label: '审查模型' },
  { value: 'JUDGE', label: 'Judge 模型' },
  { value: 'SECURITY_AUDITOR', label: '安全审计员' },
  { value: 'PERFORMANCE_ANALYST', label: '性能分析员' },
  { value: 'CODE_STYLE_CHECKER', label: '代码规范检查员' },
  { value: 'EXCEPTION_HANDLER', label: '异常处理专家' },
  { value: 'ARCHITECT_REVIEWER', label: '架构评审员' },
]

async function loadModelConfig() {
  loading.value = true
  try {
    const [providersRes, profilesRes, strategiesRes] = await Promise.all([
      get<ApiModelProvider[]>('/model-config/providers').catch(() => null),
      get<ApiModelProfile[]>('/model-config/profiles').catch(() => null),
      get<ApiReviewStrategy[]>('/model-config/strategies').catch(() => null),
    ])
    if (providersRes?.data) providers.value = providersRes.data
    if (profilesRes?.data) modelProfiles.value = profilesRes.data
    if (!smokeTestForm.profileId) {
      smokeTestForm.profileId = modelProfiles.value.find(profile => profile.enabled)?.id
    }
    if (strategiesRes?.data) {
      strategySummaries.value = strategiesRes.data.map(strategy => ({
        ...strategy,
        roleLabels: strategy.roleBindings?.map(b => `${roleLabel(b.role)} · ${b.modelProfileName || '未知模型'}`) || [],
        gatePolicy: { blockOn: strategy.blockOn || [], requireHumanReviewOn: strategy.requireHumanReviewOn || [] },
      }))
    }
  } catch (e) { console.error('加载模型配置失败', e) }
  finally { loading.value = false }
}

function resetSmokeTestPrompt() {
  smokeTestForm.prompt = defaultSmokeTestPrompt
}

async function runSmokeTest() {
  smokeTesting.value = true
  smokeTestResult.value = null
  const profile = selectedSmokeProfile.value
  try {
    const res = await post<ApiSmokeTestResult>('/model-config/invocations/smoke-test', {
      provider: profile?.providerName,
      modelName: profile?.modelName,
      prompt: smokeTestForm.prompt,
      temperature: profile?.defaultTemperature,
    })
    smokeTestResult.value = res.data ?? null
    if (res.data?.status === 'SUCCESS') message.success('模型调用烟测成功')
    else message.warning('模型调用烟测未通过，请检查配置')
  } catch (e) {
    console.error('模型调用烟测失败', e)
    message.error('模型调用烟测请求失败')
  } finally {
    smokeTesting.value = false
  }
}

function resetProfileForm() {
  Object.assign(profileForm, {
    providerId: providers.value.find(provider => provider.enabled)?.id ?? providers.value[0]?.id,
    profileKey: '',
    modelName: '',
    displayName: '',
    capabilityTagsText: '',
    defaultTemperature: 0.3,
    timeoutSeconds: 60,
    enabled: true,
  })
}

function resetProviderForm() {
  Object.assign(providerForm, {
    providerKey: '',
    name: '',
    providerType: 'OPENAI_COMPATIBLE',
    endpoint: '',
    apiKeyEnv: '',
    enabled: true,
    description: '',
  })
}

function openCreateProvider() {
  editingProviderId.value = null
  resetProviderForm()
  providerModalOpen.value = true
}

function openEditProvider(provider: ApiModelProvider) {
  editingProviderId.value = provider.id
  Object.assign(providerForm, {
    providerKey: provider.providerKey || '',
    name: provider.name || '',
    providerType: provider.providerType || 'OPENAI_COMPATIBLE',
    endpoint: provider.endpoint || '',
    apiKeyEnv: provider.apiKeyEnv || '',
    enabled: provider.enabled ?? true,
    description: provider.description || '',
  })
  providerModalOpen.value = true
}

function buildProviderPayload() {
  return {
    providerKey: providerForm.providerKey.trim(),
    name: providerForm.name.trim(),
    providerType: providerForm.providerType.trim(),
    endpoint: providerForm.endpoint.trim(),
    apiKeyEnv: providerForm.apiKeyEnv.trim(),
    enabled: providerForm.enabled,
    description: providerForm.description.trim(),
  }
}

async function saveProvider() {
  const payload = buildProviderPayload()
  if (!payload.providerKey || !payload.name) {
    message.warning('请填写供应商 Key 和供应商名称')
    return
  }
  providerSaving.value = true
  try {
    if (editingProviderId.value) {
      await put<ApiModelProvider>(`/model-config/providers/${editingProviderId.value}`, payload)
      message.success('模型供应商已更新')
    } else {
      await post<ApiModelProvider>('/model-config/providers', payload)
      message.success('模型供应商已创建')
    }
    providerModalOpen.value = false
    await loadModelConfig()
  } catch (e) {
    console.error('保存模型供应商失败', e)
    message.error('保存模型供应商失败')
  } finally {
    providerSaving.value = false
  }
}

async function deleteProvider(provider: ApiModelProvider) {
  deletingProviderId.value = provider.id
  try {
    await del(`/model-config/providers/${provider.id}`)
    message.success('模型供应商已删除')
    await loadModelConfig()
  } catch (e) {
    console.error('删除模型供应商失败', e)
    message.error('删除模型供应商失败')
  } finally {
    deletingProviderId.value = null
  }
}

function splitListText(value: string): string[] {
  return value
    .split(/[,，\s]+/)
    .map(item => item.trim())
    .filter(Boolean)
}

function resetStrategyForm() {
  Object.assign(strategyForm, {
    strategyKey: '',
    name: '',
    reviewMode: 'AGENT',
    description: '',
    recommendedForText: '',
    blockOnText: 'BLOCKER',
    requireHumanReviewOnText: 'MAJOR',
    advisoryOnText: 'MINOR',
    enabled: true,
    roleBindings: [{
      role: 'WORKER',
      modelProfileId: modelProfiles.value.find(profile => profile.enabled)?.id,
      temperature: 0.3,
    }],
  })
}

function openCreateStrategy() {
  editingStrategyId.value = null
  resetStrategyForm()
  strategyModalOpen.value = true
}

function openEditStrategy(strategy: ApiReviewStrategy) {
  editingStrategyId.value = strategy.id
  Object.assign(strategyForm, {
    strategyKey: strategy.strategyKey || '',
    name: strategy.name || '',
    reviewMode: strategy.reviewMode || 'AGENT',
    description: strategy.description || '',
    recommendedForText: (strategy.recommendedFor || []).join(', '),
    blockOnText: (strategy.blockOn || []).join(', '),
    requireHumanReviewOnText: (strategy.requireHumanReviewOn || []).join(', '),
    advisoryOnText: (strategy.advisoryOn || []).join(', '),
    enabled: strategy.enabled ?? true,
    roleBindings: (strategy.roleBindings || []).map(binding => ({
      role: binding.role,
      modelProfileId: binding.modelProfileId,
      temperature: binding.temperature ?? 0.3,
      skills: binding.skills || [],
    })),
  })
  if (!strategyForm.roleBindings.length) addStrategyBinding()
  strategyModalOpen.value = true
}

function addStrategyBinding() {
  strategyForm.roleBindings.push({
    role: 'WORKER',
    modelProfileId: modelProfiles.value.find(profile => profile.enabled)?.id,
    temperature: 0.3,
    skills: [],
  })
}

function removeStrategyBinding(index: number) {
  strategyForm.roleBindings.splice(index, 1)
}

function buildStrategyPayload() {
  return {
    strategyKey: strategyForm.strategyKey.trim(),
    name: strategyForm.name.trim(),
    reviewMode: strategyForm.reviewMode,
    description: strategyForm.description.trim(),
    recommendedFor: splitListText(strategyForm.recommendedForText),
    blockOn: splitListText(strategyForm.blockOnText),
    requireHumanReviewOn: splitListText(strategyForm.requireHumanReviewOnText),
    advisoryOn: splitListText(strategyForm.advisoryOnText),
    enabled: strategyForm.enabled,
    roleBindings: strategyForm.roleBindings
      .filter(binding => binding.role && binding.modelProfileId)
      .map(binding => ({
        role: binding.role,
        modelProfileId: binding.modelProfileId,
        temperature: binding.temperature ?? 0.3,
        skills: binding.skills || [],
      })),
  }
}

async function saveStrategy() {
  const payload = buildStrategyPayload()
  if (!payload.strategyKey || !payload.name || !payload.roleBindings.length) {
    message.warning('请填写策略 Key、策略名称，并至少保留一个角色绑定')
    return
  }
  strategySaving.value = true
  try {
    if (editingStrategyId.value) {
      await put<ApiReviewStrategy>(`/model-config/strategies/${editingStrategyId.value}`, payload)
      message.success('审查策略已更新')
    } else {
      await post<ApiReviewStrategy>('/model-config/strategies', payload)
      message.success('审查策略已创建')
    }
    strategyModalOpen.value = false
    await loadModelConfig()
  } catch (e) {
    console.error('保存审查策略失败', e)
    message.error('保存审查策略失败')
  } finally {
    strategySaving.value = false
  }
}

async function deleteStrategy(strategy: ApiReviewStrategy) {
  deletingStrategyId.value = strategy.id
  try {
    await del(`/model-config/strategies/${strategy.id}`)
    message.success('审查策略已删除')
    await loadModelConfig()
  } catch (e) {
    console.error('删除审查策略失败', e)
    message.error('删除审查策略失败')
  } finally {
    deletingStrategyId.value = null
  }
}

function openCreateProfile() {
  editingProfileId.value = null
  resetProfileForm()
  profileModalOpen.value = true
}

function openEditProfile(profile: ApiModelProfile) {
  editingProfileId.value = profile.id
  Object.assign(profileForm, {
    providerId: profile.providerId,
    profileKey: profile.profileKey || '',
    modelName: profile.modelName || '',
    displayName: profile.displayName || '',
    capabilityTagsText: (profile.capabilityTags || []).join(', '),
    defaultTemperature: profile.defaultTemperature ?? 0.3,
    timeoutSeconds: profile.timeoutSeconds ?? 60,
    enabled: profile.enabled ?? true,
  })
  profileModalOpen.value = true
}

function buildProfilePayload() {
  return {
    providerId: profileForm.providerId,
    profileKey: profileForm.profileKey.trim(),
    modelName: profileForm.modelName.trim(),
    displayName: profileForm.displayName.trim(),
    capabilityTags: profileForm.capabilityTagsText
      .split(/[,，\s]+/)
      .map(tag => tag.trim())
      .filter(Boolean),
    defaultTemperature: profileForm.defaultTemperature,
    timeoutSeconds: profileForm.timeoutSeconds,
    enabled: profileForm.enabled,
  }
}

async function saveProfile() {
  const payload = buildProfilePayload()
  if (!payload.providerId || !payload.profileKey || !payload.modelName || !payload.displayName) {
    message.warning('请填写供应商、档案 Key、显示名称和模型名称')
    return
  }
  profileSaving.value = true
  try {
    if (editingProfileId.value) {
      await put<ApiModelProfile>(`/model-config/profiles/${editingProfileId.value}`, payload)
      message.success('模型档案已更新')
    } else {
      await post<ApiModelProfile>('/model-config/profiles', payload)
      message.success('模型档案已创建')
    }
    profileModalOpen.value = false
    await loadModelConfig()
  } catch (e) {
    console.error('保存模型档案失败', e)
    message.error('保存模型档案失败')
  } finally {
    profileSaving.value = false
  }
}

async function deleteProfile(profile: ApiModelProfile) {
  deletingProfileId.value = profile.id
  try {
    await del(`/model-config/profiles/${profile.id}`)
    message.success('模型档案已删除')
    if (smokeTestForm.profileId === profile.id) smokeTestForm.profileId = undefined
    await loadModelConfig()
  } catch (e) {
    console.error('删除模型档案失败', e)
    message.error('删除模型档案失败')
  } finally {
    deletingProfileId.value = null
  }
}

function roleLabel(role: string): string {
  const labels: Record<string, string> = { WORKER: '审查模型', JUDGE: 'Judge 模型', SECURITY_AUDITOR: '安全审计员', PERFORMANCE_ANALYST: '性能分析员', CODE_STYLE_CHECKER: '代码规范检查员', EXCEPTION_HANDLER: '异常处理专家', ARCHITECT_REVIEWER: '架构评审员' }
  return labels[role] || role
}

onMounted(loadModelConfig)
</script>
