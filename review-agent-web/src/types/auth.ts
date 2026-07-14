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

export type PlatformPermission =
  | 'ACCESS_MANAGE'
  | 'GOVERNANCE_VIEW'
  | 'GOVERNANCE_MANAGE'
  | 'INTEGRATION_VIEW'
  | 'INTEGRATION_MANAGE'
  | 'CREDENTIAL_ROTATE'
  | 'MODEL_VIEW'
  | 'MODEL_MANAGE'
  | 'OPERATIONS_VIEW'
  | 'OPERATIONS_MANAGE'

export interface AccessProfile {
  userId: number
  username: string
  displayName: string
  role: string
  normalizedRole: 'ADMIN' | 'GOVERNANCE_MANAGER' | 'OPERATOR' | 'REVIEWER' | string
  permissions: PlatformPermission[]
}

export interface UserAccess {
  id: number
  username: string
  displayName: string
  email?: string | null
  role: string
  normalizedRole: AccessProfile['normalizedRole']
  status: string
  permissions: PlatformPermission[]
}

export interface AccessAuditLog {
  id: number
  actorUserId?: number | null
  actorUsername: string
  actionType: string
  actionStatus: string
  targetUserId?: number | null
  targetUsername?: string | null
  projectId?: number | null
  previousRole?: string | null
  newRole?: string | null
  detail?: string | null
  createdAt: string
}

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload extends LoginPayload {
  displayName: string
  email?: string
}
