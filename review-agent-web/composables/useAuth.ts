import type { ApiResponse } from '~/types/api'
import type { AuthToken, LoginPayload, RegisterPayload, UserProfile } from '~/types/auth'

const TOKEN_KEY = 'review-agent-token'
const USER_KEY = 'review-agent-user'

function getBaseURL(): string {
  const config = useRuntimeConfig()
  return config.public.apiBaseUrl as string
}

export function useAuth() {
  const token = useState<string | null>('auth-token', () => null)
  const user = useState<UserProfile | null>('auth-user', () => null)

  function restoreAuth() {
    if (!import.meta.client || token.value) return

    const storedToken = localStorage.getItem(TOKEN_KEY)
    const storedUser = localStorage.getItem(USER_KEY)
    if (!storedToken) return

    token.value = storedToken
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser) as UserProfile
      } catch {
        localStorage.removeItem(USER_KEY)
      }
    }
  }

  function setAuth(auth: AuthToken) {
    token.value = auth.token
    user.value = auth.user

    if (import.meta.client) {
      localStorage.setItem(TOKEN_KEY, auth.token)
      localStorage.setItem(USER_KEY, JSON.stringify(auth.user))
    }
  }

  function clearAuth() {
    token.value = null
    user.value = null

    if (import.meta.client) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }

  async function login(payload: LoginPayload) {
    const response = await $fetch<ApiResponse<AuthToken>>('/auth/login', {
      method: 'POST',
      baseURL: getBaseURL(),
      body: payload,
    })

    if (response.data) {
      setAuth(response.data)
    }

    return response
  }

  async function register(payload: RegisterPayload) {
    const response = await $fetch<ApiResponse<AuthToken>>('/auth/register', {
      method: 'POST',
      baseURL: getBaseURL(),
      body: payload,
    })

    if (response.data) {
      setAuth(response.data)
    }

    return response
  }

  async function logout() {
    try {
      await $fetch<ApiResponse<void>>('/auth/logout', {
        method: 'POST',
        baseURL: getBaseURL(),
        headers: token.value ? { Authorization: `Bearer ${token.value}` } : undefined,
      })
    } finally {
      clearAuth()
      await navigateTo('/login')
    }
  }

  const isAuthenticated = computed(() => Boolean(token.value))

  return {
    token,
    user,
    isAuthenticated,
    restoreAuth,
    setAuth,
    clearAuth,
    login,
    register,
    logout,
  }
}
