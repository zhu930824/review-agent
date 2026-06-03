interface ApiEnv {
  VITE_API_BASE_URL?: string
}

export function getApiBaseUrl(env: ApiEnv = import.meta.env as ApiEnv): string {
  const value = env.VITE_API_BASE_URL?.trim()
  return value || '/api'
}
