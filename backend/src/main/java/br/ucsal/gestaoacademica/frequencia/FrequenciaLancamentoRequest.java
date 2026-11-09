package br.ucsal.gestaoacademica.frequencia;

import jakarta.validation.constraints.NotNull;

public record FrequenciaLancamentoRequest(
        @NotNull(message = "Informe a matricula") Long matriculaId,
        @NotNull(message = "Informe a presenca") Boolean presente) {
}
