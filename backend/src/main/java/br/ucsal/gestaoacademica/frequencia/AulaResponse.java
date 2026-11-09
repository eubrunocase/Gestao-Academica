package br.ucsal.gestaoacademica.frequencia;

import java.time.LocalDate;

public record AulaResponse(Long id, LocalDate data, String conteudo) {

    public static AulaResponse from(Aula aula) {
        return new AulaResponse(aula.getId(), aula.getData(), aula.getConteudo());
    }
}
