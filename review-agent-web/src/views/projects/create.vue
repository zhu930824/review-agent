<template>
  <div>
    <!-- Header -->
    <a-space direction="vertical" :size="4" style="margin-bottom: 24px">
      <a-typography-title :level="4" style="margin: 0">创建项目</a-typography-title>
      <a-typography-text type="secondary">添加一个新的代码仓库进行审查</a-typography-text>
    </a-space>

    <a-card :bordered="false" class="ra-form-card">
      <a-form layout="vertical" :model="form" @finish="handleSubmit">
        <a-form-item label="项目名称" required>
          <a-input v-model:value="form.name" placeholder="输入项目名称">
            <template #prefix><FolderOutlined /></template>
          </a-input>
        </a-form-item>

        <a-form-item label="项目描述">
          <a-textarea v-model:value="form.description" placeholder="简要描述项目（可选）" :rows="3" />
        </a-form-item>

        <a-form-item label="Git 仓库地址" required>
          <a-input v-model:value="form.repoUrl" placeholder="https://github.com/owner/repo">
            <template #prefix><LinkOutlined /></template>
          </a-input>
        </a-form-item>

        <a-form-item label="默认分支">
          <a-input v-model:value="form.defaultBranch" placeholder="main" />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="submitting">创建项目</a-button>
            <a-button @click="router.push('/projects')">取消</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useApi } from '@/composables/useApi'
import { FolderOutlined, LinkOutlined } from '@ant-design/icons-vue'
import type { Project } from '@/types/project'

const router = useRouter()
const { post } = useApi()
const submitting = ref(false)

const form = reactive({
  name: '',
  description: '',
  repoUrl: '',
  defaultBranch: 'main',
})

async function handleSubmit() {
  if (!form.name || !form.repoUrl) return
  submitting.value = true
  try {
    const res = await post<Project>('/projects', {
      name: form.name,
      repoUrl: form.repoUrl,
      defaultBranch: form.defaultBranch || 'main',
      description: form.description || undefined,
    })
    if (res.data) {
      router.push(`/projects/${res.data.id}`)
    }
  } catch {
    // useApi 统一处理错误
  } finally {
    submitting.value = false
  }
}
</script>
