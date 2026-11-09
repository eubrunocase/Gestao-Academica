package br.ucsal.gestaoacademica.frequencia;

import java.math.BigDecimal;
import java.util.List;

public record TurmaFrequenciaResponse(
        Long turmaId,
        List<AulaResponse> aulas,
        List<AlunoFrequencia> alunos) {

    public record AlunoFrequencia(Long matriculaId, Long alunoId, String nome,
            String numeroMatricula, long presencas, long totalAulas, BigDecimal percentual,
            List<RegistroFrequenciaResponse> registros) {
    }
}
