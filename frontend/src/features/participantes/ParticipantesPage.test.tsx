import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import ParticipantesPage from './ParticipantesPage'

function renderizar() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <ParticipantesPage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

afterEach(() => vi.unstubAllGlobals())

describe('ParticipantesPage', () => {
  it('mostra colaboradores e suas restrições', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify([{
      id: 1,
      nome: 'Maria Silva',
      cpf: '52998224725',
      restricoes: ['VEGANO', 'SEM_GLUTEN'],
    }]), { status: 200 })))

    renderizar()

    expect(await screen.findByText('Maria Silva')).toBeInTheDocument()
    expect(screen.getByText('vegano')).toBeInTheDocument()
    expect(screen.getByText('sem gluten')).toBeInTheDocument()
  })

  it('cria colaborador e envia CPF apenas com dígitos', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify([]), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify([]), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ id: 1, nome: 'João', cpf: '52998224725', restricoes: ['SEM_LACTOSE'] }), { status: 201 }))
      .mockResolvedValueOnce(new Response(JSON.stringify([{ id: 1, nome: 'João', cpf: '52998224725', restricoes: ['SEM_LACTOSE'] }]), { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()

    renderizar()
    await user.click(await screen.findByRole('button', { name: '+ Novo colaborador' }))

    const dialog = screen.getByRole('dialog')
    await user.type(within(dialog).getByLabelText('Nome'), 'João')
    await user.type(within(dialog).getByLabelText('CPF'), '52998224725')
    await user.click(within(dialog).getByLabelText('sem lactose'))
    await user.click(within(dialog).getByRole('button', { name: 'Salvar colaborador' }))

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(4))
    const chamadaPost = fetchMock.mock.calls.find((chamada) => chamada[1]?.method === 'POST')
    expect(chamadaPost).toBeDefined()
    expect(JSON.parse(chamadaPost?.[1]?.body as string)).toEqual({
      nome: 'João',
      cpf: '52998224725',
      restricoes: ['SEM_LACTOSE'],
    })
  })

  it('valida que o CPF tem 11 dígitos', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify([]), { status: 200 })))
    const user = userEvent.setup()

    renderizar()
    await user.click(await screen.findByRole('button', { name: '+ Novo colaborador' }))

    const dialog = screen.getByRole('dialog')
    await user.type(within(dialog).getByLabelText('Nome'), 'João')
    await user.type(within(dialog).getByLabelText('CPF'), '123')
    await user.click(within(dialog).getByRole('button', { name: 'Salvar colaborador' }))

    expect(await within(dialog).findByText('Informe 11 dígitos de CPF.')).toBeInTheDocument()
  })
})
