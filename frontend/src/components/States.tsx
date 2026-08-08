import type { ReactNode } from 'react'
import { ApiError } from '../api/client'

/** A placeholder with the rough shape of the content it stands in for. */
export function Skeleton({ className = '' }: { className?: string }) {
  return <div className={`animate-pulse rounded-md bg-ink-100 ${className}`} />
}

export function Loading({ label = 'Loading' }: { label?: string }) {
  return (
    <div className="flex items-center gap-2.5 py-8 text-sm text-ink-400" role="status">
      <span
        aria-hidden
        className="size-4 animate-spin rounded-full border-2 border-ink-300 border-t-transparent"
      />
      {label}…
    </div>
  )
}

/**
 * Shows what the API said went wrong. Because the backend answers with problem
 * details, the message is already written for a person.
 */
export function ErrorNotice({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  const title = error instanceof ApiError ? error.title : 'Something went wrong'
  const detail = error instanceof Error ? error.message : String(error)

  return (
    <div role="alert" className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3">
      <p className="text-sm font-semibold text-rose-800">{title}</p>
      <p className="mt-0.5 text-sm text-rose-700">{detail}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="mt-2 text-sm font-medium text-rose-800 underline underline-offset-2 hover:no-underline"
        >
          Try again
        </button>
      )}
    </div>
  )
}

export function EmptyState({
  title,
  hint,
  action,
}: {
  title: string
  hint?: string
  action?: ReactNode
}) {
  return (
    <div className="flex flex-col items-center gap-1 py-10 text-center">
      <p className="text-sm font-medium text-ink-700">{title}</p>
      {hint && <p className="max-w-sm text-sm text-ink-400">{hint}</p>}
      {action && <div className="mt-3">{action}</div>}
    </div>
  )
}
