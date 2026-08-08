import type { ButtonHTMLAttributes, ReactNode } from 'react'

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger'

const variants: Record<Variant, string> = {
  primary: 'bg-brand-600 text-white hover:bg-brand-700 disabled:hover:bg-brand-600',
  secondary: 'border border-ink-300 bg-white text-ink-700 hover:bg-ink-50',
  ghost: 'text-ink-500 hover:bg-ink-100 hover:text-ink-800',
  danger: 'border border-rose-200 bg-white text-rose-600 hover:bg-rose-50',
}

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  loading?: boolean
  children: ReactNode
}

export default function Button({
  variant = 'primary',
  loading = false,
  disabled,
  className = '',
  children,
  ...rest
}: Props) {
  return (
    <button
      {...rest}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      className={`inline-flex items-center justify-center gap-2 rounded-lg px-3.5 py-2 text-sm font-medium
        transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600
        disabled:cursor-not-allowed disabled:opacity-55 ${variants[variant]} ${className}`}
    >
      {loading && (
        <span
          aria-hidden
          className="size-3.5 animate-spin rounded-full border-2 border-current border-t-transparent"
        />
      )}
      {children}
    </button>
  )
}
