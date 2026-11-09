package br.ucsal.gestaoacademica.avaliacao;

import java.math.BigDecimal;
import java.util.List;

public record TurmaNotasResponse(
        Long turmaId,
        List<AvaliacaoResponse> avaliacoes,
        List<AlunoNotas> alunos) {

    public record AlunoNotas(Long matriculaId, Long alunoId, String nome, String numeroMatricula,
            BigDecimal media, BigDecimal frequencia, List<NotaResponse> notas) {
    }
}
