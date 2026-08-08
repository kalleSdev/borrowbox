import { useState } from 'react'
import { useDeleteMember, useMembers } from '../api/hooks'
import type { Member } from '../api/types'
import Button from '../components/Button'
import MemberDialog from '../components/MemberDialog'
import { EmptyState, ErrorNotice, Skeleton } from '../components/States'

export default function Members() {
  const members = useMembers()
  const remove = useDeleteMember()
  const [editing, setEditing] = useState<Member | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)

  function open(member: Member | null) {
    setEditing(member)
    setDialogOpen(true)
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-ink-900">Members</h1>
          <p className="mt-1 text-ink-500">Everyone lending and borrowing in the pool.</p>
        </div>
        <Button onClick={() => open(null)}>Sign up a member</Button>
      </div>

      {remove.isError && <ErrorNotice error={remove.error} />}

      {members.isError ? (
        <ErrorNotice error={members.error} onRetry={() => members.refetch()} />
      ) : members.isLoading ? (
        <Skeleton className="h-64 w-full" />
      ) : members.data?.length === 0 ? (
        <EmptyState
          title="Nobody has signed up yet"
          action={<Button onClick={() => open(null)}>Sign up the first member</Button>}
        />
      ) : (
        <div className="overflow-x-auto rounded-xl border border-ink-200 bg-white">
          <table className="w-full min-w-3xl text-sm">
            <thead>
              <tr className="border-b border-ink-100 text-left text-xs tracking-wide text-ink-400 uppercase">
                <th scope="col" className="px-5 py-3 font-medium">Name</th>
                <th scope="col" className="px-5 py-3 font-medium">Contact</th>
                <th scope="col" className="px-5 py-3 text-right font-medium">Credits</th>
                <th scope="col" className="px-5 py-3 text-right font-medium">Items</th>
                <th scope="col" className="px-5 py-3 text-right font-medium">Joined</th>
                <th scope="col" className="px-5 py-3"><span className="sr-only">Actions</span></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {members.data?.map((member) => (
                <tr key={member.id} className="transition-colors hover:bg-ink-50/60">
                  <td className="px-5 py-3.5">
                    <p className="font-medium text-ink-900">{member.name}</p>
                    <p className="tabular text-xs text-ink-400">{member.id}</p>
                  </td>
                  <td className="px-5 py-3.5 text-ink-600">
                    <p>{member.email}</p>
                    <p className="tabular text-xs text-ink-400">{member.mobile}</p>
                  </td>
                  <td className="tabular px-5 py-3.5 text-right font-medium text-ink-900">
                    {member.credits}
                  </td>
                  <td className="tabular px-5 py-3.5 text-right text-ink-600">
                    {member.ownedItemCount}
                  </td>
                  <td className="tabular px-5 py-3.5 text-right text-ink-400">
                    Day {member.joinedOnDay}
                  </td>
                  <td className="px-5 py-3.5">
                    <div className="flex justify-end gap-1">
                      <Button variant="ghost" onClick={() => open(member)}>
                        Edit
                      </Button>
                      <Button
                        variant="ghost"
                        className="text-rose-600 hover:bg-rose-50 hover:text-rose-700"
                        onClick={() => remove.mutate(member.id)}
                      >
                        Remove
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <MemberDialog open={dialogOpen} member={editing} onClose={() => setDialogOpen(false)} />
    </div>
  )
}
