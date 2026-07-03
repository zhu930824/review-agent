import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('knowledge page turns backend query results into a typed workbench', () => {
  const knowledgeView = readFileSync(join(process.cwd(), 'src/views/knowledge.vue'), 'utf8')

  assert.match(knowledgeView, /get<KnowledgeNode\[]>\(`\/knowledge\/query\?keyword=\$\{encodeURIComponent\(keyword\.value\)\}`\)/)
  assert.match(knowledgeView, /interface KnowledgeNode/)
  assert.match(knowledgeView, /typeFilter/)
  assert.match(knowledgeView, /filteredNodes/)
  assert.match(knowledgeView, /typeStats/)
  assert.match(knowledgeView, /value="MEMORY"/)
  assert.match(knowledgeView, /value="RULE"/)
  assert.match(knowledgeView, /value="FINDING"/)
  assert.match(knowledgeView, /显示 {{ filteredNodes\.length }} \/ {{ nodes\.length }} 个知识节点/)
})
