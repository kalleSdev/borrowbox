import type { ProblemDetail } from './types'

const BASE = '/api'

/**
 * A failed request, carrying whatever the API said about why.
 *
 * The backend answers every error with a problem detail, so `detail` is
 * already a sentence written for a person and can go straight on screen.
 * `fieldErrors` is filled in when the failure was per-field validation.
 */
export class ApiError extends Error {
  readonly status: number
  readonly title: string
  readonly fieldErrors: Record<string, string>

  constructor(status: number, problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `Request failed with ${status}`)
    this.name = 'ApiError'
    this.status = status
    this.title = problem.title ?? 'Something went wrong'
    this.fieldErrors = problem.errors ?? {}
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${BASE}${path}`, {
      ...init,
      headers: init?.body ? { 'Content-Type': 'application/json', ...init?.headers } : init?.headers,
    })
  } catch {
    // fetch only rejects when the request never made it, so this is the API
    // being down rather than the API saying no.
    throw new ApiError(0, {
      title: 'Cannot reach the server',
      detail: 'The BorrowBox API is not responding. Is it running on port 8080?',
    })
  }

  if (!response.ok) {
    const problem = (await response.json().catch(() => ({}))) as ProblemDetail
    throw new ApiError(response.status, problem)
  }

  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  del: (path: string) => request<void>(path, { method: 'DELETE' }),
}

/** Builds a query string, leaving out anything empty. */
export function query(params: Record<string, string | number | undefined>): string {
  const search = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && String(value).trim() !== '') {
      search.set(key, String(value))
    }
  }
  const qs = search.toString()
  return qs ? `?${qs}` : ''
}
