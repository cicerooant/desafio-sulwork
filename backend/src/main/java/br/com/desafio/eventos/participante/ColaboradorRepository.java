package br.com.desafio.eventos.participante;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {
    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
