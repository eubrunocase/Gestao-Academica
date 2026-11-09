package br.ucsal.gestaoacademica.academico;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DisciplinaRequest(
        @NotBlank(message = "Informe o nome da disciplina") @Size(max = 120) String nome,
        @NotNull(message = "Informe a carga horaria")
        @Min(value = 1, message = "Carga horaria deve ser positiva") Integer cargaHoraria,
        @NotNull(message = "Informe o curso") Long cursoId,
        List<Long> prerequisitoIds) {
}
