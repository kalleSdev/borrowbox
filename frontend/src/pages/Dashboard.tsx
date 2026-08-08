import { Link } from 'react-router-dom'
import { useClock, useContracts, useEvents, useItems, useMembers } from '../api/hooks'
import ActivityFeed from '../components/ActivityFeed'
import Button from '../components/Button'
import Card from '../components/Card'
import { ErrorNotice, Skeleton } from '../components/States'
import { useAdvanceDay } from '../api/hooks'

function Stat({ label, value, hint }: { label: string; value?: number; hint?: string }) {
  return (
    <div className="rounded-xl border border-ink-200 bg-white px-5 py-4">
      <p className="text-xs font-medium tracking-wide text-ink-400 uppercase">{label}</p>
      {value === undefined ? (
        <Skeleton className="mt-2 h-8 w-16" />
      ) : (
        <p className="tabular mt-1 text-3xl font-semibold tracking-tight text-ink-900">{value}</p>
      )}
      {hint && <p className="mt-1 text-xs text-ink-400">{hint}</p>}
    </div>
  )
}

export default function Dashboard() {
  const clock = useClock()
  const members = useMembers()
  const items = useItems()
  const contracts = useContracts()
  const events = useEvents(12)
  const advance = useAdvanceDay()

  const today = clock.data?.currentDay
  const activeLoans = contracts.data?.filter(
    (c) => today !== undefined && c.startDay <= today && c.endDay >= today,
  ).length
  const creditsInPlay = members.data?.reduce((total, member) => total + member.credits, 0)

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-ink-900">Dashboard</h1>
          <p className="mt-1 text-ink-500">What is happening across the lending pool.</p>
        </div>
      </div>

      {clock.isError && <ErrorNotice error={clock.error} onRetry={() => clock.refetch()} />}

      <Card bodyClassName="p-5 sm:p-6">
        <div className="flex flex-wrap items-center justify-between gap-5">
          <div>
            <p className="text-xs font-medium tracking-wide text-ink-400 uppercase">Simulated clock</p>
            {today === undefined ? (
              <Skeleton className="mt-2 h-10 w-28" />
            ) : (
              <p className="tabular mt-1 text-4xl font-semibold tracking-tight text-ink-900">
                Day {today}
              </p>
            )}
            <p className="mt-1.5 max-w-md text-sm text-ink-500">
              Loans start and end when the calendar moves. Nothing happens on its own.
            </p>
          </div>

          <Button onClick={() => advance.mutate()} loading={advance.isPending}>
            Advance the day
          </Button>
        </div>

        {advance.isError && (
          <div className="mt-4">
            <ErrorNotice error={advance.error} />
          </div>
        )}

        {advance.data && advance.data.events.length > 0 && (
          <ul className="mt-5 space-y-1.5 border-t border-ink-100 pt-4">
            {advance.data.events.map((event, i) => (
              <li key={i} className="text-sm text-ink-600">
                {event.description}
              </li>
            ))}
          </ul>
        )}
      </Card>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Stat label="Members" value={members.data?.length} />
        <Stat label="Items listed" value={items.data?.length} />
        <Stat label="Loans running" value={activeLoans} hint={`on day ${today ?? '—'}`} />
        <Stat label="Credits in circulation" value={creditsInPlay} />
      </div>

      <div className="grid gap-6 lg:grid-cols-5">
        <Card
          className="lg:col-span-3"
          title="Recent activity"
          action={
            <span className="text-xs text-ink-400">
              {events.data?.length ? `${events.data.length} newest first` : ''}
            </span>
          }
        >
          {events.isError ? (
            <ErrorNotice error={events.error} onRetry={() => events.refetch()} />
          ) : (
            <ActivityFeed events={events.data} loading={events.isLoading} />
          )}
        </Card>

        <Card
          className="lg:col-span-2"
          title="Available today"
          action={
            <Link to="/catalogue" className="text-xs font-medium text-brand-700 hover:underline">
              Browse all
            </Link>
          }
        >
          {items.isLoading ? (
            <div className="space-y-3">
              {[0, 1, 2].map((i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          ) : (
            <ul className="divide-y divide-ink-100">
              {items.data
                ?.filter((item) => item.availableToday)
                .slice(0, 5)
                .map((item) => (
                  <li key={item.id} className="flex items-center justify-between gap-3 py-2.5 first:pt-0">
                    <div className="min-w-0">
                      <Link
                        to={`/catalogue/${item.id}`}
                        className="block truncate text-sm font-medium text-ink-800 hover:text-brand-700"
                      >
                        {item.name}
                      </Link>
                      <p className="truncate text-xs text-ink-400">from {item.ownerName}</p>
                    </div>
                    <span className="tabular shrink-0 text-sm text-ink-500">
                      {item.costPerDay}/day
                    </span>
                  </li>
                ))}
              {items.data?.every((item) => !item.availableToday) && (
                <li className="py-6 text-center text-sm text-ink-400">
                  Everything is out on loan today.
                </li>
              )}
            </ul>
          )}
        </Card>
      </div>
    </div>
  )
}
