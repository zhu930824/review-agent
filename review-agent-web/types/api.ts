export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageParams {
  pageNum: number
  pageSize: number
}

export interface PageResult<T> {
  total: number
  pageNum: number
  pageSize: number
  records: T[]
}
