package br.ucsal.gestaoacademica.academico;

import jakarta.validation.constraints.NotNull;

public record MatriculaRequest(
        @NotNull(message = "Informe a turma") Long turmaId,
        Long alunoId) {
}
