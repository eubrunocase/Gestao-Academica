package br.ucsal.gestaoacademica.common;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, Object> detalhes;

    public BusinessException(String mensagem) {
        this(HttpStatus.BAD_REQUEST, mensagem, Map.of());
    }

    public BusinessException(HttpStatus status, String mensagem) {
        this(status, mensagem, Map.of());
    }

    public BusinessException(HttpStatus status, String mensagem, Map<String, Object> detalhes) {
        super(mensagem);
        this.status = status;
        this.detalhes = detalhes == null ? Map.of() : Map.copyOf(detalhes);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Object> getDetalhes() {
        return detalhes;
    }
}
