package br.ucsal.gestaoacademica.academico;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TurmaRequest(
        @NotNull(message = "Informe a disciplina") Long disciplinaId,
        @NotNull(message = "Informe o professor") Long professorId,
        @NotBlank(message = "Informe o periodo letivo") String periodoLetivo,
        @NotNull(message = "Informe o limite de vagas")
        @Min(value = 1, message = "Limite de vagas deve ser positivo") Integer limiteVagas) {
}
