<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 40%, #ec4899 100%); position: relative; overflow: hidden">
    <div style="position: absolute; top: -150px; right: -100px; width: 400px; height: 400px; background: radial-gradient(circle, rgba(255,255,255,0.2), transparent 60%); border-radius: 50%"></div>
    <div style="position: absolute; bottom: -100px; left: -80px; width: 300px; height: 300px; background: radial-gradient(circle, rgba(255,255,255,0.15), transparent 60%); border-radius: 50%"></div>
    <a-card class="glass-strong" style="width: 420px; border-radius: 20px !important">
      <div style="text-align: center; margin-bottom: 24px">
        <SafetyCertificateOutlined style="font-size: 36px; color: #4F46E5" />
        <h2 style="margin: 12px 0 4px; font-weight: 700">Review Agent</h2>
        <p style="color: #999; font-size: 13px">AI Code Review Platform</p>
      </div>

      <a-form layout="vertical" :model="form" @finish="handleLogin">
        <a-form-item label="用户名" required>
          <a-input v-model:value="form.username" size="large" placeholder="请输入用户名">
            <template #prefix><UserOutlined /></template>
          </a-input>
        </a-form-item>

        <a-form-item label="密码" required>
          <a-input-password v-model:value="form.password" size="large" placeholder="请输入密码">
            <template #prefix><LockOutlined /></template>
          </a-input-password>
        </a-form-item>

        <a-alert v-if="errorMessage" type="error" :message="errorMessage" show-icon closable style="margin-bottom: 16px" @close="errorMessage = ''" />

        <a-form-item>
          <a-button type="primary" html-type="submit" size="large" block :loading="loading">登录</a-button>
        </a-form-item>
      </a-form>

      <div style="text-align: center; font-size: 14px; color: #999; margin-top: 8px">
        <span>还没有账号？</span>
        <router-link to="/register" style="font-weight: 700; color: #4F46E5; margin-left: 4px">创建账号</router-link>
      </div>
    </a-card>
  </div>
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
