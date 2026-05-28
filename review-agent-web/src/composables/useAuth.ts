import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const API_BASE = '/api'
const TOKEN_KEY = 'review-agent-token'
const USER_KEY = 'review-agent-user'

const token = ref<string | null>(null)
const user = ref<any | null>(null)

function restoreAuth() {
  const stored = localStorage.getItem(TOKEN_KEY)
  if (stored) token.value = stored
  const storedUser = localStorage.getItem(USER_KEY)
  if (storedUser) {
    try { user.value = JSON.parse(storedUser) } catch { /* ignore */ }
  }
}

function setAuth(auth: { token: string; user: any }) {
  token.value = auth.token
  user.value = auth.user
  localStorage.setItem(TOKEN_KEY, auth.token)
  localStorage.setItem(USER_KEY, JSON.stringify(auth.user))
}

function clearAuth() {
  token.value = null
  user.value = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
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
