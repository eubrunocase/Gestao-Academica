package br.ucsal.gestaoacademica.avaliacao;

import br.ucsal.gestaoacademica.security.SecurityUtils;
import br.ucsal.gestaoacademica.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/turmas/{turmaId}/notas")
@PreAuthorize("hasAnyRole('PROFESSOR','COORDENADOR','SECRETARIA','ADMIN')")
public class NotaController {

    private final NotaService notaService;

    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    @GetMapping
    public TurmaNotasResponse listar(@PathVariable Long turmaId) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return notaService.listarNotasDaTurma(turmaId, principal.getId(), principal.getPerfil());
    }

    @PutMapping
    public List<NotaResponse> lancar(@PathVariable Long turmaId,
            @Valid @RequestBody NotasLoteRequest request) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return notaService.lancarLote(turmaId, request, principal.getId(), principal.getPerfil());
    }
}
