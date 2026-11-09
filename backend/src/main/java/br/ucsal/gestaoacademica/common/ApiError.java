package br.ucsal.gestaoacademica.common;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String mensagem,
        Map<String, String> campos) {

    public static ApiError of(int status, String mensagem) {
        return new ApiError(LocalDateTime.now(), status, mensagem, null);
    }

    public static ApiError of(int status, String mensagem, Map<String, String> campos) {
        return new ApiError(LocalDateTime.now(), status, mensagem, campos);
    }
}
