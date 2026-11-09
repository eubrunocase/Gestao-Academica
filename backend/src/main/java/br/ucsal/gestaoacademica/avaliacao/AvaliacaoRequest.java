package br.ucsal.gestaoacademica.avaliacao;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AvaliacaoRequest(
        @NotBlank(message = "Informe a descricao") String descricao,
        @NotNull(message = "Informe o peso")
        @DecimalMin(value = "0.1", message = "Peso deve ser maior que zero") BigDecimal peso) {
}
