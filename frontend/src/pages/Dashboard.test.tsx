import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import Dashboard from './Dashboard'
import { renderWithProviders } from '../test/render'
import { problem, server } from '../test/server'

describe('the dashboard', () => {
  it('leads with the day, because nothing moves without it', async () => {
    renderWithProviders(<Dashboard />)

    expect(await screen.findByText('Day 0')).toBeInTheDocument()
  })

  it('counts the pool', async () => {
    renderWithProviders(<Dashboard />)

    expect(await screen.findByText('Members')).toBeInTheDocument()
    expect(screen.getByText('Credits in circulation')).toBeInTheDocument()
    // 530 + 100 across the two seeded members
    expect(await screen.findByText('630')).toBeInTheDocument()
  })

  it('only offers what is actually free today', async () => {
    renderWithProviders(<Dashboard />)

    expect(await screen.findByText('Laptop')).toBeInTheDocument()
    expect(screen.queryByText('Mountain bike')).not.toBeInTheDocument()
  })

  it('says so when nothing has happened yet', async () => {
    renderWithProviders(<Dashboard />)

    expect(await screen.findByText('Nothing has happened yet')).toBeInTheDocument()
  })

  it('reports what the new day brought when the clock is advanced', async () => {
    const user = userEvent.setup()
    server.use(
      http.post('/api/clock/advance', () =>
        HttpResponse.json({
          currentDay: 1,
          events: [
            { day: 1, type: 'DAY_ADVANCED', description: 'Day 1 begins.' },
            { day: 1, type: 'LOAN_STARTED', description: 'Bob picks up Laptop from Alice.' },
          ],
        }),
      ),
    )
    renderWithProviders(<Dashboard />)
    await screen.findByText('Day 0')

    await user.click(screen.getByRole('button', { name: 'Advance the day' }))

    expect(await screen.findByText('Bob picks up Laptop from Alice.')).toBeInTheDocument()
    expect(screen.getByText('Day 1 begins.')).toBeInTheDocument()
  })

  it('surfaces a failure to advance instead of looking like nothing happened', async () => {
    const user = userEvent.setup()
    server.use(
      http.post('/api/clock/advance', () => problem(500, 'Server error', 'The clock jammed.')),
    )
    renderWithProviders(<Dashboard />)
    await screen.findByText('Day 0')

    await user.click(screen.getByRole('button', { name: 'Advance the day' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('The clock jammed.')
  })

  it('tells you when the API is not answering at all', async () => {
    server.use(http.get('/api/clock', () => HttpResponse.error()))
    renderWithProviders(<Dashboard />)

    expect(await screen.findByRole('alert')).toHaveTextContent('Cannot reach the server')
  })
})
