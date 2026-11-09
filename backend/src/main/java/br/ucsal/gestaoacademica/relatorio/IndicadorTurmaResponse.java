package br.ucsal.gestaoacademica.relatorio;

import java.math.BigDecimal;
import java.util.List;

public record IndicadorTurmaResponse(
        Long turmaId,
        String disciplina,
        String professor,
        String periodoLetivo,
        List<PontoAluno> alunos,
        List<EvolucaoAvaliacao> evolucao) {

    public record PontoAluno(Long matriculaId, String nome, BigDecimal media,
            BigDecimal frequencia) {
    }

    public record EvolucaoAvaliacao(Long avaliacaoId, String descricao, BigDecimal peso,
            BigDecimal media, long notasLancadas) {
    }
}
