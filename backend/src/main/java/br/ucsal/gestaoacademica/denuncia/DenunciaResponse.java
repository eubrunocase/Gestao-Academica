package br.ucsal.gestaoacademica.denuncia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DenunciaResponse(
        Long id,
        String protocolo,
        boolean anonima,
        String denunciante,
        String categoria,
        String descricao,
        LocalDate dataAproximada,
        StatusDenuncia status,
        LocalDateTime criadoEm,
        List<AndamentoResponse> andamentos) {

    /**
     * Consulta por protocolo nunca expoe a identificacao do denunciante.
     */
    public static DenunciaResponse semDenunciante(Denuncia denuncia,
            List<AndamentoResponse> andamentos) {
        return new DenunciaResponse(denuncia.getId(), denuncia.getProtocolo(),
                denuncia.isAnonima(), null, denuncia.getCategoria(), denuncia.getDescricao(),
                denuncia.getDataAproximada(), denuncia.getStatus(), denuncia.getCriadoEm(),
                andamentos);
    }

    public static DenunciaResponse completo(Denuncia denuncia,
            List<AndamentoResponse> andamentos) {
        String denunciante = denuncia.isAnonima() || denuncia.getDenunciante() == null
                ? null : denuncia.getDenunciante().getNome();
        return new DenunciaResponse(denuncia.getId(), denuncia.getProtocolo(),
                denuncia.isAnonima(), denunciante, denuncia.getCategoria(),
                denuncia.getDescricao(), denuncia.getDataAproximada(), denuncia.getStatus(),
                denuncia.getCriadoEm(), andamentos);
    }
}
