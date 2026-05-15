// composables/useApi.ts - 统一的 HTTP 请求封装
import type { BaseResult } from '~/types/api'

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
  return {
    'Content-Type': 'application/json',
    ...extra,
  }
}

async function request<T>(
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH',
  url: string,
  body?: unknown,
  options?: RequestOptions,
): Promise<BaseResult<T>> {
  const baseURL = getBaseURL()
  const retries = options?.retry ?? 0

  for (let attempt = 0; attempt <= retries; attempt++) {
    try {
      const response = await $fetch<BaseResult<T>>(url, {
        method,
        baseURL,
        headers: buildHeaders(options?.headers),
        body: body ?? undefined,
        timeout: options?.timeout ?? 30000,
      })
      return response
    } catch (error: unknown) {
      if (attempt >= retries) {
        console.error(`[useApi] ${method} ${url} 请求失败:`, error)
        throw error
      }
      await new Promise((r) => setTimeout(r, 500 * 2 ** attempt))
    }
  }

  throw new Error('请求失败，已达到最大重试次数')
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
