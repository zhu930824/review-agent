import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

const root = process.cwd()

const copyFiles = [
  'src/views/dashboard.vue',
  'src/views/models.vue',
  'src/views/reviews/create.vue',
  'src/views/reviews/detail.vue',
  'src/components/layout/AppSidebar.vue',
  'src/utils/modelStrategies.ts',
  'src/utils/reviewMetrics.ts',
  'src/utils/prePrGate.ts',
  'src/utils/prePrReport.ts',
]

const mojibakeMarkers = [
  '瀹℃煡',
  '妯″瀷',
  '璐ㄩ噺',
  '闃绘柇',
  '浜哄伐',
  '绛栫暐',
  '鏌ョ湅',
  '鍙戣捣',
]

test('business-facing pages do not contain mojibake copy', () => {
  for (const file of copyFiles) {
    const source = readFileSync(join(root, file), 'utf8')

    for (const marker of mojibakeMarkers) {
      assert.equal(source.includes(marker), false, `${file} contains mojibake marker ${marker}`)
    }
  }
})

test('business-facing pages keep readable Chinese copy', () => {
  const dashboard = readFileSync(join(root, 'src/views/dashboard.vue'), 'utf8')
  const models = readFileSync(join(root, 'src/views/models.vue'), 'utf8')
  const create = readFileSync(join(root, 'src/views/reviews/create.vue'), 'utf8')
  const detail = readFileSync(join(root, 'src/views/reviews/detail.vue'), 'utf8')

  assert.ok(dashboard.includes('质量驾驶舱'))
  assert.ok(models.includes('模型配置'))
  assert.ok(create.includes('发起 Pre-PR 审查'))
  assert.ok(detail.includes('审查详情'))
})
