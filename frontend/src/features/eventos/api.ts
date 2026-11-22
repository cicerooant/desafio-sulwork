import type { Evento, NovoEvento } from './types'

async function requisicao<T>(caminho: string, init?: RequestInit): Promise<T> {
  const resposta = await fetch(caminho, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
  })

  if (!resposta.ok) {
    const problema = await resposta.json().catch(() => null)
    throw new Error(problema?.detail ?? 'Não foi possível concluir a operação.')
  }

  if (resposta.status === 204) return undefined as T
  return resposta.json() as Promise<T>
}

export function listarEventos() {
  return requisicao<Evento[]>('/api/eventos')
}

export function criarEvento(evento: NovoEvento) {
  return requisicao<Evento>('/api/eventos', {
    method: 'POST',
    body: JSON.stringify(evento),
  })
}
