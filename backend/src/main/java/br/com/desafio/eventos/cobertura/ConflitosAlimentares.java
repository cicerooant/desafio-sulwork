package br.com.desafio.eventos.cobertura;

import br.com.desafio.eventos.item.Alergeno;
import br.com.desafio.eventos.participante.RestricaoAlimentar;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

public final class ConflitosAlimentares {

    private static final Map<RestricaoAlimentar, Map<Alergeno, NivelConflitoAlimentar>> MAPA = Map.ofEntries(
            Map.entry(RestricaoAlimentar.SEM_GLUTEN, Map.of(Alergeno.GLUTEN, NivelConflitoAlimentar.ALTO)),
            Map.entry(RestricaoAlimentar.SEM_LACTOSE, Map.of(Alergeno.LACTOSE, NivelConflitoAlimentar.ALTO)),
            Map.entry(RestricaoAlimentar.SEM_LEITE, Map.of(Alergeno.LEITE, NivelConflitoAlimentar.ALTO)),
            Map.entry(RestricaoAlimentar.SEM_AMENDOIM, Map.of(Alergeno.AMENDOIM, NivelConflitoAlimentar.ALTO)),
            Map.entry(RestricaoAlimentar.SEM_CASTANHAS, Map.of(Alergeno.CASTANHAS, NivelConflitoAlimentar.ALTO)),
            Map.entry(RestricaoAlimentar.SEM_SOJA, Map.of(Alergeno.SOJA, NivelConflitoAlimentar.ALTO)),
            Map.entry(RestricaoAlimentar.SEM_OVO, Map.of(Alergeno.OVO, NivelConflitoAlimentar.ALTO)),
            Map.entry(RestricaoAlimentar.SEM_PEIXE, Map.of(Alergeno.PEIXE, NivelConflitoAlimentar.ALTO)),
            Map.entry(RestricaoAlimentar.SEM_CRUSTACEOS, Map.of(Alergeno.CRUSTACEOS, NivelConflitoAlimentar.ALTO)),
            Map.entry(RestricaoAlimentar.SEM_GERGELIM, Map.of(Alergeno.GERGELIM, NivelConflitoAlimentar.ALTO)),
            Map.entry(RestricaoAlimentar.VEGANO, Map.of(
                    Alergeno.LEITE, NivelConflitoAlimentar.MEDIO,
                    Alergeno.LACTOSE, NivelConflitoAlimentar.MEDIO,
                    Alergeno.OVO, NivelConflitoAlimentar.MEDIO,
                    Alergeno.PEIXE, NivelConflitoAlimentar.MEDIO,
                    Alergeno.CRUSTACEOS, NivelConflitoAlimentar.MEDIO
            )),
            Map.entry(RestricaoAlimentar.VEGETARIANO, Map.of(
                    Alergeno.PEIXE, NivelConflitoAlimentar.MEDIO,
                    Alergeno.CRUSTACEOS, NivelConflitoAlimentar.MEDIO
            ))
    );

    private ConflitosAlimentares() {
    }

    public static List<ConflitoAlimentar> encontrar(
            Long colaboradorId,
            String nomeColaborador,
            Set<RestricaoAlimentar> restricoes,
            Set<Alergeno> alergenos
    ) {
        return restricoes.stream()
                .flatMap(restricao -> MAPA.getOrDefault(restricao, Map.of()).entrySet().stream()
                        .filter(conflito -> alergenos.contains(conflito.getKey()))
                        .map(conflito -> new ConflitoAlimentar(
                                colaboradorId,
                                nomeColaborador,
                                restricao,
                                conflito.getKey(),
                                conflito.getValue()
                        )))
                .sorted(Comparator.comparing(ConflitoAlimentar::nomeColaborador)
                        .thenComparing(conflito -> conflito.restricao().name())
                        .thenComparing(conflito -> conflito.alergeno().name()))
                .toList();
    }
}
