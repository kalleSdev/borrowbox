import type { ReactNode } from 'react'

type Tone = 'neutral' | 'available' | 'busy' | 'accent'

const tones: Record<Tone, string> = {
  neutral: 'bg-ink-100 text-ink-600',
  available: 'bg-emerald-50 text-emerald-700',
  busy: 'bg-amber-50 text-amber-700',
  accent: 'bg-brand-50 text-brand-700',
}

export default function Badge({ tone = 'neutral', children }: { tone?: Tone; children: ReactNode }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium whitespace-nowrap ${tones[tone]}`}
    >
      {children}
    </span>
  )
}
