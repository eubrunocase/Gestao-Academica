package br.ucsal.gestaoacademica.security;

import br.ucsal.gestaoacademica.auditoria.AuditoriaService;
import br.ucsal.gestaoacademica.auditoria.EventoSeguranca;
import br.ucsal.gestaoacademica.common.ApiError;
import br.ucsal.gestaoacademica.common.IpUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final AuditoriaService auditoriaService;

    public RestAccessDeniedHandler(ObjectMapper objectMapper, AuditoriaService auditoriaService) {
        this.objectMapper = objectMapper;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        auditoriaService.registrarEvento(EventoSeguranca.ACESSO_NEGADO, null,
                IpUtils.obterIp(request));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(),
                ApiError.of(403, "Acesso negado para o seu perfil"));
    }
}
