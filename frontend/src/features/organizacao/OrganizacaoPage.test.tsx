import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import OrganizacaoPage from './OrganizacaoPage'

function renderizar() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={["/eventos/1/organizacao"]}>
        <Routes>
          <Route path="/eventos/:eventoId/organizacao" element={<OrganizacaoPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

afterEach(() => vi.unstubAllGlobals())

describe('OrganizacaoPage', () => {
  it('lista categorias e itens do evento', async () => {
    vi.stubGlobal('fetch', vi.fn((url: string) => {
      if (url === '/api/eventos/1') {
        return Promise.resolve(new Response(JSON.stringify({ id: 1, nome: 'Almoço', status: 'ABERTO', tipo: 'ALMOCO', descricao: '', dataHora: '2027-04-15T12:00:00', prazoAlteracao: '2027-04-14T12:00:00' }), { status: 200 }))
      }
      if (url === '/api/eventos/1/categorias') {
        return Promise.resolve(new Response(JSON.stringify([{ id: 10, eventoId: 1, nome: 'Lanches', metaQuantidade: 20 }]), { status: 200 }))
      }
      if (url === '/api/eventos/1/categorias/10/itens') {
        return Promise.resolve(new Response(JSON.stringify([{ id: 20, categoriaId: 10, nome: 'Pão', quantidadeNecessaria: 10, obrigatorio: true, alergenos: ['GLUTEN'] }]), { status: 200 }))
      }
      return Promise.resolve(new Response(JSON.stringify([]), { status: 404 }))
    }))

    renderizar()

    expect(await screen.findByText('Almoço')).toBeInTheDocument()
    expect((await screen.findAllByText('Lanches')).length).toBeGreaterThan(0)
    expect(await screen.findByText('Pão')).toBeInTheDocument()
    expect(screen.getByText('Obrigatório')).toBeInTheDocument()
  })

  it('cria categoria e envia a meta opcional', async () => {
    const fetchMock = vi.fn((url: string, init?: RequestInit) => {
      if (url === '/api/eventos/1') {
        return Promise.resolve(new Response(JSON.stringify({ id: 1, nome: 'Almoço', status: 'RASCUNHO', tipo: 'ALMOCO', descricao: '', dataHora: '2027-04-15T12:00:00', prazoAlteracao: '2027-04-14T12:00:00' }), { status: 200 }))
      }
      if (url === '/api/eventos/1/categorias' && init?.method === 'POST') {
        return Promise.resolve(new Response(JSON.stringify({ id: 11, eventoId: 1, nome: 'Bebidas', metaQuantidade: 12 }), { status: 201 }))
      }
      if (url === '/api/eventos/1/categorias') {
        return Promise.resolve(new Response(JSON.stringify([]), { status: 200 }))
      }
      return Promise.resolve(new Response(JSON.stringify([]), { status: 200 }))
    })
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()

    renderizar()
    await user.click(await screen.findByRole('button', { name: '+ Nova categoria' }))

    const dialog = screen.getByRole('dialog')
    await user.type(within(dialog).getByLabelText('Nome'), 'Bebidas')
    await user.type(within(dialog).getByLabelText('Meta (opcional)'), '12')
    await user.click(within(dialog).getByRole('button', { name: 'Criar categoria' }))

    await waitFor(() => {
      const chamada = fetchMock.mock.calls.find((item) => item[1]?.method === 'POST')
      expect(chamada).toBeDefined()
      expect(JSON.parse(chamada?.[1]?.body as string)).toEqual({ nome: 'Bebidas', metaQuantidade: 12 })
    })
  })

  it('impede alterações em evento encerrado', async () => {
    vi.stubGlobal('fetch', vi.fn((url: string) => {
      if (url === '/api/eventos/1') {
        return Promise.resolve(new Response(JSON.stringify({ id: 1, nome: 'Almoço', status: 'ENCERRADO', tipo: 'ALMOCO', descricao: '', dataHora: '2027-04-15T12:00:00', prazoAlteracao: '2027-04-14T12:00:00' }), { status: 200 }))
      }
      return Promise.resolve(new Response(JSON.stringify([]), { status: 200 }))
    }))

    renderizar()

    expect(await screen.findByText('Categorias e itens são somente leitura neste status.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: '+ Nova categoria' })).not.toBeInTheDocument()
  })
})
