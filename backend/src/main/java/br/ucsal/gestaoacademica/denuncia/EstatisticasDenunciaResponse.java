package br.ucsal.gestaoacademica.denuncia;

import java.util.Map;

public record EstatisticasDenunciaResponse(
        long total,
        long anonimas,
        Map<String, Long> porStatus,
        Map<String, Long> porCategoria) {
}
