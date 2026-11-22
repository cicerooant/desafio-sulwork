import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { criarEvento, listarEventos } from './api'
import type { NovoEvento, StatusEvento } from './types'
import { Link } from 'react-router-dom'

const labelsStatus: Record<StatusEvento, string> = {
  RASCUNHO: 'Rascunho',
  ABERTO: 'Aberto',
  ENCERRADO: 'Encerrado',
  REALIZADO: 'Realizado',
  CANCELADO: 'Cancelado',
}

export default function EventosPage() {
  const queryClient = useQueryClient()
  const [busca, setBusca] = useState('')
  const [statusFiltro, setStatusFiltro] = useState<'TODOS' | StatusEvento>('TODOS')
  const [modalAberto, setModalAberto] = useState(false)
  const eventosQuery = useQuery({ queryKey: ['eventos'], queryFn: listarEventos })
  const criarMutation = useMutation({
    mutationFn: criarEvento,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['eventos'] })
      setModalAberto(false)
    },
  })

  const eventosFiltrados = useMemo(() => {
    const termo = busca.toLowerCase()
    return (eventosQuery.data ?? []).filter((evento) => {
      const corresponde = `${evento.nome} ${evento.tipo}`.toLowerCase().includes(termo)
      return corresponde && (statusFiltro === 'TODOS' || evento.status === statusFiltro)
    })
  }, [busca, statusFiltro, eventosQuery.data])

  return (
    <div className="min-h-screen bg-canvas text-ink">
      <header className="border-b border-line bg-white">
        <div className="mx-auto flex h-[76px] max-w-6xl items-center justify-between px-5 sm:px-8">
          <Link to="/eventos">
            <p className="text-base font-semibold">mesa comum</p>
            <p className="text-xs text-[#879087]">organizar fica mais leve</p>
          </Link>
          <nav className="flex gap-4 text-sm" aria-label="Navegação principal">
            <Link to="/eventos" className="font-semibold text-moss">Eventos</Link>
            <Link to="/participantes" className="text-[#657065] hover:text-moss">Participantes</Link>
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-5 pb-16 pt-10 sm:px-8">
        <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-medium text-moss">Planejar juntos</p>
            <h1 className="mt-1 text-4xl font-semibold tracking-tight">Seus eventos</h1>
          </div>
          <button className="botao-principal" onClick={() => setModalAberto(true)} type="button">
            + Criar evento
          </button>
        </div>

        <section className="mt-8 grid gap-3 sm:grid-cols-3" aria-label="Resumo dos eventos">
          <Resumo titulo="Total de eventos" valor={eventosQuery.data?.length ?? 0} />
          <Resumo
            titulo="Inscrições abertas"
            valor={eventosQuery.data?.filter((evento) => evento.status === 'ABERTO').length ?? 0}
          />
          <Resumo titulo="Encontrados" valor={eventosFiltrados.length} />
        </section>

        <section className="mt-10">
          <div className="flex flex-col gap-3 border-b border-line pb-5 sm:flex-row sm:justify-between">
            <div>
              <h2 className="text-lg font-semibold">Todos os eventos</h2>
            </div>
            <div className="flex gap-2">
              <input
                aria-label="Buscar evento"
                className="campo-formulario"
                onChange={(event) => setBusca(event.target.value)}
                placeholder="Buscar evento..."
                type="search"
                value={busca}
              />
              <select
                aria-label="Filtrar por status"
                className="campo-formulario"
                onChange={(event) => setStatusFiltro(event.target.value as 'TODOS' | StatusEvento)}
                value={statusFiltro}
              >
                <option value="TODOS">Todos os status</option>
                {Object.entries(labelsStatus).map(([status, label]) => (
                  <option key={status} value={status}>{label}</option>
                ))}
              </select>
            </div>
          </div>

          {eventosQuery.isLoading && <p className="mt-6 text-sm text-[#7c857c]">Carregando eventos...</p>}
          {eventosQuery.isError && (
            <p className="mt-6 rounded-lg bg-rose-50 p-4 text-sm text-rose-800">
              Não foi possível carregar os eventos.
            </p>
          )}

          {!eventosQuery.isLoading && !eventosQuery.isError && eventosFiltrados.length === 0 && (
            <div className="mt-6 rounded-2xl border border-dashed border-[#d6ddd4] bg-white py-12 text-center">
              <h3 className="font-semibold">Nenhum evento encontrado</h3>
              <p className="mt-1 text-sm text-[#7c857c]">Crie um evento ou ajuste os filtros.</p>
            </div>
          )}

          <div className="mt-5 space-y-3">
            {eventosFiltrados.map((evento) => (
              <article className="rounded-2xl border border-line bg-white p-5 shadow-card" key={evento.id}>
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <h3 className="font-semibold">{evento.nome}</h3>
                    <p className="mt-1 text-sm text-[#747d74]">{evento.descricao || 'Sem descrição'}</p>
                  </div>
                  <span className="rounded-full bg-[#edf4ed] px-3 py-1 text-xs font-semibold text-moss">
                    {labelsStatus[evento.status]}
                  </span>
                </div>
                <p className="mt-3 text-xs text-[#687268]">
                  {evento.tipo} • {new Date(evento.dataHora).toLocaleString('pt-BR')}
                </p>
                <Link
                  className="mt-4 inline-block text-sm font-semibold text-moss hover:underline"
                  to={`/eventos/${evento.id}/organizacao`}
                >
                  Organizar categorias e itens →
                </Link>
              </article>
            ))}
          </div>
        </section>
      </main>

      {modalAberto && (
        <ModalNovoEvento
          criando={criarMutation.isPending}
          erro={criarMutation.error?.message}
          onClose={() => setModalAberto(false)}
          onSubmit={(evento) => criarMutation.mutateAsync(evento)}
        />
      )}
    </div>
  )
}

function Resumo({ titulo, valor }: { titulo: string; valor: number }) {
  return (
    <div className="rounded-2xl border border-line bg-white p-5 shadow-card">
      <p className="text-xs font-medium text-[#7b857b]">{titulo}</p>
      <p className="mt-2 text-3xl font-semibold">{valor}</p>
    </div>
  )
}

function ModalNovoEvento({
  criando,
  erro,
  onClose,
  onSubmit,
}: {
  criando: boolean
  erro?: string
  onClose: () => void
  onSubmit: (evento: NovoEvento) => Promise<unknown>
}) {
  const { register, handleSubmit, getValues, formState: { errors } } = useForm<NovoEvento>()

  async function enviar(evento: NovoEvento) {
    await onSubmit({
      ...evento,
      dataHora: `${evento.dataHora}:00`,
      prazoAlteracao: `${evento.prazoAlteracao}:00`,
    })
  }

  return (
    <div className="fixed inset-0 z-30 flex items-center justify-center bg-[#172018]/40 p-4" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <section aria-labelledby="modal-titulo" aria-modal="true" className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl" role="dialog">
        <h2 id="modal-titulo" className="text-2xl font-semibold">Criar evento</h2>
        <form className="mt-6 space-y-4" onSubmit={handleSubmit(enviar)}>
          <label className="block">
            <span className="campo-label">Nome do evento</span>
            <input className="campo-formulario" {...register('nome', { required: 'Informe o nome do evento.' })} />
            {errors.nome && <span className="mensagem-erro">{errors.nome.message}</span>}
          </label>
          <div className="grid gap-4 sm:grid-cols-2">
            <label className="block">
              <span className="campo-label">Tipo</span>
              <select className="campo-formulario" {...register('tipo', { required: true })}>
                <option value="ALMOCO">Almoço</option>
                <option value="CAFE_DA_MANHA">Café da manhã</option>
                <option value="LANCHE">Lanche</option>
                <option value="OUTRO">Outro</option>
              </select>
            </label>
            <label className="block">
              <span className="campo-label">Data e horário</span>
              <input className="campo-formulario" type="datetime-local" {...register('dataHora', { required: 'Informe a data e o horário.' })} />
              {errors.dataHora && <span className="mensagem-erro">{errors.dataHora.message}</span>}
            </label>
          </div>
          <label className="block">
            <span className="campo-label">Prazo para alterações</span>
            <input
              className="campo-formulario"
              type="datetime-local"
              {...register('prazoAlteracao', {
                required: 'Informe o prazo para alterações.',
                validate: (prazo) => prazo < getValues('dataHora') || 'O prazo precisa ser anterior à data do evento.',
              })}
            />
            {errors.prazoAlteracao && <span className="mensagem-erro">{errors.prazoAlteracao.message}</span>}
          </label>
          <label className="block">
            <span className="campo-label">Descrição (opcional)</span>
            <textarea className="campo-formulario min-h-20" {...register('descricao')} />
          </label>
          {erro && <p className="rounded-lg bg-rose-50 p-3 text-sm text-rose-800" role="alert">{erro}</p>}
          <div className="flex justify-end gap-3 border-t border-line pt-4">
            <button className="botao-secundario" onClick={onClose} type="button">Cancelar</button>
            <button className="botao-principal" disabled={criando} type="submit">
              {criando ? 'Criando...' : 'Criar evento'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
