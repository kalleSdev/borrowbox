import { describe, expect, it, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import BookLoanDialog from './BookLoanDialog'
import { renderWithProviders } from '../test/render'
import { alice, bob, laptop, problem, server } from '../test/server'

describe('booking a loan', () => {
  it('will not offer the owner as the borrower', async () => {
    renderWithProviders(<BookLoanDialog item={laptop} onClose={() => {}} />)

    const borrower = await screen.findByLabelText('Borrower')
    expect(borrower).toHaveTextContent('Bob')
    expect(borrower).not.toHaveTextContent(alice.name)
  })

  it('shows each member with what they can afford', async () => {
    renderWithProviders(<BookLoanDialog item={laptop} onClose={() => {}} />)

    expect(await screen.findByRole('option', { name: 'Bob — 100 credits' })).toBeInTheDocument()
  })

  it('works out the cost as the days change, both ends counted', async () => {
    const user = userEvent.setup()
    renderWithProviders(<BookLoanDialog item={laptop} onClose={() => {}} />)
    await screen.findByLabelText('Borrower')

    const total = screen.getByRole('status', { name: 'Loan total' })
    expect(total).toHaveTextContent('1 day, both ends included')
    expect(total).toHaveTextContent('50 credits')

    await user.clear(screen.getByLabelText('Last day'))
    await user.type(screen.getByLabelText('Last day'), '2')

    await waitFor(() => expect(total).toHaveTextContent('3 days, both ends included'))
    expect(total).toHaveTextContent('150 credits')
  })

  it('warns before submitting when the borrower cannot afford it', async () => {
    const user = userEvent.setup()
    renderWithProviders(<BookLoanDialog item={laptop} onClose={() => {}} />)
    await screen.findByLabelText('Borrower')

    // Three days at 50 a day is 150, and Bob has 100.
    await user.clear(screen.getByLabelText('Last day'))
    await user.type(screen.getByLabelText('Last day'), '2')
    await user.selectOptions(screen.getByLabelText('Borrower'), bob.id)

    expect(await screen.findByText('Bob only has 100 credits.')).toBeInTheDocument()
  })

  it('says nothing about affordability when the borrower can pay', async () => {
    const user = userEvent.setup()
    renderWithProviders(<BookLoanDialog item={laptop} onClose={() => {}} />)
    await screen.findByLabelText('Borrower')

    await user.selectOptions(screen.getByLabelText('Borrower'), bob.id)

    expect(screen.queryByText(/only has/)).not.toBeInTheDocument()
  })

  it('cannot be confirmed until a borrower is chosen', async () => {
    renderWithProviders(<BookLoanDialog item={laptop} onClose={() => {}} />)
    await screen.findByLabelText('Borrower')

    expect(screen.getByRole('button', { name: 'Confirm loan' })).toBeDisabled()
  })

  it('closes once the loan goes through', async () => {
    const user = userEvent.setup()
    const onClose = vi.fn()
    server.use(http.post('/api/contracts', () => HttpResponse.json({}, { status: 201 })))
    renderWithProviders(<BookLoanDialog item={laptop} onClose={onClose} />)
    await screen.findByLabelText('Borrower')

    await user.selectOptions(screen.getByLabelText('Borrower'), bob.id)
    await user.click(screen.getByRole('button', { name: 'Confirm loan' }))

    await waitFor(() => expect(onClose).toHaveBeenCalled())
  })

  it('stays open and shows why when the API refuses', async () => {
    const user = userEvent.setup()
    const onClose = vi.fn()
    server.use(
      http.post('/api/contracts', () =>
        problem(422, 'Not allowed', 'The item is already booked for part of that period.'),
      ),
    )
    renderWithProviders(<BookLoanDialog item={laptop} onClose={onClose} />)
    await screen.findByLabelText('Borrower')

    await user.selectOptions(screen.getByLabelText('Borrower'), bob.id)
    await user.click(screen.getByRole('button', { name: 'Confirm loan' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'The item is already booked for part of that period.',
    )
    expect(onClose).not.toHaveBeenCalled()
  })

  it('renders nothing at all when no item is being borrowed', () => {
    const { container } = renderWithProviders(<BookLoanDialog item={null} onClose={() => {}} />)

    expect(container).toBeEmptyDOMElement()
  })
})
