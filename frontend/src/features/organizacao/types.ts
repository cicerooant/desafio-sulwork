export type Categoria = {
  id: number
  eventoId: number
  nome: string
  metaQuantidade: number | null
}

export type NovaCategoria = {
  nome: string
  metaQuantidade: number | null
}

export type Alergeno =
  | 'GLUTEN'
  | 'LEITE'
  | 'LACTOSE'
  | 'AMENDOIM'
  | 'CASTANHAS'
  | 'SOJA'
  | 'OVO'
  | 'PEIXE'
  | 'CRUSTACEOS'
  | 'GERGELIM'

export type Item = {
  id: number
  categoriaId: number
  nome: string
  quantidadeNecessaria: number
  obrigatorio: boolean
  alergenos: Alergeno[]
}

export type NovoItem = {
  nome: string
  quantidadeNecessaria: number
  obrigatorio: boolean
  alergenos: Alergeno[]
}
