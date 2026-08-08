import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useClock, useContracts, useMembers } from '../api/hooks'
import Badge from '../components/Badge'
import { SelectField } from '../components/Field'
import { EmptyState, ErrorNotice, Skeleton } from '../components/States'

type Status = 'Upcoming' | 'Out now' | 'Returned'

function statusOf(startDay: number, endDay: number, today: number): Status {
  if (endDay < today) return 'Returned'
  if (startDay > today) return 'Upcoming'
  return 'Out now'
}

const tones = { 'Out now': 'available', Upcoming: 'accent', Returned: 'neutral' } as const

export default function Loans() {
  const [memberId, setMemberId] = useState('')
  const clock = useClock()
  const members = useMembers()
  const contracts = useContracts(memberId || undefined)

  const today = clock.data?.currentDay ?? 0
  const loans = [...(contracts.data ?? [])].sort(
    (a, b) => a.startDay - b.startDay || a.itemName.localeCompare(b.itemName),
  )

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-ink-900">Loans</h1>
          <p className="mt-1 text-ink-500">
            Every agreement on the books, against day <span className="tabular">{today}</span>.
          </p>
        </div>
        <div className="w-64">
          <SelectField
            label="Filter by member"
            value={memberId}
            onChange={(e) => setMemberId(e.target.value)}
          >
            <option value="">Everyone</option>
            {members.data?.map((member) => (
              <option key={member.id} value={member.id}>
                {member.name}
              </option>
            ))}
          </SelectField>
        </div>
      </div>

      {contracts.isError ? (
        <ErrorNotice error={contracts.error} onRetry={() => contracts.refetch()} />
      ) : contracts.isLoading ? (
        <Skeleton className="h-52 w-full" />
      ) : loans.length === 0 ? (
        <EmptyState
          title={memberId ? 'No loans for this member' : 'Nothing has been borrowed yet'}
          hint="Book something from the catalogue and it will appear here."
        />
      ) : (
        <div className="overflow-x-auto rounded-xl border border-ink-200 bg-white">
          <table className="w-full min-w-3xl text-sm">
            <thead>
              <tr className="border-b border-ink-100 text-left text-xs tracking-wide text-ink-400 uppercase">
                <th scope="col" className="px-5 py-3 font-medium">Item</th>
                <th scope="col" className="px-5 py-3 font-medium">Lender</th>
                <th scope="col" className="px-5 py-3 font-medium">Borrower</th>
                <th scope="col" className="px-5 py-3 font-medium">Period</th>
                <th scope="col" className="px-5 py-3 text-right font-medium">Cost</th>
                <th scope="col" className="px-5 py-3 text-right font-medium">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {loans.map((loan, i) => {
                const status = statusOf(loan.startDay, loan.endDay, today)
                return (
                  <tr key={`${loan.itemId}-${i}`} className="transition-colors hover:bg-ink-50/60">
                    <td className="px-5 py-3.5">
                      <Link
                        to={`/catalogue/${loan.itemId}`}
                        className="font-medium text-ink-900 hover:text-brand-700"
                      >
                        {loan.itemName}
                      </Link>
                    </td>
                    <td className="px-5 py-3.5 text-ink-600">{loan.lenderName}</td>
                    <td className="px-5 py-3.5 text-ink-600">{loan.borrowerName}</td>
                    <td className="tabular px-5 py-3.5 text-ink-600">
                      Day {loan.startDay}–{loan.endDay}
                      <span className="ml-1.5 text-xs text-ink-400">
                        ({loan.durationInDays}d)
                      </span>
                    </td>
                    <td className="tabular px-5 py-3.5 text-right font-medium text-ink-900">
                      {loan.cost}
                    </td>
                    <td className="px-5 py-3.5 text-right">
                      <Badge tone={tones[status]}>{status}</Badge>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
