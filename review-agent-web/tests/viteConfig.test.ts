import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

const configPath = join(process.cwd(), 'vite.config.ts')

test('vite config defines manual chunks for large vendor dependencies', () => {
  const config = readFileSync(configPath, 'utf8')

  assert.match(config, /manualChunks/)
  assert.match(config, /ant-design-vue/)
  assert.match(config, /@ant-design\/icons-vue/)
  assert.match(config, /vue-router/)
})
