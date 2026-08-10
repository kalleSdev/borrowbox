import { setupServer } from 'msw/node'
import { http, HttpResponse } from 'msw'
import type { Clock, Contract, Item, Member } from '../api/types'

/**
 * A stand-in for the API, at the network layer rather than by stubbing fetch.
 *
 * <p>Tests exercise the real client, the real error handling and the real query
 * hooks; the only thing replaced is the server on the other end. That means a
 * test fails if the client stops parsing problem details properly, which
 * mocking the hooks would hide.
 */

export const alice: Member = {
  id: 'aaaaaa',
  name: 'Alice',
  email: 'alice@example.com',
  mobile: '0700000001',
  credits: 530,
  joinedOnDay: 0,
  ownedItemCount: 2,
}

export const bob: Member = {
  id: 'bbbbbb',
  name: 'Bob',
  email: 'bob@example.com',
  mobile: '0700000002',
  credits: 100,
  joinedOnDay: 0,
  ownedItemCount: 0,
}

export const laptop: Item = {
  id: 'lap',
  name: 'Laptop',
  description: 'Performance laptop',
  category: 'Electronics',
  costPerDay: 50,
  ownerId: alice.id,
  ownerName: alice.name,
  listedOnDay: 0,
  availableToday: true,
  contracts: [],
}

export const bike: Item = {
  id: 'bik',
  name: 'Mountain bike',
  description: 'Hardtail, medium frame',
  category: 'Sports',
  costPerDay: 10,
  ownerId: alice.id,
  ownerName: alice.name,
  listedOnDay: 0,
  availableToday: false,
  contracts: [],
}

const clock: Clock = { currentDay: 0, events: [] }
const noContracts: Contract[] = []

/** Matches the shape the backend's ApiExceptionHandler produces. */
export function problem(status: number, title: string, detail: string, errors?: Record<string, string>) {
  return HttpResponse.json({ type: 'about:blank', status, title, detail, errors }, { status })
}

export const handlers = [
  http.get('/api/members', () => HttpResponse.json([alice, bob])),
  http.get('/api/clock', () => HttpResponse.json(clock)),
  http.get('/api/contracts', () => HttpResponse.json(noContracts)),
  http.get('/api/events', () => HttpResponse.json([])),

  http.get('/api/items', ({ request }) => {
    const url = new URL(request.url)
    const name = url.searchParams.get('name')
    const maxPrice = url.searchParams.get('maxPrice')

    if (maxPrice && Number.isNaN(Number(maxPrice))) {
      return problem(400, 'Bad request', `"${maxPrice}" is not a number of credits.`)
    }

    let results = [laptop, bike]
    if (name) {
      results = results.filter((i) => i.name.toLowerCase().includes(name.toLowerCase()))
    }
    if (maxPrice) {
      results = results.filter((i) => i.costPerDay <= Number(maxPrice))
    }
    return HttpResponse.json(results)
  }),
]

export const server = setupServer(...handlers)
