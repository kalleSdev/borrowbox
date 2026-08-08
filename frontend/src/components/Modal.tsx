import { useEffect, useRef, type ReactNode } from 'react'

interface Props {
  open: boolean
  title: string
  onClose: () => void
  children: ReactNode
}

/**
 * A dialog built on the native <dialog> element, so focus trapping, Escape and
 * the backdrop come from the browser rather than being reimplemented.
 */
export default function Modal({ open, title, onClose, children }: Props) {
  const ref = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  return (
    <dialog
      ref={ref}
      onCancel={(e) => {
        e.preventDefault()
        onClose()
      }}
      onClick={(e) => {
        if (e.target === ref.current) onClose()
      }}
      className="m-auto w-[min(32rem,calc(100vw-2rem))] rounded-xl border border-ink-200 bg-white p-0
        shadow-xl backdrop:bg-ink-900/35 backdrop:backdrop-blur-[2px]"
    >
      <div className="flex items-center justify-between gap-4 border-b border-ink-100 px-5 py-3.5">
        <h2 className="text-sm font-semibold tracking-tight text-ink-900">{title}</h2>
        <button
          onClick={onClose}
          aria-label="Close"
          className="rounded-md px-2 py-1 text-ink-400 transition-colors hover:bg-ink-100 hover:text-ink-700"
        >
          ✕
        </button>
      </div>
      <div className="p-5">{children}</div>
    </dialog>
  )
}
