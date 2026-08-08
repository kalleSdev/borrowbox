import { useState } from 'react'
import { ApiError } from '../api/client'
import { useCreateItem, useMembers } from '../api/hooks'
import Button from './Button'
import Field, { SelectField } from './Field'
import Modal from './Modal'
import { ErrorNotice } from './States'

const blank = { ownerId: '', name: '', description: '', category: '', costPerDay: '10' }

export default function NewItemDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  const members = useMembers()
  const create = useCreateItem()
  const [form, setForm] = useState(blank)

  const fieldErrors = create.error instanceof ApiError ? create.error.fieldErrors : {}
  const set = (key: keyof typeof blank) => (value: string) => setForm((f) => ({ ...f, [key]: value }))

  function submit() {
    create.mutate(
      { ...form, costPerDay: Number(form.costPerDay) },
      {
        onSuccess: () => {
          setForm(blank)
          onClose()
        },
      },
    )
  }

  return (
    <Modal open={open} title="List an item" onClose={onClose}>
      <div className="space-y-4">
        <p className="text-sm text-ink-500">
          Listing something earns the owner a one-off 100 credit bonus, which is what keeps credits
          flowing into the pool.
        </p>

        <SelectField
          label="Owner"
          value={form.ownerId}
          error={fieldErrors.ownerId}
          onChange={(e) => set('ownerId')(e.target.value)}
        >
          <option value="">Choose a member…</option>
          {members.data?.map((member) => (
            <option key={member.id} value={member.id}>
              {member.name}
            </option>
          ))}
        </SelectField>

        <Field
          label="Name"
          value={form.name}
          error={fieldErrors.name}
          placeholder="Cordless drill"
          onChange={(e) => set('name')(e.target.value)}
        />
        <Field
          label="Description"
          value={form.description}
          error={fieldErrors.description}
          placeholder="18V, two batteries"
          onChange={(e) => set('description')(e.target.value)}
        />

        <div className="grid grid-cols-2 gap-3">
          <Field
            label="Category"
            value={form.category}
            error={fieldErrors.category}
            placeholder="Tools"
            onChange={(e) => set('category')(e.target.value)}
          />
          <Field
            label="Credits per day"
            type="number"
            min={0}
            value={form.costPerDay}
            error={fieldErrors.costPerDay}
            onChange={(e) => set('costPerDay')(e.target.value)}
          />
        </div>

        {create.isError && Object.keys(fieldErrors).length === 0 && (
          <ErrorNotice error={create.error} />
        )}

        <div className="flex justify-end gap-2 pt-1">
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button onClick={submit} loading={create.isPending}>
            List it
          </Button>
        </div>
      </div>
    </Modal>
  )
}
