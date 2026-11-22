import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import EventosPage from './EventosPage'

function renderizar() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <EventosPage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

afterEach(() => vi.unstubAllGlobals())

describe('EventosPage', () => {
  it('lista eventos da API e filtra pela busca', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify([{
      id: 1,
      nome: 'Almoço de boas-vindas',
      descricao: 'Receber o time',
      tipo: 'ALMOCO',
      dataHora: '2027-04-15T12:30:00',
      prazoAlteracao: '2027-04-14T18:00:00',
      status: 'ABERTO',
    }]), { status: 200 })))
    const user = userEvent.setup()

    renderizar()

    expect(await screen.findByText('Almoço de boas-vindas')).toBeInTheDocument()
    await user.type(screen.getByRole('searchbox', { name: 'Buscar evento' }), 'inexistente')
    expect(screen.getByText('Nenhum evento encontrado')).toBeInTheDocument()
  })

  it('valida prazo e cria evento', async () => {
    const novoEvento = {
      id: 2,
      nome: 'Café da equipe',
      descricao: '',
      tipo: 'CAFE_DA_MANHA',
      dataHora: '2027-06-02T09:00:00',
      prazoAlteracao: '2027-06-01T18:00:00',
      status: 'RASCUNHO',
    }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify([]), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify(novoEvento), { status: 201 }))
      .mockResolvedValueOnce(new Response(JSON.stringify([novoEvento]), { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()

    renderizar()
    await user.click(await screen.findByRole('button', { name: '+ Criar evento' }))

    const dialog = screen.getByRole('dialog')
    await user.type(within(dialog).getByLabelText('Nome do evento'), 'Café da equipe')
    await user.selectOptions(within(dialog).getByLabelText('Tipo'), 'CAFE_DA_MANHA')
    await user.type(within(dialog).getByLabelText('Data e horário'), '2027-06-02T09:00')
    await user.type(within(dialog).getByLabelText('Prazo para alterações'), '2027-06-01T18:00')
    await user.click(within(dialog).getByRole('button', { name: 'Criar evento' }))

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(3))
    expect(JSON.parse(fetchMock.mock.calls[1][1]?.body as string)).toEqual({
      nome: 'Café da equipe',
      descricao: '',
      tipo: 'CAFE_DA_MANHA',
      dataHora: '2027-06-02T09:00:00',
      prazoAlteracao: '2027-06-01T18:00:00',
    })
  })

  it('impede prazo igual à data do evento', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify([]), { status: 200 })))
    const user = userEvent.setup()

    renderizar()
    await user.click(await screen.findByRole('button', { name: '+ Criar evento' }))

    const dialog = screen.getByRole('dialog')
    await user.type(within(dialog).getByLabelText('Nome do evento'), 'Café da equipe')
    await user.type(within(dialog).getByLabelText('Data e horário'), '2027-06-02T09:00')
    await user.type(within(dialog).getByLabelText('Prazo para alterações'), '2027-06-02T09:00')
    await user.click(within(dialog).getByRole('button', { name: 'Criar evento' }))

    expect(await within(dialog).findByText('O prazo precisa ser anterior à data do evento.')).toBeInTheDocument()
  })
})
