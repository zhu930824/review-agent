import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('models page exposes backend model profile management actions', () => {
  const modelsView = readFileSync(join(process.cwd(), 'src/views/models.vue'), 'utf8')

  assert.match(modelsView, /新增模型供应商/)
  assert.match(modelsView, /编辑模型供应商/)
  assert.match(modelsView, /确认删除这个模型供应商？/)
  assert.match(modelsView, /post<ApiModelProvider>\('\/model-config\/providers'/)
  assert.match(modelsView, /put<ApiModelProvider>\(`\/model-config\/providers\/\$\{editingProviderId\.value\}`/)
  assert.match(modelsView, /del\(`\/model-config\/providers\/\$\{provider\.id\}`/)
  assert.match(modelsView, /新增模型档案/)
  assert.match(modelsView, /编辑模型档案/)
  assert.match(modelsView, /确认删除这个模型档案？/)
  assert.match(modelsView, /post<ApiModelProfile>\('\/model-config\/profiles'/)
  assert.match(modelsView, /put<ApiModelProfile>\(`\/model-config\/profiles\/\$\{editingProfileId\.value\}`/)
  assert.match(modelsView, /del\(`\/model-config\/profiles\/\$\{profile\.id\}`/)
  assert.match(modelsView, /capabilityTags: profileForm\.capabilityTagsText/)
  assert.match(modelsView, /新增审查策略/)
  assert.match(modelsView, /编辑审查策略/)
  assert.match(modelsView, /确认删除这个审查策略？/)
  assert.match(modelsView, /post<ApiReviewStrategy>\('\/model-config\/strategies'/)
  assert.match(modelsView, /put<ApiReviewStrategy>\(`\/model-config\/strategies\/\$\{editingStrategyId\.value\}`/)
  assert.match(modelsView, /del\(`\/model-config\/strategies\/\$\{strategy\.id\}`/)
  assert.match(modelsView, /roleBindings: strategyForm\.roleBindings/)
})
