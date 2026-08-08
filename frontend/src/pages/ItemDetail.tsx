import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useClock, useDeleteItem, useItem } from '../api/hooks'
import Badge from '../components/Badge'
import BookLoanDialog from '../components/BookLoanDialog'
import Button from '../components/Button'
import Card from '../components/Card'
import { EmptyState, ErrorNotice, Loading } from '../components/States'

export default function ItemDetail() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const item = useItem(id)
  const clock = useClock()
  const remove = useDeleteItem()
  const [borrowing, setBorrowing] = useState(false)

  if (item.isLoading) return <Loading label="Loading item" />
  if (item.isError) return <ErrorNotice error={item.error} onRetry={() => item.refetch()} />
  if (!item.data) return null

  const today = clock.data?.currentDay ?? 0
  const loans = [...item.data.contracts].sort((a, b) => a.startDay - b.startDay)

  return (
    <div className="space-y-6">
      <Link to="/catalogue" className="text-sm text-ink-500 hover:text-brand-700">
        ← Back to the catalogue
      </Link>

      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-2xl font-semibold tracking-tight text-ink-900">{item.data.name}</h1>
            <Badge tone={item.data.availableToday ? 'available' : 'busy'}>
              {item.data.availableToday ? 'Free today' : 'On loan'}
            </Badge>
          </div>
          <p className="mt-1 text-ink-500">{item.data.description}</p>
        </div>
        <Button onClick={() => setBorrowing(true)}>Borrow this</Button>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card title="Details" className="lg:col-span-1">
          <dl className="space-y-3 text-sm">
            {[
              ['Owner', item.data.ownerName],
              ['Category', item.data.category],
              ['Cost', `${item.data.costPerDay} credits per day`],
              ['Listed on', `Day ${item.data.listedOnDay}`],
              ['Item id', item.data.id],
            ].map(([label, value]) => (
              <div key={label} className="flex justify-between gap-4">
                <dt className="text-ink-400">{label}</dt>
                <dd className="text-right font-medium text-ink-800">{value}</dd>
              </div>
            ))}
          </dl>
        </Card>

        <Card title="Booking history" className="lg:col-span-2" bodyClassName="p-0">
          {loans.length === 0 ? (
            <EmptyState title="Never been borrowed" hint="Its calendar is completely free." />
          ) : (
            <ul className="divide-y divide-ink-100">
              {loans.map((loan, i) => {
                const running = loan.startDay <= today && loan.endDay >= today
                const finished = loan.endDay < today
                return (
                  <li key={i} className="flex items-center justify-between gap-4 px-5 py-3.5">
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-ink-800">{loan.borrowerName}</p>
                      <p className="tabular text-xs text-ink-400">
                        Day {loan.startDay} to {loan.endDay} · {loan.durationInDays}{' '}
                        {loan.durationInDays === 1 ? 'day' : 'days'}
                      </p>
                    </div>
                    <div className="flex shrink-0 items-center gap-3">
                      <Badge tone={running ? 'available' : finished ? 'neutral' : 'accent'}>
                        {running ? 'Out now' : finished ? 'Returned' : 'Upcoming'}
                      </Badge>
                      <span className="tabular text-sm font-medium text-ink-700">
                        {loan.cost} cr
                      </span>
                    </div>
                  </li>
                )
              })}
            </ul>
          )}
        </Card>
      </div>

      <Card title="Remove this item">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <p className="max-w-lg text-sm text-ink-500">
            {loans.length > 0
              ? 'This item has loans booked against it, so it cannot be removed. Deleting it would take the record of those loans with it.'
              : 'Nobody has booked this, so it can be taken off the catalogue.'}
          </p>
          <Button
            variant="danger"
            loading={remove.isPending}
            disabled={loans.length > 0}
            onClick={() => remove.mutate(id, { onSuccess: () => navigate('/catalogue') })}
          >
            Remove
          </Button>
        </div>
        {remove.isError && (
          <div className="mt-4">
            <ErrorNotice error={remove.error} />
          </div>
        )}
      </Card>

      <BookLoanDialog item={borrowing ? item.data : null} onClose={() => setBorrowing(false)} />
    </div>
  )
}
