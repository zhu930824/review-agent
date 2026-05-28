<template>
  <a-space direction="vertical" :size="16" style="width:100%">
    <div>
      <h2 style="margin:0">工程知识图谱</h2>
      <p style="margin-top:4px;color:#94a3b8;font-size:13px">团队经验 -> 规则 -> 可执行化</p>
    </div>

    <a-space style="width:100%">
      <a-input
        v-model:value="keyword"
        placeholder="搜索记忆、规则、发现问题..."
        style="flex:1"
        @pressEnter="search"
      >
        <template #prefix>
          <SearchOutlined />
        </template>
      </a-input>
      <a-button type="primary" @click="search">搜索</a-button>
    </a-space>

    <div v-if="loading" style="text-align:center;padding:48px 0">
      <a-spin size="large" />
    </div>

    <template v-else>
      <a-card
        v-for="node in nodes"
        :key="node.id"
        size="small"
        style="margin-bottom:12px"
      >
        <a-space :size="12" align="start">
          <!-- 类型图标 -->
          <div
            :style="{
              width:'36px',height:'36px',borderRadius:'10px',
              display:'flex',alignItems:'center',justifyContent:'center',
              background: nodeIconBg(node.type)
            }"
          >
            <component :is="nodeIcon(node.type)" :style="{fontSize:'16px',color:nodeIconColor(node.type)}" />
          </div>

          <div style="flex:1;min-width:0">
            <a-space :size="4" wrap>
              <a-tag :color="nodeTagColor(node.type)">{{ node.type }}</a-tag>
              <span style="font-weight:600;font-size:14px">{{ node.title }}</span>
              <a-tag v-if="node.severity" color="red" size="small">{{ node.severity }}</a-tag>
            </a-space>
            <p style="margin:4px 0 0;font-size:12px;color:#94a3b8;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">
              {{ node.content }}
            </p>
            <a-space v-if="node.tags?.length" :size="4" wrap style="margin-top:8px">
              <a-tag v-for="tag in node.tags" :key="tag" style="font-size:12px">{{ tag }}</a-tag>
            </a-space>
          </div>
        </a-space>
      </a-card>

      <div v-if="!nodes.length && searched" style="text-align:center;padding:32px 0;color:#94a3b8">
        未找到匹配的知识节点
      </div>
    </template>
  </a-space>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { SearchOutlined, BookOutlined, SafetyCertificateOutlined, ExclamationCircleOutlined } from '@ant-design/icons-vue'
import { useApi } from '@/composables/useApi'

const { get } = useApi()
const loading = ref(false)
const keyword = ref('')
const searched = ref(false)
const nodes = ref<any[]>([])

// 知识节点类型对应的图标和颜色
const iconMap: Record<string, any> = {
  MEMORY: BookOutlined,
  RULE: SafetyCertificateOutlined,
  FINDING: ExclamationCircleOutlined,
}

function nodeIcon(type: string) {
  return iconMap[type] || ExclamationCircleOutlined
}

function nodeIconBg(type: string): string {
  const map: Record<string, string> = { MEMORY: '#f3e8ff', RULE: '#fef3c7', FINDING: '#dbeafe' }
  return map[type] || '#dbeafe'
}

function nodeIconColor(type: string): string {
  const map: Record<string, string> = { MEMORY: '#9333ea', RULE: '#d97706', FINDING: '#2563eb' }
  return map[type] || '#2563eb'
}

function nodeTagColor(type: string): string {
  const map: Record<string, string> = { MEMORY: 'purple', RULE: 'orange', FINDING: 'blue' }
  return map[type] || 'blue'
}

async function search() {
  loading.value = true
  searched.value = true
  try {
    const res = await get<any[]>(`/knowledge/query?keyword=${encodeURIComponent(keyword.value)}`)
    if (res.data) nodes.value = res.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(() => {
  search()
})
</script>

