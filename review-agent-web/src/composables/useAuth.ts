import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { clearStoredAuth, getStoredAuthToken, getStoredAuthUser, setStoredAuth } from '@/utils/authStorage'
import { getApiBaseUrl } from '@/utils/apiConfig'

const API_BASE = getApiBaseUrl()

const token = ref<string | null>(null)
const user = ref<any | null>(null)

function restoreAuth() {
  const stored = getStoredAuthToken()
  if (stored) token.value = stored
  user.value = getStoredAuthUser()
}

function setAuth(auth: { token: string; user: any }) {
  token.value = auth.token
  user.value = auth.user
  setStoredAuth(localStorage, auth)
}

function clearAuth() {
  token.value = null
  user.value = null
  clearStoredAuth()
}

export function useAuth() {
  const router = useRouter()
  const isAuthenticated = computed(() => !!token.value)

  async function login(payload: { username: string; password: string }) {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
    const data = await res.json()
    if (data.data) setAuth(data.data)
    return data
  }

  async function register(payload: { username: string; password: string; email: string }) {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
    const data = await res.json()
    if (data.data) setAuth(data.data)
    return data
  }

  async function logout() {
    await fetch(`${API_BASE}/auth/logout`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
    }).catch(() => {})
    clearAuth()
    router.push('/login')
  }

  return { token, user, isAuthenticated, restoreAuth, setAuth, clearAuth, login, register, logout }
}
