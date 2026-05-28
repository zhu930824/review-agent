import { useRouter } from 'vue-router'
import { useAuth } from './useAuth'

const API_BASE = '/api'

async function request<T>(method: string, url: string, body?: any): Promise<{ code: number; data?: T; message?: string }> {
  const { token } = useAuth()
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (token.value) headers['Authorization'] = `Bearer ${token.value}`

  const res = await fetch(`${API_BASE}${url}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  })

  if (res.status === 401) {
    const router = useRouter()
    localStorage.removeItem('review-agent-token')
    router.push('/login')
    throw new Error('Unauthorized')
  }

  return res.json()
}

export function useApi() {
  return {
    get: <T>(url: string) => request<T>('GET', url),
    post: <T>(url: string, body?: any) => request<T>('POST', url, body),
    put: <T>(url: string, body?: any) => request<T>('PUT', url, body),
    patch: <T>(url: string, body?: any) => request<T>('PATCH', url, body),
    del: <T>(url: string) => request<T>('DELETE', url),
  }
}
