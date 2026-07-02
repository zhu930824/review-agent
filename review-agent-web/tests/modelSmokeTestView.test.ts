import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('models page exposes model invocation smoke test entry', () => {
  const modelsView = readFileSync(join(process.cwd(), 'src/views/models.vue'), 'utf8')

  assert.match(modelsView, /模型调用烟测/)
  assert.match(modelsView, /runSmokeTest/)
  assert.match(modelsView, /post<ApiSmokeTestResult>\('\/model-config\/invocations\/smoke-test'/)
  assert.match(modelsView, /smokeTestResult\.promptTokens/)
  assert.match(modelsView, /smokeTestResult\.completionTokens/)
  assert.match(modelsView, /smokeTestResult\.costMicroCents/)
  assert.match(modelsView, /smokeTestResult\.errorMessage/)
})
