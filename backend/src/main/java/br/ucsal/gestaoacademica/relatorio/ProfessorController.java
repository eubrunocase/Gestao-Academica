package br.ucsal.gestaoacademica.relatorio;

import br.ucsal.gestaoacademica.academico.TurmaResponse;
import br.ucsal.gestaoacademica.academico.TurmaService;
import br.ucsal.gestaoacademica.security.SecurityUtils;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/professor")
@PreAuthorize("hasRole('PROFESSOR')")
public class ProfessorController {

    private final TurmaService turmaService;

    public ProfessorController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @GetMapping("/turmas")
    public List<TurmaResponse> turmas() {
        return turmaService.listarDoProfessor(SecurityUtils.usuarioId());
    }
}
