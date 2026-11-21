package br.com.desafio.eventos.evento;

public enum StatusEvento {
    RASCUNHO,
    ABERTO,
    ENCERRADO,
    REALIZADO,
    CANCELADO;

    public boolean podeTransicionarPara(StatusEvento proximoStatus) {
        return switch (this) {
            case RASCUNHO -> proximoStatus == ABERTO || proximoStatus == CANCELADO;
            case ABERTO -> proximoStatus == ENCERRADO || proximoStatus == CANCELADO;
            case ENCERRADO -> proximoStatus == REALIZADO || proximoStatus == CANCELADO;
            case REALIZADO, CANCELADO -> false;
        };
    }
}
