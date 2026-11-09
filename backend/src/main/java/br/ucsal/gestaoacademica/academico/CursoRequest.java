package br.ucsal.gestaoacademica.academico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CursoRequest(
        @NotBlank(message = "Informe o nome do curso") @Size(max = 120) String nome) {
}
