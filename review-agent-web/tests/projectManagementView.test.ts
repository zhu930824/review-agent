import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('project detail page exposes backend project update and delete actions', () => {
  const detailView = readFileSync(join(process.cwd(), 'src/views/projects/detail.vue'), 'utf8')

  assert.match(detailView, /编辑项目/)
  assert.match(detailView, /确认删除这个项目？/)
  assert.match(detailView, /openEditProject/)
  assert.match(detailView, /handleUpdateProject/)
  assert.match(detailView, /handleDeleteProject/)
  assert.match(detailView, /put<Project>\(`\/projects\/\$\{projectId\.value\}`/)
  assert.match(detailView, /del\(`\/projects\/\$\{projectId\.value\}`/)
  assert.match(detailView, /router\.push\('\/projects'\)/)
})
