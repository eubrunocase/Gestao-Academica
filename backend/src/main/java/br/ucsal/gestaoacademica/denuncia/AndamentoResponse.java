package br.ucsal.gestaoacademica.denuncia;

import java.time.LocalDateTime;

public record AndamentoResponse(
        Long id,
        String etapa,
        String descricao,
        String autor,
        LocalDateTime criadoEm) {

    public static AndamentoResponse from(DenunciaAndamento andamento) {
        return new AndamentoResponse(andamento.getId(), andamento.getEtapa(),
                andamento.getDescricao(), andamento.getAutor().getNome(),
                andamento.getCriadoEm());
    }
}
