package br.ucsal.gestaoacademica.avaliacao;

import java.math.BigDecimal;

public record AvaliacaoResponse(Long id, String descricao, BigDecimal peso) {

    public static AvaliacaoResponse from(Avaliacao avaliacao) {
        return new AvaliacaoResponse(avaliacao.getId(), avaliacao.getDescricao(),
                avaliacao.getPeso());
    }
}
