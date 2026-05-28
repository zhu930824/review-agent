<template>
  <a-space direction="vertical" :size="16" style="width:100%">
    <!-- 标题栏 -->
    <a-space style="width:100%;justify-content:space-between;flex-wrap:wrap">
      <div>
        <h2 style="margin:0">治理中心</h2>
        <p style="margin-top:4px;color:#94a3b8;font-size:13px">对标 AI Review、质量门禁和安全扫描产品能力，把平台路线图转成可执行的规则包、集成和工作流。</p>
      </div>
      <a-space wrap>
        <RouterLink to="/reviews/create">
          <a-button type="primary">
            <template #icon><PlayCircleOutlined /></template>
            按模板发起审查
          </a-button>
        </RouterLink>
        <RouterLink to="/settings/models">
          <a-button>
            <template #icon><SettingOutlined /></template>
            配置模型策略
          </a-button>
        </RouterLink>
      </a-space>
    </a-space>

    <!-- KPI 卡片 -->
    <a-row :gutter="16">
      <a-col :xl="6" :md="12" :span="24" style="margin-bottom:16px">
        <a-card size="small">
          <a-space :size="8" style="width:100%;justify-content:space-between">
            <div>
              <div style="font-size:12px;font-weight:600;text-transform:uppercase;color:#94a3b8;letter-spacing:0.5px">市场能力覆盖</div>
              <div style="font-size:28px;font-weight:800;background:linear-gradient(135deg,#6366f1,#a855f7);-webkit-background-clip:text;-webkit-text-fill-color:transparent;margin-top:4px">
                {{ coverage.coveragePercent }}%
              </div>
              <div style="font-size:12px;color:#94a3b8;margin-top:2px">按已启用与部分具备折算</div>
            </div>
            <BarChartOutlined style="font-size:20px;color:#6366f1" />
          </a-space>
          <a-progress :percent="coverage.coveragePercent" :show-info="false" stroke-color="linear-gradient(90deg,#6366f1,#a855f7)" style="margin-top:8px" />
        </a-card>
      </a-col>

      <a-col :xl="6" :md="12" :span="24" style="margin-bottom:16px">
        <a-card size="small">
          <a-space :size="8" style="width:100%;justify-content:space-between">
            <div>
              <div style="font-size:12px;font-weight:600;text-transform:uppercase;color:#94a3b8;letter-spacing:0.5px">已启用能力</div>
              <div style="font-size:28px;font-weight:800;color:#059669;margin-top:4px">{{ coverage.enabled }}</div>
              <div style="font-size:12px;color:#94a3b8;margin-top:2px">PR 摘要、行级审查、质量门禁、效能分析</div>
            </div>
            <CheckCircleOutlined style="font-size:20px;color:#059669" />
          </a-space>
        </a-card>
      </a-col>

      <a-col :xl="6" :md="12" :span="24" style="margin-bottom:16px">
        <a-card size="small">
          <a-space :size="8" style="width:100%;justify-content:space-between">
            <div>
              <div style="font-size:12px;font-weight:600;text-transform:uppercase;color:#94a3b8;letter-spacing:0.5px">待补齐关键项</div>
              <div style="font-size:28px;font-weight:800;color:#d97706;margin-top:4px">{{ coverage.partial + coverage.planned }}</div>
              <div style="font-size:12px;color:#94a3b8;margin-top:2px">安全扫描、状态检查、SARIF、自动修复</div>
            </div>
            <ExclamationCircleOutlined style="font-size:20px;color:#d97706" />
          </a-space>
        </a-card>
      </a-col>

      <a-col :xl="6" :md="12" :span="24" style="margin-bottom:16px">
        <a-card size="small">
          <a-space :size="8" style="width:100%;justify-content:space-between">
            <div>
              <div style="font-size:12px;font-weight:600;text-transform:uppercase;color:#94a3b8;letter-spacing:0.5px">工作流模板</div>
              <div style="font-size:28px;font-weight:800;margin-top:4px">{{ workflowTemplates.length }}</div>
              <div style="font-size:12px;color:#94a3b8;margin-top:2px">覆盖日常自查、发布门禁和架构评审</div>
            </div>
            <AppstoreOutlined style="font-size:20px;color:#6366f1" />
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <!-- 主内容区 -->
    <a-row :gutter="16">
      <!-- 市场能力对标表格 -->
      <a-col :xl="16" :span="24">
        <a-card size="small" style="margin-bottom:16px">
          <template #title>
            <a-space :size="8">
              <AppstoreOutlined style="font-size:20px;color:#6366f1" />
              <span style="font-weight:600">市场能力对标</span>
            </a-space>
          </template>
          <template #extra>
            <a-tag color="processing">{{ marketCapabilities.length }} 项能力</a-tag>
          </template>
          <a-table
            :columns="capabilityColumns"
            :data-source="marketCapabilities"
            :pagination="false"
            row-key="id"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'name'">
                <div>
                  <div style="font-weight:600;font-size:13px">{{ record.name }}</div>
                  <div style="font-size:12px;color:#94a3b8;line-height:1.4;margin-top:2px">{{ record.platformMove }}</div>
                </div>
              </template>
              <template v-else-if="column.key === 'category'">
                <a-tag>{{ categoryLabel(record.category) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'status'">
                <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'businessImpact'">
                <a-tag :color="impactColor(record.businessImpact)">{{ impactLabel(record.businessImpact) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'sourceProducts'">
                <a-space :size="4" wrap>
                  <a-tag v-for="product in record.sourceProducts" :key="product" color="blue">{{ product }}</a-tag>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <!-- 右侧边栏 -->
      <a-col :xl="8" :span="24">
        <!-- 优先落地项 -->
        <a-card size="small" style="margin-bottom:16px">
          <template #title>
            <a-space :size="8">
              <ThunderboltOutlined style="font-size:16px;color:#d97706" />
              <span style="font-weight:600;font-size:14px">优先落地项</span>
            </a-space>
          </template>
          <a-space direction="vertical" :size="8" style="width:100%">
            <a-card
              v-for="action in recommendedActions"
              :key="action.id"
              size="small"
              :body-style="{ padding: '12px' }"
              style="background:#f8fafc"
            >
              <a-space :size="8" style="width:100%;justify-content:space-between">
                <div>
                  <div style="font-weight:600;font-size:13px">{{ action.name }}</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:2px;line-height:1.4">{{ action.platformMove }}</div>
                </div>
                <a-tag color="orange">{{ action.priorityScore }}</a-tag>
              </a-space>
            </a-card>
          </a-space>
        </a-card>

        <!-- 发布门禁策略包 -->
        <a-card size="small">
          <template #title>
            <a-space :size="8">
              <SafetyOutlined style="font-size:16px;color:#ef4444" />
              <span style="font-weight:600;font-size:14px">发布门禁策略包</span>
            </a-space>
          </template>
          <a-space direction="vertical" :size="12" style="width:100%">
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">要求能力</div>
              <a-space :size="4" wrap>
                <a-tag v-for="capability in releasePolicy.requiredCapabilities" :key="capability" color="red">{{ capability }}</a-tag>
              </a-space>
            </div>
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">建议策略</div>
              <a-space :size="4" wrap>
                <a-tag v-for="strategy in releasePolicy.suggestedStrategies" :key="strategy" color="processing">{{ strategy }}</a-tag>
              </a-space>
            </div>
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">人工检查点</div>
              <a-space direction="vertical" :size="4">
                <a-space v-for="checkpoint in releasePolicy.requiredHumanCheckpoints" :key="checkpoint" :size="4">
                  <UserOutlined style="color:#d97706;font-size:14px" />
                  <span style="font-size:12px">{{ checkpoint }}</span>
                </a-space>
              </a-space>
            </div>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <!-- 底部区域：集成路线图 + 业务工作流模板 -->
    <a-row :gutter="16">
      <!-- 集成路线图 -->
      <a-col :xl="12" :span="24" style="margin-bottom:16px">
        <a-card size="small">
          <template #title>
            <a-space :size="8">
              <EnvironmentOutlined style="font-size:20px;color:#6366f1" />
              <span style="font-weight:600">集成路线图</span>
            </a-space>
          </template>
          <a-row :gutter="16">
            <a-col v-for="stage in rolloutStages" :key="stage.key" :md="8" :span="24">
              <a-space :size="4" style="margin-bottom:8px">
                <component :is="stage.iconComp" :style="{fontSize:'20px',color:stage.iconColor}" />
                <span style="font-weight:600;font-size:13px">{{ stage.label }}</span>
              </a-space>
              <div v-for="connector in connectorsByStage[stage.key]" :key="connector.id">
                <a-card size="small" :body-style="{ padding: '12px' }" style="background:#f8fafc;margin-bottom:8px">
                  <div style="font-weight:600;font-size:13px">{{ connector.name }}</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:4px;line-height:1.4">{{ connector.businessValue }}</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:4px">{{ connector.implementationHint }}</div>
                </a-card>
              </div>
            </a-col>
          </a-row>
        </a-card>
      </a-col>

      <!-- 业务工作流模板 -->
      <a-col :xl="12" :span="24" style="margin-bottom:16px">
        <a-card size="small">
          <template #title>
            <a-space :size="8">
              <SyncOutlined style="font-size:20px;color:#059669" />
              <span style="font-weight:600">业务工作流模板</span>
            </a-space>
          </template>
          <a-space direction="vertical" :size="8" style="width:100%">
            <a-card
              v-for="workflow in workflowTemplates"
              :key="workflow.id"
              size="small"
              :body-style="{ padding: '16px' }"
              style="background:#f8fafc"
            >
              <a-space :size="8" style="width:100%;justify-content:space-between;flex-wrap:wrap">
                <div>
                  <div style="font-weight:600;font-size:13px">{{ workflow.name }}</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:4px;line-height:1.4">{{ workflow.scenario }}</div>
                </div>
                <a-tag color="processing">{{ workflow.strategyId }}</a-tag>
              </a-space>
              <a-space :size="4" wrap style="margin-top:8px">
                <a-tag v-for="packId in workflow.rulePackIds" :key="packId">{{ packId }}</a-tag>
                <a-tag v-for="integrationId in workflow.integrationIds" :key="integrationId" color="blue">{{ integrationId }}</a-tag>
              </a-space>
              <div style="font-size:12px;color:#94a3b8;margin-top:8px">{{ workflow.successMetric }}</div>
            </a-card>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </a-space>
</template>

<script setup lang="ts">
import { BarChartOutlined, CheckCircleOutlined, ExclamationCircleOutlined, AppstoreOutlined, ThunderboltOutlined, SafetyOutlined, UserOutlined, EnvironmentOutlined, SyncOutlined, PlayCircleOutlined, SettingOutlined, ClockCircleOutlined } from '@ant-design/icons-vue'
import type { BusinessImpact, CapabilityStatus, RolloutStage } from '@/types/governance'
import { compileGovernancePolicyPack, getCapabilityCoverageSummary, getConnectorsByStage, getRecommendedNextActions, marketCapabilities, workflowTemplates } from '@/utils/governanceCatalog'

const coverage = getCapabilityCoverageSummary()
const recommendedActions = getRecommendedNextActions(5)
const connectorsByStage = getConnectorsByStage()
const releasePolicy = compileGovernancePolicyPack(['security-release', 'ai-generated-code', 'compliance-evidence'])

const capabilityColumns = [
  { title: '能力', key: 'name', dataIndex: 'name' },
  { title: '类别', key: 'category', dataIndex: 'category' },
  { title: '状态', key: 'status', dataIndex: 'status' },
  { title: '业务影响', key: 'businessImpact', dataIndex: 'businessImpact' },
  { title: '市场信号', key: 'sourceProducts', dataIndex: 'sourceProducts' },
]

const rolloutStages: Array<{ key: RolloutStage; label: string; iconComp: any; iconColor: string }> = [
  { key: 'live', label: '已可用', iconComp: CheckCircleOutlined, iconColor: '#22c55e' },
  { key: 'next', label: '下一阶段', iconComp: SyncOutlined, iconColor: '#6366f1' },
  { key: 'later', label: '后续扩展', iconComp: ClockCircleOutlined, iconColor: '#94a3b8' },
]

function statusLabel(status: CapabilityStatus) { return { ENABLED: '已启用', PARTIAL: '部分具备', PLANNED: '待建设' }[status] }
function statusColor(status: CapabilityStatus) { return { ENABLED: 'green', PARTIAL: 'orange', PLANNED: 'default' }[status] }
function impactLabel(impact: BusinessImpact) { return { HIGH: '高', MEDIUM: '中', LOW: '低' }[impact] }
function impactColor(impact: BusinessImpact) { return { HIGH: 'red', MEDIUM: 'blue', LOW: 'default' }[impact] }
function categoryLabel(category: string) { return { AI_REVIEW: 'AI 审查', QUALITY: '质量', SECURITY: '安全', INTEGRATION: '集成', KNOWLEDGE: '知识库', ANALYTICS: '分析' }[category] ?? category }
</script>
