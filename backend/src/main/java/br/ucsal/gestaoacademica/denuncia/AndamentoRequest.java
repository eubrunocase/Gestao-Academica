package br.ucsal.gestaoacademica.denuncia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AndamentoRequest(
        @NotBlank(message = "Informe a etapa") @Size(max = 80) String etapa,
        @NotBlank(message = "Informe a descricao") @Size(max = 2000) String descricao,
        StatusDenuncia status) {
}
