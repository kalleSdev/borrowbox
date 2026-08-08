import { NavLink, Outlet } from 'react-router-dom'

const links = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/catalogue', label: 'Catalogue' },
  { to: '/members', label: 'Members' },
  { to: '/loans', label: 'Loans' },
]

export default function Layout() {
  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-10 border-b border-ink-200 bg-white/85 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center gap-6 px-4 py-3 sm:px-6">
          <NavLink to="/" className="flex shrink-0 items-center gap-2">
            <span
              aria-hidden
              className="grid size-8 place-items-center rounded-lg bg-brand-600 text-sm font-bold text-white"
            >
              B
            </span>
            <span className="text-lg font-semibold tracking-tight text-ink-900">BorrowBox</span>
          </NavLink>

          <nav className="-mx-1 flex flex-1 items-center gap-1 overflow-x-auto">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.end}
                className={({ isActive }) =>
                  `rounded-lg px-3 py-1.5 text-sm font-medium whitespace-nowrap transition-colors ${
                    isActive
                      ? 'bg-brand-50 text-brand-700'
                      : 'text-ink-500 hover:bg-ink-100 hover:text-ink-800'
                  }`
                }
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6">
        <Outlet />
      </main>

      <footer className="mx-auto max-w-6xl px-4 pb-10 text-sm text-ink-400 sm:px-6">
        Time in BorrowBox is simulated. Nothing moves until you advance the day.
      </footer>
    </div>
  )
}
