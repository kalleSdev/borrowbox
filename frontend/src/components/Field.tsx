import type { InputHTMLAttributes, ReactNode, SelectHTMLAttributes } from 'react'

const control =
  `w-full rounded-lg border border-ink-300 bg-white px-3 py-2 text-sm text-ink-800 transition-colors
   placeholder:text-ink-400 focus:border-brand-500 focus:outline-2 focus:outline-offset-0
   focus:outline-brand-500/30 aria-[invalid=true]:border-rose-400`

function Wrapper({ label, error, children }: { label: string; error?: string; children: ReactNode }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-sm font-medium text-ink-700">{label}</span>
      {children}
      {error && <span className="mt-1 block text-sm text-rose-600">{error}</span>}
    </label>
  )
}

interface FieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
}

/** A labelled input that shows the API's per-field validation message under it. */
export default function Field({ label, error, className = '', ...rest }: FieldProps) {
  return (
    <Wrapper label={label} error={error}>
      <input {...rest} aria-invalid={Boolean(error)} className={`${control} ${className}`} />
    </Wrapper>
  )
}

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string
  error?: string
  children: ReactNode
}

export function SelectField({ label, error, children, className = '', ...rest }: SelectProps) {
  return (
    <Wrapper label={label} error={error}>
      <select {...rest} aria-invalid={Boolean(error)} className={`${control} ${className}`}>
        {children}
      </select>
    </Wrapper>
  )
}
