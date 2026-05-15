export interface UserProfile {
  id: number
  username: string
  displayName: string
  email?: string | null
  role: string
}

export interface AuthToken {
  token: string
  tokenType: string
  expiresAt: number
  user: UserProfile
}

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload extends LoginPayload {
  displayName: string
  email?: string
}
