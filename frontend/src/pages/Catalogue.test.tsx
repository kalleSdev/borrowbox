import { describe, expect, it } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Catalogue from './Catalogue'
import { renderWithProviders } from '../test/render'

describe('the catalogue', () => {
  it('lists what is on offer, with who owns it and what it costs', async () => {
    renderWithProviders(<Catalogue />)

    expect(await screen.findByText('Laptop')).toBeInTheDocument()
    expect(screen.getByText('Mountain bike')).toBeInTheDocument()
    expect(screen.getAllByText(/from Alice/)).toHaveLength(2)
    expect(screen.getByText('50')).toBeInTheDocument()
  })

  it('marks what is free today and what is out on loan', async () => {
    renderWithProviders(<Catalogue />)

    expect(await screen.findByText('Free today')).toBeInTheDocument()
    expect(screen.getByText('On loan')).toBeInTheDocument()
  })

  it('narrows the list by name', async () => {
    const user = userEvent.setup()
    renderWithProviders(<Catalogue />)
    await screen.findByText('Laptop')

    await user.type(screen.getByLabelText('Search by name'), 'bike')

    await waitFor(() => expect(screen.queryByText('Laptop')).not.toBeInTheDocument())
    expect(screen.getByText('Mountain bike')).toBeInTheDocument()
  })

  it('narrows the list by price', async () => {
    const user = userEvent.setup()
    renderWithProviders(<Catalogue />)
    await screen.findByText('Laptop')

    await user.type(screen.getByLabelText('Maximum credits per day'), '20')

    await waitFor(() => expect(screen.queryByText('Laptop')).not.toBeInTheDocument())
    expect(screen.getByText('Mountain bike')).toBeInTheDocument()
  })

  it('explains an empty result rather than showing a blank page', async () => {
    const user = userEvent.setup()
    renderWithProviders(<Catalogue />)
    await screen.findByText('Laptop')

    await user.type(screen.getByLabelText('Search by name'), 'kayak')

    expect(await screen.findByText('Nothing matches those filters')).toBeInTheDocument()
  })

  it('shows what the API said when the price is not a number', async () => {
    const user = userEvent.setup()
    renderWithProviders(<Catalogue />)
    await screen.findByText('Laptop')

    await user.type(screen.getByLabelText('Maximum credits per day'), 'cheap')

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '"cheap" is not a number of credits.',
    )
  })

  it('offers a way back once a filter is on', async () => {
    const user = userEvent.setup()
    renderWithProviders(<Catalogue />)
    await screen.findByText('Laptop')

    expect(screen.queryByRole('button', { name: 'Clear' })).not.toBeInTheDocument()
    await user.type(screen.getByLabelText('Search by name'), 'bike')

    await user.click(screen.getByRole('button', { name: 'Clear' }))

    expect(await screen.findByText('Laptop')).toBeInTheDocument()
  })
})
