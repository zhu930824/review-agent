import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import test from 'node:test'

test('governance page renders ci status readiness panel', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /ciStatusReadiness/)
  assert.match(governanceView, /getCiStatusIntegrationReadiness/)
  assert.match(governanceView, /backendSignals/)
  assert.match(governanceView, /nextActions/)
})

test('governance page turns telemetry readiness gaps into action items', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /telemetryGapActions/)
  assert.match(governanceView, /loadTelemetryReadiness/)
  assert.match(governanceView, /get<TelemetryReadinessItem\[]>\('\/operations\/telemetry-readiness'\)/)
  assert.match(governanceView, /buildTelemetryGapActions/)
  assert.match(governanceView, /telemetryReadinessColor/)
})

test('governance page loads backend catalog before falling back to local catalog', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /loadGovernanceCatalog/)
  assert.match(governanceView, /get<MarketCapability\[]>\('\/governance\/capabilities'\)/)
  assert.match(governanceView, /get<IntegrationConnector\[]>\('\/governance\/connectors'\)/)
  assert.match(governanceView, /get<GovernanceRulePack\[]>\('\/governance\/rule-packs'\)/)
  assert.match(governanceView, /get<WorkflowTemplate\[]>\('\/governance\/workflows'\)/)
  assert.match(governanceView, /get<GovernanceRulePackChange\[]>\('\/governance\/rule-pack-changes\?limit=20'\)/)
  assert.match(governanceView, /get<GovernanceRulePackVersion\[]>\('\/governance\/rule-pack-versions\?limit=20'\)/)
  assert.match(governanceView, /fallbackCapabilities/)
  assert.match(governanceView, /fallbackWorkflowTemplates/)
})

test('governance page renders rule pack change records', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /规则包变更/)
  assert.match(governanceView, /rulePackChanges/)
  assert.match(governanceView, /rulePackChangeStatusColor/)
  assert.match(governanceView, /handleRulePackChangeAction/)
  assert.match(governanceView, /previewRulePackChange/)
  assert.match(governanceView, /\/governance\/rule-pack-changes\/\$\{change\.id\}\/dry-run/)
  assert.match(governanceView, /\/governance\/rule-pack-changes\/\$\{change\.id\}\/\$\{action\}/)
  assert.match(governanceView, /预览/)
  assert.match(governanceView, /批准/)
  assert.match(governanceView, /应用/)
  assert.match(governanceView, /拒绝/)
  assert.match(governanceView, /回滚/)
  assert.match(governanceView, /change\.rulePackKey/)
  assert.match(governanceView, /Finding #/)
})

test('governance page previews rule pack change dry-run result', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')
  const governanceTypes = readFileSync(join(process.cwd(), 'src/types/governance.ts'), 'utf8')

  assert.match(governanceTypes, /export interface GovernanceRulePackDryRun/)
  assert.match(governanceView, /rulePackDryRunModalOpen/)
  assert.match(governanceView, /rulePackDryRun\.impactSummary/)
  assert.match(governanceView, /rulePackDryRun\.proposedControls/)
  assert.match(governanceView, /formatJson\(rulePackDryRun\.controlsSnapshot\)/)
})

test('governance page renders rule pack version snapshots', () => {
  const governanceView = readFileSync(join(process.cwd(), 'src/views/governance.vue'), 'utf8')

  assert.match(governanceView, /规则包版本/)
  assert.match(governanceView, /rulePackVersions/)
  assert.match(governanceView, /loadRulePackVersions/)
  assert.match(governanceView, /rulePackVersionStatusColor/)
  assert.match(governanceView, /version\.rulePackKey/)
  assert.match(governanceView, /version\.versionNo/)
  assert.match(governanceView, /Change #/)
})

test('backend exposes governance rule pack change records from accepted learning candidates', () => {
  const serverRoot = join(process.cwd(), '../review-agent-server/src/main')
  const controller = readFileSync(join(serverRoot, 'java/com/review/agent/controller/GovernanceController.java'), 'utf8')
  const operationsService = readFileSync(join(serverRoot, 'java/com/review/agent/service/impl/OperationsRemediationQueueServiceImpl.java'), 'utf8')
  const repository = readFileSync(join(serverRoot, 'java/com/review/agent/infrastructure/persistence/JdbcGovernanceRulePackChangeRepository.java'), 'utf8')
  const versionRepository = readFileSync(join(serverRoot, 'java/com/review/agent/infrastructure/persistence/JdbcGovernanceRulePackVersionRepository.java'), 'utf8')
  const migration = readFileSync(join(serverRoot, 'resources/db/migration/V14__governance_rule_pack_change.sql'), 'utf8')
  const versionMigration = readFileSync(join(serverRoot, 'resources/db/migration/V16__governance_rule_pack_version.sql'), 'utf8')

  assert.match(controller, /@GetMapping\("\/rule-pack-changes"\)/)
  assert.match(controller, /@GetMapping\("\/rule-pack-versions"\)/)
  assert.match(controller, /@PostMapping\("\/rule-pack-changes\/\{id\}\/approve"\)/)
  assert.match(controller, /@PostMapping\("\/rule-pack-changes\/\{id\}\/dry-run"\)/)
  assert.match(controller, /@PostMapping\("\/rule-pack-changes\/\{id\}\/apply"\)/)
  assert.match(controller, /@PostMapping\("\/rule-pack-changes\/\{id\}\/reject"\)/)
  assert.match(controller, /@PostMapping\("\/rule-pack-changes\/\{id\}\/rollback"\)/)
  assert.match(controller, /rulePackVersionRepository\.dryRunChange\(id\)/)
  assert.match(controller, /rulePackVersionRepository\.applyChange\(id\)/)
  assert.match(controller, /rulePackVersionRepository\.rollbackChange\(id\)/)
  assert.match(operationsService, /proposeFromRuleLearningCandidate\(candidate\)/)
  assert.match(repository, /INSERT INTO governance_rule_pack_change/)
  assert.match(repository, /UPDATE governance_rule_pack_change/)
  assert.match(repository, /DEFAULT_RULE_PACK_KEY = "team-rule-memory"/)
  assert.match(versionRepository, /INSERT INTO governance_rule_pack_version/)
  assert.match(versionRepository, /INSERT INTO governance_rule_pack/)
  assert.match(versionRepository, /UPDATE governance_rule_pack/)
  assert.match(versionRepository, /controls\.add\(proposedControl\)/)
  assert.match(versionRepository, /controls\.removeIf\(proposedControl::equals\)/)
  assert.match(versionRepository, /version_status = 'ROLLED_BACK'/)
  assert.match(versionRepository, /dryRunChange/)
  assert.match(versionRepository, /SELECT controls/)
  assert.match(versionRepository, /setProposedControls/)
  assert.match(versionRepository, /controls_snapshot/)
  assert.match(migration, /CREATE TABLE IF NOT EXISTS governance_rule_pack_change/)
  assert.match(migration, /UNIQUE KEY uk_governance_rule_pack_change_finding/)
  assert.match(versionMigration, /CREATE TABLE IF NOT EXISTS governance_rule_pack_version/)
  assert.match(versionMigration, /UNIQUE KEY uk_rule_pack_version_change/)
})
