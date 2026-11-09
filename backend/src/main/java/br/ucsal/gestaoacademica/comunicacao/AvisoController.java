package br.ucsal.gestaoacademica.comunicacao;

import br.ucsal.gestaoacademica.security.SecurityUtils;
import br.ucsal.gestaoacademica.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/avisos")
public class AvisoController {

    private final AvisoService avisoService;

    public AvisoController(AvisoService avisoService) {
        this.avisoService = avisoService;
    }

    @GetMapping
    public List<AvisoResponse> listar() {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return avisoService.listar(principal.getId(), principal.getPerfil());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSOR','COORDENADOR','SECRETARIA','ADMIN')")
    public AvisoResponse criar(@Valid @RequestBody AvisoRequest request) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return avisoService.criar(request, principal.getId(), principal.getPerfil());
    }
}
