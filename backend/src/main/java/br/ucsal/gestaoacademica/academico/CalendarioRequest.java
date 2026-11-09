package br.ucsal.gestaoacademica.academico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CalendarioRequest(
        @NotBlank(message = "Informe o periodo letivo") String periodoLetivo,
        @NotNull(message = "Informe o inicio da matricula") LocalDate inicioMatricula,
        @NotNull(message = "Informe o fim da matricula") LocalDate fimMatricula,
        @NotNull(message = "Informe o fim do periodo") LocalDate fimPeriodo) {
}
