import type { Categoria, Item, NovaCategoria, NovoItem } from './types'

async function requisicao<T>(caminho: string, init?: RequestInit): Promise<T> {
  const resposta = await fetch(caminho, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  })

  if (!resposta.ok) {
    const problema = await resposta.json().catch(() => null)
    throw new Error(problema?.detail ?? 'Não foi possível concluir a operação.')
  }

  if (resposta.status === 204) return undefined as T
  return resposta.json() as Promise<T>
}

export function listarCategorias(eventoId: number) {
  return requisicao<Categoria[]>(`/api/eventos/${eventoId}/categorias`)
}

export function criarCategoria(eventoId: number, categoria: NovaCategoria) {
  return requisicao<Categoria>(`/api/eventos/${eventoId}/categorias`, {
    method: 'POST',
    body: JSON.stringify(categoria),
  })
}

export function excluirCategoria(eventoId: number, categoriaId: number) {
  return requisicao<void>(`/api/eventos/${eventoId}/categorias/${categoriaId}`, {
    method: 'DELETE',
  })
}

export function listarItens(eventoId: number, categoriaId: number) {
  return requisicao<Item[]>(`/api/eventos/${eventoId}/categorias/${categoriaId}/itens`)
}

export function criarItem(eventoId: number, categoriaId: number, item: NovoItem) {
  return requisicao<Item>(`/api/eventos/${eventoId}/categorias/${categoriaId}/itens`, {
    method: 'POST',
    body: JSON.stringify(item),
  })
}

export function excluirItem(eventoId: number, categoriaId: number, itemId: number) {
  return requisicao<void>(`/api/eventos/${eventoId}/categorias/${categoriaId}/itens/${itemId}`, {
    method: 'DELETE',
  })
}
