package br.ucsal.gestaoacademica.denuncia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record DenunciaRequest(
        @NotNull(message = "Informe se a denuncia e anonima") Boolean anonima,
        @NotBlank(message = "Informe a categoria") @Size(max = 80) String categoria,
        @NotBlank(message = "Informe a descricao") @Size(max = 4000) String descricao,
        LocalDate dataAproximada) {
}
