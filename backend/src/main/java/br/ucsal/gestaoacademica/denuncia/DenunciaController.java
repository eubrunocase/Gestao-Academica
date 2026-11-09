package br.ucsal.gestaoacademica.denuncia;

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
@RequestMapping("/api/denuncias")
public class DenunciaController {

    private final DenunciaService denunciaService;

    public DenunciaController(DenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public DenunciaCriadaResponse criar(@Valid @RequestBody DenunciaRequest request) {
        return denunciaService.criar(request, SecurityUtils.usuarioId());
    }

    @GetMapping("/protocolo/{protocolo}")
    @PreAuthorize("isAuthenticated()")
    public DenunciaResponse consultar(@PathVariable String protocolo) {
        return denunciaService.consultarPorProtocolo(protocolo);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COORDENADOR','ADMIN')")
    public List<DenunciaResponse> listar() {
        return denunciaService.listar();
    }

    @PostMapping("/{id}/andamentos")
    @PreAuthorize("hasAnyRole('COORDENADOR','ADMIN')")
    public DenunciaResponse adicionarAndamento(@PathVariable Long id,
            @Valid @RequestBody AndamentoRequest request) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return denunciaService.adicionarAndamento(id, request, principal.getId());
    }

    @GetMapping("/estatisticas")
    @PreAuthorize("hasAnyRole('COORDENADOR','ADMIN')")
    public EstatisticasDenunciaResponse estatisticas() {
        return denunciaService.estatisticas();
    }
}
