package br.ucsal.gestaoacademica.frequencia;

import java.math.BigDecimal;

public record AlunoFrequenciaResponse(
        Long matriculaId,
        String disciplina,
        String professor,
        String periodoLetivo,
        long totalAulas,
        long presencas,
        long faltas,
        BigDecimal percentual,
        int frequenciaMinima,
        boolean alerta) {
}
