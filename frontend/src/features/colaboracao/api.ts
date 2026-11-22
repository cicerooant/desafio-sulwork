import type { Cobertura, Contribuicao } from './types'

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

export function listarContribuicoes(eventoId: number) {
  return requisicao<Contribuicao[]>(`/api/eventos/${eventoId}/contribuicoes`)
}

export function criarContribuicao(eventoId: number, colaboradorId: number, itemId: number, quantidade: number) {
  return requisicao<Contribuicao>(`/api/eventos/${eventoId}/contribuicoes`, {
    method: 'POST',
    body: JSON.stringify({ colaboradorId, itemId, quantidade }),
  })
}

export function removerContribuicao(eventoId: number, contribuicaoId: number) {
  return requisicao<void>(`/api/eventos/${eventoId}/contribuicoes/${contribuicaoId}`, {
    method: 'DELETE',
  })
}

export function buscarCobertura(eventoId: number) {
  return requisicao<Cobertura>(`/api/eventos/${eventoId}/cobertura`)
}
