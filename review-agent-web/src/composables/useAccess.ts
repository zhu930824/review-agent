import { computed, ref } from 'vue'
import { getApiBaseUrl } from '@/utils/apiConfig'
import { clearStoredAuth, getStoredAuthToken, getStoredAuthUser } from '@/utils/authStorage'
import type { AccessProfile, PlatformPermission, UserProfile } from '@/types/auth'

const accessProfile = ref<AccessProfile | null>(null)
let loadingPromise: Promise<AccessProfile | null> | null = null

const rolePermissions: Record<string, PlatformPermission[]> = {
  ADMIN: ['ACCESS_MANAGE', 'GOVERNANCE_VIEW', 'GOVERNANCE_MANAGE', 'INTEGRATION_VIEW', 'INTEGRATION_MANAGE', 'CREDENTIAL_ROTATE', 'MODEL_VIEW', 'MODEL_MANAGE', 'OPERATIONS_VIEW', 'OPERATIONS_MANAGE'],
  GOVERNANCE_MANAGER: ['GOVERNANCE_VIEW', 'GOVERNANCE_MANAGE', 'INTEGRATION_VIEW', 'INTEGRATION_MANAGE', 'CREDENTIAL_ROTATE', 'MODEL_VIEW', 'MODEL_MANAGE', 'OPERATIONS_VIEW'],
  OPERATOR: ['GOVERNANCE_VIEW', 'INTEGRATION_VIEW', 'MODEL_VIEW', 'OPERATIONS_VIEW', 'OPERATIONS_MANAGE'],
  REVIEWER: ['MODEL_VIEW'],
}

function normalizeRole(role?: string | null): string {
  const normalized = (role || '').trim().toUpperCase().replaceAll('-', '_').replaceAll(' ', '_')
  if (['ADMIN', 'ADMINISTRATOR', '管理员', '平台管理员'].includes(normalized)) return 'ADMIN'
  if (['GOVERNANCE_MANAGER', '治理管理员', '治理负责人'].includes(normalized)) return 'GOVERNANCE_MANAGER'
  if (['OPERATOR', '运营人员', '运营管理员'].includes(normalized)) return 'OPERATOR'
  return 'REVIEWER'
}

function fallbackProfile(): AccessProfile | null {
  const user = getStoredAuthUser<UserProfile>()
  if (!user) return null
  const normalizedRole = normalizeRole(user.role)
  return {
    userId: user.id,
    username: user.username,
    displayName: user.displayName,
    role: user.role,
    normalizedRole,
    permissions: rolePermissions[normalizedRole] || rolePermissions.REVIEWER,
  }
}

export async function loadAccessProfile(force = false): Promise<AccessProfile | null> {
  if (accessProfile.value && !force) return accessProfile.value
  if (loadingPromise && !force) return loadingPromise
  const token = getStoredAuthToken()
  if (!token) {
    accessProfile.value = null
    return null
  }

  loadingPromise = fetch(`${getApiBaseUrl()}/auth/access-profile`, {
    headers: { Authorization: `Bearer ${token}` },
  })
    .then(async response => {
      if (response.status === 401) {
        clearStoredAuth()
        return null
      }
      if (!response.ok) return fallbackProfile()
      const result = await response.json()
      return (result.data as AccessProfile | undefined) ?? fallbackProfile()
    })
    .catch(() => fallbackProfile())
    .then(profile => {
      accessProfile.value = profile
      loadingPromise = null
      return profile
    })
  return loadingPromise
}

export function clearAccessProfile() {
  accessProfile.value = null
  loadingPromise = null
}

export function canAccess(permission: PlatformPermission): boolean {
  const profile = accessProfile.value ?? fallbackProfile()
  return profile?.permissions.includes(permission) ?? false
}

export function useAccess() {
  return {
    accessProfile,
    permissions: computed(() => accessProfile.value?.permissions ?? fallbackProfile()?.permissions ?? []),
    can: canAccess,
    loadAccessProfile,
    clearAccessProfile,
  }
}
