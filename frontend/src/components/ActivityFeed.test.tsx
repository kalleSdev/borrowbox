import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import ActivityFeed from './ActivityFeed'
import type { ActivityEvent } from '../api/types'

const events: ActivityEvent[] = [
  { day: 2, type: 'LOAN_ENDED', description: 'Bob returns Laptop to Alice.' },
  { day: 1, type: 'LOAN_STARTED', description: 'Bob picks up Laptop from Alice.' },
  { day: 0, type: 'LOAN_AGREED', description: 'Bob books Laptop from Alice.' },
]

describe('the activity feed', () => {
  it('labels each entry by the type the API sent, not by reading the sentence', () => {
    render(<ActivityFeed events={events} />)

    expect(screen.getByText('Returned')).toBeInTheDocument()
    expect(screen.getByText('Picked up')).toBeInTheDocument()
    expect(screen.getByText('Booked')).toBeInTheDocument()
  })

  it('keeps the order it was given', () => {
    render(<ActivityFeed events={events} />)

    const entries = screen.getAllByRole('listitem')
    expect(entries[0]).toHaveTextContent('Bob returns Laptop to Alice.')
    expect(entries[2]).toHaveTextContent('Bob books Laptop from Alice.')
  })

  it('says which day each thing happened on', () => {
    render(<ActivityFeed events={events} />)

    expect(screen.getByText('Day 2')).toBeInTheDocument()
    expect(screen.getByText('Day 0')).toBeInTheDocument()
  })

  it('explains an empty feed rather than showing nothing', () => {
    render(<ActivityFeed events={[]} />)

    expect(screen.getByText('Nothing has happened yet')).toBeInTheDocument()
  })

  it('shows placeholders while it is still loading', () => {
    const { container } = render(<ActivityFeed loading />)

    expect(container.querySelectorAll('.animate-pulse')).toHaveLength(3)
    expect(screen.queryByText('Nothing has happened yet')).not.toBeInTheDocument()
  })

  it('does not fall over on an event type it has never seen', () => {
    const unknown = [
      { day: 1, type: 'SOMETHING_NEW', description: 'A new kind of thing happened.' },
    ] as unknown as ActivityEvent[]

    render(<ActivityFeed events={unknown} />)

    expect(screen.getByText('A new kind of thing happened.')).toBeInTheDocument()
  })
})
