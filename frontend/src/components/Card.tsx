import type { ReactNode } from 'react'

interface Props {
  title?: ReactNode
  action?: ReactNode
  children: ReactNode
  className?: string
  bodyClassName?: string
}

export default function Card({ title, action, children, className = '', bodyClassName = 'p-5' }: Props) {
  return (
    <section
      className={`rounded-xl border border-ink-200 bg-white shadow-[0_1px_2px_rgb(15_23_42/0.04)] ${className}`}
    >
      {(title || action) && (
        <header className="flex items-center justify-between gap-3 border-b border-ink-100 px-5 py-3.5">
          <h2 className="text-sm font-semibold tracking-tight text-ink-900">{title}</h2>
          {action}
        </header>
      )}
      <div className={bodyClassName}>{children}</div>
    </section>
  )
}
