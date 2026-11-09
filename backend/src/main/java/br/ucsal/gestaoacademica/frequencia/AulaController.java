package br.ucsal.gestaoacademica.frequencia;

import br.ucsal.gestaoacademica.security.SecurityUtils;
import br.ucsal.gestaoacademica.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/turmas/{turmaId}/aulas")
@PreAuthorize("hasAnyRole('PROFESSOR','COORDENADOR','SECRETARIA','ADMIN')")
public class AulaController {

    private final FrequenciaService frequenciaService;

    public AulaController(FrequenciaService frequenciaService) {
        this.frequenciaService = frequenciaService;
    }

    @GetMapping
    public List<AulaResponse> listar(@PathVariable Long turmaId) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return frequenciaService.listarAulas(turmaId, principal.getId(), principal.getPerfil());
    }

    @PostMapping
    public AulaResponse criar(@PathVariable Long turmaId,
            @Valid @RequestBody AulaRequest request) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return frequenciaService.criarAula(turmaId, request, principal.getId(),
                principal.getPerfil());
    }
}
