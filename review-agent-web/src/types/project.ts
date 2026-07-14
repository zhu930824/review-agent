export interface Project {
  id: number
  name: string
  description: string
  repoUrl: string
  defaultBranch: string
  localPath: string | null
  status: 'PENDING' | 'CLONING' | 'READY' | 'ERROR'
  createdAt: string
  updatedAt: string
  cloneErrorMessage: string | null
}

export interface CreateProjectParams {
  name: string
  repoUrl: string
  defaultBranch?: string
  description?: string
  /** GitLab Personal Access Token（可选），配置后通过 API 获取 diff，无需本地克隆 */
  gitlabToken?: string
}

export interface UpdateProjectParams {
  name?: string
  repoUrl?: string
  defaultBranch?: string
  description?: string
}

export interface ProjectGitLabReviewPolicy {
  projectId: number
  configured: boolean
  autoReviewEnabled: boolean
  reviewDrafts: boolean
  publishSummaryEnabled: boolean
  targetBranchPattern: string | null
}

export type ProjectMemberRole = 'OWNER' | 'MAINTAINER' | 'REVIEWER'

export interface ProjectMember {
  userId: number
  username: string
  displayName: string | null
  email: string | null
  role: ProjectMemberRole
  status: string
  currentUser: boolean
  permissions: string[]
  updatedAt: string
}
