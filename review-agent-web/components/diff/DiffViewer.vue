<template>
  <div class="space-y-2">
    <!-- 文件路径 -->
    <div class="flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400">
      <UIcon name="i-heroicons-document-text" class="h-4 w-4" />
      <span class="font-mono text-xs">{{ filePath }}</span>
    </div>

    <!-- Diff 内容展示区域（占位实现，后续可替换为专业 diff 库） -->
    <div
      class="overflow-auto rounded-lg border border-gray-200 bg-gray-950 font-mono text-xs leading-relaxed dark:border-gray-700"
    >
      <table class="w-full">
        <tbody>
          <tr
            v-for="(line, idx) in diffLines"
            :key="idx"
            :class="lineClass(line.type)"
          >
            <td class="select-none px-2 py-0.5 text-right text-gray-500 w-10">
              {{ line.oldLine }}
            </td>
            <td class="select-none px-2 py-0.5 text-right text-gray-500 w-10">
              {{ line.newLine }}
            </td>
            <td class="whitespace-pre px-3 py-0.5">
              <span class="mr-1 select-none">{{ line.prefix }}</span>
              <span>{{ line.content }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
// components/diff/DiffViewer.vue - 代码 Diff 查看组件
// 占位实现：按行解析 diff 文本并着色

const props = defineProps<{
  filePath: string
  diffContent: string
}>()

/** Diff 行数据 */
interface DiffLine {
  oldLine: string
  newLine: string
  prefix: string
  content: string
  type: 'add' | 'del' | 'header' | 'normal'
}

/**
 * 解析 unified diff 格式文本为行数组
 * 当前为占位实现，仅做简单解析
 */
const diffLines = computed<DiffLine[]>(() => {
  const lines = props.diffContent.split('\n')
  let oldLineNum = 0
  let newLineNum = 0

  return lines.map((line) => {
    if (line.startsWith('@@')) {
      // 解析 hunk header，提取行号
      const match = line.match(/@@ -(\d+),?\d* \+(\d+),?\d* @@/)
      if (match) {
        oldLineNum = parseInt(match[1], 10)
        newLineNum = parseInt(match[2], 10)
      }
      return { oldLine: '···', newLine: '···', prefix: '', content: line, type: 'header' as const }
    }

    if (line.startsWith('+')) {
      newLineNum++
      return { oldLine: '', newLine: String(newLineNum), prefix: '+', content: line.slice(1), type: 'add' as const }
    }
    if (line.startsWith('-')) {
      oldLineNum++
      return { oldLine: String(oldLineNum), newLine: '', prefix: '-', content: line.slice(1), type: 'del' as const }
    }

    oldLineNum++
    newLineNum++
    return { oldLine: String(oldLineNum), newLine: String(newLineNum), prefix: ' ', content: line.startsWith(' ') ? line.slice(1) : line, type: 'normal' as const }
  })
})

/** 根据 diff 类型返回行样式类 */
function lineClass(type: string): string {
  switch (type) {
    case 'add':
      return 'bg-green-900/30 text-green-300'
    case 'del':
      return 'bg-red-900/30 text-red-300'
    case 'header':
      return 'bg-cyan-900/40 text-cyan-400 font-medium'
    default:
      return 'text-gray-300'
  }
}
</script>
