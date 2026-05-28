<template>
  <div>
    <div style="display: flex; align-items: center; gap: 8px; font-size: 13px; color: #999; margin-bottom: 8px">
      <FileTextOutlined />
      <span style="font-family: monospace; font-size: 12px">{{ filePath }}</span>
    </div>

    <div style="overflow: auto; border-radius: 6px; border: 1px solid #d9d9d9; font-family: monospace; font-size: 12px; line-height: 1.5">
      <table style="width: 100%; border-collapse: collapse">
        <tbody>
          <tr v-for="(line, idx) in diffLines" :key="idx" :style="lineStyle(line.type)">
            <td style="user-select: none; padding: 2px 8px; text-align: right; color: #999; width: 40px">{{ line.oldLine }}</td>
            <td style="user-select: none; padding: 2px 8px; text-align: right; color: #999; width: 40px">{{ line.newLine }}</td>
            <td style="padding: 2px 12px; white-space: pre">
              <span style="user-select: none; margin-right: 4px">{{ line.prefix }}</span>
              <span>{{ line.content }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FileTextOutlined } from '@ant-design/icons-vue'

const props = defineProps<{ filePath: string; diffContent: string }>()

interface DiffLine {
  oldLine: string; newLine: string; prefix: string; content: string; type: 'add' | 'del' | 'header' | 'normal'
}

const diffLines = computed<DiffLine[]>(() => {
  const lines = props.diffContent.split('\n')
  let oldLineNum = 0; let newLineNum = 0

  return lines.map(line => {
    if (line.startsWith('@@')) {
      const match = line.match(/@@ -(\d+),?\d* \+(\d+),?\d* @@/)
      if (match) { oldLineNum = parseInt(match[1], 10); newLineNum = parseInt(match[2], 10) }
      return { oldLine: '···', newLine: '···', prefix: '', content: line, type: 'header' as const }
    }
    if (line.startsWith('+')) { newLineNum++; return { oldLine: '', newLine: String(newLineNum), prefix: '+', content: line.slice(1), type: 'add' as const } }
    if (line.startsWith('-')) { oldLineNum++; return { oldLine: String(oldLineNum), newLine: '', prefix: '-', content: line.slice(1), type: 'del' as const } }
    oldLineNum++; newLineNum++
    return { oldLine: String(oldLineNum), newLine: String(newLineNum), prefix: ' ', content: line.startsWith(' ') ? line.slice(1) : line, type: 'normal' as const }
  })
})

function lineStyle(type: string): Record<string, string> {
  switch (type) {
    case 'add': return { background: '#f6ffed', color: '#389e0d' }
    case 'del': return { background: '#fff2f0', color: '#cf1322' }
    case 'header': return { background: '#e6f7ff', color: '#096dd9', fontWeight: '500' }
    default: return { color: '#666' }
  }
}
</script>
