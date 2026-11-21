package br.com.desafio.eventos.evento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @Size(max = 2000)
    @Column(length = 2000)
    private String descricao;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String tipo;

    @NotNull
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @NotNull
    @Column(name = "prazo_alteracao", nullable = false)
    private LocalDateTime prazoAlteracao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEvento status = StatusEvento.RASCUNHO;

    protected Evento() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public LocalDateTime getPrazoAlteracao() {
        return prazoAlteracao;
    }

    public void setPrazoAlteracao(LocalDateTime prazoAlteracao) {
        this.prazoAlteracao = prazoAlteracao;
    }

    public StatusEvento getStatus() {
        return status;
    }

    public void setStatus(StatusEvento status) {
        this.status = status;
    }
}
