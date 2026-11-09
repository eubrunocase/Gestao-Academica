package br.ucsal.gestaoacademica.denuncia;

import java.time.LocalDateTime;

public record DenunciaCriadaResponse(String protocolo, StatusDenuncia status, boolean anonima,
        LocalDateTime criadoEm) {
}
