package br.com.desafio.eventos.participante;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "colaborador")
public class Colaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank
    @CpfValido
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @ElementCollection
    @CollectionTable(
            name = "colaborador_restricao",
            joinColumns = @JoinColumn(name = "colaborador_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "restricao", nullable = false, length = 30)
    private Set<RestricaoAlimentar> restricoes = new HashSet<>();

    protected Colaborador() {
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Set<RestricaoAlimentar> getRestricoes() {
        return restricoes;
    }

    public void setRestricoes(Set<RestricaoAlimentar> restricoes) {
        this.restricoes.clear();
        this.restricoes.addAll(restricoes);
    }
}
