// composables/useApi.ts - 统一的 HTTP 请求封装
import type { ApiResponse } from '~/types/api'

/**
 * 从 runtimeConfig 获取后端 API 基地址
 * 返回已配置好 baseURL 的 $fetch 实例
 */
function getBaseURL(): string {
  const config = useRuntimeConfig()
  return config.public.apiBaseUrl as string
}

/** 通用请求选项：允许覆盖默认配置 */
interface RequestOptions {
  /** 自定义 headers */
  headers?: Record<string, string>
  /** 超时时间（ms） */
  timeout?: number
  /** 请求重试次数 */
  retry?: number
}

/**
 * 构建统一请求头
 */
function buildHeaders(extra?: Record<string, string>): Record<string, string> {
  return {
    'Content-Type': 'application/json',
    ...extra,
  }
}

/**
 * 统一的 fetch 封装，自动拼接 baseURL 并处理错误
 */
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
      const response = await $fetch<ApiResponse<T>>(url, {
        method,
        baseURL,
        headers: buildHeaders(options?.headers),
        body: body ?? undefined,
        timeout: options?.timeout ?? 30000,
      })
      return response
    } catch (error: unknown) {
      // 最后一次重试仍失败，抛出错误
      if (attempt >= retries) {
        console.error(`[useApi] ${method} ${url} 请求失败:`, error)
        throw error
      }
      // 指数退避
      await new Promise((r) => setTimeout(r, 500 * 2 ** attempt))
    }
  }

  // 理论上不会走到这里，但保证 TypeScript 类型完备
  throw new Error('请求失败，已达到最大重试次数')
}

/**
 * 导出的类型化请求方法集合
 */
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
