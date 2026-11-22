import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import type { Evento } from '../eventos/types'
import {
  cancelarInscricao,
  criarColaborador,
  inscreverColaborador,
  listarColaboradores,
  listarInscricoes,
} from './api'
import type { NovoColaborador, RestricaoAlimentar } from './types'

const restricoes: RestricaoAlimentar[] = [
  'SEM_GLUTEN',
  'SEM_LACTOSE',
  'SEM_LEITE',
  'SEM_AMENDOIM',
  'SEM_CASTANHAS',
  'SEM_SOJA',
  'SEM_OVO',
  'SEM_PEIXE',
  'SEM_CRUSTACEOS',
  'SEM_GERGELIM',
  'VEGETARIANO',
  'VEGANO',
]

const labelRestricao = (valor: string) => valor.toLowerCase().replaceAll('_', ' ')

async function listarEventos(): Promise<Evento[]> {
  const resposta = await fetch('/api/eventos')
  if (!resposta.ok) throw new Error('Não foi possível carregar os eventos.')
  return resposta.json()
}

export default function ParticipantesPage() {
  const queryClient = useQueryClient()
  const [eventoId, setEventoId] = useState<number | null>(null)
  const [modalAberto, setModalAberto] = useState(false)

  const colaboradoresQuery = useQuery({ queryKey: ['colaboradores'], queryFn: listarColaboradores })
  const eventosQuery = useQuery({ queryKey: ['eventos'], queryFn: listarEventos })

  const eventoSelecionado = eventosQuery.data?.find((evento) => evento.id === eventoId)
  const inscricoesQuery = useQuery({
    queryKey: ['inscricoes', eventoId],
    queryFn: () => listarInscricoes(eventoId as number),
    enabled: eventoId !== null,
  })

  const criarMutation = useMutation({
    mutationFn: criarColaborador,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['colaboradores'] })
      setModalAberto(false)
    },
  })

  const inscreverMutation = useMutation({
    mutationFn: (colaboradorId: number) => inscreverColaborador(eventoId as number, colaboradorId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['inscricoes', eventoId] }),
  })

  const cancelarMutation = useMutation({
    mutationFn: (inscricaoId: number) => cancelarInscricao(eventoId as number, inscricaoId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['inscricoes', eventoId] }),
  })

  return (
    <div className="min-h-screen bg-canvas text-ink">
      <header className="border-b border-line bg-white">
        <div className="mx-auto flex h-[76px] max-w-6xl items-center justify-between px-5 sm:px-8">
          <Link to="/eventos" className="text-base font-semibold">mesa comum</Link>
          <nav className="flex gap-4 text-sm" aria-label="Navegação principal">
            <Link to="/eventos" className="text-[#657065] hover:text-moss">Eventos</Link>
            <Link to="/participantes" className="font-semibold text-moss">Participantes</Link>
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-5 pb-16 pt-10 sm:px-8">
        <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-medium text-moss">Quem participa</p>
            <h1 className="mt-1 text-4xl font-semibold tracking-tight">Participantes</h1>
          </div>
          <button className="botao-principal" onClick={() => setModalAberto(true)} type="button">
            + Novo colaborador
          </button>
        </div>

        <section className="mt-8 grid gap-6 lg:grid-cols-[1fr_1fr]">
          <div className="rounded-2xl border border-line bg-white p-5 shadow-card">
            <h2 className="text-lg font-semibold">Colaboradores cadastrados</h2>
            {colaboradoresQuery.isLoading && <p className="mt-4 text-sm text-[#7c857c]">Carregando...</p>}
            {colaboradoresQuery.isError && <p className="mt-4 text-sm text-rose-700">Não foi possível carregar os colaboradores.</p>}
            <div className="mt-4 space-y-2">
              {colaboradoresQuery.data?.map((colaborador) => (
                <article className="flex items-start justify-between rounded-xl border border-line px-4 py-3" key={colaborador.id}>
                  <div>
                    <p className="font-medium">{colaborador.nome}</p>
                    <p className="mt-1 text-xs text-[#7c857c]">CPF: {colaborador.cpf}</p>
                    {colaborador.restricoes.length > 0 && (
                      <div className="mt-2 flex flex-wrap gap-1">
                        {colaborador.restricoes.map((restricao) => (
                          <span className="rounded-full bg-amber-50 px-2 py-1 text-xs text-amber-800" key={restricao}>
                            {labelRestricao(restricao)}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                </article>
              ))}
              {!colaboradoresQuery.isLoading && colaboradoresQuery.data?.length === 0 && (
                <p className="rounded-xl border border-dashed border-line py-8 text-center text-sm text-[#7c857c]">
                  Nenhum colaborador cadastrado.
                </p>
              )}
            </div>
          </div>

          <div className="rounded-2xl border border-line bg-white p-5 shadow-card">
            <h2 className="text-lg font-semibold">Inscrições por evento</h2>
            <label className="mt-4 block">
              <span className="campo-label">Evento</span>
              <select
                aria-label="Selecionar evento"
                className="campo-formulario"
                onChange={(event) => setEventoId(event.target.value ? Number(event.target.value) : null)}
                value={eventoId ?? ''}
              >
                <option value="">Selecione um evento</option>
                {eventosQuery.data?.map((evento) => (
                  <option key={evento.id} value={evento.id}>{evento.nome}</option>
                ))}
              </select>
            </label>

            {eventoSelecionado && eventoSelecionado.status !== 'ABERTO' && (
              <p className="mt-3 rounded-lg bg-stone-50 p-3 text-sm text-stone-700">
                Inscrição disponível apenas em evento ABERTO.
              </p>
            )}

            {eventoId !== null && eventoSelecionado?.status === 'ABERTO' && (
              <div className="mt-4">
                <p className="text-sm font-medium">Adicionar participante</p>
                <div className="mt-2 flex flex-wrap gap-2">
                  {colaboradoresQuery.data?.map((colaborador) => (
                    <button
                      className="rounded-lg border border-line px-3 py-2 text-sm hover:border-[#93b29a] hover:bg-[#f4f7f2]"
                      disabled={inscricoesQuery.data?.some((inscricao) => inscricao.colaboradorId === colaborador.id) || inscreverMutation.isPending}
                      key={colaborador.id}
                      onClick={() => inscreverMutation.mutate(colaborador.id)}
                      type="button"
                    >
                      {colaborador.nome}
                    </button>
                  ))}
                </div>
                {inscreverMutation.isError && (
                  <p className="mt-2 text-sm text-rose-700" role="alert">{inscreverMutation.error.message}</p>
                )}
              </div>
            )}

            {eventoId !== null && (
              <div className="mt-5 space-y-2">
                {inscricoesQuery.isLoading && <p className="text-sm text-[#7c857c]">Carregando inscrições...</p>}
                {inscricoesQuery.data?.map((inscricao) => (
                  <article className="flex items-center justify-between rounded-xl border border-line px-4 py-3" key={inscricao.id}>
                    <div>
                      <p className="font-medium">{inscricao.nomeColaborador}</p>
                      {inscricao.restricoes.length > 0 && (
                        <p className="mt-1 text-xs text-amber-800">
                          {inscricao.restricoes.map(labelRestricao).join(', ')}
                        </p>
                      )}
                    </div>
                    {eventoSelecionado?.status === 'ABERTO' && (
                      <button
                        className="text-sm text-rose-700 hover:underline"
                        disabled={cancelarMutation.isPending}
                        onClick={() => cancelarMutation.mutate(inscricao.id)}
                        type="button"
                      >
                        Remover
                      </button>
                    )}
                  </article>
                ))}
                {inscricoesQuery.isSuccess && inscricoesQuery.data.length === 0 && (
                  <p className="rounded-xl border border-dashed border-line py-8 text-center text-sm text-[#7c857c]">
                    Nenhum participante inscrito.
                  </p>
                )}
              </div>
            )}
          </div>
        </section>
      </main>

      {modalAberto && (
        <ModalNovoColaborador
          criando={criarMutation.isPending}
          erro={criarMutation.error?.message}
          onClose={() => setModalAberto(false)}
          onSubmit={(colaborador) => criarMutation.mutateAsync(colaborador)}
        />
      )}
    </div>
  )
}

function ModalNovoColaborador({
  criando,
  erro,
  onClose,
  onSubmit,
}: {
  criando: boolean
  erro?: string
  onClose: () => void
  onSubmit: (colaborador: NovoColaborador) => Promise<unknown>
}) {
  const { register, handleSubmit, setValue, getValues, formState: { errors } } = useForm<NovoColaborador>()
  const [selecionadas, setSelecionadas] = useState<RestricaoAlimentar[]>([])

  function alternar(restricao: RestricaoAlimentar) {
    const proximas = selecionadas.includes(restricao)
      ? selecionadas.filter((item) => item !== restricao)
      : [...selecionadas, restricao]
    setSelecionadas(proximas)
    setValue('restricoes', proximas)
  }

  async function enviar(colaborador: NovoColaborador) {
    await onSubmit({
      ...colaborador,
      cpf: colaborador.cpf.replace(/\D/g, ''),
      restricoes: getValues('restricoes') ?? [],
    })
  }

  return (
    <div className="fixed inset-0 z-30 flex items-center justify-center bg-[#172018]/40 p-4" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <section aria-labelledby="modal-colaborador" aria-modal="true" className="max-h-[calc(100vh-2rem)] w-full max-w-lg overflow-y-auto rounded-2xl bg-white p-6 shadow-2xl" role="dialog">
        <h2 id="modal-colaborador" className="text-2xl font-semibold">Novo colaborador</h2>
        <form className="mt-6 space-y-4" onSubmit={handleSubmit(enviar)}>
          <label className="block">
            <span className="campo-label">Nome</span>
            <input className="campo-formulario" {...register('nome', { required: 'Informe o nome.' })} />
            {errors.nome && <span className="mensagem-erro">{errors.nome.message}</span>}
          </label>
          <label className="block">
            <span className="campo-label">CPF</span>
            <input
              className="campo-formulario"
              inputMode="numeric"
              placeholder="000.000.000-00"
              {...register('cpf', {
                required: 'Informe o CPF.',
                validate: (cpf) => cpf.replace(/\D/g, '').length === 11 || 'Informe 11 dígitos de CPF.',
              })}
            />
            {errors.cpf && <span className="mensagem-erro">{errors.cpf.message}</span>}
          </label>
          <fieldset>
            <legend className="campo-label">Restrições alimentares</legend>
            <div className="grid max-h-48 grid-cols-2 gap-2 overflow-y-auto rounded-lg border border-line p-3">
              {restricoes.map((restricao) => (
                <label className="flex items-center gap-2 text-sm" key={restricao}>
                  <input
                    checked={selecionadas.includes(restricao)}
                    onChange={() => alternar(restricao)}
                    type="checkbox"
                  />
                  {labelRestricao(restricao)}
                </label>
              ))}
            </div>
          </fieldset>
          {erro && <p className="rounded-lg bg-rose-50 p-3 text-sm text-rose-800" role="alert">{erro}</p>}
          <div className="flex justify-end gap-3 border-t border-line pt-4">
            <button className="botao-secundario" onClick={onClose} type="button">Cancelar</button>
            <button className="botao-principal" disabled={criando} type="submit">
              {criando ? 'Salvando...' : 'Salvar colaborador'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
