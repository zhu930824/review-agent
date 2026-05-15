export default defineNuxtRouteMiddleware((to) => {
  if (import.meta.server) return

  const publicRoutes = new Set(['/login', '/register'])
  const { token, restoreAuth } = useAuth()
  restoreAuth()

  const isPublicRoute = publicRoutes.has(to.path)
  if (!token.value && !isPublicRoute) {
    return navigateTo('/login')
  }

  if (token.value && isPublicRoute) {
    return navigateTo('/')
  }
})
