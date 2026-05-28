<template>
  <a-space direction="vertical" :size="16" style="width:100%">
    <div>
      <h2 style="margin:0">模型配置</h2>
      <p style="margin-top:4px;color:#94a3b8;font-size:13px">管理模型供应商、模型档案、审查策略和质量闸门</p>
    </div>

    <!-- Provider Cards -->
    <a-row :gutter="16">
      <a-col v-for="(provider, index) in providerCounts" :key="provider.providerId" :lg="8" :span="24" style="margin-bottom:16px">
        <a-card size="small" hoverable>
          <a-space direction="vertical" :size="4" style="width:100%">
            <div style="font-weight:600;font-size:14px">{{ provider.providerName }}</div>
            <div style="font-size:12px;color:#94a3b8">{{ provider.enabledProfiles }} / {{ provider.totalProfiles }} 个模型启用</div>
            <a-tag :color="provider.enabled ? 'green' : 'default'">{{ provider.enabled ? '已启用' : '未启用' }}</a-tag>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

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
            <a-tag color="processing">{{ modelProfiles.length }} 个可用</a-tag>
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
                <a-tag color="processing">{{ strategy.reviewMode }}</a-tag>
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
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </a-space>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ApiOutlined, AppstoreOutlined } from '@ant-design/icons-vue'
import { useApi } from '@/composables/useApi'

interface ApiModelProvider { id: number; providerKey: string; name: string; providerType: string; endpoint: string; apiKeyEnv: string; enabled: boolean; description: string; profiles?: ApiModelProfile[] }
interface ApiModelProfile { id: number; providerId: number; providerName?: string; profileKey: string; modelName: string; displayName: string; capabilityTags: string[]; defaultTemperature: number; timeoutSeconds: number; enabled: boolean }
interface ApiReviewStrategy { id: number; strategyKey: string; name: string; reviewMode: string; description: string; recommendedFor: string[]; blockOn: string[]; requireHumanReviewOn: string[]; advisoryOn: string[]; enabled: boolean; roleBindings: { role: string; modelProfileName: string }[] }

const { get } = useApi()
const loading = ref(false)
const providers = ref<ApiModelProvider[]>([])
const modelProfiles = ref<ApiModelProfile[]>([])
const strategySummaries = ref<(ApiReviewStrategy & { roleLabels: string[]; gatePolicy: { blockOn: string[]; requireHumanReviewOn: string[] } })[]>([])

const modelColumns = [
  { title: '模型', key: 'displayName', dataIndex: 'displayName' },
  { title: '供应商', key: 'providerName', dataIndex: 'providerName' },
  { title: '能力标签', key: 'capabilityTags', dataIndex: 'capabilityTags' },
  { title: '温度', key: 'defaultTemperature', dataIndex: 'defaultTemperature' },
  { title: '超时', key: 'timeoutSeconds', dataIndex: 'timeoutSeconds' },
  { title: '状态', key: 'enabled', dataIndex: 'enabled' },
]

const providerCounts = computed(() => providers.value.map(provider => {
  const profiles = modelProfiles.value.filter(p => p.providerId === provider.id)
  return { providerId: provider.providerKey, providerName: provider.name, enabled: provider.enabled, totalProfiles: profiles.length, enabledProfiles: profiles.filter(p => p.enabled).length }
}))

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

function roleLabel(role: string): string {
  const labels: Record<string, string> = { WORKER: '审查模型', JUDGE: 'Judge 模型', SECURITY_AUDITOR: '安全审计员', PERFORMANCE_ANALYST: '性能分析员', CODE_STYLE_CHECKER: '代码规范检查员', EXCEPTION_HANDLER: '异常处理专家', ARCHITECT_REVIEWER: '架构评审员' }
  return labels[role] || role
}

onMounted(loadModelConfig)
</script>

