import type { Colaborador, Inscricao, NovoColaborador } from './types'

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

export function listarColaboradores() {
  return requisicao<Colaborador[]>('/api/colaboradores')
}

export function criarColaborador(colaborador: NovoColaborador) {
  return requisicao<Colaborador>('/api/colaboradores', {
    method: 'POST',
    body: JSON.stringify(colaborador),
  })
}

export function listarInscricoes(eventoId: number) {
  return requisicao<Inscricao[]>(`/api/eventos/${eventoId}/inscricoes`)
}

export function inscreverColaborador(eventoId: number, colaboradorId: number) {
  return requisicao<Inscricao>(`/api/eventos/${eventoId}/inscricoes`, {
    method: 'POST',
    body: JSON.stringify({ colaboradorId }),
  })
}

export function cancelarInscricao(eventoId: number, inscricaoId: number) {
  return requisicao<void>(`/api/eventos/${eventoId}/inscricoes/${inscricaoId}`, {
    method: 'DELETE',
  })
}
