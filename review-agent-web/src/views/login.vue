<template>
  <main class="auth-page">
    <section class="auth-panel">
      <div class="auth-card">
        <div class="auth-brand">
          <div class="auth-logo">
            <SafetyCertificateOutlined />
          </div>
          <div>
            <h1 class="auth-brand-title">Review Agent</h1>
            <div class="auth-brand-subtitle">研发质量治理平台</div>
          </div>
        </div>

        <h2 class="auth-title">欢迎回来</h2>
        <p class="auth-copy">登录后继续查看 Pre-PR Gate、模型遥测和团队治理任务。</p>

        <a-form layout="vertical" :model="form" @finish="handleLogin">
          <a-form-item label="用户名" required>
            <a-input v-model:value="form.username" size="large" placeholder="请输入用户名" autocomplete="username">
              <template #prefix><UserOutlined /></template>
            </a-input>
          </a-form-item>

          <a-form-item label="密码" required>
            <a-input-password v-model:value="form.password" size="large" placeholder="请输入密码" autocomplete="current-password">
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>

          <a-alert v-if="errorMessage" type="error" :message="errorMessage" show-icon closable style="margin-bottom: 16px" @close="errorMessage = ''" />

          <a-form-item>
            <a-button class="auth-submit" type="primary" html-type="submit" size="large" block :loading="loading">登录</a-button>
          </a-form-item>
        </a-form>

        <div class="auth-footer">
          <span>还没有账号？</span>
          <router-link to="/register">创建账号</router-link>
        </div>
      </div>
    </section>

    <section class="auth-aside" aria-label="平台价值说明">
      <div class="auth-aside-content">
        <div class="auth-kicker">Pre-PR Gate · Model Telemetry · Governance</div>
        <h2>把 AI Review 接入真实交付门禁。</h2>
        <p>用多模型策略、人工确认、CI 回写和运营指标，把一次审查变成可持续治理闭环。</p>
        <div class="auth-metrics">
          <div class="auth-metric">
            <strong>Gate</strong>
            <span>后端持久化准入决策</span>
          </div>
          <div class="auth-metric">
            <strong>CI</strong>
            <span>状态回写与失败重试</span>
          </div>
          <div class="auth-metric">
            <strong>Ops</strong>
            <span>策略压力和修复队列</span>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { UserOutlined, LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const { login } = useAuth()
const loading = ref(false)
const errorMessage = ref('')
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  if (!form.username || !form.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    await login({ username: form.username, password: form.password })
    await router.push('/')
  } catch (error: any) {
    errorMessage.value = error?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>
