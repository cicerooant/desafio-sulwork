import { useEffect, useState, type ReactNode } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import type { Evento } from '../eventos/types'
import {
  criarCategoria,
  criarItem,
  excluirCategoria,
  excluirItem,
  listarCategorias,
  listarItens,
} from './api'
import type { Alergeno, NovaCategoria, NovoItem } from './types'

const alergenos: Alergeno[] = ['GLUTEN', 'LEITE', 'LACTOSE', 'AMENDOIM', 'CASTANHAS', 'SOJA', 'OVO', 'PEIXE', 'CRUSTACEOS', 'GERGELIM']
const labelAlergeno = (valor: string) => valor.toLowerCase().replaceAll('_', ' ')

async function buscarEvento(eventoId: number): Promise<Evento> {
  const resposta = await fetch(`/api/eventos/${eventoId}`)
  if (!resposta.ok) throw new Error('Não foi possível carregar o evento.')
  return resposta.json()
}

export default function OrganizacaoPage() {
  const { eventoId: eventoIdParam } = useParams()
  const eventoId = Number(eventoIdParam)
  const queryClient = useQueryClient()
  const [categoriaSelecionada, setCategoriaSelecionada] = useState<number | null>(null)
  const [modalCategoria, setModalCategoria] = useState(false)
  const [modalItem, setModalItem] = useState(false)

  const eventoQuery = useQuery({ queryKey: ['evento', eventoId], queryFn: () => buscarEvento(eventoId), enabled: Number.isFinite(eventoId) })
  const categoriasQuery = useQuery({ queryKey: ['categorias', eventoId], queryFn: () => listarCategorias(eventoId), enabled: Number.isFinite(eventoId) })
  const itensQuery = useQuery({
    queryKey: ['itens', eventoId, categoriaSelecionada],
    queryFn: () => listarItens(eventoId, categoriaSelecionada as number),
    enabled: categoriaSelecionada !== null,
  })

  useEffect(() => {
    if (categoriasQuery.data?.length && categoriaSelecionada === null) {
      setCategoriaSelecionada(categoriasQuery.data[0].id)
    }
  }, [categoriasQuery.data, categoriaSelecionada])

  const criarCategoriaMutation = useMutation({
    mutationFn: (categoria: NovaCategoria) => criarCategoria(eventoId, categoria),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['categorias', eventoId] })
      setModalCategoria(false)
    },
  })

  const excluirCategoriaMutation = useMutation({
    mutationFn: (categoriaId: number) => excluirCategoria(eventoId, categoriaId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['categorias', eventoId] })
      setCategoriaSelecionada(null)
    },
  })

  const criarItemMutation = useMutation({
    mutationFn: (item: NovoItem) => criarItem(eventoId, categoriaSelecionada as number, item),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['itens', eventoId, categoriaSelecionada] }),
  })

  const excluirItemMutation = useMutation({
    mutationFn: (itemId: number) => excluirItem(eventoId, categoriaSelecionada as number, itemId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['itens', eventoId, categoriaSelecionada] }),
  })

  const podeAlterar = eventoQuery.data?.status === 'RASCUNHO' || eventoQuery.data?.status === 'ABERTO'

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
            <p className="text-sm font-medium text-moss">Organização do evento</p>
            <h1 className="mt-1 text-4xl font-semibold tracking-tight">
              {eventoQuery.data?.nome ?? 'Categorias e itens'}
            </h1>
          </div>
          {podeAlterar && (
            <button className="botao-principal" onClick={() => setModalCategoria(true)} type="button">
              + Nova categoria
            </button>
          )}
        </div>

        {!podeAlterar && eventoQuery.isSuccess && (
          <p className="mt-6 rounded-lg bg-stone-100 p-3 text-sm text-stone-700">
            Categorias e itens são somente leitura neste status.
          </p>
        )}

        <section className="mt-8 grid gap-6 lg:grid-cols-[280px_1fr]">
          <aside className="rounded-2xl border border-line bg-white p-4 shadow-card">
            <h2 className="px-1 text-sm font-semibold">Categorias</h2>
            {categoriasQuery.isLoading && <p className="mt-3 px-1 text-sm text-[#7c857c]">Carregando...</p>}
            {categoriasQuery.isError && <p className="mt-3 px-1 text-sm text-rose-700">Não foi possível carregar.</p>}
            <div className="mt-3 space-y-2">
              {categoriasQuery.data?.map((categoria) => (
                <button
                  className={`w-full rounded-lg px-3 py-3 text-left text-sm transition ${categoriaSelecionada === categoria.id ? 'bg-[#edf4ed] font-semibold text-moss' : 'hover:bg-canvas'}`}
                  key={categoria.id}
                  onClick={() => setCategoriaSelecionada(categoria.id)}
                  type="button"
                >
                  <span className="block">{categoria.nome}</span>
                  <span className="mt-1 block text-xs font-normal text-[#7c857c]">
                    Meta: {categoria.metaQuantidade ?? 'usar soma dos itens'}
                  </span>
                </button>
              ))}
              {!categoriasQuery.isLoading && categoriasQuery.data?.length === 0 && (
                <p className="px-1 py-6 text-center text-sm text-[#7c857c]">Nenhuma categoria.</p>
              )}
            </div>
          </aside>

          <div className="rounded-2xl border border-line bg-white p-5 shadow-card">
            {categoriaSelecionada ? (
              (() => {
                const categoria = categoriasQuery.data?.find((item) => item.id === categoriaSelecionada)
                return (
                  <>
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <h2 className="text-lg font-semibold">{categoria?.nome}</h2>
                        <p className="mt-1 text-sm text-[#7c857c]">
                          Meta: {categoria?.metaQuantidade ?? 'soma das quantidades necessárias'}
                        </p>
                      </div>
                      {podeAlterar && (
                        <div className="flex gap-2">
                          <button
                            className="botao-secundario"
                            onClick={() => setModalItem(true)}
                            type="button"
                          >
                            + Item
                          </button>
                          <button
                            className="rounded-lg px-3 py-2 text-sm text-rose-700 hover:bg-rose-50"
                            onClick={() => excluirCategoriaMutation.mutate(categoriaSelecionada)}
                            type="button"
                          >
                            Excluir
                          </button>
                        </div>
                      )}
                    </div>

                    {itensQuery.isLoading && <p className="mt-5 text-sm text-[#7c857c]">Carregando itens...</p>}
                    {itensQuery.isError && <p className="mt-5 text-sm text-rose-700">Não foi possível carregar os itens.</p>}
                    <div className="mt-5 space-y-2">
                      {itensQuery.data?.map((item) => (
                        <article className="flex items-start justify-between gap-3 rounded-xl border border-line px-4 py-3" key={item.id}>
                          <div>
                            <div className="flex flex-wrap items-center gap-2">
                              <p className="font-medium">{item.nome}</p>
                              <span className={`rounded-full px-2 py-1 text-xs ${item.obrigatorio ? 'bg-amber-50 text-amber-800' : 'bg-sky-50 text-sky-800'}`}>
                                {item.obrigatorio ? 'Obrigatório' : 'Opcional'}
                              </span>
                            </div>
                            <p className="mt-1 text-xs text-[#7c857c]">Quantidade necessária: {item.quantidadeNecessaria}</p>
                            {item.alergenos.length > 0 && (
                              <p className="mt-2 text-xs text-amber-800">
                                Alérgenos: {item.alergenos.map(labelAlergeno).join(', ')}
                              </p>
                            )}
                          </div>
                          {podeAlterar && (
                            <button
                              className="text-sm text-rose-700 hover:underline"
                              onClick={() => excluirItemMutation.mutate(item.id)}
                              type="button"
                            >
                              Excluir
                            </button>
                          )}
                        </article>
                      ))}
                      {!itensQuery.isLoading && itensQuery.data?.length === 0 && (
                        <p className="rounded-xl border border-dashed border-line py-10 text-center text-sm text-[#7c857c]">
                          Nenhum item nesta categoria.
                        </p>
                      )}
                    </div>
                  </>
                )
              })()
            ) : (
              <p className="py-10 text-center text-sm text-[#7c857c]">Selecione ou crie uma categoria.</p>
            )}
          </div>
        </section>
      </main>

      {modalCategoria && (
        <ModalCategoria
          criando={criarCategoriaMutation.isPending}
          erro={criarCategoriaMutation.error?.message}
          onClose={() => setModalCategoria(false)}
          onSubmit={(categoria) => criarCategoriaMutation.mutateAsync(categoria)}
        />
      )}
      {modalItem && (
        <ModalItem
          criando={criarItemMutation.isPending}
          erro={criarItemMutation.error?.message}
          onClose={() => setModalItem(false)}
          onSubmit={(item) => criarItemMutation.mutateAsync(item)}
        />
      )}
    </div>
  )
}

function ModalCategoria({ criando, erro, onClose, onSubmit }: {
  criando: boolean
  erro?: string
  onClose: () => void
  onSubmit: (categoria: NovaCategoria) => Promise<unknown>
}) {
  const { register, handleSubmit, formState: { errors } } = useForm<NovaCategoria>()

  return (
    <Modal titulo="Nova categoria" onClose={onClose}>
      <form className="mt-6 space-y-4" onSubmit={handleSubmit((categoria) => onSubmit({
        nome: categoria.nome,
        metaQuantidade: categoria.metaQuantidade === null || categoria.metaQuantidade === undefined
          ? null
          : Number(categoria.metaQuantidade),
      }))}>
        <label className="block">
          <span className="campo-label">Nome</span>
          <input className="campo-formulario" {...register('nome', { required: 'Informe o nome.' })} />
          {errors.nome && <span className="mensagem-erro">{errors.nome.message}</span>}
        </label>
        <label className="block">
          <span className="campo-label">Meta (opcional)</span>
          <input className="campo-formulario" min={1} type="number" {...register('metaQuantidade')} />
        </label>
        {erro && <p className="rounded-lg bg-rose-50 p-3 text-sm text-rose-800">{erro}</p>}
        <AcoesModal criando={criando} texto="Criar categoria" onClose={onClose} />
      </form>
    </Modal>
  )
}

function ModalItem({ criando, erro, onClose, onSubmit }: {
  criando: boolean
  erro?: string
  onClose: () => void
  onSubmit: (item: NovoItem) => Promise<unknown>
}) {
  const { register, handleSubmit, setValue, getValues, formState: { errors } } = useForm<NovoItem>()
  const [selecionados, setSelecionados] = useState<Alergeno[]>([])

  function alternar(alergeno: Alergeno) {
    const proximos = selecionados.includes(alergeno)
      ? selecionados.filter((item) => item !== alergeno)
      : [...selecionados, alergeno]
    setSelecionados(proximos)
    setValue('alergenos', proximos)
  }

  return (
    <Modal titulo="Novo item" onClose={onClose}>
      <form className="mt-6 space-y-4" onSubmit={handleSubmit((item) => onSubmit({
        nome: item.nome,
        quantidadeNecessaria: Number(item.quantidadeNecessaria),
        obrigatorio: item.obrigatorio,
        alergenos: getValues('alergenos') ?? [],
      }))}>
        <label className="block">
          <span className="campo-label">Nome</span>
          <input className="campo-formulario" {...register('nome', { required: 'Informe o nome.' })} />
          {errors.nome && <span className="mensagem-erro">{errors.nome.message}</span>}
        </label>
        <label className="block">
          <span className="campo-label">Quantidade necessária</span>
          <input className="campo-formulario" min={1} type="number" {...register('quantidadeNecessaria', { required: 'Informe a quantidade.', valueAsNumber: true })} />
          {errors.quantidadeNecessaria && <span className="mensagem-erro">{errors.quantidadeNecessaria.message}</span>}
        </label>
        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" {...register('obrigatorio')} />
          Item obrigatório
        </label>
        <fieldset>
          <legend className="campo-label">Alérgenos</legend>
          <div className="grid grid-cols-2 gap-2 rounded-lg border border-line p-3">
            {alergenos.map((alergeno) => (
              <label className="flex items-center gap-2 text-sm" key={alergeno}>
                <input checked={selecionados.includes(alergeno)} onChange={() => alternar(alergeno)} type="checkbox" />
                {labelAlergeno(alergeno)}
              </label>
            ))}
          </div>
        </fieldset>
        {erro && <p className="rounded-lg bg-rose-50 p-3 text-sm text-rose-800">{erro}</p>}
        <AcoesModal criando={criando} texto="Criar item" onClose={onClose} />
      </form>
    </Modal>
  )
}

function Modal({ titulo, onClose, children }: { titulo: string; onClose: () => void; children: ReactNode }) {
  return (
    <div className="fixed inset-0 z-30 flex items-center justify-center bg-[#172018]/40 p-4" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <section aria-labelledby="modal-titulo" aria-modal="true" className="max-h-[calc(100vh-2rem)] w-full max-w-lg overflow-y-auto rounded-2xl bg-white p-6 shadow-2xl" role="dialog">
        <h2 id="modal-titulo" className="text-2xl font-semibold">{titulo}</h2>
        {children}
      </section>
    </div>
  )
}

function AcoesModal({ criando, texto, onClose }: { criando: boolean; texto: string; onClose: () => void }) {
  return (
    <div className="flex justify-end gap-3 border-t border-line pt-4">
      <button className="botao-secundario" onClick={onClose} type="button">Cancelar</button>
      <button className="botao-principal" disabled={criando} type="submit">{criando ? 'Salvando...' : texto}</button>
    </div>
  )
}
