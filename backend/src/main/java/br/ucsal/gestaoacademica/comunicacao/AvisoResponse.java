package br.ucsal.gestaoacademica.comunicacao;

import java.time.LocalDateTime;

public record AvisoResponse(
        Long id,
        String autor,
        String titulo,
        String mensagem,
        EscopoAviso escopo,
        Long turmaId,
        String turmaDisciplina,
        Long cursoId,
        String cursoNome,
        LocalDateTime criadoEm) {

    public static AvisoResponse from(Aviso aviso) {
        return new AvisoResponse(aviso.getId(),
                aviso.getAutor().getNome(),
                aviso.getTitulo(),
                aviso.getMensagem(),
                aviso.getEscopo(),
                aviso.getTurma() == null ? null : aviso.getTurma().getId(),
                aviso.getTurma() == null ? null : aviso.getTurma().getDisciplina().getNome(),
                aviso.getCurso() == null ? null : aviso.getCurso().getId(),
                aviso.getCurso() == null ? null : aviso.getCurso().getNome(),
                aviso.getCriadoEm());
    }
}
