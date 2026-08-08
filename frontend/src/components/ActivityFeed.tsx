import type { ActivityEvent, EventType } from '../api/types'
import { EmptyState, Skeleton } from './States'

/**
 * How each kind of event reads in the feed. The backend sends a type alongside
 * the sentence precisely so the client can do this without parsing prose.
 */
const style: Record<EventType, { dot: string; label: string }> = {
  DAY_ADVANCED: { dot: 'bg-ink-300', label: 'New day' },
  LOAN_AGREED: { dot: 'bg-brand-500', label: 'Booked' },
  LOAN_STARTED: { dot: 'bg-emerald-500', label: 'Picked up' },
  LOAN_ENDED: { dot: 'bg-amber-500', label: 'Returned' },
  MEMBER_JOINED: { dot: 'bg-violet-500', label: 'Joined' },
  ITEM_LISTED: { dot: 'bg-sky-500', label: 'Listed' },
}

export default function ActivityFeed({
  events,
  loading,
}: {
  events?: ActivityEvent[]
  loading?: boolean
}) {
  if (loading) {
    return (
      <div className="space-y-3">
        {[0, 1, 2].map((i) => (
          <Skeleton key={i} className="h-10 w-full" />
        ))}
      </div>
    )
  }

  if (!events?.length) {
    return (
      <EmptyState
        title="Nothing has happened yet"
        hint="Book a loan or advance the day and it will show up here."
      />
    )
  }

  return (
    <ol className="relative space-y-4 before:absolute before:top-2 before:bottom-2 before:left-[3.5px] before:w-px before:bg-ink-200">
      {events.map((event, index) => {
        const tone = style[event.type] ?? style.DAY_ADVANCED
        return (
          <li key={`${event.day}-${index}`} className="relative flex gap-3 pl-5">
            <span
              aria-hidden
              className={`absolute top-1.5 left-0 size-2 rounded-full ring-2 ring-white ${tone.dot}`}
            />
            <div className="min-w-0 flex-1">
              <p className="text-sm text-ink-800">{event.description}</p>
              <p className="mt-0.5 text-xs text-ink-400">
                <span className="tabular">Day {event.day}</span>
                <span className="mx-1.5">·</span>
                {tone.label}
              </p>
            </div>
          </li>
        )
      })}
    </ol>
  )
}
