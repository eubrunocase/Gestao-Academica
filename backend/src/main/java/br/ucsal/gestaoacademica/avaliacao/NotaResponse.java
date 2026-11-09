package br.ucsal.gestaoacademica.avaliacao;

import java.math.BigDecimal;

public record NotaResponse(Long id, Long avaliacaoId, Long matriculaId, BigDecimal valor) {

    public static NotaResponse from(Nota nota) {
        return new NotaResponse(nota.getId(), nota.getAvaliacao().getId(),
                nota.getMatricula().getId(), nota.getValor());
    }
}
