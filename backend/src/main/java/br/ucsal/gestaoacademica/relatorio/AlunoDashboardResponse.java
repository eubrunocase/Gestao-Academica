package br.ucsal.gestaoacademica.relatorio;

import br.ucsal.gestaoacademica.avaliacao.BoletimAlunoResponse;
import br.ucsal.gestaoacademica.comunicacao.AvisoResponse;
import java.math.BigDecimal;
import java.util.List;

public record AlunoDashboardResponse(
        String nome,
        String periodoLetivo,
        int disciplinasAtivas,
        BigDecimal mediaGeral,
        BigDecimal frequenciaMedia,
        boolean alertaFrequencia,
        List<AvisoResponse> avisos,
        List<BoletimAlunoResponse> boletins) {
}
