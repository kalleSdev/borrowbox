import { describe, expect, it } from 'vitest'
import { http, HttpResponse } from 'msw'
import { ApiError, api, query } from './client'
import { problem, server } from '../test/server'

describe('query', () => {
  it('leaves out anything empty', () => {
    expect(query({ name: 'drill', maxPrice: '', other: undefined })).toBe('?name=drill')
  })

  it('is empty when there is nothing to send', () => {
    expect(query({ name: '', maxPrice: '   ' })).toBe('')
  })

  it('escapes values rather than pasting them in', () => {
    expect(query({ name: 'power drill & bit' })).toBe('?name=power+drill+%26+bit')
  })
})

describe('the client', () => {
  it('returns the parsed body on success', async () => {
    server.use(http.get('/api/thing', () => HttpResponse.json({ ok: true })))

    await expect(api.get('/thing')).resolves.toEqual({ ok: true })
  })

  it('turns a problem detail into an error a person can read', async () => {
    server.use(
      http.post('/api/contracts', () =>
        problem(422, 'Not allowed', 'The borrower has 100 credits but this loan costs 250.'),
      ),
    )

    const error = await api.post('/contracts', {}).catch((e: unknown) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect((error as ApiError).status).toBe(422)
    expect((error as ApiError).title).toBe('Not allowed')
    expect((error as ApiError).message).toBe(
      'The borrower has 100 credits but this loan costs 250.',
    )
  })

  it('keeps the per-field messages off a validation failure', async () => {
    server.use(
      http.post('/api/members', () =>
        problem(400, 'Invalid request', 'Some of the details are missing or malformed.', {
          email: 'That does not look like an email address',
        }),
      ),
    )

    const error = (await api.post('/members', {}).catch((e: unknown) => e)) as ApiError

    expect(error.fieldErrors).toEqual({
      email: 'That does not look like an email address',
    })
  })

  it('has no field errors when the failure was not about fields', async () => {
    server.use(http.get('/api/thing', () => problem(404, 'Not found', 'No item with id zzz.')))

    const error = (await api.get('/thing').catch((e: unknown) => e)) as ApiError

    expect(error.fieldErrors).toEqual({})
  })

  it('copes with an error response that has no body', async () => {
    server.use(http.get('/api/thing', () => new HttpResponse(null, { status: 500 })))

    const error = (await api.get('/thing').catch((e: unknown) => e)) as ApiError

    expect(error.status).toBe(500)
    expect(error.message).toContain('500')
  })

  it('returns nothing for a 204 rather than trying to parse it', async () => {
    server.use(http.delete('/api/thing', () => new HttpResponse(null, { status: 204 })))

    await expect(api.del('/thing')).resolves.toBeUndefined()
  })

  it('says the server is unreachable when the request never lands', async () => {
    server.use(http.get('/api/thing', () => HttpResponse.error()))

    const error = (await api.get('/thing').catch((e: unknown) => e)) as ApiError

    expect(error.status).toBe(0)
    expect(error.title).toBe('Cannot reach the server')
    expect(error.message).toContain('port 8080')
  })
})
