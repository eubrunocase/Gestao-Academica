package br.ucsal.gestaoacademica.avaliacao;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record NotaLancamentoRequest(
        @NotNull(message = "Informe a avaliacao") Long avaliacaoId,
        @NotNull(message = "Informe a matricula") Long matriculaId,
        @NotNull(message = "Informe a nota")
        @DecimalMin(value = "0.0", message = "Nota minima e 0")
        @DecimalMax(value = "10.0", message = "Nota maxima e 10") BigDecimal valor) {
}
