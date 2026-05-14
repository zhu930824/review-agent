<template>
  <header class="relative z-50 mx-3 mt-3 mb-0 flex h-16 items-center justify-between rounded-2xl bg-white px-6 shadow-sm animate-fade-in dark:bg-slate-800">
    <div class="flex items-center gap-4">
      <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-500 shadow-md shadow-indigo-500/25">
        <UIcon name="i-heroicons-code-bracket-square" class="h-5 w-5 text-white" />
      </div>
      <div>
        <h1 class="text-base font-bold text-slate-800 dark:text-white">Code Review Agent</h1>
        <p class="text-xs font-medium text-slate-400 dark:text-slate-500">AI-Powered Quality Assurance</p>
      </div>
    </div>

    <div class="flex items-center gap-2">
      <button
        class="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-50 text-slate-400 shadow-sm transition-all hover:bg-slate-100 hover:text-slate-600 dark:bg-slate-700/50 dark:hover:bg-slate-700 dark:hover:text-slate-200"
        type="button"
        aria-label="通知"
      >
        <UIcon name="i-heroicons-bell" class="h-5 w-5" />
      </button>

      <button
        class="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-50 text-slate-400 shadow-sm transition-all hover:bg-slate-100 hover:text-slate-600 dark:bg-slate-700/50 dark:hover:bg-slate-700 dark:hover:text-slate-200"
        type="button"
        aria-label="切换主题"
        @click="toggleColorMode"
      >
        <UIcon :name="colorModeIcon" class="h-5 w-5" />
      </button>

      <div ref="accountMenuRef" class="relative ml-1">
        <button
          class="flex h-10 items-center gap-3 rounded-xl bg-slate-50 px-3 shadow-sm transition-all hover:bg-slate-100 dark:bg-slate-700/50 dark:hover:bg-slate-700"
          type="button"
          :aria-expanded="accountMenuOpen"
          aria-haspopup="menu"
          @click.stop="toggleAccountMenu"
        >
          <UAvatar :alt="displayName" size="sm" />
          <div class="hidden text-left sm:block">
            <p class="text-sm font-bold leading-4 text-slate-700 dark:text-slate-200">{{ displayName }}</p>
            <p class="text-xs text-slate-400 dark:text-slate-500">{{ userRole }}</p>
          </div>
          <UIcon
            name="i-heroicons-chevron-down"
            class="h-4 w-4 text-slate-400 transition-transform"
            :class="accountMenuOpen ? 'rotate-180' : ''"
          />
        </button>

        <Transition
          enter-active-class="transition duration-150 ease-out"
          enter-from-class="translate-y-1 opacity-0"
          enter-to-class="translate-y-0 opacity-100"
          leave-active-class="transition duration-100 ease-in"
          leave-from-class="translate-y-0 opacity-100"
          leave-to-class="translate-y-1 opacity-0"
        >
          <div
            v-if="accountMenuOpen"
            class="absolute right-0 top-12 z-[100] w-56 overflow-hidden rounded-xl border border-slate-100 bg-white p-2 shadow-xl shadow-slate-200/70 dark:border-slate-700 dark:bg-slate-800 dark:shadow-slate-950/40"
            role="menu"
          >
            <div class="flex items-center gap-3 border-b border-slate-100 px-2 py-3 dark:border-slate-700">
              <UAvatar :alt="displayName" size="md" />
              <div class="min-w-0">
                <p class="truncate text-sm font-bold text-slate-800 dark:text-white">{{ displayName }}</p>
                <p class="truncate text-xs text-slate-400 dark:text-slate-500">{{ userEmail }}</p>
              </div>
            </div>

            <div class="grid gap-1 py-2">
              <button
                v-for="item in accountMenuItems"
                :key="item.label"
                class="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-left text-sm font-medium text-slate-600 transition-colors hover:bg-slate-50 hover:text-slate-900 dark:text-slate-300 dark:hover:bg-slate-700/60 dark:hover:text-white"
                :class="item.danger ? 'text-red-500 hover:text-red-600 dark:text-red-400 dark:hover:text-red-300' : ''"
                type="button"
                role="menuitem"
                @click="item.action"
              >
                <UIcon :name="item.icon" class="h-4 w-4" />
                <span>{{ item.label }}</span>
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </header>

  <UModal v-model="profileModalOpen">
    <div class="p-5">
      <div class="flex items-center gap-3 border-b border-slate-100 pb-4 dark:border-slate-700">
        <UAvatar :alt="displayName" size="md" />
        <div class="min-w-0">
          <h2 class="text-base font-bold text-slate-800 dark:text-white">个人中心</h2>
          <p class="truncate text-xs text-slate-400 dark:text-slate-500">{{ userEmail }}</p>
        </div>
      </div>

      <div class="mt-4 grid gap-3">
        <UFormGroup label="用户名">
          <UInput :model-value="user?.username || '-'" size="sm" disabled />
        </UFormGroup>

        <UFormGroup label="显示名称">
          <UInput :model-value="displayName" size="sm" disabled />
        </UFormGroup>

        <UFormGroup label="邮箱">
          <UInput :model-value="userEmail" size="sm" disabled />
        </UFormGroup>

        <UFormGroup label="角色">
          <UInput :model-value="userRole" size="sm" disabled />
        </UFormGroup>

        <div class="mt-1 flex items-center justify-between rounded-lg bg-slate-50 px-3 py-2 text-xs text-slate-500 dark:bg-slate-800 dark:text-slate-400">
          <span>当前主题</span>
          <span class="font-semibold text-slate-700 dark:text-slate-200">{{ themeLabel }}</span>
        </div>
      </div>
    </div>
  </UModal>
</template>

<script setup lang="ts">
const colorMode = useColorMode()
const toast = useToast()
const { user, restoreAuth, logout: logoutAuth } = useAuth()

const accountMenuRef = ref<HTMLElement | null>(null)
const accountMenuOpen = ref(false)
const profileModalOpen = ref(false)

const displayName = computed(() => user.value?.displayName || user.value?.username || 'Admin')
const userEmail = computed(() => user.value?.email || '未设置邮箱')
const userRole = computed(() => user.value?.role || '管理员')
const colorModeIcon = computed(() =>
  colorMode.value === 'dark' ? 'i-heroicons-sun' : 'i-heroicons-moon'
)
const themeLabel = computed(() => (colorMode.value === 'dark' ? '深色' : '浅色'))

const accountMenuItems = [
  {
    label: '个人中心',
    icon: 'i-heroicons-user-circle',
    action: openProfileCenter,
  },
  {
    label: '系统设置',
    icon: 'i-heroicons-cog-6-tooth',
    action: openSystemSettings,
  },
  {
    label: '修改密码',
    icon: 'i-heroicons-key',
    action: openPasswordChange,
  },
  {
    label: '退出登录',
    icon: 'i-heroicons-arrow-right-on-rectangle',
    danger: true,
    action: logout,
  },
]

function toggleColorMode() {
  colorMode.preference = colorMode.value === 'dark' ? 'light' : 'dark'
}

function toggleAccountMenu() {
  accountMenuOpen.value = !accountMenuOpen.value
}

function closeAccountMenu() {
  accountMenuOpen.value = false
}

function openProfileCenter() {
  closeAccountMenu()
  profileModalOpen.value = true
}

function openSystemSettings() {
  closeAccountMenu()
  navigateTo('/settings/models')
}

function openPasswordChange() {
  closeAccountMenu()
  toast.add({ title: '修改密码功能待接入', color: 'blue' })
}

async function logout() {
  closeAccountMenu()
  await logoutAuth()
}

function handleDocumentClick(event: MouseEvent) {
  const target = event.target as Node | null
  if (!target || accountMenuRef.value?.contains(target)) return
  closeAccountMenu()
}

onMounted(() => {
  restoreAuth()
  document.addEventListener('click', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick)
})
</script>
