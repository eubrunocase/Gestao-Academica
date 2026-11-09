package br.ucsal.gestaoacademica.relatorio;

import br.ucsal.gestaoacademica.security.SecurityUtils;
import br.ucsal.gestaoacademica.security.UsuarioPrincipal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/indicadores")
public class IndicadorController {

    private final RelatorioService relatorioService;

    public IndicadorController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/disciplinas")
    @PreAuthorize("hasRole('COORDENADOR')")
    public List<IndicadorDisciplinaResponse> disciplinas() {
        return relatorioService.indicadoresDisciplinas();
    }

    @GetMapping("/turmas/{id}")
    @PreAuthorize("hasAnyRole('COORDENADOR','PROFESSOR')")
    public IndicadorTurmaResponse turma(@PathVariable Long id) {
        UsuarioPrincipal principal = SecurityUtils.principal();
        return relatorioService.indicadoresTurma(id, principal.getId(), principal.getPerfil());
    }
}
