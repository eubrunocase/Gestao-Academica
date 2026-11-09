package br.ucsal.gestaoacademica.avaliacao;

import br.ucsal.gestaoacademica.security.SecurityUtils;
import br.ucsal.gestaoacademica.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/turmas/{turmaId}/avaliacoes")
@PreAuthorize("hasAnyRole('PROFESSOR','COORDENADOR','SECRETARIA','ADMIN')")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    public List<AvaliacaoResponse> listar(@PathVariable Long turmaId) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return avaliacaoService.listar(turmaId, principal.getId(), principal.getPerfil());
    }

    @PostMapping
    public AvaliacaoResponse criar(@PathVariable Long turmaId,
            @Valid @RequestBody AvaliacaoRequest request) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return avaliacaoService.criar(turmaId, request, principal.getId(), principal.getPerfil());
    }

    @DeleteMapping("/{avaliacaoId}")
    public void excluir(@PathVariable Long turmaId, @PathVariable Long avaliacaoId) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        avaliacaoService.excluir(avaliacaoId, principal.getId(), principal.getPerfil());
    }
}
