export type StatusEvento = 'RASCUNHO' | 'ABERTO' | 'ENCERRADO' | 'REALIZADO' | 'CANCELADO'

export type Evento = {
  id: number
  nome: string
  descricao: string | null
  tipo: string
  dataHora: string
  prazoAlteracao: string
  status: StatusEvento
}

export type NovoEvento = {
  nome: string
  descricao: string
  tipo: string
  dataHora: string
  prazoAlteracao: string
}
