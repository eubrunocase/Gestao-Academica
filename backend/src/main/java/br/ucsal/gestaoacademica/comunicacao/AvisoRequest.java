package br.ucsal.gestaoacademica.comunicacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvisoRequest(
        @NotBlank(message = "Informe o titulo") @Size(max = 160) String titulo,
        @NotBlank(message = "Informe a mensagem") @Size(max = 2000) String mensagem,
        @NotNull(message = "Informe o escopo") EscopoAviso escopo,
        Long turmaId,
        Long cursoId) {
}
