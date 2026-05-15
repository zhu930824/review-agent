export interface BaseResult<T = unknown> {
  code: number
  msg: string
  data: T
  tips?: string
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
