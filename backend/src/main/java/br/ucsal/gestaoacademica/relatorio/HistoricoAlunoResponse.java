package br.ucsal.gestaoacademica.relatorio;

import br.ucsal.gestaoacademica.academico.StatusMatricula;
import java.math.BigDecimal;
import java.util.List;

public record HistoricoAlunoResponse(
        Long alunoId,
        String nome,
        String numeroMatricula,
        BigDecimal mediaGeral,
        List<ItemHistorico> disciplinas) {

    public record ItemHistorico(
            Long matriculaId,
            String periodoLetivo,
            String disciplina,
            String curso,
            Integer cargaHoraria,
            String professor,
            BigDecimal media,
            BigDecimal frequencia,
            StatusMatricula status) {
    }
}
