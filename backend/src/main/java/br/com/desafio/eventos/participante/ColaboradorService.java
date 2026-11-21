package br.com.desafio.eventos.participante;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ColaboradorService {

    private final ColaboradorRepository colaboradorRepository;
    private final InscricaoEventoRepository inscricaoEventoRepository;

    public ColaboradorService(
            ColaboradorRepository colaboradorRepository,
            InscricaoEventoRepository inscricaoEventoRepository
    ) {
        this.colaboradorRepository = colaboradorRepository;
        this.inscricaoEventoRepository = inscricaoEventoRepository;
    }

    @Transactional
    public ColaboradorResponse criar(ColaboradorRequest request) {
        if (colaboradorRepository.existsByCpf(request.cpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");
        }

        Colaborador colaborador = new Colaborador();
        aplicarDados(colaborador, request);
        return ColaboradorResponse.de(colaboradorRepository.save(colaborador));
    }

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listar() {
        return colaboradorRepository.findAll()
                .stream()
                .map(ColaboradorResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ColaboradorResponse buscarPorId(Long id) {
        return ColaboradorResponse.de(buscarColaborador(id));
    }

    @Transactional
    public ColaboradorResponse atualizar(Long id, ColaboradorRequest request) {
        Colaborador colaborador = buscarColaborador(id);
        if (colaboradorRepository.existsByCpfAndIdNot(request.cpf(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado por outro colaborador");
        }

        aplicarDados(colaborador, request);
        return ColaboradorResponse.de(colaborador);
    }

    @Transactional
    public void excluir(Long id) {
        Colaborador colaborador = buscarColaborador(id);
        if (inscricaoEventoRepository.existsByColaboradorId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Colaborador inscrito em eventos não pode ser excluído"
            );
        }
        colaboradorRepository.delete(colaborador);
    }

    private Colaborador buscarColaborador(Long id) {
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Colaborador não encontrado"));
    }

    private void aplicarDados(Colaborador colaborador, ColaboradorRequest request) {
        colaborador.setNome(request.nome());
        colaborador.setCpf(request.cpf());
        colaborador.setRestricoes(request.restricoes());
    }
}
