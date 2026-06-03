export const AUTH_TOKEN_KEY = 'review-agent-token'
export const AUTH_USER_KEY = 'review-agent-user'

type StorageLike = Pick<Storage, 'getItem' | 'setItem' | 'removeItem'>

export interface StoredAuth {
  token: string
  user: unknown
}

export function getStoredAuthToken(storage: StorageLike = localStorage): string | null {
  return storage.getItem(AUTH_TOKEN_KEY)
}

export function getStoredAuthUser<T = unknown>(storage: StorageLike = localStorage): T | null {
  const storedUser = storage.getItem(AUTH_USER_KEY)
  if (!storedUser) return null

  try {
    return JSON.parse(storedUser) as T
  } catch {
    return null
  }
}

export function setStoredAuth(storage: StorageLike = localStorage, auth: StoredAuth): void {
  storage.setItem(AUTH_TOKEN_KEY, auth.token)
  storage.setItem(AUTH_USER_KEY, JSON.stringify(auth.user))
}

export function clearStoredAuth(storage: StorageLike = localStorage): void {
  storage.removeItem(AUTH_TOKEN_KEY)
  storage.removeItem(AUTH_USER_KEY)
}
