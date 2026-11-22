export type RestricaoAlimentar =
  | 'SEM_GLUTEN'
  | 'SEM_LACTOSE'
  | 'SEM_LEITE'
  | 'SEM_AMENDOIM'
  | 'SEM_CASTANHAS'
  | 'SEM_SOJA'
  | 'SEM_OVO'
  | 'SEM_PEIXE'
  | 'SEM_CRUSTACEOS'
  | 'SEM_GERGELIM'
  | 'VEGETARIANO'
  | 'VEGANO'

export type Colaborador = {
  id: number
  nome: string
  cpf: string
  restricoes: RestricaoAlimentar[]
}

export type NovoColaborador = {
  nome: string
  cpf: string
  restricoes: RestricaoAlimentar[]
}

export type Inscricao = {
  id: number
  eventoId: number
  colaboradorId: number
  nomeColaborador: string
  restricoes: RestricaoAlimentar[]
}
