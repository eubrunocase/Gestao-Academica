package br.ucsal.gestaoacademica.relatorio;

import br.ucsal.gestaoacademica.academico.StatusMatricula;
import java.math.BigDecimal;
import java.util.List;

public record RelatorioTurmaResponse(
        Long turmaId,
        String disciplina,
        String professor,
        String periodoLetivo,
        boolean encerrada,
        BigDecimal mediaTurma,
        long totalAlunos,
        long aprovados,
        long reprovadosNota,
        long reprovadosFalta,
        long ativos,
        List<AlunoRelatorio> alunos) {

    public record AlunoRelatorio(
            Long matriculaId,
            Long alunoId,
            String nome,
            String numeroMatricula,
            BigDecimal media,
            BigDecimal frequencia,
            StatusMatricula situacao) {
    }
}
