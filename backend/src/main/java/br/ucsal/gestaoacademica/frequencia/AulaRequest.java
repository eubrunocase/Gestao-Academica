package br.ucsal.gestaoacademica.frequencia;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AulaRequest(
        @NotNull(message = "Informe a data da aula") LocalDate data,
        String conteudo) {
}
