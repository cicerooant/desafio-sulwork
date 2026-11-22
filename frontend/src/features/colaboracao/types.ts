export type Contribuicao = {
  id: number
  eventoId: number
  colaboradorId: number
  nomeColaborador: string
  itemId: number
  nomeItem: string
  quantidade: number
}

export type CategoriaCobertura = {
  categoriaId: number
  nome: string
  metaQuantidade: number
  quantidadeContribuida: number
  quantidadeFaltante: number
  cobertura: number
}

export type Conflito = {
  colaboradorId: number
  nomeColaborador: string
  restricao: string
  alergeno: string
  nivel: 'ALTO' | 'MEDIO'
}

export type ItemCobertura = {
  itemId: number
  categoriaId: number
  nomeCategoria: string
  nomeItem: string
  quantidadeNecessaria: number
  quantidadeContribuida: number
  quantidadeFaltante: number
  cobertura: number
  obrigatorio: boolean
  conflitos: Conflito[]
}

export type Cobertura = {
  coberturaGeral: number
  categorias: CategoriaCobertura[]
  itens: ItemCobertura[]
  itensObrigatoriosPendentes: ItemCobertura[]
  oQueFalta: ItemCobertura[]
}
