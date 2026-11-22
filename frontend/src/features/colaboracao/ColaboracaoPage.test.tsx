import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import ColaboracaoPage from './ColaboracaoPage'

function renderizar() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/eventos/1/colaboracao']}>
        <Routes>
          <Route path="/eventos/:eventoId/colaboracao" element={<ColaboracaoPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

afterEach(() => vi.unstubAllGlobals())

const cobertura = {
  coberturaGeral: 0.5,
  categorias: [{ categoriaId: 10, nome: 'Lanches', metaQuantidade: 10, quantidadeContribuida: 5, quantidadeFaltante: 5, cobertura: 0.5 }],
  itens: [{ itemId: 20, categoriaId: 10, nomeCategoria: 'Lanches', nomeItem: 'Pão', quantidadeNecessaria: 10, quantidadeContribuida: 5, quantidadeFaltante: 5, cobertura: 0.5, obrigatorio: true, conflitos: [] }],
  itensObrigatoriosPendentes: [{ itemId: 20, categoriaId: 10, nomeCategoria: 'Lanches', nomeItem: 'Pão', quantidadeNecessaria: 10, quantidadeContribuida: 5, quantidadeFaltante: 5, cobertura: 0.5, obrigatorio: true, conflitos: [] }],
  oQueFalta: [{ itemId: 20, categoriaId: 10, nomeCategoria: 'Lanches', nomeItem: 'Pão', quantidadeNecessaria: 10, quantidadeContribuida: 5, quantidadeFaltante: 5, cobertura: 0.5, obrigatorio: true, conflitos: [] }],
}

describe('ColaboracaoPage', () => {
  it('exibe cobertura, categorias e o que falta', async () => {
    vi.stubGlobal('fetch', vi.fn((url: string) => {
      if (url === '/api/eventos/1') return Promise.resolve(new Response(JSON.stringify({ id: 1, nome: 'Almoço', status: 'ABERTO', tipo: 'ALMOCO', descricao: '', dataHora: '2027-04-15T12:00:00', prazoAlteracao: '2027-04-14T12:00:00' }), { status: 200 }))
      if (url === '/api/eventos/1/cobertura') return Promise.resolve(new Response(JSON.stringify(cobertura), { status: 200 }))
      return Promise.resolve(new Response(JSON.stringify([]), { status: 200 }))
    }))

    renderizar()

    expect(await screen.findByText('50%')).toBeInTheDocument()
    expect(screen.getByText('Lanches')).toBeInTheDocument()
    expect(screen.getAllByText('Pão').length).toBeGreaterThan(0)
    expect(screen.getByText(/Faltam 5/)).toBeInTheDocument()
  })

  it('envia contribuição para participante e item', async () => {
    const fetchMock = vi.fn((url: string, init?: RequestInit) => {
      if (url === '/api/eventos/1') return Promise.resolve(new Response(JSON.stringify({ id: 1, nome: 'Almoço', status: 'ABERTO', tipo: 'ALMOCO', descricao: '', dataHora: '2027-04-15T12:00:00', prazoAlteracao: '2027-04-14T12:00:00' }), { status: 200 }))
      if (url === '/api/eventos/1/inscricoes') return Promise.resolve(new Response(JSON.stringify([{ id: 30, eventoId: 1, colaboradorId: 3, nomeColaborador: 'Maria', restricoes: [] }]), { status: 200 }))
      if (url === '/api/eventos/1/cobertura') return Promise.resolve(new Response(JSON.stringify(cobertura), { status: 200 }))
      if (url === '/api/eventos/1/contribuicoes' && init?.method === 'POST') return Promise.resolve(new Response(JSON.stringify({ id: 40, eventoId: 1, colaboradorId: 3, nomeColaborador: 'Maria', itemId: 20, nomeItem: 'Pão', quantidade: 2 }), { status: 201 }))
      if (url === '/api/eventos/1/contribuicoes') return Promise.resolve(new Response(JSON.stringify([]), { status: 200 }))
      return Promise.resolve(new Response(JSON.stringify([]), { status: 200 }))
    })
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()

    renderizar()
    await user.click(await screen.findByRole('button', { name: '+ Adicionar contribuição' }))

    const dialog = screen.getByRole('dialog')
    await user.selectOptions(within(dialog).getByLabelText('Participante'), '3')
    await user.selectOptions(within(dialog).getByLabelText('Item'), '20')
    await user.type(within(dialog).getByLabelText('Quantidade'), '2')
    await user.click(within(dialog).getByRole('button', { name: 'Adicionar' }))

    await waitFor(() => {
      const chamada = fetchMock.mock.calls.find((item) => item[1]?.method === 'POST')
      expect(chamada).toBeDefined()
      expect(JSON.parse(chamada?.[1]?.body as string)).toEqual({ colaboradorId: 3, itemId: 20, quantidade: 2 })
    })
  })

  it('não permite alterar contribuições em evento encerrado', async () => {
    vi.stubGlobal('fetch', vi.fn((url: string) => {
      if (url === '/api/eventos/1') return Promise.resolve(new Response(JSON.stringify({ id: 1, nome: 'Almoço', status: 'ENCERRADO', tipo: 'ALMOCO', descricao: '', dataHora: '2027-04-15T12:00:00', prazoAlteracao: '2027-04-14T12:00:00' }), { status: 200 }))
      if (url === '/api/eventos/1/cobertura') return Promise.resolve(new Response(JSON.stringify(cobertura), { status: 200 }))
      return Promise.resolve(new Response(JSON.stringify([]), { status: 200 }))
    }))

    renderizar()

    expect(await screen.findByText(/Contribuições só podem ser alteradas/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: '+ Adicionar contribuição' })).not.toBeInTheDocument()
  })
})
