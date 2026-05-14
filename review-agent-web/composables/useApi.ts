import type { ApiResponse } from '~/types/api'

function getBaseURL(): string {
  const config = useRuntimeConfig()
  return config.public.apiBaseUrl as string
}

interface RequestOptions {
  headers?: Record<string, string>
  timeout?: number
  retry?: number
}

function buildHeaders(extra?: Record<string, string>): Record<string, string> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...extra,
  }

  const { token } = useAuth()
  if (token.value) {
    headers.Authorization = `Bearer ${token.value}`
  }

  return headers
}

async function handleUnauthorized() {
  const { clearAuth } = useAuth()
  clearAuth()
  if (import.meta.client && useRoute().path !== '/login') {
    await navigateTo('/login')
  }
}

async function request<T>(
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH',
  url: string,
  body?: unknown,
  options?: RequestOptions,
): Promise<ApiResponse<T>> {
  const baseURL = getBaseURL()
  const retries = options?.retry ?? 0

  for (let attempt = 0; attempt <= retries; attempt++) {
    try {
      return await $fetch<ApiResponse<T>>(url, {
        method,
        baseURL,
        headers: buildHeaders(options?.headers),
        body: body ?? undefined,
        timeout: options?.timeout ?? 30000,
      })
    } catch (error: any) {
      if (error?.status === 401 || error?.statusCode === 401) {
        await handleUnauthorized()
      }

      if (attempt >= retries) {
        console.error(`[useApi] ${method} ${url} request failed:`, error)
        throw error
      }

      await new Promise((resolve) => setTimeout(resolve, 500 * 2 ** attempt))
    }
  }

  throw new Error('Request failed after all retry attempts')
}

export function useApi() {
  const get = <T>(url: string, options?: RequestOptions) =>
    request<T>('GET', url, undefined, options)

  const post = <T>(url: string, body?: unknown, options?: RequestOptions) =>
    request<T>('POST', url, body, options)

  const put = <T>(url: string, body?: unknown, options?: RequestOptions) =>
    request<T>('PUT', url, body, options)

  const del = <T>(url: string, options?: RequestOptions) =>
    request<T>('DELETE', url, undefined, options)

  const patch = <T>(url: string, body?: unknown, options?: RequestOptions) =>
    request<T>('PATCH', url, body, options)

  return { get, post, put, del, patch }
}
