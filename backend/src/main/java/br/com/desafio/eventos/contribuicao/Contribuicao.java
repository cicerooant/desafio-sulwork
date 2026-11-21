package br.com.desafio.eventos.contribuicao;

import br.com.desafio.eventos.item.Item;
import br.com.desafio.eventos.participante.InscricaoEvento;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "contribuicao")
public class Contribuicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscricao_evento_id", nullable = false)
    private InscricaoEvento inscricaoEvento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantidade;

    protected Contribuicao() {
    }

    public Long getId() {
        return id;
    }

    public InscricaoEvento getInscricaoEvento() {
        return inscricaoEvento;
    }

    public void setInscricaoEvento(InscricaoEvento inscricaoEvento) {
        this.inscricaoEvento = inscricaoEvento;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
