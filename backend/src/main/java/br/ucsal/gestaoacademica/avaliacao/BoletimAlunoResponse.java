package br.ucsal.gestaoacademica.avaliacao;

import br.ucsal.gestaoacademica.academico.StatusMatricula;
import java.math.BigDecimal;
import java.util.List;

public record BoletimAlunoResponse(
        Long matriculaId,
        String disciplina,
        String professor,
        String periodoLetivo,
        StatusMatricula status,
        BigDecimal media,
        BigDecimal notaNecessaria,
        BigDecimal frequencia,
        boolean alertaFrequencia,
        List<AvaliacaoNotaResponse> avaliacoes) {

    public record AvaliacaoNotaResponse(Long avaliacaoId, String descricao, BigDecimal peso,
            BigDecimal valor) {
    }
}
