package br.ucsal.gestaoacademica.auditoria;

import br.ucsal.gestaoacademica.common.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuditoriaService auditoriaService;

    public AdminController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/logs-seguranca")
    public PageResponse<AuditoriaService.LogSegurancaResponse> logsSeguranca(
            @PageableDefault(size = 30, sort = "criadoEm", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return auditoriaService.listarSeguranca(pageable);
    }

    @GetMapping("/logs-auditoria")
    public PageResponse<AuditoriaService.LogAuditoriaResponse> logsAuditoria(
            @PageableDefault(size = 30, sort = "criadoEm", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return auditoriaService.listarAuditoria(pageable);
    }
}
