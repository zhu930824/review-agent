export interface AuthRouteTarget {
  meta?: {
    guest?: boolean
  }
}

export function resolveAuthRedirect(to: AuthRouteTarget, token: string | null): true | string {
  if (to.meta?.guest) return token ? '/' : true
  if (!token) return '/login'
  return true
}
