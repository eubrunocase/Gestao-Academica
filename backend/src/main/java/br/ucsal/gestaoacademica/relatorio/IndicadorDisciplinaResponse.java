package br.ucsal.gestaoacademica.relatorio;

import java.math.BigDecimal;

public record IndicadorDisciplinaResponse(
        Long disciplinaId,
        String disciplina,
        String curso,
        long matriculas,
        BigDecimal media,
        long aprovados,
        long reprovadosNota,
        long reprovadosFalta,
        long emAndamento,
        BigDecimal taxaAprovacao,
        BigDecimal taxaReprovacao) {
}
