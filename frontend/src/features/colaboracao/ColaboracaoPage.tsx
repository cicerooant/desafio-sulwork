import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import type { Evento } from '../eventos/types'
import type { Inscricao } from '../participantes/types'
import { buscarCobertura, criarContribuicao, listarContribuicoes, removerContribuicao } from './api'

async function buscarEvento(eventoId: number): Promise<Evento> {
  const resposta = await fetch(`/api/eventos/${eventoId}`)
  if (!resposta.ok) throw new Error('Não foi possível carregar o evento.')
  return resposta.json()
}

async function listarInscricoes(eventoId: number): Promise<Inscricao[]> {
  const resposta = await fetch(`/api/eventos/${eventoId}/inscricoes`)
  if (!resposta.ok) throw new Error('Não foi possível carregar os participantes.')
  return resposta.json()
}

type NovaContribuicao = {
  colaboradorId: number
  itemId: number
  quantidade: number
}

export default function ColaboracaoPage() {
  const { eventoId: parametro } = useParams()
  const eventoId = Number(parametro)
  const queryClient = useQueryClient()
  const [modalAberto, setModalAberto] = useState(false)

  const eventoQuery = useQuery({ queryKey: ['evento', eventoId], queryFn: () => buscarEvento(eventoId), enabled: Number.isFinite(eventoId) })
  const inscricoesQuery = useQuery({ queryKey: ['inscricoes', eventoId], queryFn: () => listarInscricoes(eventoId), enabled: Number.isFinite(eventoId) })
  const contribuicoesQuery = useQuery({ queryKey: ['contribuicoes', eventoId], queryFn: () => listarContribuicoes(eventoId), enabled: Number.isFinite(eventoId) })
  const coberturaQuery = useQuery({ queryKey: ['cobertura', eventoId], queryFn: () => buscarCobertura(eventoId), enabled: Number.isFinite(eventoId) })

  const podeContribuir = eventoQuery.data?.status === 'ABERTO'
  const criarMutation = useMutation({
    mutationFn: (dados: NovaContribuicao) => criarContribuicao(eventoId, dados.colaboradorId, dados.itemId, dados.quantidade),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['contribuicoes', eventoId] })
      await queryClient.invalidateQueries({ queryKey: ['cobertura', eventoId] })
      setModalAberto(false)
    },
  })
  const removerMutation = useMutation({
    mutationFn: (contribuicaoId: number) => removerContribuicao(eventoId, contribuicaoId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['contribuicoes', eventoId] })
      await queryClient.invalidateQueries({ queryKey: ['cobertura', eventoId] })
    },
  })

  const itens = coberturaQuery.data?.itens ?? []

  return (
    <div className="min-h-screen bg-canvas text-ink">
      <header className="border-b border-line bg-white">
        <div className="mx-auto flex h-[76px] max-w-6xl items-center justify-between px-5 sm:px-8">
          <Link to="/eventos" className="text-base font-semibold">mesa comum</Link>
          <nav className="flex gap-4 text-sm" aria-label="Navegação principal">
            <Link to="/eventos" className="text-[#657065] hover:text-moss">Eventos</Link>
            <Link to="/participantes" className="text-[#657065] hover:text-moss">Participantes</Link>
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-5 pb-16 pt-10 sm:px-8">
        <Link className="text-sm text-[#657065] hover:text-moss" to="/eventos">← Voltar aos eventos</Link>
        <div className="mt-5 flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-medium text-moss">Contribuições</p>
            <h1 className="mt-1 text-4xl font-semibold tracking-tight">{eventoQuery.data?.nome ?? 'Cobertura do evento'}</h1>
          </div>
          {podeContribuir && (
            <button className="botao-principal" onClick={() => setModalAberto(true)} type="button">
              + Adicionar contribuição
            </button>
          )}
        </div>

        {!podeContribuir && eventoQuery.isSuccess && (
          <p className="mt-6 rounded-lg bg-stone-100 p-3 text-sm text-stone-700">
            Contribuições só podem ser alteradas em evento ABERTO antes do prazo.
          </p>
        )}

        {coberturaQuery.isLoading && <p className="mt-8 text-sm text-[#7c857c]">Calculando cobertura...</p>}
        {coberturaQuery.isError && <p className="mt-8 text-sm text-rose-700">Não foi possível calcular a cobertura.</p>}

        {coberturaQuery.data && (
          <>
            <section className="mt-8 grid gap-3 sm:grid-cols-3" aria-label="Resumo da cobertura">
              <Resumo titulo="Cobertura geral" valor={`${Math.round(coberturaQuery.data.coberturaGeral * 100)}%`} />
              <Resumo titulo="Itens pendentes" valor={coberturaQuery.data.itensObrigatoriosPendentes.length} />
              <Resumo titulo="Itens que faltam" valor={coberturaQuery.data.oQueFalta.length} />
            </section>

            <section className="mt-8 grid gap-6 lg:grid-cols-[1fr_320px]">
              <div className="rounded-2xl border border-line bg-white p-5 shadow-card">
                <h2 className="text-lg font-semibold">Cobertura por categoria</h2>
                <div className="mt-4 space-y-4">
                  {coberturaQuery.data.categorias.map((categoria) => (
                    <div key={categoria.categoriaId}>
                      <div className="mb-2 flex justify-between gap-3 text-sm">
                        <span className="font-medium">{categoria.nome}</span>
                        <span className="text-[#737d73]">{categoria.quantidadeContribuida} / {categoria.metaQuantidade}</span>
                      </div>
                      <div className="h-2 overflow-hidden rounded-full bg-[#edf0ea]">
                        <div className="h-full rounded-full bg-moss" style={{ width: `${Math.round(categoria.cobertura * 100)}%` }} />
                      </div>
                      <p className="mt-1 text-xs text-[#7c857c]">Faltam {categoria.quantidadeFaltante}</p>
                    </div>
                  ))}
                </div>

                <h2 className="mt-8 text-lg font-semibold">O que falta</h2>
                <div className="mt-3 space-y-2">
                  {coberturaQuery.data.oQueFalta.map((item) => (
                    <article className="flex items-center justify-between rounded-xl border border-line px-4 py-3" key={item.itemId}>
                      <div>
                        <p className="font-medium">{item.nomeItem}</p>
                        <p className="mt-1 text-xs text-[#7c857c]">{item.nomeCategoria} • faltam {item.quantidadeFaltante}</p>
                      </div>
                      {item.obrigatorio && <span className="rounded-full bg-amber-50 px-2 py-1 text-xs text-amber-800">Obrigatório</span>}
                    </article>
                  ))}
                  {coberturaQuery.data.oQueFalta.length === 0 && (
                    <p className="rounded-xl border border-dashed border-line py-8 text-center text-sm text-[#7c857c]">Tudo foi atendido.</p>
                  )}
                </div>

                {coberturaQuery.data.itens.some((item) => item.conflitos.length > 0) && (
                  <>
                    <h2 className="mt-8 text-lg font-semibold">Conflitos alimentares</h2>
                    <div className="mt-3 space-y-2">
                      {coberturaQuery.data.itens.flatMap((item) => item.conflitos.map((conflito) => (
                        <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-800" key={`${item.itemId}-${conflito.colaboradorId}-${conflito.alergeno}`}>
                          {conflito.nomeColaborador}: {conflito.restricao.replaceAll('_', ' ').toLowerCase()} x {conflito.alergeno.toLowerCase()} ({conflito.nivel}) — {item.nomeItem}
                        </p>
                      )))}
                    </div>
                  </>
                )}
              </div>

              <aside className="rounded-2xl border border-line bg-white p-5 shadow-card">
                <h2 className="text-lg font-semibold">Contribuições</h2>
                {contribuicoesQuery.isLoading && <p className="mt-4 text-sm text-[#7c857c]">Carregando...</p>}
                <div className="mt-4 space-y-2">
                  {contribuicoesQuery.data?.map((contribuicao) => (
                    <article className="rounded-xl border border-line px-3 py-3" key={contribuicao.id}>
                      <div className="flex items-start justify-between gap-2">
                        <div>
                          <p className="text-sm font-medium">{contribuicao.nomeItem}</p>
                          <p className="mt-1 text-xs text-[#7c857c]">{contribuicao.nomeColaborador} • quantidade {contribuicao.quantidade}</p>
                        </div>
                        {podeContribuir && (
                          <button
                            aria-label={`Remover contribuição de ${contribuicao.nomeColaborador}`}
                            className="text-xs text-rose-700 hover:underline"
                            disabled={removerMutation.isPending}
                            onClick={() => removerMutation.mutate(contribuicao.id)}
                            type="button"
                          >
                            Remover
                          </button>
                        )}
                      </div>
                    </article>
                  ))}
                  {!contribuicoesQuery.isLoading && contribuicoesQuery.data?.length === 0 && (
                    <p className="py-8 text-center text-sm text-[#7c857c]">Nenhuma contribuição ainda.</p>
                  )}
                </div>
              </aside>
            </section>
          </>
        )}
      </main>

      {modalAberto && (
        <ModalContribuicao
          criando={criarMutation.isPending}
          erro={criarMutation.error?.message}
          itens={itens}
          inscritos={inscricoesQuery.data ?? []}
          onClose={() => setModalAberto(false)}
          onSubmit={(dados) => criarMutation.mutateAsync(dados)}
        />
      )}
    </div>
  )
}

function Resumo({ titulo, valor }: { titulo: string; valor: number | string }) {
  return (
    <div className="rounded-2xl border border-line bg-white p-5 shadow-card">
      <p className="text-xs font-medium text-[#7b857b]">{titulo}</p>
      <p className="mt-2 text-3xl font-semibold">{valor}</p>
    </div>
  )
}

function ModalContribuicao({ criando, erro, itens, inscritos, onClose, onSubmit }: {
  criando: boolean
  erro?: string
  itens: import('./types').ItemCobertura[]
  inscritos: Inscricao[]
  onClose: () => void
  onSubmit: (dados: NovaContribuicao) => Promise<unknown>
}) {
  const { register, handleSubmit, formState: { errors } } = useForm<NovaContribuicao>()

  return (
    <div className="fixed inset-0 z-30 flex items-center justify-center bg-[#172018]/40 p-4" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <section aria-labelledby="modal-contribuicao" aria-modal="true" className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl" role="dialog">
        <h2 id="modal-contribuicao" className="text-2xl font-semibold">Nova contribuição</h2>
        <form className="mt-6 space-y-4" onSubmit={handleSubmit((dados) => onSubmit({
          ...dados,
          colaboradorId: Number(dados.colaboradorId),
          itemId: Number(dados.itemId),
          quantidade: Number(dados.quantidade),
        }))}>
          <label className="block">
            <span className="campo-label">Participante</span>
            <select className="campo-formulario" {...register('colaboradorId', { required: 'Selecione o participante.' })}>
              <option value="">Selecione</option>
              {inscritos.map((inscricao) => <option key={inscricao.id} value={inscricao.colaboradorId}>{inscricao.nomeColaborador}</option>)}
            </select>
            {errors.colaboradorId && <span className="mensagem-erro">{errors.colaboradorId.message}</span>}
          </label>
          <label className="block">
            <span className="campo-label">Item</span>
            <select className="campo-formulario" {...register('itemId', { required: 'Selecione o item.' })}>
              <option value="">Selecione</option>
              {itens.map((item) => <option key={item.itemId} value={item.itemId}>{item.nomeItem} • faltam {item.quantidadeFaltante}</option>)}
            </select>
            {errors.itemId && <span className="mensagem-erro">{errors.itemId.message}</span>}
          </label>
          <label className="block">
            <span className="campo-label">Quantidade</span>
            <input className="campo-formulario" min={1} type="number" {...register('quantidade', { required: 'Informe a quantidade.', valueAsNumber: true })} />
            {errors.quantidade && <span className="mensagem-erro">{errors.quantidade.message}</span>}
          </label>
          {erro && <p className="rounded-lg bg-rose-50 p-3 text-sm text-rose-800">{erro}</p>}
          <div className="flex justify-end gap-3 border-t border-line pt-4">
            <button className="botao-secundario" onClick={onClose} type="button">Cancelar</button>
            <button className="botao-principal" disabled={criando} type="submit">{criando ? 'Salvando...' : 'Adicionar'}</button>
          </div>
        </form>
      </section>
    </div>
  )
}
