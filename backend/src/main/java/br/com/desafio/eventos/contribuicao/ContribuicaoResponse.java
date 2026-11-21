package br.com.desafio.eventos.contribuicao;

public record ContribuicaoResponse(
        Long id,
        Long eventoId,
        Long colaboradorId,
        String nomeColaborador,
        Long itemId,
        String nomeItem,
        Integer quantidade
) {
    public static ContribuicaoResponse de(Contribuicao contribuicao) {
        return new ContribuicaoResponse(
                contribuicao.getId(),
                contribuicao.getItem().getCategoria().getEvento().getId(),
                contribuicao.getInscricaoEvento().getColaborador().getId(),
                contribuicao.getInscricaoEvento().getColaborador().getNome(),
                contribuicao.getItem().getId(),
                contribuicao.getItem().getNome(),
                contribuicao.getQuantidade()
        );
    }
}
