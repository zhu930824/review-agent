<template>
  <a-space direction="vertical" :size="16" style="width:100%">
    <div>
      <h2 style="margin:0">AI Gateway</h2>
      <p style="margin-top:4px;color:#94a3b8;font-size:13px">Prompt 管理 · 模型调用审计 · 统计分析</p>
    </div>

    <a-row :gutter="16">
      <!-- Prompt 列表 -->
      <a-col :lg="12" :span="24">
        <a-card size="small" title="Prompt 模板">
          <a-spin v-if="promptsLoading" style="display:flex;justify-content:center;padding:24px 0" />
          <div v-else>
            <a-card
              v-for="p in prompts"
              :key="p.templateKey"
              size="small"
              hoverable
              style="margin-bottom:8px"
              :body-style="{ padding: '12px' }"
              @click="selectedPrompt = p"
            >
              <a-space :size="8">
                <a-tag color="processing">{{ p.category }}</a-tag>
                <span style="font-weight:600;font-size:13px">{{ p.name }}</span>
              </a-space>
              <p style="margin:4px 0 0;font-size:12px;color:#94a3b8">{{ p.templateKey }} · v{{ p.version }}</p>
            </a-card>
          </div>
        </a-card>
      </a-col>

      <!-- Prompt 详情 -->
      <a-col :lg="12" :span="24">
        <a-card size="small" :title="selectedPrompt ? selectedPrompt.name : '选择 Prompt 查看详情'">
          <template v-if="selectedPrompt">
            <a-space direction="vertical" :size="8" style="width:100%">
              <span style="font-size:12px;color:#94a3b8">模板变量: {{ selectedPrompt.variables || '无' }}</span>
              <pre style="background:#1e293b;color:#cbd5e1;font-size:12px;padding:16px;border-radius:12px;max-height:256px;overflow:auto;margin:0">{{ selectedPrompt.content }}</pre>
            </a-space>
          </template>
          <p v-else style="text-align:center;color:#94a3b8;padding:32px 0;font-size:13px">点击左侧 Prompt 查看详情</p>
        </a-card>
      </a-col>
    </a-row>

    <!-- 调用统计 -->
    <a-card size="small" title="调用统计">
      <a-row v-if="stats" :gutter="12">
        <a-col :span="6">
          <div style="text-align:center;padding:16px;border-radius:12px;background:#eef2ff">
            <div style="font-size:24px;font-weight:800;color:#4f46e5">{{ stats.totalCalls }}</div>
            <div style="margin-top:4px;font-size:12px;color:#6366f1">总调用次数</div>
          </div>
        </a-col>
        <a-col :span="6">
          <div style="text-align:center;padding:16px;border-radius:12px;background:#f3e8ff">
            <div style="font-size:24px;font-weight:800;color:#9333ea">{{ formatTokens(stats.totalTokens) }}</div>
            <div style="margin-top:4px;font-size:12px;color:#a855f7">总 Token</div>
          </div>
        </a-col>
        <a-col :span="6">
          <div style="text-align:center;padding:16px;border-radius:12px;background:#fef3c7">
            <div style="font-size:24px;font-weight:800;color:#d97706">{{ stats.avgLatencyMs.toFixed(0) }}ms</div>
            <div style="margin-top:4px;font-size:12px;color:#f59e0b">平均延迟</div>
          </div>
        </a-col>
        <a-col :span="6">
          <div style="text-align:center;padding:16px;border-radius:12px;background:#fee2e2">
            <div style="font-size:24px;font-weight:800;color:#dc2626">{{ stats.failedCalls }}</div>
            <div style="margin-top:4px;font-size:12px;color:#ef4444">失败次数</div>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </a-space>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useApi } from '@/composables/useApi'

const { get } = useApi()
const promptsLoading = ref(false)
const prompts = ref<any[]>([])
const selectedPrompt = ref<any>(null)
const stats = ref<any>(null)

function formatTokens(n: number): string {
  if (n >= 10000) return (n / 1000).toFixed(1) + 'K'
  return String(n)
}

async function loadPrompts() {
  promptsLoading.value = true
  try {
    const res = await get<any[]>('/gateway/prompts')
    if (res.data) prompts.value = res.data
  } catch (e) { console.error(e) }
  finally { promptsLoading.value = false }
}

async function loadStats() {
  try {
    const res = await get<any>('/gateway/stats')
    if (res.data) stats.value = res.data
  } catch (e) { console.error(e) }
}

onMounted(() => {
  loadPrompts()
  loadStats()
})
</script>

