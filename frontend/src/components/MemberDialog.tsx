import { useEffect, useState } from 'react'
import { ApiError } from '../api/client'
import { useCreateMember, useUpdateMember } from '../api/hooks'
import type { Member } from '../api/types'
import Button from './Button'
import Field from './Field'
import Modal from './Modal'
import { ErrorNotice } from './States'

const blank = { name: '', email: '', mobile: '' }

interface Props {
  open: boolean
  /** The member being edited, or null when signing a new one up. */
  member: Member | null
  onClose: () => void
}

export default function MemberDialog({ open, member, onClose }: Props) {
  const [form, setForm] = useState(blank)
  const create = useCreateMember()
  const update = useUpdateMember(member?.id ?? '')
  const action = member ? update : create

  useEffect(() => {
    if (!open) return
    setForm(member ? { name: member.name, email: member.email, mobile: member.mobile } : blank)
    create.reset()
    update.reset()
    // Re-seed only when the dialog opens on a different member.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, member?.id])

  const fieldErrors = action.error instanceof ApiError ? action.error.fieldErrors : {}
  const set = (key: keyof typeof blank) => (value: string) => setForm((f) => ({ ...f, [key]: value }))

  return (
    <Modal open={open} title={member ? `Edit ${member.name}` : 'Sign up a member'} onClose={onClose}>
      <div className="space-y-4">
        <Field
          label="Name"
          value={form.name}
          error={fieldErrors.name}
          placeholder="Ada Lovelace"
          onChange={(e) => set('name')(e.target.value)}
        />
        <Field
          label="Email"
          type="email"
          value={form.email}
          error={fieldErrors.email}
          placeholder="ada@example.com"
          onChange={(e) => set('email')(e.target.value)}
        />
        <Field
          label="Mobile"
          value={form.mobile}
          error={fieldErrors.mobile}
          placeholder="0700000000"
          onChange={(e) => set('mobile')(e.target.value)}
        />

        <p className="text-sm text-ink-400">
          Both the email and the mobile have to be unique across every member.
        </p>

        {action.isError && Object.keys(fieldErrors).length === 0 && (
          <ErrorNotice error={action.error} />
        )}

        <div className="flex justify-end gap-2 pt-1">
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button
            loading={action.isPending}
            onClick={() => action.mutate(form, { onSuccess: onClose })}
          >
            {member ? 'Save changes' : 'Sign up'}
          </Button>
        </div>
      </div>
    </Modal>
  )
}
