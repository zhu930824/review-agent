<template>
  <div>
    <!-- Header -->
    <div style="display: flex; flex-wrap: wrap; align-items: flex-end; justify-content: space-between; gap: 16px; margin-bottom: 24px">
      <a-space direction="vertical" :size="4">
        <a-typography-title :level="3" style="margin: 0">质量驾驶舱</a-typography-title>
        <a-typography-text type="secondary">
          跟踪 Pre-PR 通过率、阻断风险和多模型审查稳定性
        </a-typography-text>
      </a-space>
      <a-button type="primary" size="large" @click="router.push('/reviews/create')">
        <template #icon><PlayCircleOutlined /></template>
        发起 Pre-PR
      </a-button>
    </div>

    <!-- KPI 统计卡片 -->
    <a-row :gutter="[24, 24]" style="margin-bottom: 24px">
      <a-col v-for="stat in stats" :key="stat.label" :xs="24" :sm="12" :xl="6">
        <a-card :bordered="false" style="border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06)">
          <div style="display: flex; align-items: flex-start; justify-content: space-between">
            <div>
              <a-typography-text type="secondary" style="font-size: 12px; font-weight: 600; text-transform: uppercase">
                {{ stat.label }}
              </a-typography-text>
              <div style="font-size: 30px; font-weight: 800; color: #0f172a; margin-top: 8px; line-height: 1.2">
                {{ stat.value }}
              </div>
              <a-typography-text type="secondary" style="font-size: 12px">
                {{ stat.hint }}
              </a-typography-text>
            </div>
            <div
              :style="{
                width: '40px', height: '40px', display: 'flex', alignItems: 'center',
                justifyContent: 'center', borderRadius: '12px', ...stat.iconStyle
              }"
            >
              <component :is="stat.icon" style="font-size: 20px" />
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 主要内容区域 -->
    <a-row :gutter="[24, 24]">
      <!-- 左侧：最近审查表 -->
      <a-col :xs="24" :xl="16">
        <a-card
          :bordered="false"
          style="border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06)"
          :body-style="{ padding: '0' }"
        >
          <div style="display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; border-bottom: 1px solid #f0f0f0">
            <a-space :size="12">
              <div style="width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; border-radius: 10px; background: #e6f4ff; color: #1677ff">
                <ClockCircleOutlined style="font-size: 16px" />
              </div>
              <a-typography-title :level="5" style="margin: 0">最近审查</a-typography-title>
            </a-space>
            <a-tag color="blue" style="cursor: pointer" @click="router.push('/reviews/create')">
              <PlusOutlined />
              新建
            </a-tag>
          </div>

          <a-table
            :dataSource="recentReviews"
            :columns="reviewColumns"
            :loading="loading"
            :pagination="false"
            row-key="id"
            style="padding: 0 16px"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="statusTagColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'reviewMode'">
                <a-tag>{{ record.reviewMode }}</a-tag>
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
      </a-col>

      <!-- 右侧边栏 -->
      <a-col :xs="24" :xl="8">
        <a-space direction="vertical" :size="24" style="width: 100%">
          <!-- 风险压力 -->
          <a-card :bordered="false" style="border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06)">
            <a-space :size="12" style="margin-bottom: 16px">
              <div style="width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; border-radius: 10px; background: #fff2f0; color: #ff4d4f">
                <WarningOutlined style="font-size: 16px" />
              </div>
              <a-typography-title :level="5" style="margin: 0; font-size: 14px">风险压力</a-typography-title>
            </a-space>

            <div style="margin-bottom: 16px">
              <div style="display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 8px">
                <a-typography-text type="secondary">Pre-PR 通过率</a-typography-text>
                <a-typography-text strong>{{ metrics.prePrPassRate }}%</a-typography-text>
              </div>
              <a-progress
                :percent="metrics.prePrPassRate"
                :stroke-color="metrics.prePrPassRate >= 80 ? '#52c41a' : '#faad14'"
                :show-info="false"
                size="small"
              />
            </div>

            <a-row :gutter="12">
              <a-col :span="12">
                <div style="background: linear-gradient(135deg, #fff2f0, #fff1f0); border-radius: 12px; padding: 16px">
                  <div style="font-size: 12px; font-weight: 600; color: #ff4d4f; margin-bottom: 4px">阻断审查</div>
                  <div style="font-size: 28px; font-weight: 800; color: #cf1322; line-height: 1.2">
                    {{ metrics.blockedReviews }}
                  </div>
                </div>
              </a-col>
              <a-col :span="12">
                <div style="background: linear-gradient(135deg, #fffbe6, #fff8e1); border-radius: 12px; padding: 16px">
                  <div style="font-size: 12px; font-weight: 600; color: #d48806; margin-bottom: 4px">待人工确认</div>
                  <div style="font-size: 28px; font-weight: 800; color: #ad6800; line-height: 1.2">
                    {{ metrics.pendingHumanConfirmations }}
                  </div>
                </div>
              </a-col>
            </a-row>
          </a-card>

          <!-- 模型稳定性 -->
          <a-card :bordered="false" style="border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06)">
            <a-space :size="12" style="margin-bottom: 16px">
              <div style="width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; border-radius: 10px; background: #eef2ff; color: #4f46e5">
                <ControlOutlined style="font-size: 16px" />
              </div>
              <a-typography-title :level="5" style="margin: 0; font-size: 14px">模型稳定性</a-typography-title>
            </a-space>

            <div style="display: flex; align-items: center; justify-content: space-between">
              <div>
                <div style="font-size: 32px; font-weight: 800; background: linear-gradient(135deg, #4f46e5, #9333ea); -webkit-background-clip: text; -webkit-text-fill-color: transparent; line-height: 1.2">
                  {{ metrics.modelSuccessRate }}%
                </div>
                <a-typography-text type="secondary" style="font-size: 12px">模型成功率</a-typography-text>
              </div>
              <div style="width: 56px; height: 56px; display: flex; align-items: center; justify-content: center; border-radius: 14px; background: linear-gradient(135deg, #eef2ff, #f3e8ff); box-shadow: 0 1px 2px rgba(0,0,0,0.06)">
                <BarChartOutlined style="font-size: 28px; color: #4f46e5" />
              </div>
            </div>
          </a-card>

          <!-- 治理成熟度 -->
          <a-card :bordered="false" style="border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.06)">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px">
              <a-space :size="12">
                <div style="width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; border-radius: 10px; background: #f6ffed; color: #52c41a">
                  <AppstoreOutlined style="font-size: 16px" />
                </div>
                <a-typography-title :level="5" style="margin: 0; font-size: 14px">治理成熟度</a-typography-title>
              </a-space>
              <router-link to="/governance" style="display: flex; align-items: center; gap: 4px; font-size: 12px; font-weight: 600; color: #4f46e5; text-decoration: none">
                进入
                <ArrowRightOutlined />
              </router-link>
            </div>

            <div style="margin-bottom: 16px">
              <div style="display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 8px">
                <a-typography-text type="secondary">市场能力覆盖</a-typography-text>
                <a-typography-text strong>{{ governanceCoverage.coveragePercent }}%</a-typography-text>
              </div>
              <a-progress
                :percent="governanceCoverage.coveragePercent"
                :show-info="false"
                size="small"
              />
            </div>

            <div style="background: #f9fafb; border-radius: 10px; padding: 16px">
              <a-typography-text type="secondary" style="font-size: 12px">下一优先级</a-typography-text>
              <div style="font-size: 14px; font-weight: 700; color: #0f172a; margin-top: 4px">
                {{ topGovernanceAction?.name }}
              </div>
            </div>
          </a-card>
        </a-space>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import {
  PlayCircleOutlined, ClockCircleOutlined, PlusOutlined, ArrowRightOutlined,
  WarningOutlined, ControlOutlined, BarChartOutlined, AppstoreOutlined
} from '@ant-design/icons-vue'
import {
  FolderOutlined, SearchOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import type { Project } from '@/types/project'
import type { Review, ReviewDetail } from '@/types/review'
import type { PageResult } from '@/types/api'
import { getCapabilityCoverageSummary, getRecommendedNextActions } from '@/utils/governanceCatalog'
import { useApi } from '@/composables/useApi'
import { deriveDashboardMetrics } from '@/utils/reviewMetrics'

const router = useRouter()
const { get } = useApi()
const loading = ref(false)
const projectTotal = ref(0)
const recentReviews = ref<Review[]>([])
const recentDetails = ref<ReviewDetail[]>([])
const governanceCoverage = getCapabilityCoverageSummary()
const topGovernanceAction = getRecommendedNextActions(1)[0]

const metrics = computed(() => deriveDashboardMetrics(recentReviews.value, recentDetails.value))

const stats = computed(() => [
  {
    label: '项目总数', value: projectTotal.value, hint: '已纳入治理的代码库',
    icon: FolderOutlined,
    iconStyle: { background: '#eef2ff', color: '#4f46e5' },
  },
  {
    label: '审查总数', value: metrics.value.totalReviews, hint: '最近列表样本',
    icon: SearchOutlined,
    iconStyle: { background: '#e6f4ff', color: '#1677ff' },
  },
  {
    label: '阻断问题', value: metrics.value.blockerFindings, hint: 'BLOCKER 级风险',
    icon: WarningOutlined,
    iconStyle: { background: '#fff2f0', color: '#ff4d4f' },
  },
  {
    label: '已完成审查', value: metrics.value.completedReviews,
    hint: `${metrics.value.runningReviews} 个执行中`,
    icon: CheckCircleOutlined,
    iconStyle: { background: '#f6ffed', color: '#52c41a' },
  },
])

const reviewColumns = [
  { title: '项目', dataIndex: 'projectName', key: 'projectName' },
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
    RUNNING: 'processing',
    COMPLETED: 'success',
    FAILED: 'error',
  }
  return map[status] ?? 'default'
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { PENDING: '等待中', RUNNING: '审查中', COMPLETED: '已完成', FAILED: '失败' }
  return map[status] ?? status
}

async function loadDashboard() {
  loading.value = true
  try {
    const [projectsRes, reviewsRes] = await Promise.all([
      get<PageResult<Project>>('/projects?pageNum=1&pageSize=1'),
      get<PageResult<Review>>('/reviews?pageNum=1&pageSize=10'),
    ])

    projectTotal.value = projectsRes.data?.total ?? 0
    recentReviews.value = reviewsRes.data?.records ?? []
    recentDetails.value = (await Promise.all(
      recentReviews.value.slice(0, 5).map(async review => {
        try {
          const res = await get<ReviewDetail>(`/reviews/${review.id}`)
          return res.data
        } catch {
          return null
        }
      }),
    )).filter((item): item is ReviewDetail => Boolean(item))
  } catch (e) {
    console.error('加载质量驾驶舱失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>
