import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="py-20 text-center">
      <p className="text-sm font-medium tracking-wide text-ink-400 uppercase">404</p>
      <h1 className="mt-2 text-2xl font-semibold tracking-tight text-ink-900">
        There is nothing here
      </h1>
      <p className="mt-1 text-ink-500">That page does not exist.</p>
      <Link
        to="/"
        className="mt-5 inline-block rounded-lg bg-brand-600 px-3.5 py-2 text-sm font-medium text-white hover:bg-brand-700"
      >
        Back to the dashboard
      </Link>
    </div>
  )
}
