package br.ucsal.gestaoacademica.avaliacao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record NotasLoteRequest(
        @NotEmpty(message = "Informe ao menos uma nota")
        List<@Valid NotaLancamentoRequest> notas) {
}
