package br.ucsal.gestaoacademica.common;

import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.auditoria.EventoSeguranca;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final AuditoriaService auditoriaService;

    public ApiExceptionHandler(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> tratarRegraDeNegocio(BusinessException ex) {
        ApiError erro = ex.getDetalhes().isEmpty()
                ? ApiError.of(ex.getStatus().value(), ex.getMessage())
                : new ApiError(java.time.LocalDateTime.now(), ex.getStatus().value(),
                        ex.getMessage(), null);
        return ResponseEntity.status(ex.getStatus()).body(erro);
    }

    @ExceptionHandler(MatriculaException.class)
    public ResponseEntity<ErroMatricula> tratarMatricula(MatriculaException ex) {
        ErroMatricula erro = ErroMatricula.of(ex.getMotivo().name(), ex.getMessage(),
                ex.getDisciplinasPendentes(), ex.getTurmasAlternativas());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Dados invalidos", campos));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> tratarConstraint(ConstraintViolationException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violacao -> campos.putIfAbsent(
                violacao.getPropertyPath().toString(), violacao.getMessage()));
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Dados invalidos", campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> tratarCorpoInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Corpo da requisicao invalido"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> tratarNaoEncontrado(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, "Recurso nao encontrado"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> tratarIntegridade(DataIntegrityViolationException ex) {
        log.warn("Violacao de integridade: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, "Operacao conflita com dados existentes"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> tratarAcessoNegado(AccessDeniedException ex,
            HttpServletRequest request) {
        auditoriaService.registrarEvento(EventoSeguranca.ACESSO_NEGADO, null,
                IpUtils.obterIp(request));
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(403, "Acesso negado para o seu perfil"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> tratarInesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(500, "Erro interno do servidor"));
    }
}
