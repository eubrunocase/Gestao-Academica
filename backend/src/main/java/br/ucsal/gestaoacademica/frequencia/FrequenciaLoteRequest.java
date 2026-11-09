package br.ucsal.gestaoacademica.frequencia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record FrequenciaLoteRequest(
        @NotEmpty(message = "Informe ao menos um registro")
        List<@Valid FrequenciaLancamentoRequest> registros) {
}
