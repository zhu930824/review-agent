import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('project cards use Vue Router instead of NuxtLink', () => {
  const component = readFileSync(join(process.cwd(), 'src/components/project/ProjectCard.vue'), 'utf8')

  assert.doesNotMatch(component, /NuxtLink/)
  assert.match(component, /RouterLink/)
})
