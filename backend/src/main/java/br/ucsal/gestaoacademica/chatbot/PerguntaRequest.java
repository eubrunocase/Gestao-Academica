package br.ucsal.gestaoacademica.chatbot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerguntaRequest(
        @NotBlank(message = "Digite sua duvida.")
        @Size(max = 1000, message = "A pergunta deve ter no maximo 1000 caracteres")
        String pergunta) {
}
