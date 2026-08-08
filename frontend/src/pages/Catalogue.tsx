import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useDeferredValue } from 'react'
import { useItems } from '../api/hooks'
import type { Item } from '../api/types'
import BookLoanDialog from '../components/BookLoanDialog'
import Badge from '../components/Badge'
import Button from '../components/Button'
import { EmptyState, ErrorNotice, Skeleton } from '../components/States'
import NewItemDialog from '../components/NewItemDialog'

export default function Catalogue() {
  const [name, setName] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [borrowing, setBorrowing] = useState<Item | null>(null)
  const [listing, setListing] = useState(false)

  // Keep the filters out of the query key until typing settles, so every
  // keystroke does not become a request.
  const filters = useDeferredValue({ name, maxPrice })
  const items = useItems(filters)

  const filtering = name !== '' || maxPrice !== ''

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-ink-900">Catalogue</h1>
          <p className="mt-1 text-ink-500">Everything the members are willing to lend.</p>
        </div>
        <Button onClick={() => setListing(true)}>List an item</Button>
      </div>

      <div className="flex flex-wrap gap-3 rounded-xl border border-ink-200 bg-white p-3">
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Search by name…"
          aria-label="Search by name"
          className="min-w-52 flex-1 rounded-lg border border-ink-300 px-3 py-2 text-sm
            placeholder:text-ink-400 focus:border-brand-500 focus:outline-2 focus:outline-brand-500/30"
        />
        <input
          value={maxPrice}
          onChange={(e) => setMaxPrice(e.target.value)}
          inputMode="numeric"
          placeholder="Max credits per day"
          aria-label="Maximum credits per day"
          className="w-52 rounded-lg border border-ink-300 px-3 py-2 text-sm
            placeholder:text-ink-400 focus:border-brand-500 focus:outline-2 focus:outline-brand-500/30"
        />
        {filtering && (
          <Button
            variant="ghost"
            onClick={() => {
              setName('')
              setMaxPrice('')
            }}
          >
            Clear
          </Button>
        )}
      </div>

      {items.isError ? (
        <ErrorNotice error={items.error} onRetry={() => items.refetch()} />
      ) : items.isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[0, 1, 2].map((i) => (
            <Skeleton key={i} className="h-44 w-full" />
          ))}
        </div>
      ) : items.data?.length === 0 ? (
        <EmptyState
          title={filtering ? 'Nothing matches those filters' : 'The catalogue is empty'}
          hint={
            filtering
              ? 'Try a shorter search, or a higher price ceiling.'
              : 'List the first item and it will show up here.'
          }
        />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {items.data?.map((item) => (
            <article
              key={item.id}
              className="flex flex-col rounded-xl border border-ink-200 bg-white p-5 transition-shadow
                hover:shadow-[0_2px_12px_rgb(15_23_42/0.06)]"
            >
              <div className="flex items-start justify-between gap-3">
                <Link
                  to={`/catalogue/${item.id}`}
                  className="font-semibold tracking-tight text-ink-900 hover:text-brand-700"
                >
                  {item.name}
                </Link>
                <Badge tone={item.availableToday ? 'available' : 'busy'}>
                  {item.availableToday ? 'Free today' : 'On loan'}
                </Badge>
              </div>

              <p className="mt-1.5 line-clamp-2 text-sm text-ink-500">{item.description}</p>

              <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-ink-400">
                <Badge>{item.category}</Badge>
                <span>from {item.ownerName}</span>
              </div>

              <div className="mt-4 flex items-end justify-between gap-3 border-t border-ink-100 pt-4">
                <p className="tabular text-lg font-semibold text-ink-900">
                  {item.costPerDay}
                  <span className="ml-1 text-xs font-normal text-ink-400">credits/day</span>
                </p>
                <Button variant="secondary" onClick={() => setBorrowing(item)}>
                  Borrow
                </Button>
              </div>
            </article>
          ))}
        </div>
      )}

      <BookLoanDialog item={borrowing} onClose={() => setBorrowing(null)} />
      <NewItemDialog open={listing} onClose={() => setListing(false)} />
    </div>
  )
}
