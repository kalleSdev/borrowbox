import { useEffect, useState } from 'react'
import { useBookLoan, useClock, useMembers } from '../api/hooks'
import type { Item } from '../api/types'
import { ApiError } from '../api/client'
import Button from './Button'
import Field, { SelectField } from './Field'
import Modal from './Modal'
import { ErrorNotice } from './States'

interface Props {
  item: Item | null
  onClose: () => void
}

/**
 * Books an item out.
 *
 * <p>The cost is worked out here only to show it before you commit. The
 * backend recalculates it and is the one that decides, so the two can never
 * disagree about what was actually charged.
 */
export default function BookLoanDialog({ item, onClose }: Props) {
  const clock = useClock()
  const members = useMembers()
  const book = useBookLoan()

  const today = clock.data?.currentDay ?? 0
  const [borrowerId, setBorrowerId] = useState('')
  const [startDay, setStartDay] = useState(today)
  const [endDay, setEndDay] = useState(today)

  useEffect(() => {
    if (item) {
      setBorrowerId('')
      setStartDay(today)
      setEndDay(today)
      book.reset()
    }
    // Only re-seed the form when a different item opens the dialog.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [item?.id, today])

  if (!item) return null

  const eligible = members.data?.filter((member) => member.id !== item.ownerId) ?? []
  const borrower = eligible.find((member) => member.id === borrowerId)
  const days = Math.max(0, endDay - startDay + 1)
  const cost = days * item.costPerDay
  const affordable = !borrower || borrower.credits >= cost
  const fieldErrors = book.error instanceof ApiError ? book.error.fieldErrors : {}

  function submit() {
    book.mutate(
      { itemId: item!.id, borrowerId, startDay, endDay },
      { onSuccess: onClose },
    )
  }

  return (
    <Modal open title={`Borrow ${item.name}`} onClose={onClose}>
      <div className="space-y-4">
        <p className="text-sm text-ink-500">
          {item.ownerName} charges{' '}
          <span className="tabular font-medium text-ink-700">{item.costPerDay} credits</span> a day.
          Today is day <span className="tabular">{today}</span>.
        </p>

        <SelectField
          label="Borrower"
          value={borrowerId}
          error={fieldErrors.borrowerId}
          onChange={(e) => setBorrowerId(e.target.value)}
        >
          <option value="">Choose a member…</option>
          {eligible.map((member) => (
            <option key={member.id} value={member.id}>
              {member.name} — {member.credits} credits
            </option>
          ))}
        </SelectField>

        <div className="grid grid-cols-2 gap-3">
          <Field
            label="First day"
            type="number"
            min={today}
            value={startDay}
            error={fieldErrors.startDay}
            onChange={(e) => setStartDay(Number(e.target.value))}
          />
          <Field
            label="Last day"
            type="number"
            min={startDay}
            value={endDay}
            error={fieldErrors.endDay}
            onChange={(e) => setEndDay(Number(e.target.value))}
          />
        </div>

        <div
          role="status"
          aria-label="Loan total"
          className="flex items-baseline justify-between rounded-lg bg-ink-50 px-4 py-3"
        >
          <span className="text-sm text-ink-500">
            {days} {days === 1 ? 'day' : 'days'}, both ends included
          </span>
          <span className="tabular text-lg font-semibold text-ink-900">{cost} credits</span>
        </div>

        {borrower && !affordable && (
          <p className="text-sm text-amber-700">
            {borrower.name} only has {borrower.credits} credits.
          </p>
        )}

        {book.isError && <ErrorNotice error={book.error} />}

        <div className="flex justify-end gap-2 pt-1">
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button onClick={submit} loading={book.isPending} disabled={!borrowerId || days === 0}>
            Confirm loan
          </Button>
        </div>
      </div>
    </Modal>
  )
}
