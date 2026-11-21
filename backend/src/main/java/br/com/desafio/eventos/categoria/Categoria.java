package br.com.desafio.eventos.categoria;

import br.com.desafio.eventos.evento.Evento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "categoria",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_categoria_evento_nome",
                columnNames = {"evento_id", "nome"}
        )
)
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @Positive
    @Column(name = "meta_quantidade")
    private Integer metaQuantidade;

    protected Categoria() {
    }

    public Long getId() {
        return id;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getMetaQuantidade() {
        return metaQuantidade;
    }

    public void setMetaQuantidade(Integer metaQuantidade) {
        this.metaQuantidade = metaQuantidade;
    }
}
