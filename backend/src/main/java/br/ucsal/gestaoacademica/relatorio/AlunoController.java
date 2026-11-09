package br.ucsal.gestaoacademica.relatorio;

import br.ucsal.gestaoacademica.academico.MatriculaResponse;
import br.ucsal.gestaoacademica.academico.MatriculaService;
import br.ucsal.gestaoacademica.academico.TurmaDisponivelResponse;
import br.ucsal.gestaoacademica.avaliacao.BoletimAlunoResponse;
import br.ucsal.gestaoacademica.avaliacao.NotaService;
import br.ucsal.gestaoacademica.frequencia.AlunoFrequenciaResponse;
import br.ucsal.gestaoacademica.frequencia.FrequenciaService;
import br.ucsal.gestaoacademica.security.SecurityUtils;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aluno")
@PreAuthorize("hasRole('ALUNO')")
public class AlunoController {

    private final MatriculaService matriculaService;
    private final NotaService notaService;
    private final FrequenciaService frequenciaService;
    private final RelatorioService relatorioService;

    public AlunoController(MatriculaService matriculaService, NotaService notaService,
            FrequenciaService frequenciaService, RelatorioService relatorioService) {
        this.matriculaService = matriculaService;
        this.notaService = notaService;
        this.frequenciaService = frequenciaService;
        this.relatorioService = relatorioService;
    }

    @GetMapping("/turmas-disponiveis")
    public List<TurmaDisponivelResponse> turmasDisponiveis() {
        return matriculaService.turmasDisponiveis(SecurityUtils.usuarioId());
    }

    @GetMapping("/matriculas")
    public List<MatriculaResponse> matriculas() {
        return matriculaService.listarDoAluno(SecurityUtils.usuarioId());
    }

    @GetMapping("/dashboard")
    public AlunoDashboardResponse dashboard() {
        return relatorioService.dashboard(SecurityUtils.usuarioId());
    }

    @GetMapping("/notas")
    public List<BoletimAlunoResponse> notas() {
        return notaService.boletinsDoAluno(SecurityUtils.usuarioId());
    }

    @GetMapping("/frequencia")
    public List<AlunoFrequenciaResponse> frequencia() {
        return frequenciaService.frequenciasDoAluno(SecurityUtils.usuarioId());
    }

    @GetMapping("/historico")
    public HistoricoAlunoResponse historico() {
        return relatorioService.historico(SecurityUtils.usuarioId(), SecurityUtils.usuarioId(),
                SecurityUtils.principal().getPerfil());
    }
}
