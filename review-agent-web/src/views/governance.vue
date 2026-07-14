<template>
  <a-space direction="vertical" :size="16" style="width:100%" class="governance-page">
    <!-- 标题栏 -->
    <a-space style="width:100%;justify-content:space-between;flex-wrap:wrap">
      <div>
        <h2 style="margin:0">治理中心</h2>
        <p style="margin-top:4px;color:#94a3b8;font-size:13px">对标 AI Review、质量门禁和安全扫描产品能力，把平台路线图转成可执行的规则包、集成和工作流。</p>
      </div>
      <a-space wrap>
        <RouterLink to="/reviews/create">
          <a-button type="primary">
            <template #icon><PlayCircleOutlined /></template>
            按模板发起审查
          </a-button>
        </RouterLink>
        <RouterLink to="/settings/models">
          <a-button>
            <template #icon><SettingOutlined /></template>
            配置模型策略
          </a-button>
        </RouterLink>
      </a-space>
    </a-space>

    <!-- KPI 卡片 -->
    <a-row :gutter="16" class="kpi-row">
      <a-col :xl="6" :md="12" :span="24">
        <a-card size="small" class="governance-kpi-card">
          <a-space :size="8" style="width:100%;justify-content:space-between">
            <div>
              <div style="font-size:12px;font-weight:600;text-transform:uppercase;color:#94a3b8;letter-spacing:0.5px">市场能力覆盖</div>
              <div style="font-size:28px;font-weight:800;background:linear-gradient(135deg,#6366f1,#a855f7);-webkit-background-clip:text;-webkit-text-fill-color:transparent;margin-top:4px">
                {{ coverage.coveragePercent }}%
              </div>
              <div style="font-size:12px;color:#94a3b8;margin-top:2px">按已启用与部分具备折算</div>
            </div>
            <BarChartOutlined style="font-size:20px;color:#6366f1" />
          </a-space>
          <a-progress :percent="coverage.coveragePercent" :show-info="false" stroke-color="linear-gradient(90deg,#6366f1,#a855f7)" style="margin-top:8px" />
        </a-card>
      </a-col>

      <a-col :xl="6" :md="12" :span="24">
        <a-card size="small" class="governance-kpi-card">
          <a-space :size="8" style="width:100%;justify-content:space-between">
            <div>
              <div style="font-size:12px;font-weight:600;text-transform:uppercase;color:#94a3b8;letter-spacing:0.5px">已启用能力</div>
              <div style="font-size:28px;font-weight:800;color:#059669;margin-top:4px">{{ coverage.enabled }}</div>
              <div style="font-size:12px;color:#94a3b8;margin-top:2px">PR 摘要、行级审查、质量门禁、效能分析</div>
            </div>
            <CheckCircleOutlined style="font-size:20px;color:#059669" />
          </a-space>
        </a-card>
      </a-col>

      <a-col :xl="6" :md="12" :span="24">
        <a-card size="small" class="governance-kpi-card">
          <a-space :size="8" style="width:100%;justify-content:space-between">
            <div>
              <div style="font-size:12px;font-weight:600;text-transform:uppercase;color:#94a3b8;letter-spacing:0.5px">待补齐关键项</div>
              <div style="font-size:28px;font-weight:800;color:#d97706;margin-top:4px">{{ coverage.partial + coverage.planned }}</div>
              <div style="font-size:12px;color:#94a3b8;margin-top:2px">安全扫描、状态检查、SARIF、自动修复</div>
            </div>
            <ExclamationCircleOutlined style="font-size:20px;color:#d97706" />
          </a-space>
        </a-card>
      </a-col>

      <a-col :xl="6" :md="12" :span="24">
        <a-card size="small" class="governance-kpi-card">
          <a-space :size="8" style="width:100%;justify-content:space-between">
            <div>
              <div style="font-size:12px;font-weight:600;text-transform:uppercase;color:#94a3b8;letter-spacing:0.5px">工作流模板</div>
              <div style="font-size:28px;font-weight:800;margin-top:4px">{{ workflowTemplates.length }}</div>
              <div style="font-size:12px;color:#94a3b8;margin-top:2px">覆盖日常自查、发布门禁和架构评审</div>
            </div>
            <AppstoreOutlined style="font-size:20px;color:#6366f1" />
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <!-- 主内容区 -->
    <a-row :gutter="16" class="governance-content-row">
      <!-- 市场能力对标表格 -->
      <a-col :xl="16" :span="24" style="margin-bottom:16px">
        <a-card size="small">
          <template #title>
            <a-space :size="8">
              <AppstoreOutlined style="font-size:20px;color:#6366f1" />
              <span style="font-weight:600">市场能力对标</span>
            </a-space>
          </template>
          <template #extra>
            <a-tag color="processing">{{ marketCapabilities.length }} 项能力</a-tag>
          </template>
          <a-table
            :columns="capabilityColumns"
            :data-source="marketCapabilities"
            :pagination="false"
            row-key="id"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'name'">
                <div>
                  <div style="font-weight:600;font-size:13px">{{ record.name }}</div>
                  <div style="font-size:12px;color:#94a3b8;line-height:1.4;margin-top:2px">{{ record.platformMove }}</div>
                </div>
              </template>
              <template v-else-if="column.key === 'category'">
                <a-tag>{{ categoryLabel(record.category) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'status'">
                <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'businessImpact'">
                <a-tag :color="impactColor(record.businessImpact)">{{ impactLabel(record.businessImpact) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'sourceProducts'">
                <a-space :size="4" wrap>
                  <a-tag v-for="product in record.sourceProducts" :key="product" color="blue">{{ product }}</a-tag>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <!-- 右侧边栏 -->
      <a-col :xl="8" :span="24">
        <div class="governance-sidebar-cards">
        <a-card size="small">
          <template #title>
            <a-space :size="8">
              <ThunderboltOutlined style="font-size:16px;color:#d97706" />
              <span style="font-weight:600;font-size:14px">优先落地项</span>
            </a-space>
          </template>
          <a-space direction="vertical" :size="8" style="width:100%">
            <a-card
              v-for="action in recommendedActions"
              :key="action.id"
              size="small"
              class="governance-nested-card"
            >
              <a-space :size="8" style="width:100%;justify-content:space-between">
                <div>
                  <div style="font-weight:600;font-size:13px">{{ action.name }}</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:2px;line-height:1.4">{{ action.platformMove }}</div>
                </div>
                <a-tag color="orange">{{ action.priorityScore }}</a-tag>
              </a-space>
            </a-card>
          </a-space>
        </a-card>

        <!-- 发布门禁策略包 -->
        <a-card size="small">
          <template #title>
            <a-space :size="8">
              <SafetyOutlined style="font-size:16px;color:#ef4444" />
              <span style="font-weight:600;font-size:14px">发布门禁策略包</span>
            </a-space>
          </template>
          <a-space direction="vertical" :size="12" style="width:100%">
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">要求能力</div>
              <a-space :size="4" wrap>
                <a-tag v-for="capability in releasePolicy.requiredCapabilities" :key="capability" color="red">{{ capability }}</a-tag>
              </a-space>
            </div>
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">建议策略</div>
              <a-space :size="4" wrap>
                <a-tag v-for="strategy in releasePolicy.suggestedStrategies" :key="strategy" color="processing">{{ strategy }}</a-tag>
              </a-space>
            </div>
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">人工检查点</div>
              <a-space direction="vertical" :size="4">
                <a-space v-for="checkpoint in releasePolicy.requiredHumanCheckpoints" :key="checkpoint" :size="4">
                  <UserOutlined style="color:#d97706;font-size:14px" />
                  <span style="font-size:12px">{{ checkpoint }}</span>
                </a-space>
              </a-space>
            </div>
          </a-space>
        </a-card>

        <a-card size="small">
          <template #title>
            <a-space :size="8">
              <SafetyOutlined style="font-size:16px;color:#059669" />
              <span style="font-weight:600;font-size:14px">规则包版本</span>
            </a-space>
          </template>
          <a-space v-if="rulePackVersions.length" direction="vertical" :size="8" style="width:100%">
            <a-card
              v-for="version in rulePackVersions"
              :key="version.id"
              size="small"
              class="governance-nested-card"
            >
              <a-space direction="vertical" :size="6" style="width:100%">
                <a-space :size="6" wrap>
                  <a-tag color="processing">{{ version.rulePackKey }}</a-tag>
                  <a-tag color="green">v{{ version.versionNo }}</a-tag>
                  <a-tag :color="rulePackVersionStatusColor(version.versionStatus)">{{ version.versionStatus }}</a-tag>
                </a-space>
                <div style="font-weight:600;font-size:13px">{{ version.title }}</div>
                <div style="font-size:12px;color:#64748b;line-height:1.5">{{ version.rationale || '暂无版本说明' }}</div>
                <div style="font-size:12px;color:#94a3b8">Change #{{ version.sourceChangeId }} · {{ version.createdBy || 'governance' }}</div>
                <a-button
                  size="small"
                  :disabled="!version.controlsSnapshot"
                  @click="viewRulePackVersionSnapshot(version)"
                >
                  查看快照
                </a-button>
              </a-space>
            </a-card>
          </a-space>
          <a-empty v-else description="暂无规则包版本" :image="undefined" />
        </a-card>

        <a-card size="small">
          <template #title>
            <a-space :size="8">
              <ThunderboltOutlined style="font-size:16px;color:#059669" />
              <span style="font-weight:600;font-size:14px">规则包变更</span>
            </a-space>
          </template>
          <a-space v-if="rulePackChanges.length" direction="vertical" :size="8" style="width:100%">
            <a-card
              v-for="change in rulePackChanges"
              :key="change.id"
              size="small"
              class="governance-nested-card"
            >
              <a-space direction="vertical" :size="6" style="width:100%">
                <a-space :size="6" wrap>
                  <a-tag color="processing">{{ change.rulePackKey }}</a-tag>
                  <a-tag :color="rulePackChangeStatusColor(change.status)">{{ change.status }}</a-tag>
                  <a-tag>{{ change.changeType }}</a-tag>
                </a-space>
                <div style="font-weight:600;font-size:13px">{{ change.title }}</div>
                <div style="font-size:12px;color:#64748b;line-height:1.5">{{ change.rationale || '暂无变更说明' }}</div>
                <div style="font-size:12px;color:#94a3b8">Finding #{{ change.findingId }} · {{ change.createdBy || 'operations' }}</div>
                <a-space :size="4" wrap>
                  <a-button
                    v-if="change.status === 'PROPOSED' || change.status === 'APPROVED'"
                    size="small"
                    :loading="actingRulePackChangeId === change.id && actingRulePackChangeAction === 'dry-run'"
                    @click="previewRulePackChange(change)"
                  >
                    预览
                  </a-button>
                  <a-button
                    v-if="can('GOVERNANCE_MANAGE') && change.status === 'PROPOSED'"
                    type="primary"
                    size="small"
                    :loading="actingRulePackChangeId === change.id && actingRulePackChangeAction === 'approve'"
                    @click="handleRulePackChangeAction(change, 'approve')"
                  >
                    批准
                  </a-button>
                  <a-button
                    v-if="can('GOVERNANCE_MANAGE') && change.status === 'APPROVED'"
                    type="primary"
                    size="small"
                    :loading="actingRulePackChangeId === change.id && actingRulePackChangeAction === 'apply'"
                    @click="handleRulePackChangeAction(change, 'apply')"
                  >
                    应用
                  </a-button>
                  <a-button
                    v-if="can('GOVERNANCE_MANAGE') && (change.status === 'PROPOSED' || change.status === 'APPROVED')"
                    size="small"
                    danger
                    :loading="actingRulePackChangeId === change.id && actingRulePackChangeAction === 'reject'"
                    @click="handleRulePackChangeAction(change, 'reject')"
                  >
                    拒绝
                  </a-button>
                  <a-button
                    v-if="can('GOVERNANCE_MANAGE') && change.status === 'APPLIED'"
                    size="small"
                    :loading="actingRulePackChangeId === change.id && actingRulePackChangeAction === 'rollback'"
                    @click="handleRulePackChangeAction(change, 'rollback')"
                  >
                    回滚
                  </a-button>
                </a-space>
              </a-space>
            </a-card>
          </a-space>
          <a-empty v-else description="暂无规则包变更" :image="undefined" />
        </a-card>

        <a-card size="small">
          <template #title>
            <a-space :size="8">
              <EnvironmentOutlined style="font-size:16px;color:#0891b2" />
              <span style="font-weight:600;font-size:14px">遥测行动项</span>
            </a-space>
          </template>
          <a-space v-if="telemetryGapActions.length" direction="vertical" :size="8" style="width:100%">
            <a-card
              v-for="action in telemetryGapActions"
              :key="action.key"
              size="small"
              class="governance-nested-card"
            >
              <a-space direction="vertical" :size="6" style="width:100%">
                <a-space :size="6" wrap>
                  <a-tag color="processing">{{ action.strategyKey }}</a-tag>
                  <a-tag :color="telemetryReadinessColor(action.readinessLevel)">{{ action.readinessLevel }}</a-tag>
                  <a-tag>{{ action.gapCode }}</a-tag>
                </a-space>
                <div style="font-weight:600;font-size:13px">{{ action.title }}</div>
                <div style="font-size:12px;color:#64748b;line-height:1.5">{{ action.recommendation }}</div>
              </a-space>
            </a-card>
          </a-space>
          <a-empty v-else description="暂无遥测缺口" :image="undefined" />
        </a-card>

        <a-card size="small">
          <template #title>
            <a-space :size="8">
              <SyncOutlined style="font-size:16px;color:#d97706" />
              <span style="font-weight:600;font-size:14px">CI Health Actions</span>
            </a-space>
          </template>
          <a-space v-if="ciHealthActions.length" direction="vertical" :size="8" style="width:100%">
            <a-card
              v-for="action in ciHealthActions"
              :key="action.key"
              size="small"
              class="governance-nested-card"
            >
              <a-space direction="vertical" :size="6" style="width:100%">
                <a-space :size="6" wrap>
                  <a-tag color="processing">{{ action.provider }}</a-tag>
                  <a-tag :color="ciHealthActionColor(action.severity)">{{ action.healthStatus }}</a-tag>
                  <a-tag>{{ action.latestSignal }}</a-tag>
                </a-space>
                <div style="font-weight:600;font-size:13px">{{ action.title }}</div>
                <div style="font-size:12px;color:#64748b;line-height:1.5">{{ action.recommendation }}</div>
              </a-space>
            </a-card>
          </a-space>
          <a-empty v-else description="No CI health actions" :image="undefined" />
        </a-card>

        <a-card size="small" class="governance-card-scroll">
          <template #title>
            <a-space :size="8">
              <SyncOutlined style="font-size:16px;color:#6366f1" />
              <span style="font-weight:600;font-size:14px">CI 回写就绪度</span>
            </a-space>
          </template>
          <a-space direction="vertical" :size="12" style="width:100%">
            <a-space :size="6" wrap>
              <a-tag color="processing">{{ ciStatusReadiness.name }}</a-tag>
              <a-tag :color="statusColor(ciStatusReadiness.status)">{{ statusLabel(ciStatusReadiness.status) }}</a-tag>
              <a-tag>{{ ciStatusReadiness.stage }}</a-tag>
            </a-space>
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">依赖能力</div>
              <a-space :size="4" wrap>
                <a-tag v-for="capability in ciStatusReadiness.requiredCapabilityIds" :key="capability" color="blue">{{ capability }}</a-tag>
              </a-space>
            </div>
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">后端回写信号</div>
              <a-space :size="4" wrap>
                <a-tag v-for="signal in ciStatusReadiness.backendSignals" :key="signal" color="green">{{ signal }}</a-tag>
              </a-space>
            </div>
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">下一步</div>
              <a-space direction="vertical" :size="4">
                <span v-for="action in ciStatusReadiness.nextActions" :key="action" style="font-size:12px;color:#475569;line-height:1.5">{{ action }}</span>
              </a-space>
            </div>
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">CI Provider Health</div>
              <a-row v-if="ciIntegrationHealth.length" :gutter="[8,8]">
                <a-col
                  v-for="item in ciIntegrationHealth"
                  :key="item.connectorKey"
                  :xs="24"
                  :md="8"
                >
                  <a-card size="small" class="governance-sub-card" style="height:100%">
                    <a-space direction="vertical" :size="6" style="width:100%">
                      <a-space :size="4" wrap style="width:100%;justify-content:space-between">
                        <span style="font-size:12px;font-weight:700">{{ item.provider }}</span>
                        <a-tag :color="ciHealthColor(item.healthStatus)">{{ item.healthStatus }}</a-tag>
                      </a-space>
                      <div style="font-size:12px;color:#64748b;word-break:break-all">{{ item.connectorKey }}</div>
                      <a-space :size="4" wrap>
                        <a-tag color="green">S {{ item.successCount }}</a-tag>
                        <a-tag color="red">F {{ item.failedCount }}</a-tag>
                        <a-tag>Skip {{ item.skippedCount }}</a-tag>
                      </a-space>
                      <div style="font-size:12px;color:#475569;line-height:1.45">{{ item.summary || 'No recent writeback data.' }}</div>
                      <div v-if="item.latestExternalResult" style="font-size:12px;color:#94a3b8">
                        Latest external: {{ item.latestExternalResult }}
                      </div>
                    </a-space>
                  </a-card>
                </a-col>
              </a-row>
              <div v-else style="font-size:12px;color:#94a3b8">No CI health data yet.</div>
            </div>
            <a-form layout="vertical" size="small" style="margin-top:4px">
              <a-alert
                v-if="credentialSecurityHealth"
                show-icon
                :type="credentialSecurityHealth.status === 'SECURE' ? 'success' : ['MIGRATION_REQUIRED', 'KEY_MISMATCH'].includes(credentialSecurityHealth.status) ? 'error' : 'warning'"
                :message="`Credential security: ${credentialSecurityHealth.status}`"
                style="margin-bottom:12px"
              >
                <template #description>
                  <div>{{ credentialSecurityHealth.recommendation }}</div>
                  <a-space :size="4" wrap style="margin-top:6px">
                    <a-tag color="green">Active {{ credentialSecurityHealth.activeKeyCredentialCount }}</a-tag>
                    <a-tag :color="credentialSecurityHealth.previousKeyCredentialCount > 0 ? 'orange' : 'default'">
                      Previous {{ credentialSecurityHealth.previousKeyCredentialCount }}
                    </a-tag>
                    <a-tag :color="credentialSecurityHealth.plaintextCredentialCount > 0 ? 'red' : 'default'">
                      Plaintext {{ credentialSecurityHealth.plaintextCredentialCount }}
                    </a-tag>
                    <a-tag :color="credentialSecurityHealth.unreadableCredentialCount > 0 ? 'red' : 'default'">
                      Unreadable {{ credentialSecurityHealth.unreadableCredentialCount }}
                    </a-tag>
                  </a-space>
                  <div
                    v-if="can('CREDENTIAL_ROTATE') && credentialSecurityHealth.rotationRequired && credentialSecurityHealth.unreadableCredentialCount === 0"
                    style="margin-top:8px"
                  >
                    <a-popconfirm
                      title="确认使用当前主密钥重新加密全部历史凭据？"
                      ok-text="执行轮换"
                      cancel-text="取消"
                      @confirm="rotateCredentials"
                    >
                      <a-button size="small" danger :loading="credentialRotationRunning">
                        轮换到当前密钥
                      </a-button>
                    </a-popconfirm>
                  </div>
                </template>
              </a-alert>
              <a-alert
                v-if="credentialRotationResult"
                type="success"
                show-icon
                closable
                :message="`Credential rotation: ${credentialRotationResult.status}`"
                :description="`${credentialRotationResult.message} Rotated ${credentialRotationResult.rotatedCredentialCount}.`"
                style="margin-bottom:12px"
                @close="credentialRotationResult = null"
              />
              <a-form-item label="CI Provider">
                <a-select
                  v-model:value="ciProviderSelection"
                  style="width:100%"
                  @change="handleCiConnectorChange"
                >
                  <a-select-option
                    v-for="option in ciProviderOptions"
                    :key="option.connectorKey"
                    :value="option.connectorKey"
                  >
                    {{ option.label }}
                  </a-select-option>
                </a-select>
                <div style="margin-top:4px;font-size:12px;color:#94a3b8;line-height:1.5">
                  {{ selectedCiProviderOption.description }}
                </div>
              </a-form-item>
              <div v-if="ciConfigForm.provider === 'JENKINS'" class="jenkins-instance-toolbar">
                <a-space :size="8" wrap style="width:100%">
                  <a-select
                    v-if="!creatingJenkinsInstance"
                    v-model:value="ciConfigForm.connectorKey"
                    style="min-width:240px;flex:1"
                    placeholder="选择 Jenkins 实例"
                    @change="selectJenkinsInstance"
                  >
                    <a-select-option
                      v-for="instance in jenkinsInstances"
                      :key="instance.connectorKey"
                      :value="instance.connectorKey"
                    >
                      {{ instance.displayName || instance.connectorKey }}
                      <span v-if="instance.projectId"> · Project #{{ instance.projectId }}</span>
                    </a-select-option>
                  </a-select>
                  <a-button v-if="can('INTEGRATION_MANAGE')" size="small" @click="startNewJenkinsInstance">
                    新建实例
                  </a-button>
                  <a-popconfirm
                    v-if="can('INTEGRATION_MANAGE') && !creatingJenkinsInstance && ciConfigForm.connectorKey"
                    title="删除当前 Jenkins 实例配置？历史日志会保留。"
                    ok-text="删除"
                    cancel-text="取消"
                    @confirm="deleteJenkinsInstance"
                  >
                    <a-button size="small" danger>删除实例</a-button>
                  </a-popconfirm>
                  <a-button v-if="creatingJenkinsInstance" size="small" @click="cancelNewJenkinsInstance">取消</a-button>
                </a-space>
              </div>
              <a-row v-if="ciConfigForm.provider === 'JENKINS'" :gutter="8">
                <a-col :xs="24" :md="8">
                  <a-form-item
                    label="实例 Key"
                    :validate-status="creatingJenkinsInstance && !jenkinsInstanceKeyValid ? 'error' : undefined"
                    :help="creatingJenkinsInstance && !jenkinsInstanceKeyValid ? '使用小写字母、数字、点、下划线或连字符，最长 64 位' : undefined"
                  >
                    <a-input
                      v-model:value="ciConfigForm.instanceKey"
                      :disabled="!creatingJenkinsInstance"
                      addon-before="jenkins-pipeline:"
                      placeholder="team-a"
                    />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="8">
                  <a-form-item label="实例名称">
                    <a-input v-model:value="ciConfigForm.displayName" placeholder="研发一部 Jenkins" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="8">
                  <a-form-item label="绑定项目 ID">
                    <a-input-number v-model:value="ciConfigForm.projectId" :min="1" :precision="0" style="width:100%" placeholder="留空表示全局" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="8">
                <a-col :xs="24" :md="12">
                  <a-form-item :label="selectedCiProviderOption.ownerLabel">
                    <a-input v-model:value="ciConfigForm.repoOwner" :placeholder="selectedCiProviderOption.ownerPlaceholder" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item :label="selectedCiProviderOption.nameLabel">
                    <a-input v-model:value="ciConfigForm.repoName" :placeholder="selectedCiProviderOption.namePlaceholder" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item :label="selectedCiProviderOption.urlLabel">
                <a-input v-model:value="ciConfigForm.repoUrl" :placeholder="selectedCiProviderOption.urlPlaceholder" />
              </a-form-item>
              <a-form-item label="Status Context">
                <a-input v-model:value="ciConfigForm.statusContext" :placeholder="selectedCiProviderOption.statusPlaceholder" />
              </a-form-item>
              <a-form-item label="Notification Webhook">
                <a-input v-model:value="ciConfigForm.notificationWebhookUrl" placeholder="https://hooks.example.com/review-agent-ci-health" />
              </a-form-item>
              <a-form-item v-if="ciConfigForm.provider === 'JENKINS'" label="Jenkins Parameters">
                <a-textarea
                  v-model:value="ciConfigForm.jenkinsParameterTemplate"
                  :auto-size="{ minRows: 4, maxRows: 8 }"
                  placeholder="REVIEW_AGENT_REVIEW_ID=${reviewId}
REVIEW_AGENT_STATE=${jenkinsState}
REVIEW_AGENT_COMMIT_SHA=${commitSha}
REVIEW_AGENT_CONTEXT=${context}"
                />
              </a-form-item>
              <div v-if="ciConfigForm.provider === 'JENKINS'" class="jenkins-inbound-guide">
                <a-space :size="6" wrap style="margin-bottom:6px">
                  <a-tag color="processing">Pipeline 入站审查</a-tag>
                  <code>POST /api/integration/webhooks/jenkins/reviews</code>
                  <a-tag :color="ciConfigForm.webhookSecretConfigured ? 'green' : 'orange'">
                    {{ ciConfigForm.webhookSecretConfigured ? 'Webhook Secret 已配置' : '需先配置 Webhook Secret' }}
                  </a-tag>
                </a-space>
                <div style="font-size:12px;color:#64748b;line-height:1.5;margin-bottom:6px">
                  Jenkins 使用 <code>X-Review-Agent-Token</code> 触发 Pre-PR Review，再轮询专用 Gate 接口决定流水线是否放行。
                </div>
                <div style="font-size:12px;font-weight:600;color:#475569;margin-bottom:6px">推荐：Shared Library</div>
                <pre class="jenkins-pipeline-snippet" style="margin-bottom:8px">{{ jenkinsSharedLibrarySnippet }}</pre>
                <div style="font-size:12px;font-weight:600;color:#475569;margin-bottom:6px">低层 HTTP 调用</div>
                <pre class="jenkins-pipeline-snippet">{{ jenkinsPipelineSnippet }}</pre>
              </div>
              <a-row :gutter="8">
                <a-col :xs="24" :md="12">
                  <a-form-item label="API Token">
                    <a-input-password v-model:value="ciConfigForm.apiToken" :placeholder="ciConfigForm.tokenConfigured ? '已配置，留空则不更新' : selectedCiProviderOption.tokenPlaceholder" />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="12">
                  <a-form-item label="Webhook Secret">
                    <a-input-password v-model:value="ciConfigForm.webhookSecret" :placeholder="ciConfigForm.webhookSecretConfigured ? '已配置，留空则不更新' : 'Webhook secret'" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-space :size="12" wrap>
                <a-checkbox v-model:checked="ciConfigForm.checksEnabled">{{ selectedCiProviderOption.writebackLabel }}</a-checkbox>
                <a-checkbox v-model:checked="ciConfigForm.sarifUploadEnabled">启用 SARIF 上传</a-checkbox>
                <a-button v-if="can('INTEGRATION_MANAGE')" type="primary" size="small" :loading="ciConfigSaving" :disabled="!jenkinsInstanceKeyValid" @click="saveCiStatusConfig">保存配置</a-button>
                <a-button v-if="can('INTEGRATION_MANAGE')" size="small" :loading="ciConnectionTesting" :disabled="!jenkinsInstanceKeyValid" @click="testCiConnection">
                  <template #icon><ApiOutlined /></template>
                  保存并测试
                </a-button>
              </a-space>
              <a-alert
                v-if="ciConnectionTestResult"
                show-icon
                :type="ciConnectionTestResult.status === 'SUCCESS' ? 'success' : ciConnectionTestResult.status === 'FAILED' ? 'error' : 'warning'"
                :message="`${ciConnectionTestResult.provider || ciConnectionTestResult.connectorKey}: ${ciConnectionTestResult.status}`"
                style="margin-top:10px"
              >
                <template #description>
                  <div>{{ ciConnectionTestResult.message }} · {{ ciConnectionTestResult.latencyMs }}ms</div>
                  <div v-if="ciConnectionTestResult.requestUrl" style="margin-top:2px;word-break:break-all">{{ ciConnectionTestResult.requestUrl }}</div>
                </template>
              </a-alert>
            </a-form>
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">最近回写</div>
              <a-space v-if="can('INTEGRATION_MANAGE')" style="width:100%;justify-content:flex-end;margin-bottom:6px">
                <a-button size="small" :loading="jenkinsResultRefreshing" @click="refreshJenkinsResults">
                  刷新 Jenkins 结果
                </a-button>
              </a-space>
              <a-space v-if="ciWritebacks.length" direction="vertical" :size="6" style="width:100%">
                <a-card
                  v-for="item in ciWritebacks"
                  :key="item.id"
                  size="small"
                  class="governance-sub-card"
                >
                  <a-space :size="8" style="width:100%;justify-content:space-between;align-items:flex-start">
                    <div style="min-width:0">
                      <div style="font-size:12px;font-weight:600">#{{ item.reviewId || '-' }} · {{ item.state || '-' }}</div>
                      <div style="font-size:12px;color:#94a3b8;margin-top:2px;word-break:break-all">{{ item.commitSha || 'no commit' }}</div>
                      <div v-if="item.externalQueueUrl || item.externalBuildUrl" style="font-size:12px;color:#64748b;margin-top:2px;word-break:break-all">
                        Jenkins: {{ item.externalBuildUrl || item.externalQueueUrl }}
                      </div>
                      <div v-if="item.errorMessage" style="font-size:12px;color:#dc2626;margin-top:2px;line-height:1.4">{{ item.errorMessage }}</div>
                    </div>
                    <a-space :size="4" wrap style="justify-content:flex-end">
                      <a-tag :color="writebackStatusColor(item.writebackStatus)">{{ item.writebackStatus }}</a-tag>
                      <a-tag v-if="item.externalBuildResult" :color="jenkinsBuildResultColor(item.externalBuildResult)">
                        Jenkins {{ item.externalBuildNumber ? `#${item.externalBuildNumber}` : '' }} {{ item.externalBuildResult }}
                      </a-tag>
                      <a-tag>retry {{ item.retryCount }}</a-tag>
                      <a-button
                        v-if="can('INTEGRATION_MANAGE') && item.writebackStatus === 'FAILED'"
                        size="small"
                        :loading="ciWritebackRetryingIds.has(item.id)"
                        @click="retryCiWriteback(item)"
                      >
                        重试
                      </a-button>
                    </a-space>
                  </a-space>
                </a-card>
              </a-space>
              <div v-else style="font-size:12px;color:#94a3b8">暂无回写记录</div>
            </div>
            <div>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">最近集成动作</div>
              <a-space v-if="integrationActions.length" direction="vertical" :size="6" style="width:100%">
                <a-card
                  v-for="item in integrationActions"
                  :key="item.id"
                  size="small"
                  class="governance-sub-card"
                >
                  <a-space :size="8" style="width:100%;justify-content:space-between;align-items:flex-start">
                    <div style="min-width:0">
                      <div style="font-size:12px;font-weight:600">{{ item.actionType }}</div>
                      <div style="font-size:12px;color:#94a3b8;margin-top:2px;word-break:break-all">
                        {{ item.targetKey || item.commitSha || 'no target' }}
                      </div>
                      <div v-if="item.errorMessage" style="font-size:12px;color:#dc2626;margin-top:2px;line-height:1.4">{{ item.errorMessage }}</div>
                    </div>
                    <a-tag :color="actionStatusColor(item.actionStatus)">{{ item.actionStatus }}</a-tag>
                  </a-space>
                </a-card>
              </a-space>
              <div v-else style="font-size:12px;color:#94a3b8">暂无集成动作</div>
            </div>
            <div>
              <div v-if="webhookTriggerHealth" style="padding:8px 10px;margin-bottom:10px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:6px">
                <a-space :size="8" wrap>
                  <span style="font-size:12px;font-weight:600;color:#475569">GitLab review queue</span>
                  <a-tag :color="ciHealthColor(webhookTriggerHealth.healthStatus)">{{ webhookTriggerHealth.healthStatus }}</a-tag>
                  <span style="font-size:12px;color:#64748b">{{ webhookTriggerHealth.summary }}</span>
                  <span v-if="webhookTriggerHealth.oldestPendingAt" style="font-size:11px;color:#94a3b8">
                    Oldest {{ formatDateTime(webhookTriggerHealth.oldestPendingAt) }}
                  </span>
                </a-space>
              </div>
              <template v-for="instance in jenkinsInstances" :key="`health-${instance.connectorKey}`">
                <div
                  v-if="jenkinsReviewTriggerHealth[instance.connectorKey]"
                  style="padding:8px 10px;margin-bottom:10px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:6px"
                >
                  <a-space :size="8" wrap>
                    <span style="font-size:12px;font-weight:600;color:#475569">
                      {{ instance.displayName || instance.connectorKey }} review queue
                    </span>
                    <a-tag :color="ciHealthColor(jenkinsReviewTriggerHealth[instance.connectorKey].healthStatus)">
                      {{ jenkinsReviewTriggerHealth[instance.connectorKey].healthStatus }}
                    </a-tag>
                    <span style="font-size:12px;color:#64748b">
                      {{ jenkinsReviewTriggerHealth[instance.connectorKey].summary }}
                    </span>
                    <span
                      v-if="jenkinsReviewTriggerHealth[instance.connectorKey].oldestPendingAt"
                      style="font-size:11px;color:#94a3b8"
                    >
                      Oldest {{ formatDateTime(jenkinsReviewTriggerHealth[instance.connectorKey].oldestPendingAt || '') }}
                    </span>
                  </a-space>
                </div>
              </template>
              <div style="font-size:12px;font-weight:600;color:#94a3b8;margin-bottom:6px">Recent Webhook Deliveries</div>
              <a-space v-if="webhookDeliveries.length" direction="vertical" :size="6" style="width:100%">
                <a-card
                  v-for="item in webhookDeliveries"
                  :key="item.id"
                  size="small"
                  class="governance-sub-card"
                >
                  <a-space :size="8" style="width:100%;justify-content:space-between;align-items:flex-start">
                    <div style="min-width:0">
                      <a-space :size="4" wrap>
                        <a-tag color="processing">{{ item.provider }}</a-tag>
                        <a-tag>{{ item.eventType }}</a-tag>
                      </a-space>
                      <div style="font-size:12px;color:#94a3b8;margin-top:4px;word-break:break-all">
                        {{ item.deliveryId }}
                      </div>
                      <div v-if="item.triggerStatus" style="font-size:12px;color:#64748b;margin-top:2px">
                        Review trigger:
                        <a-tag :color="webhookTriggerStatusColor(item.triggerStatus)" style="margin-left:4px">
                          {{ item.triggerStatus }}
                        </a-tag>
                        <router-link v-if="item.triggerReviewId" :to="`/reviews/${item.triggerReviewId}`" style="margin-left:4px">
                          #{{ item.triggerReviewId }}
                        </router-link>
                      </div>
                      <div v-if="item.triggerKey" style="font-size:11px;color:#94a3b8;margin-top:2px;word-break:break-all">
                        {{ item.triggerKey }}
                      </div>
                      <div v-if="item.triggerMessage" style="font-size:12px;color:#d97706;margin-top:2px;line-height:1.4">{{ item.triggerMessage }}</div>
                      <div v-if="item.triggerNextRetryAt" style="font-size:11px;color:#64748b;margin-top:2px">
                        Retry {{ item.triggerRetryCount ?? 0 }} · next {{ formatDateTime(item.triggerNextRetryAt) }}
                      </div>
                      <a-button
                        v-if="can('INTEGRATION_MANAGE') && ['FAILED', 'EXHAUSTED'].includes(item.triggerStatus || '') && item.triggerKey"
                        type="link"
                        size="small"
                        :loading="retryingWebhookTriggerKey === item.triggerKey"
                        style="height:24px;padding:0;margin-top:2px"
                        @click="retryWebhookReviewTrigger(item)"
                      >
                        <template #icon><SyncOutlined /></template>
                        Retry review trigger
                      </a-button>
                      <div v-if="item.errorMessage" style="font-size:12px;color:#dc2626;margin-top:2px;line-height:1.4">{{ item.errorMessage }}</div>
                    </div>
                    <a-tag :color="webhookDeliveryStatusColor(item.deliveryStatus)">{{ item.deliveryStatus }}</a-tag>
                  </a-space>
                </a-card>
              </a-space>
              <div v-else style="font-size:12px;color:#94a3b8">No webhook deliveries</div>
            </div>
          </a-space>
        </a-card>
        </div>
      </a-col>
    </a-row>

    <!-- 底部区域：集成路线图 + 业务工作流模板 -->
    <a-row :gutter="16" class="governance-bottom-row">
      <!-- 集成路线图 -->
      <a-col :xl="12" :span="24">
        <a-card size="small">
          <template #title>
            <a-space :size="8">
              <EnvironmentOutlined style="font-size:20px;color:#6366f1" />
              <span style="font-weight:600">集成路线图</span>
            </a-space>
          </template>
          <a-row :gutter="16">
            <a-col v-for="stage in rolloutStages" :key="stage.key" :md="8" :span="24">
              <a-space :size="4" style="margin-bottom:8px">
                <component :is="stage.iconComp" :style="{fontSize:'20px',color:stage.iconColor}" />
                <span style="font-weight:600;font-size:13px">{{ stage.label }}</span>
              </a-space>
              <div v-for="connector in connectorsByStage[stage.key]" :key="connector.id">
                <a-card size="small" class="governance-nested-card" style="margin-bottom:8px">
                  <div style="font-weight:600;font-size:13px">{{ connector.name }}</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:4px;line-height:1.4">{{ connector.businessValue }}</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:4px">{{ connector.implementationHint }}</div>
                </a-card>
              </div>
            </a-col>
          </a-row>
        </a-card>
      </a-col>

      <!-- 业务工作流模板 -->
      <a-col :xl="12" :span="24">
        <a-card size="small">
          <template #title>
            <a-space :size="8">
              <SyncOutlined style="font-size:20px;color:#059669" />
              <span style="font-weight:600">业务工作流模板</span>
            </a-space>
          </template>
          <a-space direction="vertical" :size="8" style="width:100%">
            <a-card
              v-for="workflow in workflowTemplates"
              :key="workflow.id"
              size="small"
              class="governance-nested-card"
            >
              <a-space :size="8" style="width:100%;justify-content:space-between;flex-wrap:wrap">
                <div>
                  <div style="font-weight:600;font-size:13px">{{ workflow.name }}</div>
                  <div style="font-size:12px;color:#94a3b8;margin-top:4px;line-height:1.4">{{ workflow.scenario }}</div>
                </div>
                <a-tag color="processing">{{ workflow.strategyId }}</a-tag>
              </a-space>
              <a-space :size="4" wrap style="margin-top:8px">
                <a-tag v-for="packId in workflow.rulePackIds" :key="packId">{{ packId }}</a-tag>
                <a-tag v-for="integrationId in workflow.integrationIds" :key="integrationId" color="blue">{{ integrationId }}</a-tag>
              </a-space>
              <div style="font-size:12px;color:#94a3b8;margin-top:8px">{{ workflow.successMetric }}</div>
            </a-card>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <a-card v-if="can('ACCESS_MANAGE')" size="small" class="governance-card-scroll">
      <template #title>
        <a-space :size="8">
          <UserOutlined style="font-size:16px;color:#4f46e5" />
          <span style="font-weight:600;font-size:14px">平台访问控制</span>
        </a-space>
      </template>
      <a-list :data-source="userAccessUsers" :loading="userAccessLoading" size="small">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta :description="`${item.email || 'No email'} · ${item.status}`">
              <template #title>
                <span>{{ item.displayName || item.username }}</span>
                <span style="margin-left:6px;color:#94a3b8;font-size:12px">@{{ item.username }}</span>
              </template>
            </a-list-item-meta>
            <a-select
              :value="item.normalizedRole"
              :loading="updatingUserRoleId === item.id"
              style="width:190px"
              @change="updateUserRole(item, $event)"
            >
              <a-select-option value="ADMIN">平台管理员</a-select-option>
              <a-select-option value="GOVERNANCE_MANAGER">治理管理员</a-select-option>
              <a-select-option value="OPERATOR">运营人员</a-select-option>
              <a-select-option value="REVIEWER">审查员</a-select-option>
            </a-select>
          </a-list-item>
        </template>
      </a-list>
    </a-card>

    <a-card v-if="can('ACCESS_MANAGE')" size="small" class="governance-card-scroll">
      <template #title>
        <a-space :size="8">
          <SafetyOutlined style="font-size:16px;color:#4f46e5" />
          <span style="font-weight:600;font-size:14px">访问权限审计</span>
        </a-space>
      </template>
      <template #extra>
        <a-space :size="8" wrap>
          <a-select
            v-model:value="accessAuditActionFilter"
            allow-clear
            placeholder="全部变更类型"
            style="width:210px"
            @change="loadAccessAuditLogs"
          >
            <a-select-option value="PLATFORM_ROLE_CHANGED">平台角色变更</a-select-option>
            <a-select-option value="PROJECT_OWNER_ASSIGNED">项目 Owner 分配</a-select-option>
            <a-select-option value="PROJECT_MEMBER_ADDED">项目成员添加</a-select-option>
            <a-select-option value="PROJECT_MEMBER_ROLE_CHANGED">项目角色变更</a-select-option>
            <a-select-option value="PROJECT_MEMBER_REMOVED">项目成员移除</a-select-option>
          </a-select>
          <a-input-number
            v-model:value="accessAuditProjectFilter"
            :min="1"
            :precision="0"
            placeholder="项目 ID"
            style="width:120px"
            @press-enter="loadAccessAuditLogs"
          />
          <a-button :loading="accessAuditLoading" @click="loadAccessAuditLogs">
            <template #icon><SyncOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </template>
      <a-list :data-source="accessAuditLogs" :loading="accessAuditLoading" size="small">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta :description="item.detail || '权限配置已更新'">
              <template #title>
                <a-space :size="6" wrap>
                  <a-tag :color="accessAuditActionColor(item.actionType)">{{ accessAuditActionLabel(item.actionType) }}</a-tag>
                  <span>{{ item.actorUsername || 'SYSTEM' }}</span>
                  <span style="color:#94a3b8">→</span>
                  <span>{{ item.targetUsername || `用户 #${item.targetUserId}` }}</span>
                  <a-tag v-if="item.projectId">项目 #{{ item.projectId }}</a-tag>
                  <span v-if="item.previousRole || item.newRole" style="color:#64748b;font-size:12px">
                    {{ item.previousRole || '未授权' }} → {{ item.newRole || '已移除' }}
                  </span>
                </a-space>
              </template>
            </a-list-item-meta>
            <span style="color:#94a3b8;font-size:12px;white-space:nowrap">{{ formatDateTime(item.createdAt) }}</span>
          </a-list-item>
        </template>
        <template #locale><a-empty description="暂无权限变更记录" :image="undefined" /></template>
      </a-list>
    </a-card>

    <a-modal
      v-model:open="rulePackDryRunModalOpen"
      title="规则包变更预览"
      :footer="null"
      width="720px"
    >
      <a-space v-if="rulePackDryRun" direction="vertical" :size="12" style="width:100%">
        <a-space :size="6" wrap>
          <a-tag color="processing">{{ rulePackDryRun.rulePackKey }}</a-tag>
          <a-tag>{{ rulePackDryRun.changeType }}</a-tag>
          <a-tag color="blue">已有 {{ rulePackDryRun.existingControlCount }} 条</a-tag>
          <a-tag color="green">拟新增 {{ rulePackDryRun.proposedControlCount }} 条</a-tag>
        </a-space>
        <div style="font-weight:600">{{ rulePackDryRun.title }}</div>
        <div>
          <div style="font-size:12px;font-weight:600;color:#64748b;margin-bottom:6px">影响摘要</div>
          <a-space direction="vertical" :size="4" style="width:100%">
            <div v-for="item in rulePackDryRun.impactSummary" :key="item" style="font-size:12px;color:#475569;line-height:1.5">{{ item }}</div>
          </a-space>
        </div>
        <div>
          <div style="font-size:12px;font-weight:600;color:#64748b;margin-bottom:6px">拟新增控制项</div>
          <a-space direction="vertical" :size="6" style="width:100%">
            <a-card
              v-for="control in rulePackDryRun.proposedControls"
              :key="control"
              size="small"
              :body-style="{ padding: '10px 12px' }"
              style="background:#f8fafc"
            >
              <span style="font-size:12px;color:#334155;line-height:1.5">{{ control }}</span>
            </a-card>
          </a-space>
        </div>
        <div v-if="rulePackDryRun.controlsSnapshot">
          <div style="font-size:12px;font-weight:600;color:#64748b;margin-bottom:6px">快照 JSON</div>
          <pre style="margin:0;padding:12px;background:#0f172a;color:#e2e8f0;border-radius:8px;font-size:12px;line-height:1.5;white-space:pre-wrap;word-break:break-word;max-height:240px;overflow:auto">{{ formatJson(rulePackDryRun.controlsSnapshot) }}</pre>
        </div>
      </a-space>
      <a-empty v-else description="暂无预览数据" :image="undefined" />
    </a-modal>

    <a-modal
      v-model:open="rulePackVersionSnapshotModalOpen"
      title="规则包版本快照"
      :footer="null"
      width="760px"
    >
      <a-space v-if="selectedRulePackVersion" direction="vertical" :size="12" style="width:100%">
        <a-space :size="6" wrap>
          <a-tag color="processing">{{ selectedRulePackVersion.rulePackKey }}</a-tag>
          <a-tag color="green">v{{ selectedRulePackVersion.versionNo }}</a-tag>
          <a-tag :color="rulePackVersionStatusColor(selectedRulePackVersion.versionStatus)">{{ selectedRulePackVersion.versionStatus }}</a-tag>
          <a-tag>Change #{{ selectedRulePackVersion.sourceChangeId }}</a-tag>
        </a-space>
        <div style="font-weight:600">{{ selectedRulePackVersion.title }}</div>
        <div style="font-size:12px;color:#64748b;line-height:1.5">{{ selectedRulePackVersion.rationale || '暂无版本说明' }}</div>
        <div v-if="selectedRulePackVersion.controlsSnapshot">
          <div style="font-size:12px;font-weight:600;color:#64748b;margin-bottom:6px">控制项快照 JSON</div>
          <pre style="margin:0;padding:12px;background:#0f172a;color:#e2e8f0;border-radius:8px;font-size:12px;line-height:1.5;white-space:pre-wrap;word-break:break-word;max-height:360px;overflow:auto">{{ formatJson(selectedRulePackVersion.controlsSnapshot) }}</pre>
        </div>
        <a-empty v-else description="暂无快照内容" :image="undefined" />
      </a-space>
      <a-empty v-else description="暂无版本数据" :image="undefined" />
    </a-modal>
  </a-space>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { BarChartOutlined, CheckCircleOutlined, ExclamationCircleOutlined, AppstoreOutlined, ThunderboltOutlined, SafetyOutlined, UserOutlined, EnvironmentOutlined, SyncOutlined, PlayCircleOutlined, SettingOutlined, ClockCircleOutlined } from '@ant-design/icons-vue'
import type { BusinessImpact, CapabilityStatus, CiConnectionTestResultVO, CiIntegrationHealthVO, CiStatusConfigVO, CiStatusWritebackLogVO, CredentialRotationResultVO, CredentialSecurityHealthVO, GitLabReviewTriggerHealthVO, GovernanceRulePack, GovernanceRulePackChange, GovernanceRulePackDryRun, GovernanceRulePackVersion, IntegrationActionLogVO, IntegrationConnector, IntegrationWebhookDeliveryLogVO, JenkinsReviewTriggerHealthVO, MarketCapability, RolloutStage, WorkflowTemplate } from '@/types/governance'
import type { TelemetryReadinessItem } from '@/types/operations'
import type { AccessAuditLog, UserAccess } from '@/types/auth'
import { useApi } from '@/composables/useApi'
import { useAccess } from '@/composables/useAccess'
import { compileGovernancePolicyPack, getCapabilityCoverageSummary, getCiStatusIntegrationReadiness, getConnectorsByStage, getRecommendedNextActions, governanceRulePacks as fallbackRulePacks, integrationConnectors as fallbackConnectors, marketCapabilities as fallbackCapabilities, workflowTemplates as fallbackWorkflowTemplates } from '@/utils/governanceCatalog'
import { buildCiHealthActions, ciHealthActionColor, buildTelemetryGapActions, telemetryReadinessColor } from '@/utils/governanceTelemetry'

const { get, post, put, patch, del: remove } = useApi()
const { can, loadAccessProfile } = useAccess()
const marketCapabilities = ref<MarketCapability[]>(fallbackCapabilities)
const integrationConnectors = ref<IntegrationConnector[]>(fallbackConnectors)
const governanceRulePacks = ref<GovernanceRulePack[]>(fallbackRulePacks)
const workflowTemplates = ref<WorkflowTemplate[]>(fallbackWorkflowTemplates)
const rulePackChanges = ref<GovernanceRulePackChange[]>([])
const rulePackVersions = ref<GovernanceRulePackVersion[]>([])
const actingRulePackChangeId = ref<number | null>(null)
const actingRulePackChangeAction = ref<'dry-run' | 'approve' | 'apply' | 'reject' | 'rollback' | null>(null)
const rulePackDryRun = ref<GovernanceRulePackDryRun | null>(null)
const rulePackDryRunModalOpen = ref(false)
const selectedRulePackVersion = ref<GovernanceRulePackVersion | null>(null)
const rulePackVersionSnapshotModalOpen = ref(false)
const coverage = computed(() => getCapabilityCoverageSummary(marketCapabilities.value))
const recommendedActions = computed(() => getRecommendedNextActions(5, marketCapabilities.value))
const connectorsByStage = computed(() => getConnectorsByStage(integrationConnectors.value))
const releasePolicy = computed(() => compileGovernancePolicyPack(
  ['security-release', 'ai-generated-code', 'compliance-evidence'],
  governanceRulePacks.value,
))
const ciStatusReadiness = getCiStatusIntegrationReadiness()
const ciConfigSaving = ref(false)
const ciConnectionTesting = ref(false)
const ciConnectionTestResult = ref<CiConnectionTestResultVO | null>(null)
const credentialSecurityHealth = ref<CredentialSecurityHealthVO | null>(null)
const credentialRotationRunning = ref(false)
const credentialRotationResult = ref<CredentialRotationResultVO | null>(null)
const userAccessUsers = ref<UserAccess[]>([])
const userAccessLoading = ref(false)
const updatingUserRoleId = ref<number | null>(null)
const accessAuditLogs = ref<AccessAuditLog[]>([])
const accessAuditLoading = ref(false)
const accessAuditActionFilter = ref<string | undefined>()
const accessAuditProjectFilter = ref<number | null>(null)
const ciWritebacks = ref<CiStatusWritebackLogVO[]>([])
const ciIntegrationHealth = ref<CiIntegrationHealthVO[]>([])
const integrationActions = ref<IntegrationActionLogVO[]>([])
const webhookDeliveries = ref<IntegrationWebhookDeliveryLogVO[]>([])
const webhookTriggerHealth = ref<GitLabReviewTriggerHealthVO | null>(null)
const jenkinsReviewTriggerHealth = ref<Record<string, JenkinsReviewTriggerHealthVO>>({})
const jenkinsInstances = ref<CiStatusConfigVO[]>([])
const creatingJenkinsInstance = ref(false)
const ciProviderSelection = ref('github-checks')
const retryingWebhookTriggerKey = ref<string | null>(null)
const telemetryReadinessItems = ref<TelemetryReadinessItem[]>([])
const ciWritebackRetryingIds = ref<Set<number>>(new Set())
const jenkinsResultRefreshing = ref(false)
const ciProviderOptions = [
  {
    connectorKey: 'github-checks',
    provider: 'GITHUB',
    label: 'GitHub Checks / Status',
    description: '将 Gate 结果写回 GitHub commit status，并复用 SARIF / PR Summary 集成。',
    ownerLabel: 'Repo Owner',
    ownerPlaceholder: 'zhu930824',
    nameLabel: 'Repo Name',
    namePlaceholder: 'review-agent',
    urlLabel: 'Repository URL',
    urlPlaceholder: 'https://github.com/owner/repo',
    statusPlaceholder: 'Review Agent',
    tokenPlaceholder: 'GitHub token',
    writebackLabel: '启用 Checks 回写',
  },
  {
    connectorKey: 'gitlab-merge-request',
    provider: 'GITLAB',
    label: 'GitLab Merge Request / Pipeline',
    description: '面向企业 GitLab 私有化流程，保存 MR / Pipeline 状态回写所需的仓库绑定。',
    ownerLabel: 'Group / Namespace',
    ownerPlaceholder: 'platform/team',
    nameLabel: 'Project Path',
    namePlaceholder: 'review-agent',
    urlLabel: 'GitLab Project URL',
    urlPlaceholder: 'https://gitlab.example.com/platform/team/review-agent',
    statusPlaceholder: 'review-agent/gate',
    tokenPlaceholder: 'GitLab access token',
    writebackLabel: '启用 Pipeline 状态回写',
  },
  {
    connectorKey: 'jenkins-pipeline',
    provider: 'JENKINS',
    label: 'Jenkins Pipeline Gate',
    description: '面向企业 Jenkins 流水线，保存 Job、实例地址和凭证，作为后续 Gate 回写与构建阻断入口。',
    ownerLabel: 'Folder / Team',
    ownerPlaceholder: 'platform',
    nameLabel: 'Job Name',
    namePlaceholder: 'review-agent-ci',
    urlLabel: 'Jenkins Base URL',
    urlPlaceholder: 'https://jenkins.example.com',
    statusPlaceholder: 'Review Agent Gate',
    tokenPlaceholder: 'Jenkins API token',
    writebackLabel: '启用 Jenkins Gate 回写',
  },
]
const ciConfigForm = reactive({
  connectorKey: 'github-checks',
  instanceKey: '',
  displayName: '',
  projectId: null as number | null,
  provider: 'GITHUB',
  repoOwner: '',
  repoName: '',
  repoUrl: '',
  defaultBranch: 'main',
  statusContext: 'Review Agent',
  jenkinsParameterTemplate: '',
  notificationWebhookUrl: '',
  checksEnabled: true,
  sarifUploadEnabled: false,
  apiToken: '',
  webhookSecret: '',
  tokenConfigured: false,
  webhookSecretConfigured: false,
})

const telemetryGapActions = computed(() => buildTelemetryGapActions(telemetryReadinessItems.value))
const ciHealthActions = computed(() => buildCiHealthActions(ciIntegrationHealth.value))
const selectedCiProviderOption = computed(() =>
  ciProviderOptions.find(option => option.connectorKey === ciProviderSelection.value) || ciProviderOptions[0]
)
const activeJenkinsConnectorKey = computed(() => creatingJenkinsInstance.value
  ? `jenkins-pipeline:${ciConfigForm.instanceKey.trim().toLowerCase()}`
  : (ciConfigForm.connectorKey || 'jenkins-pipeline'))
const jenkinsInstanceKeyValid = computed(() => ciConfigForm.provider !== 'JENKINS'
  || !creatingJenkinsInstance.value
  || /^[a-z0-9][a-z0-9._-]{0,63}$/.test(ciConfigForm.instanceKey.trim().toLowerCase()))
const jenkinsSharedLibrarySnippet = computed(() => `reviewAgentGate(
  reviewAgentUrl: 'https://review-agent.example.com',
  projectId: ${ciConfigForm.projectId || 'env.REVIEW_AGENT_PROJECT_ID'},
  connectorKey: '${activeJenkinsConnectorKey.value}',
  credentialId: 'review-agent-webhook-secret',
  targetBranch: env.CHANGE_TARGET ?: 'main'
)`)
const jenkinsPipelineSnippet = computed(() => `withCredentials([string(credentialsId: 'review-agent-webhook-secret', variable: 'REVIEW_AGENT_TOKEN')]) {
  def trigger = httpRequest(
    httpMode: 'POST',
    url: "\${env.REVIEW_AGENT_URL}/api/integration/webhooks/jenkins/reviews",
    customHeaders: [
      [name: 'X-Review-Agent-Token', value: env.REVIEW_AGENT_TOKEN, maskValue: true],
      [name: 'X-Review-Agent-Connector', value: '${activeJenkinsConnectorKey.value}'],
      [name: 'X-Jenkins-Delivery', value: "\${env.JOB_NAME}:\${env.BUILD_NUMBER}"]
    ],
    contentType: 'APPLICATION_JSON',
    requestBody: groovy.json.JsonOutput.toJson([
      projectId: env.REVIEW_AGENT_PROJECT_ID as Long,
      sourceBranch: env.BRANCH_NAME,
      targetBranch: env.CHANGE_TARGET ?: 'main',
      jobName: env.JOB_NAME,
      buildNumber: env.BUILD_NUMBER,
      buildUrl: env.BUILD_URL,
      commitSha: env.GIT_COMMIT
    ])
  )
  def review = new groovy.json.JsonSlurperClassic().parseText(trigger.content).data
  if (review.triggerStatus != 'PROCESSED') {
    error("Review Agent trigger: \${review.triggerStatus} \${review.triggerMessage ?: ''}")
  }
  timeout(time: 30, unit: 'MINUTES') {
    waitUntil {
      def gateResponse = httpRequest(
        url: "\${env.REVIEW_AGENT_URL}/api/integration/webhooks/jenkins/reviews/\${review.triggerReviewId}/gate",
        customHeaders: [
          [name: 'X-Review-Agent-Token', value: env.REVIEW_AGENT_TOKEN, maskValue: true],
          [name: 'X-Review-Agent-Connector', value: '${activeJenkinsConnectorKey.value}']
        ]
      )
      def gate = new groovy.json.JsonSlurperClassic().parseText(gateResponse.content).data
      if (gate.gateStatus == 'PASSED') return true
      if (gate.gateStatus in ['BLOCKED', 'NEEDS_HUMAN_REVIEW']) error("Review Agent gate: \${gate.gateStatus}")
      sleep 10
      return false
    }
  }
}`)

async function loadGovernanceCatalog() {
  try {
    const [capabilitiesRes, connectorsRes, rulePacksRes, workflowsRes, rulePackChangesRes, rulePackVersionsRes] = await Promise.all([
      get<MarketCapability[]>('/governance/capabilities').catch(() => null),
      get<IntegrationConnector[]>('/governance/connectors').catch(() => null),
      get<GovernanceRulePack[]>('/governance/rule-packs').catch(() => null),
      get<WorkflowTemplate[]>('/governance/workflows').catch(() => null),
      get<GovernanceRulePackChange[]>('/governance/rule-pack-changes?limit=20').catch(() => null),
      get<GovernanceRulePackVersion[]>('/governance/rule-pack-versions?limit=20').catch(() => null),
    ])
    if (capabilitiesRes?.data?.length) marketCapabilities.value = capabilitiesRes.data
    if (connectorsRes?.data?.length) integrationConnectors.value = connectorsRes.data
    if (rulePacksRes?.data?.length) governanceRulePacks.value = rulePacksRes.data
    if (workflowsRes?.data?.length) workflowTemplates.value = workflowsRes.data
    rulePackChanges.value = rulePackChangesRes?.data ?? []
    rulePackVersions.value = rulePackVersionsRes?.data ?? []
  } catch (e) {
    console.error('加载治理目录失败，将使用本地目录兜底', e)
  }
}

async function loadRulePackChanges() {
  try {
    const res = await get<GovernanceRulePackChange[]>('/governance/rule-pack-changes?limit=20')
    rulePackChanges.value = res.data ?? []
  } catch (e) {
    console.error('加载规则包变更失败', e)
  }
}

async function loadRulePackVersions() {
  try {
    const res = await get<GovernanceRulePackVersion[]>('/governance/rule-pack-versions?limit=20')
    rulePackVersions.value = res.data ?? []
  } catch (e) {
    console.error('加载规则包版本失败', e)
  }
}

async function handleRulePackChangeAction(change: GovernanceRulePackChange, action: 'approve' | 'apply' | 'reject' | 'rollback') {
  actingRulePackChangeId.value = change.id
  actingRulePackChangeAction.value = action
  try {
    await post<unknown>(`/governance/rule-pack-changes/${change.id}/${action}`)
    await Promise.all([loadRulePackChanges(), loadRulePackVersions()])
  } catch (e) {
    console.error('更新规则包变更状态失败', e)
  } finally {
    actingRulePackChangeId.value = null
    actingRulePackChangeAction.value = null
  }
}

async function previewRulePackChange(change: GovernanceRulePackChange) {
  actingRulePackChangeId.value = change.id
  actingRulePackChangeAction.value = 'dry-run'
  try {
    const res = await post<GovernanceRulePackDryRun>(`/governance/rule-pack-changes/${change.id}/dry-run`)
    rulePackDryRun.value = res.data ?? null
    rulePackDryRunModalOpen.value = true
  } catch (e) {
    console.error('预览规则包变更失败', e)
  } finally {
    actingRulePackChangeId.value = null
    actingRulePackChangeAction.value = null
  }
}

function formatJson(value: string) {
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

function viewRulePackVersionSnapshot(version: GovernanceRulePackVersion) {
  selectedRulePackVersion.value = version
  rulePackVersionSnapshotModalOpen.value = true
}

function applyCiStatusConfig(config: CiStatusConfigVO) {
  const connectorFamily = config.connectorKey?.startsWith('jenkins-pipeline')
    ? 'jenkins-pipeline'
    : (config.connectorKey || ciProviderSelection.value)
  const option = ciProviderOptions.find(item => item.connectorKey === connectorFamily) || selectedCiProviderOption.value
  ciProviderSelection.value = option.connectorKey
  ciConfigForm.connectorKey = config.connectorKey || option.connectorKey
  ciConfigForm.instanceKey = ciConfigForm.connectorKey.startsWith('jenkins-pipeline:')
    ? ciConfigForm.connectorKey.slice('jenkins-pipeline:'.length)
    : ''
  ciConfigForm.displayName = config.displayName || ''
  ciConfigForm.projectId = config.projectId ?? null
  ciConfigForm.provider = config.provider || option.provider
  ciConfigForm.repoOwner = config.repoOwner || ''
  ciConfigForm.repoName = config.repoName || ''
  ciConfigForm.repoUrl = config.repoUrl || ''
  ciConfigForm.defaultBranch = config.defaultBranch || 'main'
  ciConfigForm.statusContext = config.statusContext || 'Review Agent'
  ciConfigForm.jenkinsParameterTemplate = config.jenkinsParameterTemplate || ''
  ciConfigForm.notificationWebhookUrl = config.notificationWebhookUrl || ''
  ciConfigForm.checksEnabled = config.checksEnabled ?? true
  ciConfigForm.sarifUploadEnabled = config.sarifUploadEnabled ?? false
  ciConfigForm.tokenConfigured = config.tokenConfigured
  ciConfigForm.webhookSecretConfigured = config.webhookSecretConfigured
  ciConfigForm.apiToken = ''
  ciConfigForm.webhookSecret = ''
}

async function loadCiStatusConfig(connectorKey = ciConfigForm.connectorKey) {
  try {
    const res = await get<CiStatusConfigVO>(`/integration/ci-config?connectorKey=${encodeURIComponent(connectorKey)}`)
    if (res.data) applyCiStatusConfig(res.data)
  } catch (e) {
    console.error('加载 CI 回写配置失败', e)
  }
}

async function loadJenkinsInstances() {
  try {
    const res = await get<CiStatusConfigVO[]>('/integration/ci-config/list?provider=JENKINS')
    jenkinsInstances.value = res.data ?? []
  } catch (e) {
    console.error('load Jenkins connector instances failed', e)
    jenkinsInstances.value = []
  }
}

async function selectJenkinsInstance(connectorKey: string) {
  creatingJenkinsInstance.value = false
  await loadCiStatusConfig(connectorKey)
}

function startNewJenkinsInstance() {
  creatingJenkinsInstance.value = true
  ciProviderSelection.value = 'jenkins-pipeline'
  ciConfigForm.connectorKey = ''
  ciConfigForm.instanceKey = ''
  ciConfigForm.displayName = ''
  ciConfigForm.projectId = null
  ciConfigForm.provider = 'JENKINS'
  ciConfigForm.repoOwner = ''
  ciConfigForm.repoName = ''
  ciConfigForm.repoUrl = ''
  ciConfigForm.defaultBranch = 'main'
  ciConfigForm.statusContext = 'Review Agent Gate'
  ciConfigForm.jenkinsParameterTemplate = ''
  ciConfigForm.notificationWebhookUrl = ''
  ciConfigForm.checksEnabled = true
  ciConfigForm.sarifUploadEnabled = false
  ciConfigForm.apiToken = ''
  ciConfigForm.webhookSecret = ''
  ciConfigForm.tokenConfigured = false
  ciConfigForm.webhookSecretConfigured = false
}

async function cancelNewJenkinsInstance() {
  creatingJenkinsInstance.value = false
  const preferred = jenkinsInstances.value.find(item => item.connectorKey === 'jenkins-pipeline')
    || jenkinsInstances.value[0]
  if (preferred) {
    await loadCiStatusConfig(preferred.connectorKey)
  } else {
    await handleCiConnectorChange('github-checks')
  }
}

async function deleteJenkinsInstance() {
  if (!ciConfigForm.connectorKey) return
  try {
    await remove(`/integration/ci-config?connectorKey=${encodeURIComponent(ciConfigForm.connectorKey)}`)
    await loadJenkinsInstances()
    const next = jenkinsInstances.value[0]
    if (next) {
      await loadCiStatusConfig(next.connectorKey)
    } else {
      startNewJenkinsInstance()
    }
    await loadJenkinsReviewTriggerHealth()
  } catch (e) {
    console.error('delete Jenkins connector instance failed', e)
  }
}

async function handleCiConnectorChange(connectorKey: string) {
  ciConnectionTestResult.value = null
  const option = ciProviderOptions.find(item => item.connectorKey === connectorKey) || ciProviderOptions[0]
  ciConfigForm.provider = option.provider
  ciConfigForm.statusContext = option.statusPlaceholder
  ciConfigForm.jenkinsParameterTemplate = ''
  ciConfigForm.notificationWebhookUrl = ''
  ciConfigForm.apiToken = ''
  ciConfigForm.webhookSecret = ''
  creatingJenkinsInstance.value = false
  if (connectorKey === 'jenkins-pipeline') {
    await loadJenkinsInstances()
    const preferred = jenkinsInstances.value.find(item => item.connectorKey === 'jenkins-pipeline')
      || jenkinsInstances.value[0]
    if (preferred) {
      await loadCiStatusConfig(preferred.connectorKey)
    } else {
      startNewJenkinsInstance()
    }
    return
  }
  await loadCiStatusConfig(connectorKey)
}

async function loadCiWritebacks() {
  try {
    const res = await get<CiStatusWritebackLogVO[]>('/integration/ci-config/writebacks')
    ciWritebacks.value = res.data ?? []
  } catch (e) {
    console.error('加载 CI 回写记录失败', e)
  }
}

async function loadCiIntegrationHealth() {
  try {
    const res = await get<CiIntegrationHealthVO[]>('/integration/ci-config/writebacks/health')
    ciIntegrationHealth.value = res.data ?? []
  } catch (e) {
    console.error('load CI integration health failed', e)
  }
}

async function loadCredentialSecurityHealth() {
  try {
    const res = await get<CredentialSecurityHealthVO>('/integration/ci-config/credential-health')
    credentialSecurityHealth.value = res.data ?? null
  } catch (e) {
    console.error('load credential security health failed', e)
  }
}

async function loadUserAccess() {
  if (!can('ACCESS_MANAGE')) return
  userAccessLoading.value = true
  try {
    const res = await get<UserAccess[]>('/access/users')
    userAccessUsers.value = res.data ?? []
  } catch (e) {
    console.error('load platform access users failed', e)
  } finally {
    userAccessLoading.value = false
  }
}

async function loadAccessAuditLogs() {
  if (!can('ACCESS_MANAGE')) return
  accessAuditLoading.value = true
  try {
    const params = new URLSearchParams({ limit: '50' })
    if (accessAuditActionFilter.value) params.set('actionType', accessAuditActionFilter.value)
    if (accessAuditProjectFilter.value) params.set('projectId', String(accessAuditProjectFilter.value))
    const res = await get<AccessAuditLog[]>(`/access/audit-logs?${params.toString()}`)
    accessAuditLogs.value = res.data ?? []
  } catch (e) {
    console.error('load access audit logs failed', e)
  } finally {
    accessAuditLoading.value = false
  }
}

async function updateUserRole(user: UserAccess, role: string) {
  updatingUserRoleId.value = user.id
  try {
    await patch<UserAccess>(`/access/users/${user.id}/role`, { role })
    await Promise.all([loadUserAccess(), loadAccessAuditLogs(), loadAccessProfile(true)])
  } catch (e) {
    console.error('update platform user role failed', e)
  } finally {
    updatingUserRoleId.value = null
  }
}

async function rotateCredentials() {
  credentialRotationRunning.value = true
  credentialRotationResult.value = null
  try {
    const res = await post<CredentialRotationResultVO>('/integration/ci-config/credential-rotation', {
      confirmation: 'ROTATE CREDENTIALS',
    })
    credentialRotationResult.value = res.data ?? null
    await Promise.all([loadCredentialSecurityHealth(), loadIntegrationActions()])
  } catch (e) {
    console.error('rotate integration credentials failed', e)
  } finally {
    credentialRotationRunning.value = false
  }
}

async function loadIntegrationActions() {
  try {
    const res = await get<IntegrationActionLogVO[]>('/integration/actions')
    integrationActions.value = res.data ?? []
  } catch (e) {
    console.error('加载集成动作记录失败', e)
  }
}

async function loadWebhookDeliveries() {
  try {
    const res = await get<IntegrationWebhookDeliveryLogVO[]>('/integration/webhooks/deliveries?limit=10')
    webhookDeliveries.value = res.data ?? []
  } catch (e) {
    console.error('load webhook deliveries failed', e)
  }
}

async function loadWebhookTriggerHealth() {
  try {
    const res = await get<GitLabReviewTriggerHealthVO>('/integration/webhooks/gitlab/review-triggers/health')
    webhookTriggerHealth.value = res.data ?? null
  } catch (e) {
    console.error('load GitLab review trigger health failed', e)
  }
}

async function loadJenkinsReviewTriggerHealth() {
  const entries = await Promise.all(jenkinsInstances.value.map(async instance => {
    try {
      const res = await get<JenkinsReviewTriggerHealthVO>(
        `/integration/webhooks/jenkins/review-triggers/health?connectorKey=${encodeURIComponent(instance.connectorKey)}`,
      )
      return [instance.connectorKey, res.data] as const
    } catch (e) {
      console.error(`load Jenkins review trigger health failed: ${instance.connectorKey}`, e)
      return [instance.connectorKey, undefined] as const
    }
  }))
  jenkinsReviewTriggerHealth.value = Object.fromEntries(
    entries.filter((entry): entry is readonly [string, JenkinsReviewTriggerHealthVO] => Boolean(entry[1])),
  )
}

async function retryWebhookReviewTrigger(item: IntegrationWebhookDeliveryLogVO) {
  if (!item.triggerKey) return
  retryingWebhookTriggerKey.value = item.triggerKey
  try {
    const providerPath = item.provider === 'JENKINS' ? 'jenkins' : 'gitlab'
    await post(`/integration/webhooks/${providerPath}/review-triggers/retry`, {
      connectorKey: item.connectorKey,
      triggerKey: item.triggerKey,
    })
    await loadWebhookDeliveries()
    await loadCredentialSecurityHealth()
    await Promise.all([loadWebhookTriggerHealth(), loadJenkinsReviewTriggerHealth()])
  } catch (e) {
    console.error('retry review trigger failed', e)
  } finally {
    retryingWebhookTriggerKey.value = null
  }
}

async function loadTelemetryReadiness() {
  try {
    const res = await get<TelemetryReadinessItem[]>('/operations/telemetry-readiness')
    telemetryReadinessItems.value = res.data ?? []
  } catch (e) {
    console.error('加载遥测就绪度失败', e)
  }
}

async function saveCiStatusConfig(): Promise<boolean> {
  ciConfigSaving.value = true
  try {
    const connectorKey = ciConfigForm.provider === 'JENKINS' && creatingJenkinsInstance.value
      ? `jenkins-pipeline:${ciConfigForm.instanceKey.trim().toLowerCase()}`
      : ciConfigForm.connectorKey
    const payload = {
      connectorKey,
      displayName: ciConfigForm.displayName,
      projectId: ciConfigForm.projectId,
      provider: ciConfigForm.provider,
      repoOwner: ciConfigForm.repoOwner,
      repoName: ciConfigForm.repoName,
      repoUrl: ciConfigForm.repoUrl,
      defaultBranch: ciConfigForm.defaultBranch,
      statusContext: ciConfigForm.statusContext,
      jenkinsParameterTemplate: ciConfigForm.provider === 'JENKINS' ? ciConfigForm.jenkinsParameterTemplate : '',
      notificationWebhookUrl: ciConfigForm.notificationWebhookUrl,
      checksEnabled: ciConfigForm.checksEnabled,
      sarifUploadEnabled: ciConfigForm.sarifUploadEnabled,
      apiToken: ciConfigForm.apiToken,
      webhookSecret: ciConfigForm.webhookSecret,
    }
    const res = await put<CiStatusConfigVO>('/integration/ci-config', payload)
    if (res.data) {
      applyCiStatusConfig(res.data)
      creatingJenkinsInstance.value = false
    }
    if (ciConfigForm.provider === 'JENKINS') await loadJenkinsInstances()
    await loadCiWritebacks()
    await loadCiIntegrationHealth()
    await loadIntegrationActions()
    await loadWebhookDeliveries()
    await loadJenkinsReviewTriggerHealth()
    return true
  } catch (e) {
    console.error('保存 CI 回写配置失败', e)
    return false
  } finally {
    ciConfigSaving.value = false
  }
}

async function testCiConnection() {
  ciConnectionTesting.value = true
  ciConnectionTestResult.value = null
  try {
    const saved = await saveCiStatusConfig()
    if (!saved) return
    const res = await post<CiConnectionTestResultVO>(
      `/integration/ci-config/test?connectorKey=${encodeURIComponent(ciConfigForm.connectorKey)}`,
    )
    ciConnectionTestResult.value = res.data ?? null
    await loadIntegrationActions()
  } catch (e) {
    console.error('测试 CI 连接失败', e)
  } finally {
    ciConnectionTesting.value = false
  }
}

async function retryCiWriteback(item: CiStatusWritebackLogVO) {
  ciWritebackRetryingIds.value = new Set(ciWritebackRetryingIds.value).add(item.id)
  try {
    await post<unknown>(`/integration/ci-config/writebacks/${item.id}/retry`)
    await loadCiWritebacks()
    await loadCiIntegrationHealth()
  } catch (e) {
    console.error('重试 CI 回写失败', e)
  } finally {
    const next = new Set(ciWritebackRetryingIds.value)
    next.delete(item.id)
    ciWritebackRetryingIds.value = next
  }
}

async function refreshJenkinsResults() {
  jenkinsResultRefreshing.value = true
  try {
    await post<number>('/integration/ci-config/writebacks/jenkins/refresh?limit=20')
    await loadCiWritebacks()
    await loadCiIntegrationHealth()
  } catch (e) {
    console.error('鍒锋柊 Jenkins 鏋勫缓缁撴灉澶辫触', e)
  } finally {
    jenkinsResultRefreshing.value = false
  }
}

onMounted(async () => {
  await loadAccessProfile()
  await loadJenkinsInstances()
  loadGovernanceCatalog()
  loadCiStatusConfig()
  loadCiWritebacks()
  loadCiIntegrationHealth()
  loadCredentialSecurityHealth()
  loadIntegrationActions()
  loadWebhookDeliveries()
  loadWebhookTriggerHealth()
  loadJenkinsReviewTriggerHealth()
  loadTelemetryReadiness()
  loadUserAccess()
  loadAccessAuditLogs()
})

const capabilityColumns = [
  { title: '能力', key: 'name', dataIndex: 'name' },
  { title: '类别', key: 'category', dataIndex: 'category' },
  { title: '状态', key: 'status', dataIndex: 'status' },
  { title: '业务影响', key: 'businessImpact', dataIndex: 'businessImpact' },
  { title: '市场信号', key: 'sourceProducts', dataIndex: 'sourceProducts' },
]

const rolloutStages: Array<{ key: RolloutStage; label: string; iconComp: any; iconColor: string }> = [
  { key: 'live', label: '已可用', iconComp: CheckCircleOutlined, iconColor: '#22c55e' },
  { key: 'next', label: '下一阶段', iconComp: SyncOutlined, iconColor: '#6366f1' },
  { key: 'later', label: '后续扩展', iconComp: ClockCircleOutlined, iconColor: '#94a3b8' },
]

function statusLabel(status: CapabilityStatus) { return { ENABLED: '已启用', PARTIAL: '部分具备', PLANNED: '待建设' }[status] }
function statusColor(status: CapabilityStatus) { return { ENABLED: 'green', PARTIAL: 'orange', PLANNED: 'default' }[status] }
function impactLabel(impact: BusinessImpact) { return { HIGH: '高', MEDIUM: '中', LOW: '低' }[impact] }
function impactColor(impact: BusinessImpact) { return { HIGH: 'red', MEDIUM: 'blue', LOW: 'default' }[impact] }
function categoryLabel(category: string) { return { AI_REVIEW: 'AI 审查', QUALITY: '质量', SECURITY: '安全', INTEGRATION: '集成', KNOWLEDGE: '知识库', ANALYTICS: '分析' }[category] ?? category }
function writebackStatusColor(status: string) { return { SUCCESS: 'green', FAILED: 'red', SKIPPED: 'default' }[status] ?? 'default' }
function ciHealthColor(status: string) { return { HEALTHY: 'green', DEGRADED: 'orange', UNHEALTHY: 'red', NO_DATA: 'default' }[status] ?? 'default' }
function jenkinsBuildResultColor(status: string) { return { SUCCESS: 'green', FAILURE: 'red', UNSTABLE: 'orange', ABORTED: 'default', CANCELLED: 'default', BUILDING: 'blue', QUEUED: 'processing' }[status] ?? 'default' }
function actionStatusColor(status: string) { return { UPLOADED: 'green', POSTED: 'green', FAILED: 'red', SKIPPED: 'default' }[status] ?? 'default' }
function accessAuditActionLabel(actionType: string) { return { PLATFORM_ROLE_CHANGED: '平台角色变更', PROJECT_OWNER_ASSIGNED: '项目 Owner 分配', PROJECT_MEMBER_ADDED: '项目成员添加', PROJECT_MEMBER_ROLE_CHANGED: '项目角色变更', PROJECT_MEMBER_REMOVED: '项目成员移除' }[actionType] ?? actionType }
function accessAuditActionColor(actionType: string) { return { PLATFORM_ROLE_CHANGED: 'purple', PROJECT_OWNER_ASSIGNED: 'blue', PROJECT_MEMBER_ADDED: 'green', PROJECT_MEMBER_ROLE_CHANGED: 'orange', PROJECT_MEMBER_REMOVED: 'red' }[actionType] ?? 'default' }
function webhookDeliveryStatusColor(status: string) { return { ACCEPTED: 'green', REJECTED: 'red', DUPLICATE: 'default' }[status] ?? 'default' }
function webhookTriggerStatusColor(status: string) { return { PROCESSED: 'green', DEDUPLICATED: 'blue', SKIPPED: 'default', FAILED: 'red', EXHAUSTED: 'red' }[status] ?? 'default' }
function formatDateTime(value: string) { return new Date(value).toLocaleString() }
function rulePackChangeStatusColor(status: string) { return { PROPOSED: 'orange', APPROVED: 'blue', APPLIED: 'green', REJECTED: 'red', ROLLED_BACK: 'default' }[status] ?? 'default' }
function rulePackVersionStatusColor(status: string) { return { ACTIVE: 'green', ARCHIVED: 'default', ROLLED_BACK: 'orange' }[status] ?? 'default' }
</script>

<style scoped>
.jenkins-inbound-guide {
  margin-bottom: 16px;
  padding: 12px 0;
  border-top: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
}

.jenkins-pipeline-snippet {
  max-height: 320px;
  margin: 0;
  padding: 10px 12px;
  overflow: auto;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #f8fafc;
  color: #334155;
  font-size: 12px;
  line-height: 1.55;
  white-space: pre;
}
</style>
