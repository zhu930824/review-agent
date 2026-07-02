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

        <h2 class="auth-title">创建团队账号</h2>
        <p class="auth-copy">开通后即可接入项目、发起 Pre-PR 审查并沉淀团队治理数据。</p>

        <a-form layout="vertical" :model="form" @finish="handleRegister">
          <a-form-item label="用户名" required>
            <a-input v-model:value="form.username" size="large" placeholder="请输入用户名" autocomplete="username">
              <template #prefix><UserOutlined /></template>
            </a-input>
          </a-form-item>

          <a-form-item label="邮箱" required>
            <a-input v-model:value="form.email" size="large" placeholder="请输入邮箱" autocomplete="email">
              <template #prefix><MailOutlined /></template>
            </a-input>
          </a-form-item>

          <a-form-item label="密码" required>
            <a-input-password v-model:value="form.password" size="large" placeholder="请输入密码" autocomplete="new-password">
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>

          <a-form-item label="确认密码" required>
            <a-input-password v-model:value="form.confirmPassword" size="large" placeholder="请再次输入密码" autocomplete="new-password">
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>

          <a-alert v-if="errorMessage" type="error" :message="errorMessage" show-icon closable style="margin-bottom: 16px" @close="errorMessage = ''" />

          <a-form-item>
            <a-button class="auth-submit" type="primary" html-type="submit" size="large" block :loading="loading">创建账号</a-button>
          </a-form-item>
        </a-form>

        <div class="auth-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </section>

    <section class="auth-aside" aria-label="平台能力说明">
      <div class="auth-aside-content">
        <div class="auth-kicker">Quality Gate · Rule Learning · Integration</div>
        <h2>让团队从第一次审查开始积累治理资产。</h2>
        <p>项目、策略、Finding、Gate、CI 回写和运营指标会进入同一条质量闭环。</p>
        <div class="auth-metrics">
          <div class="auth-metric">
            <strong>Review</strong>
            <span>多模型审查和人工确认</span>
          </div>
          <div class="auth-metric">
            <strong>Rule</strong>
            <span>规则候选和治理行动项</span>
          </div>
          <div class="auth-metric">
            <strong>Trace</strong>
            <span>调用遥测和集成日志</span>
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
import { UserOutlined, MailOutlined, LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const { register } = useAuth()
const loading = ref(false)
const errorMessage = ref('')
const form = reactive({ username: '', email: '', password: '', confirmPassword: '' })

async function handleRegister() {
  if (!form.username || !form.email || !form.password) {
    errorMessage.value = '请填写所有必填项'
    return
  }
  if (form.password !== form.confirmPassword) {
    errorMessage.value = '两次密码不一致'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    await register({ username: form.username, password: form.password, email: form.email })
    await router.push('/')
  } catch (error: any) {
    errorMessage.value = error?.data?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>
